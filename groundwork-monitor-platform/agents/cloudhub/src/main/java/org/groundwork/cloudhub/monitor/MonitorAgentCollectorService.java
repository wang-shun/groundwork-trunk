package org.groundwork.cloudhub.monitor;

import org.apache.log4j.Logger;
import org.groundwork.agents.monitor.CollectorResult;
import org.groundwork.agents.monitor.MonitorAgentCollector;
import org.groundwork.agents.monitor.MonitorAgentResult;
import org.groundwork.agents.monitor.MonitorConnectionConfig;
import org.groundwork.agents.monitor.VirtualSystem;
import org.groundwork.agents.utils.StringUtils;
import org.groundwork.cloudhub.configuration.ConfigurationProvider;
import org.groundwork.cloudhub.configuration.ConfigurationService;
import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.connectors.ConnectorFactory;
import org.groundwork.cloudhub.gwos.GwosService;
import org.groundwork.cloudhub.gwos.GwosServiceFactory;
import org.groundwork.cloudhub.profile.ProfileService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

@Service(MonitorAgentCollector.NAME)
public class MonitorAgentCollectorService implements MonitorAgentCollector, BeanFactoryAware {

    private static Logger log = Logger.getLogger(MonitorAgentCollectorService.class);

    @Resource(name=ConfigurationService.NAME)
    ConfigurationService configurationService;

    @Resource(name= ProfileService.NAME)
    ProfileService profileService;

    @Resource(name = GwosServiceFactory.NAME)
    private GwosServiceFactory gwosServiceFactory;

    @Resource(name = ConnectorFactory.NAME)
    private ConnectorFactory connectorFactory;

    private long startTime;
    private final ExecutorService executor;
    private BeanFactory beanFactory;
    private boolean abort = false;
    @Value("${monitor.agent.scan.at.startup}")
    private boolean scanConfigsAtStartup = true;
    @Value("${gwos.client.defaultConnectTimeout}")
    private String defaultConnectTimeout;
    @Value("${gwos.client.readConnectTimeout}")
    private String readConnectTimeout;

    private final List<CloudhubMonitorAgent> agents = new LinkedList<CloudhubMonitorAgent>();
    private final List<Future<MonitorAgentResult>> results = new LinkedList<Future<MonitorAgentResult>>();

    public MonitorAgentCollectorService() {
        executor = Executors.newCachedThreadPool(new ThreadFactory() {
            public Thread newThread(Runnable task) {
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                return thread;
            }
        });
    }

    public MonitorAgentCollectorService(boolean scanConfigsAtStartup) {
        this();
        this.scanConfigsAtStartup = scanConfigsAtStartup;
    }

    @Override
    public void suspend(String agentName) {
        CloudhubMonitorAgent agent = findAgentByName(agentName);
        if (agent != null) {
            agent.suspend();
        }
    }

    @Override
    public void unsuspend(String agentName) {
        CloudhubMonitorAgent agent = findAgentByName(agentName);
        if (agent != null) {
            agent.unsuspend();
        }
    }

    @Override
    public void setConfigurationUpdated(String agentName) {
        CloudhubMonitorAgent agent = findAgentByName(agentName);
        if (agent != null) {
            agent.setConfigurationUpdated();
        }
    }

    @Override
    public int setConfigurationUpdated(VirtualSystem virtualSystem) {
        int count = 0;
        for (CloudhubMonitorAgent agent : agents) {
            if (agent.getAgentInfo().getVirtualSystem() == virtualSystem) {
                agent.setConfigurationUpdated();
                count++;
            }
        }
        return count;
    }

    public CloudhubMonitorAgent startMonitoringConnection(MonitorConnectionConfig configuration) {
        CloudhubMonitorAgent agent = addAgentInternal((ConnectionConfiguration)configuration, true);
        if (agent != null)
            run(agent);
        return agent;
    }

    public List<CloudhubMonitorAgent> list() {
        return agents;
    }

    @PostConstruct
    private void start() {

        System.setProperty("sun.net.client.defaultConnectTimeout", defaultConnectTimeout); // "60000"); // ms (60 seconds)
        System.setProperty("sun.net.client.defaultReadTimeout", readConnectTimeout); //"30000"); // in ms (30 seconds)

        log.info(String.format("starting monitor agent collector service, config scanning is %s.",
                (scanConfigsAtStartup) ? "enabled" : "disabled"));
        startTime = System.currentTimeMillis();

        // look for any configurations in 7.0.x format and move them to location
        migrateConfigurations();

        if (scanConfigsAtStartup) {
            List<ConnectionConfiguration > configurations = null;
            try {
                configurations = configurationService.listAllConfigurations();
            }
            catch (Exception e) {
                log.error("Failed to load configuration", e);
            }
            int size = 0;
            if (configurations != null) {
                for (ConnectionConfiguration configuration : configurations) {
                    try {
                        addAgentInternal(configuration, false);
                    }
                    catch (Exception e) {
                        log.error("Failed to connect on startup ", e);
                    }
                }
                size = configurations.size();
            }
            for (CloudhubMonitorAgent job : agents) {
                Future<MonitorAgentResult> result = run(job);
                results.add(result);
            }
            log.info(String.format("monitor agent collector service started with %d configurations being monitored",
                    size));
        }
    }


    @PreDestroy
    private void shutdown() {
        log.info("shutting down monitor agent collector service, waiting to complete...");
        waitOnJobs(10);
        executor.shutdown();
        log.info("shut down of monitor agent collector service complete");
    }

