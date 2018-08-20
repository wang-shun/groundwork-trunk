package org.groundwork.cloudhub.gwos;


import org.groundwork.cloudhub.configuration.ConnectionConfiguration;
import org.groundwork.cloudhub.configuration.GWOSConfiguration;
import org.groundwork.cloudhub.monitor.CloudhubAgentInfo;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.stereotype.Service;

@Service(GwosServiceFactory.NAME)
public class GwosServiceFactory implements BeanFactoryAware {

    public static final String NAME = "GwosServiceFactory";

    private BeanFactory beanFactory;
    private boolean useBiz = true;

    /**
     * Support 7.0 and 7.1
     * as of CloudHub 2.0, no longer support 6.x
     *
     * @param connection
     * @param agentInfo
     * @return
     */
    public GwosService getGwosServicePrototype(ConnectionConfiguration connection, CloudhubAgentInfo agentInfo) {
        String version = connection.getGwos().getGwosVersion() == null ? GWOSConfiguration.DEFAULT_VERSION : connection.getGwos().getGwosVersion();
        if (version.startsWith("7.0")) {
            return (GwosService) beanFactory.getBean(GwosService.NAME70, new Object[]{connection, agentInfo});
        }
        else {
            if (useBiz) {
                return (GwosService) beanFactory.getBean(GwosService.NAMEBIZ, new Object[]{connection, agentInfo});
            }
            else {
                return (GwosService) beanFactory.getBean(GwosService.NAME71, new Object[]{connection, agentInfo});
            }
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    public boolean isUseBiz() {
        return useBiz;
    }

    public void setUseBiz(boolean useBiz) {
        this.useBiz = useBiz;
    }

}
