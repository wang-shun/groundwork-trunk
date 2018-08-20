package org.groundwork.cloudhub.synthetics;

import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.MapContext;

import java.util.Map;

public class SyntheticContext {

    private JexlContext jc;

    public SyntheticContext(Map<String, Object> metrics) {
        jc = new MapContext();
        for (String path : metrics.keySet()) {
            Object metric = metrics.get(path);
            if (metric != null) {
                jc.set(path, metric);
            }
        }
    }

    public JexlContext getJexlContext() {
        return jc;
    }

}