    private CollectorResult waitOnJobs(int secondsToWait) {
        int iterations = 0;
        int completedJobs = 0;
        CollectorResult.Status status = CollectorResult.Status.BatchRunning;
        for (CloudhubMonitorAgent agent : agents) {
            agent.shutdown(); // notify all agents to shutdown
        }
        while (iterations < secondsToWait) {
            for (Future<MonitorAgentResult> future : results) {
                if (future.isDone()) {
                    completedJobs++;
                }
            }
            if (completedJobs == agents.size()) {
                status = CollectorResult.Status.BatchSuccess;
                break;
            }
            if (abort) {
                status = CollectorResult.Status.BatchInterrupted;
            }
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                log.info("wait on jobs interrupted");
                status = CollectorResult.Status.BatchInterrupted;
                break;
            }
            iterations++;
        }
        if (status == CollectorResult.Status.BatchRunning) {
            status = CollectorResult.Status.BatchTimeout;
        }
        CollectorResult batchResult = new CollectorResult(results);
        batchResult.setStatus(status);
        batchResult.setExecutionTime(System.currentTimeMillis() - startTime);
        return batchResult;
    }

    private Future<MonitorAgentResult> run(CloudhubMonitorAgent job) {
        return executor.submit(job);
    }

    private CloudhubMonitorAgent addAgentInternal(ConnectionConfiguration configuration, boolean connect) {
        CloudhubAgentInfo agentInfo = createMonitorAgentInfo(configuration);
        CloudhubMonitorAgent job = null;
        if (agentInfo != null) {
            String cloudhubMonitorAgentBeanName = agentInfo.getCloudhubMonitorAgentBeanName();
            job = (CloudhubMonitorAgent)beanFactory.getBean(cloudhubMonitorAgentBeanName, new Object[]{configuration, agentInfo});
            if (configuration.getCommon().isServerSuspended())
                job.suspend();
            agents.add(job);
            log.info(String.format("adding thread for Agent %s for Virtual System %s",
                    configuration.getCommon().getDisplayName(), configuration.getCommon().getVirtualSystem()));

            GwosService gwosService = gwosServiceFactory.getGwosServicePrototype(configuration, agentInfo);
            if (!configuration.getCommon().isServerSuspended() && connect) {
                try {
                    if (gwosService.authenticate(configuration)) {
                        if (log.isInfoEnabled())
                            log.info("Authenticated OK for agent " + agentInfo.getName());
                    } else {
                        if (log.isInfoEnabled())
                            log.info("Authenticated Failed for agent " + agentInfo.getName() + ", suspending");
                        job.suspend();
                    }
                }
                catch (Exception e) {
                    if (log.isInfoEnabled())
                        log.info("Authenticated Failed for agent " + agentInfo.getName() + ", suspending");
                    job.suspend();
                }
            }
        }
        else {
            log.error("skipping unsupported Virtual System: " + configuration.getCommon().getVirtualSystem());
        }
        return job;
    }

    public CloudhubMonitorAgent createMonitorAgent(MonitorConnectionConfig configuration) {
        CloudhubAgentInfo agentInfo = createMonitorAgentInfo(configuration);
        String cloudhubMonitorAgentBeanName = agentInfo.getCloudhubMonitorAgentBeanName();
        return (CloudhubMonitorAgent)beanFactory.getBean(cloudhubMonitorAgentBeanName, new Object[]{configuration, agentInfo});
    }

    public CloudhubAgentInfo createMonitorAgentInfo(MonitorConnectionConfig monitorConnectionConfig) {
        ConnectionConfiguration connection = (ConnectionConfiguration)monitorConnectionConfig;
        ConfigurationProvider provider = connectorFactory.getConfigurationProvider(connection.getCommon().getVirtualSystem());
        String applicationType = ((connection.getCommon().getApplicationType() != null) ?
                connection.getCommon().getApplicationType() : provider.getApplicationType());
        CloudhubAgentInfo agentInfo = new CloudhubAgentInfo(
                provider.getHypervisorDisplayName(),
                provider.getCloudhubMonitorAgentBeanName(),
                provider.getConnectorName(),
                provider.getManagementServerDisplayName(),
                applicationType,
                connection.getCommon().getVirtualSystem(),
                connection.getCommon().getConnectionRetries(),
                connection.getCommon().getAgentId());
        agentInfo.setName(connection.getCommon().getConfigurationFile());
        return agentInfo;
    }

    private CloudhubMonitorAgent findAgentByName(String agentName) {
        boolean isUUID = StringUtils.isUUID(agentName);
        for (CloudhubMonitorAgent agent : agents) {
            if (!isUUID) {
                if (agent.getAgentInfo().getName().equals(agentName))
                    return agent;
            }
            else {
                if (agent.getAgentInfo().getAgentId().equals(agentName))
                    return agent;
            }
        }
        return null;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public CloudhubMonitorAgent lookup(String agentName) {
        return findAgentByName(agentName);
    }

    @Override
    public boolean remove(String agentIdentifier) {
        CloudhubMonitorAgent agent = findAgentByName(agentIdentifier);
        if (agent != null) {
            agents.remove(agent);
            if (log.isInfoEnabled())
                log.info("Shutting down agent " + agent.getAgentInfo().toString());
            agent.shutdown();
            return true;
        }
        return false;
    }

    private void migrateConfigurations() {
        for (VirtualSystem system : VirtualSystem.activeVirtualSystems) {
            try {
                profileService.migrateProfiles(system, configurationService.listConfigurations(system));
            }
            catch (Exception e) {
                log.error("Failed to migrate configuration for " + system + ", " + e.getMessage(), e);
            }
        }
    }

}
