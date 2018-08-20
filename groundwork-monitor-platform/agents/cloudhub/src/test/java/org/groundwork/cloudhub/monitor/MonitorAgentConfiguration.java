package org.groundwork.cloudhub.monitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;

//@Profile("testing")
@Configuration
@ComponentScan(basePackages="org.groundwork")
//, excludeFilters = @ComponentScan.Filter(type= FilterType.ASSIGNABLE_TYPE, value=MonitorAgentCollectorService.class))
//@PropertySource("classpath:/cloudhub.properties")
public class MonitorAgentConfiguration {

    /**
     * Should we scan configurations at startup
     */
    public static final String SCAN_CONFIGS_AT_STARTUP = "monitor.agent.scan.at.startup";

    @Autowired
    private Environment environment;

    public MonitorAgentConfiguration() {
    }

//    @Bean(name=MonitorAgentCollectorService.NAME)
//    MonitorAgentCollectorService getMonitorAgentCollectorService() {
//        return new MonitorAgentCollectorService(environment.getProperty(SCAN_CONFIGS_AT_STARTUP, Boolean.class, Boolean.FALSE));
//    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() {
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        propertySourcesPlaceholderConfigurer.setLocation(new ClassPathResource("cloudhub.properties"));
        return propertySourcesPlaceholderConfigurer;
    }

    @Bean
    public Environment getEnvironment() {
        return environment;
    }

}
