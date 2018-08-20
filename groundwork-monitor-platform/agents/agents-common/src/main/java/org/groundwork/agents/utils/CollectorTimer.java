package org.groundwork.agents.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by dtaylor on 1/31/17.
 */
public class CollectorTimer {

    private long startTime;
    private String mainMessage;
    private SortedMap<String,CollectionInstance> collections;
    private long timerOrder = 0;

    public CollectorTimer(String mainMessage) {


        this.collections = new TreeMap();
        this.startTime = System.currentTimeMillis();
        this.mainMessage = mainMessage;
    }

    public void start(String collectorName) {
        CollectionInstance collection = new CollectionInstance(collectorName);
        collections.put(collectorName, collection);
    }

    public long stop(String collectorName) {
        CollectionInstance collection = collections.get(collectorName);
        if (collection != null) {
            return collection.stop();
        }
        return 0;
    }

    public long stopStart(String stop, String start) {
        long result = 0;
        CollectionInstance collection = collections.get(stop);
        if (collection != null) {
            result = collection.stop();
        }
        start(start);
        return result;
    }

    public String end() {
        long executionTime = System.currentTimeMillis() - startTime;
        StringBuffer result = new StringBuffer(200);
        result.append(mainMessage).append(" [");
        List<Map.Entry<String, CollectionInstance>> list =
                new LinkedList<Map.Entry<String, CollectionInstance>>(collections.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, CollectionInstance>>() {
            public int compare(Map.Entry<String, CollectionInstance> o1,
                               Map.Entry<String, CollectionInstance> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });
        for (Map.Entry<String, CollectionInstance> collection : list) {
            result.append(collection.getValue().getTag());
            result.append("(");
            result.append(Double.toString(collection.getValue().getExecutionTime() / 1000.0));
            result.append(") ");
        }
        result.append("] - Total: ");
        result.append(Double.toString(executionTime / 1000.0));
        return result.toString();
    }

    class CollectionInstance implements Comparable<CollectionInstance> {
        private long startTime;
        private long executionTime;
        private String tag;
        private boolean isRunning = false;
        private Long order;

        public CollectionInstance() {
        }
        
        public CollectionInstance(String tag) {
            this.tag = tag;
            this.startTime = System.currentTimeMillis();
            this.isRunning = true;
            this.order = timerOrder;
            timerOrder++;
        }

        public long stop() {
            this.executionTime = (System.currentTimeMillis() - startTime);
            this.isRunning = false;
            return this.executionTime;
        }
        public long getStartTime() {
            return startTime;
        }

        public String getTag() {
            return tag;
        }

        public long getExecutionTime() {
            return (isRunning) ? (System.currentTimeMillis() - startTime) : executionTime;
        }

        public Long getOrder() {
            return order;
        }

        @Override
        public int compareTo(CollectionInstance o) {
            int result =  this.getOrder().compareTo(o.getOrder());
            return result;
        }
    }
}
