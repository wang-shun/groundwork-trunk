package com.groundwork.agents.vema.base;

public class VemaBaseQuery 
{
    private String      queryString;
    private int         thresholdWarning;
    private int         thresholdCritical;
    private boolean     graphFlag;
    private boolean     monitorFlag;
    private boolean     traceFlag;

    public VemaBaseQuery(String query, int warning, int critical, boolean isGraphed, boolean isMonitored )
    {
        this.queryString       = query;
        this.thresholdWarning  = warning;
        this.thresholdCritical = critical;
        this.graphFlag         = isGraphed;
        this.monitorFlag       = isMonitored;
        this.traceFlag         = false;          // nominal state of things.
    }

    public VemaBaseQuery(String query, int warning, int critical, boolean isGraphed, boolean isMonitored, boolean isTraced )
    {
        this.queryString       = query;
        this.thresholdWarning  = warning;
        this.thresholdCritical = critical;
        this.graphFlag         = isGraphed;
        this.monitorFlag       = isMonitored;
        this.traceFlag         = isTraced;
    }

    public String getQuery()      { return this.queryString;       }
    public int    getWarning()    { return this.thresholdWarning;  }
    public int    getCritical()   { return this.thresholdCritical; }
    public boolean isGraphed()    { return this.graphFlag;         }
    public boolean isMonitored()  { return this.monitorFlag;       }
    public boolean isTraced()     { return this.traceFlag;         }
}

