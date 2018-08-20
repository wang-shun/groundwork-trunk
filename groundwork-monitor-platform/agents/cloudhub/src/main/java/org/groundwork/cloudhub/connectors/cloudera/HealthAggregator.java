package org.groundwork.cloudhub.connectors.cloudera;

import com.cloudera.api.model.ApiHealthCheck;
import com.cloudera.api.model.ApiHealthSummary;
import com.cloudera.api.model.ApiRole;
import com.cloudera.api.v1.RolesResource;
import com.cloudera.api.v14.ServicesResourceV14;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class HealthAggregator {

    class HealthAggregationResult {
        private int good = 0;
        private int bad = 0;
        private int concerning = 0;
        private int disabled = 0;
        private int total = 0;
        private String name;

        public HealthAggregationResult(String name) {
            this.name = name;
        }

        public int getGood() {
            return good;
        }

        public void setGood(int good) {
            this.good = good;
        }

        public int getBad() {
            return bad;
        }

        public void setBad(int bad) {
            this.bad = bad;
        }

        public int getConcerning() {
            return concerning;
        }

        public void setConcerning(int concerning) {
        }

        public int getDisabled() {
            return disabled;
        }

        public void setDisabled(int disabled) {
            this.disabled = disabled;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void increment(String health) {
            total = total + 1;
            switch(health) {
                case "BAD":
                    bad = bad + 1;
                    break;
                case "CONCERNING":
                    concerning = concerning + 1;
                    break;
                case "DISABLED":
                    disabled = disabled + 1;
                    break;
                default: // GOOD
                    good = good + 1;
                    break;
            }
        }

        public String totalPercentageGood() {
            if (total == 0) {
                return ".";
            }
            StringBuffer result = new StringBuffer();
            result.append(". Percent healthy: ");
            result.append(String.format("%.2f%%", ((good / total) * 100.0)));
            return result.toString();
        }
    }

    public List<ApiHealthCheck> processKafkaHealthChecks(ServicesResourceV14 servicesResource) {
        Map<String, HealthAggregationResult> results = new HashMap<>();
        RolesResource rolesResource = servicesResource.getRolesResource("kafka");
        if (rolesResource != null) {
            for (ApiRole role : rolesResource.readRoles()) {
                HealthAggregationResult result = results.get(role.getType());
                if (result == null) {
                    result = new HealthAggregationResult(role.getType());
                    results.put(role.getType(), result);
                }
                result.increment(role.getHealthSummary().toString());
            }
        }
        List<ApiHealthCheck> healthChecks = new LinkedList<>();
        HealthAggregationResult mirror = results.get("KAFKA_MIRROR_MAKER");
        if (mirror != null) {
            ApiHealthCheck healthCheck = new ApiHealthCheck("KAFKA_KAFKA_MIRROR_MAKER_SCM_HEALTH",  mapHealthSummary(mirror));
            healthCheck.setExplanation(buildExplanation(mirror));
            healthChecks.add(healthCheck);
        }
        HealthAggregationResult broker = results.get("KAFKA_BROKER");
        if (broker != null) {
            ApiHealthCheck healthCheck = new ApiHealthCheck("KAFKA_KAFKA_BROKER_SCM_HEALTH",  mapHealthSummary(broker));
            healthCheck.setExplanation(buildExplanation(broker));
            healthChecks.add(healthCheck);
        }
        HealthAggregationResult gateway = results.get("GATEWAY");
        if (gateway != null) {
            ApiHealthCheck healthCheck = new ApiHealthCheck("KAFKA_KAFKA_GATEWAY_SCM_HEALTH",  mapHealthSummary(gateway));
            healthCheck.setExplanation(buildExplanation(gateway));
            healthChecks.add(healthCheck);
        }
        return healthChecks;
    }

    protected String buildExplanation(HealthAggregationResult result) {
        StringBuffer e = new StringBuffer();
        e.append("Role instances in status GOOD: ").append(result.good).append(", BAD: ").append(result.bad);
        e.append(", CONCERNING: ").append(result.concerning).append(", DISABLED: ").append(result.disabled);
        e.append(result.totalPercentageGood());
        return e.toString();
    }

    protected ApiHealthSummary mapHealthSummary(HealthAggregationResult result) {
        ApiHealthSummary summary = ApiHealthSummary.GOOD;
        if (result.getBad() > 0) {
            summary = ApiHealthSummary.BAD;
        }
        else if (result.getDisabled() > 0) {
            summary = ApiHealthSummary.DISABLED;
        }
        else if (result.getConcerning() > 0) {
            summary = ApiHealthSummary.CONCERNING;
        }
        return summary;
    }
}
