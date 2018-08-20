package org.groundwork.cloudhub.synthetics;

import org.apache.commons.collections.map.HashedMap;
import org.groundwork.cloudhub.AbstractAgentTest;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.groundwork.cloudhub.metrics.BaseSynthetic;
import org.groundwork.cloudhub.monitor.MonitorAgentConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MonitorAgentConfiguration.class})
public class SyntheticsTest extends AbstractAgentTest {

    @Autowired
    private Synthetics synthetics;

    @Test
    public void testSynthetics() throws Exception {

        BaseSynthetic cpuToMax = new BaseSynthetic("syn.vm.cpu.cpuToMax.used",
                "summary.quickStats.overallCpuDemand", 1.0,
                "summary.runtime.maxCpuUsage", false, true);

        // 10
        int ten = cpuToMax.compute("334", "3190");
        assert ten == 10;
        int three = cpuToMax.compute("67", "2260");
        assert three == 3;

        Map<String, Object> metrics = new HashedMap() {
            {
                put("summary.quickStats.overallMemoryUsage", 500);
                put("summary.hardware.memorySize", 2000);
                put("summary.quickStats.overallCpuDemand", 334);
                put("summary.runtime.maxCpuUsage", 3190);
                put("summary.storage.uncommitted", 2510000000L);
            }
        };

        SyntheticContext ctx = synthetics.createContext(metrics);
        Integer result = (Integer) synthetics.evaluate(ctx, "GW:divideToPercentage(summary.quickStats.overallMemoryUsage,summary.hardware.memorySize)");
        Integer result2 = (Integer) synthetics.evaluate(ctx, "GW:percentageUsed(summary.quickStats.overallCpuDemand, summary.runtime.maxCpuUsage)");
        Integer result3 = (Integer) synthetics.evaluate(ctx, "GW:percentageUnused(summary.quickStats.overallCpuDemand, summary.runtime.maxCpuUsage)");
        System.out.println(synthetics.evaluate(ctx, "Math:max(25.222,25.223)"));

        Number o = synthetics.evaluate(ctx, "GW:MB(summary.storage.uncommitted)");
        Number o1 = synthetics.evaluate(ctx, "GW:KB(summary.storage.uncommitted)");
        Number o2 = synthetics.evaluate(ctx, "GW:GB(summary.storage.uncommitted)");

        System.out.println("*** GB = " + synthetics.format((Double) o, "%1$.2f GB"));
        System.out.println("*** GB = " + synthetics.format(0f, "%1$.2fGB"));

        Number o3 = synthetics.evaluate(ctx, "GW:MB2(summary.storage.uncommitted)");
        Number o4 = synthetics.evaluate(ctx, "GW:KB2(summary.storage.uncommitted)");
        Number o5 = synthetics.evaluate(ctx, "GW:GB2(summary.storage.uncommitted)");

        System.out.println("*** result = " + synthetics.format(result.intValue(), "%d%%"));
        System.out.println("*** result2 = " + synthetics.format(result2, "%d%%"));
        System.out.println("*** result3 = " + synthetics.format(result3, "%d%%"));

        // Failure
        try {
            Number g = synthetics.evaluate(ctx, "gobblygook");
        } catch (CloudHubException e) {
            System.out.println("--" + e.getMessage() + ", " + e.getAdditional());
        }
        try {
            Number g = synthetics.evaluate(ctx, "GW:badMethod() + summary.storage.uncommitted + badVar");
        } catch (CloudHubException e) {
            System.out.println("--" + e.getMessage() + ", " + e.getAdditional());
        }

    }

    @Test
    public void testVariables() throws Exception {
        // Default case
        List<String> vars = synthetics.extractVariables("23");
        assert vars.size() == 0;
        vars = synthetics.extractVariables("((x + y) / a.b.c) + GW:MB2(summary.storage.uncommitted) * 200");
        assert vars.size() == 4;
        assert vars.get(0).equals("x");
        assert vars.get(1).equals("y");
        assert vars.get(2).equals("a.b.c");
        assert vars.get(3).equals("summary.storage.uncommitted");
    }

    @Test public void testConversion() throws Exception {

        Map<String, Object> metrics = new HashedMap() {
            {
                put("fd_open", 500);
                put("fd_max", 2000);
            }
        };   

        SyntheticContext ctx = synthetics.createContext(metrics);
        Double result = (Double) synthetics.evaluate(ctx, "(GW:toDouble(fd_open) / GW:toDouble(fd_max)) * 100.0");
        assert result == 25.0;
    }

    @Test public void testFormatting() {
        long n = 2175;
        System.out.format("%d%n", n);      //  -->  "2175"
        System.out.format("%05d%n", n);    //  -->  "02175"
        System.out.format("%+5d%n", n);    //  -->  " +2175"
        System.out.format("%,d%n", n);    // -->  " 2,175"
        System.out.format("%,d%%n", n); //  -->  "2175%%"

        double pi = Math.PI;

        System.out.format("%f%n", pi);       // -->  "3.141593"
        System.out.format("%.3f%n", pi);     // -->  "3.142"
        System.out.format("%10.3f%n", pi);   // -->  "     3.142"
        System.out.format("%-10.3f%n", pi);  // -->  "3.142"
        System.out.format("%.0f%n", pi);  // -->  "3.142"
        System.out.format(Locale.FRANCE,
                "%-10.4f%n%n", pi); // -->  "3,1416"
    }

}
