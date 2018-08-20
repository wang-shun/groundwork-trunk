package org.groundwork.rs.restwebservices;

import com.groundwork.collage.CollageFactory;
import org.groundwork.rs.biz.BizResource;
import org.groundwork.rs.biz.RTMMResource;
import org.groundwork.rs.hubs.ProfileResource;
import org.groundwork.rs.resources.*;
import org.groundwork.rs.tasks.MetricsTask;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/api")
public class FoundationRESTApplication extends Application {

	private Set<Object> singletons = new HashSet<>();
    private Set<Class<?>> prototypes = new HashSet<>();

    public FoundationRESTApplication() {
        singletons.add(new ObjectMapperContextResolver());
        singletons.add(new FoundationExceptionMapper());
        singletons.add(new CacheControlResponseFilter());

        singletons.add(new EventGeneration());
        singletons.add(new NomaNotification());
        singletons.add(new PerformanceData());
        singletons.add(new VEMAProfile());

        singletons.add(new AutoRegister());
        singletons.add(new PluginUpdates());
        singletons.add(new UploadProfiles());
        singletons.add(new AuthResource());
        singletons.add(new HostResource());
        singletons.add(new HostGroupResource());
        singletons.add(new DeviceResource());
        singletons.add(new EventResource());
        singletons.add(new ServiceResource());
        singletons.add(new StatisticResource());
        singletons.add(new CategoryResource());
        singletons.add(new MetaResource());
        singletons.add(new GraphResource());
        singletons.add(new AgentResource());
        singletons.add(new VersionResource());
        singletons.add(new PropertyTypeResource());
        singletons.add(new ApplicationTypeResource());
        singletons.add(new ConsolidationResource());
        singletons.add(new EntityTypeResource());
        singletons.add(new NotificationResource());
        singletons.add(new PerfDataResource());
        singletons.add(new SettingsResource());
        singletons.add(new BizResource());
        singletons.add(new ServiceGroupResource());
        singletons.add(new AuditLogResource());
        singletons.add(new ProfileResource());
        singletons.add(new HostIdentityResource());
        singletons.add(new HostBlacklistResource());
        singletons.add(new DeviceTemplateProfileResource());
        singletons.add(new LicenseResource());
        singletons.add(new CustomGroupResource());
        singletons.add(new RTMMResource());
        singletons.add(new CollectorConfigResource());
        singletons.add(new SuggestionsResource());
        singletons.add(new CacheStatisticsResource());
        singletons.add(new LDAPAuthResource());
        singletons.add(new CommentResource());

        // Register this with foundation to ensure it is closed properly as part of foundation lifecycle
        CollageFactory.getInstance().getFoundationLifecycleManager().register(new MetricsTask());
    }

    @Override
    public Set<Object> getSingletons() {
            return singletons;
    }

    @Override
    public Set<Class<?>> getClasses() {
        return prototypes;
    }

}
