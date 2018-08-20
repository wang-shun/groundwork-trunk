package org.groundwork.cloudhub.synthetics;

public class GroundworkFunctions {

    public GroundworkFunctions() {}

    /*
        1 Kilobyte = 1,024 Bytes
        1 Megabyte = 1,048,576 Bytes
        1 Gigabyte = 1,073,741,824 Bytes
        1 Terabyte = 1,099,511,627,776 Bytes
     */

    public Number MB(Number bytes) {
        return bytes.doubleValue() / 1048576;
    }
    public Number KB(Number bytes) {
        return bytes.doubleValue() / 1024;
    }
    public Number GB(Number bytes) {
        return bytes.doubleValue() / 1073741824L;
    }
    public Number TB(Number bytes) {
        return bytes.doubleValue() / 1099511627776L;
    }


    /**
     * This Function provides percentage usage synthetic values.
     * Calculates the usage percentage for a given <code>used</code> metric and a corresponding <code>available</code> metric.
     * Both the used metric and available metric can be scaled by corresponding scale factor parameters.
     *
     * Example:
     *
     *  scalePercentageUsed(summary.quickStats.overallMemoryUsage,summary.hardware.memorySize, 1.0, 1.0)
     *
     * @param used Represents a 'used' metric value of how much of this resource has been used such as 'overallMemoryUsage'
     * @param available Represents the totality of a resource, such as all memory available
     * @param usedScaleFactor multiply usage parameter by this value, or pass in null to not scale. Passing in 1.0 will also not scale
     * @param availableScaleFactor multiply available parameter by this value, or pass in null to not scale. Passing in 1.0 will also not scale
     * @return The percentage usage as an integer
     */
    public Integer scalePercentageUsed(Number used, Number available, Double usedScaleFactor, Double availableScaleFactor) {
        if (used.longValue() == 0 && available.longValue() == 0)
            return 0;

        Double usage = (usedScaleFactor == null) ? used.doubleValue() : (used.doubleValue() * usedScaleFactor);
        Double availableScaled = (availableScaleFactor == null) ? available.doubleValue() : (available.doubleValue() * availableScaleFactor);
        usage = (usage == 0) ? 0 : usage / availableScaled;
        return toPercentage(usage);
    }

    /**
     * This Function provides percentage unused/free synthetic values.
     * Calculates the unused(free) percentage for a given <code>unused</code> metric and a corresponding <code>available</code> metric.
     * Both the unused metric and available metric can be scaled by corresponding scale factor parameters.
     *
     * Example:
     *
     *  scalePercentageUnused(summary.freeSpace,summary.capacity, 1.0, null, true)
     *
     * @param unused  Represents a metric reference value of how much of this resource has not be used (free)
     * @param available Represents the totality of a resource, such as all disk space available
     * @param usageScaleFactor multiply usage parameter by this value, or pass in null to not scale. Passing in 1.0 will also not scale
     * @param availableScaleFactor multiply available parameter by this value, or pass in null to not scale. Passing in 1.0 will also not scale
     * @return The percentage not used (free) as an integer
     */
    public int scalePercentageUnused(Number unused, Number available, Double usageScaleFactor, Double availableScaleFactor) {
        if (unused.longValue() == 0 && available.longValue() == 0)
            return 0;

        double usage = (usageScaleFactor == null) ? unused.doubleValue() : (unused.doubleValue() * usageScaleFactor);
        double availableScaled = (availableScaleFactor == null) ? available.doubleValue() : (available.doubleValue() * availableScaleFactor);
        usage = (usage == 0) ? 0 : usage / availableScaled;
        usage = 1.0 - usage;
        return toPercentage(usage);
    }

    /**
     * This Function provides percentage usage synthetic values.
     * Calculates the usage percentage for a given <code>used</code> metric and a corresponding <code>available</code> metric.
     *
     * Example:
     *
     *  scalePercentageUsed(summary.quickStats.overallMemoryUsage, summary.hardware.memorySize)
     *
     * @param used Represents a 'used' metric value of how much of this resource has been used such as 'overallMemoryUsage'
     * @param available Represents the totality of a resource, such as all memory available
     * @return The percentage usage as an integer
     */
    public Integer percentageUsed(Number used, Number available)
    {
        return scalePercentageUsed(used, available, 1.0, null);
    }

    /**
     * This Function provides percentage unused/free synthetic values.
     * Calculates the unused(free) percentage for a given <code>unused</code> metric and a corresponding <code>available</code> metric.
     * Both the unused metric and available metric can be scaled by corresponding scale factor parameters.
     *
     * Example:
     *
     *  scalePercentageUnused(summary.freeSpace, summary.capacity)
     *
     * @param unused  Represents a metric reference value of how much of this resource has not be used (free)
     * @param available Represents the totality of a resource, such as all disk space available
     * @return The percentage not used (free) as an integer
     */
    public Integer percentageUnused(Number unused, Number available)
    {
        double demand = unused.doubleValue();
        double usage = available.doubleValue();
        return scalePercentageUnused(demand, usage, 1.0, null);
    }

    /**
     * Given two metrics, <code>dividend</code> and <code>divisor</code> divides them and returns a percentage ratio
     *
     * Example:
     *
     *  GW:divideToPercentage(summary.quickStats.overallMemoryUsage,summary.hardware.memorySize)
     *
     * @param dividend typically a usage or free type metric
     * @param divisor typically a totality type metric, such as total disk space
     * @return The percentage ratio as an integer
     */
    public Integer divideToPercentage(Number dividend, Number divisor) {
        if (divisor.intValue() == 0) return 0;
        return toPercentage(dividend.doubleValue() / divisor.doubleValue());
    }

    /**
     * Turns a number such as .87 into an integer percentage (87). Also handles rounding of percentages
     *
     * @param value the value to be rounded to a full integer percentage
     * @return the percentage value as an integer
     */
    public Integer toPercentage(Number value) {
        Double result = (Double)value * 100;
        result = max(0.0, result);
        result = (double)((int)(result+0.49));
        return result.intValue();
    }

    public Integer toPercentageLimit(Number value) {
        Double result = (Double)value * 100;
        result = max(0.0, min(100.0, result ));
        result = (double)((int)(result+0.49));
        return result.intValue();
    }

    public Double toDouble(Integer v) {
        return v.doubleValue();
    }
    public Double toDouble(Double v) {
        return v.doubleValue();
    }
    public Double toDouble(Float v) {
        return v.doubleValue();
    }
    public Double toDouble(Long integer) {
        return integer.doubleValue();
    }

    public Float toFloat(Integer v) {
        return v.floatValue();
    }
    public Float toFloat(Long v) {
        return v.floatValue();
    }
    public Float toFloat(Double v) {
        return v.floatValue();
    }
    public Float toFloat(Float v) {
        return v.floatValue();
    }

    public Integer toInteger(Double v) {
        return v.intValue();
    }
    public Integer toInteger(Float v) {
        return v.intValue();
    }
    public Integer toInteger(Integer v) {
        return v.intValue();
    }
    public Integer toInteger(Long v) {
        return v.intValue();
    }

    public Long toLong(Double v) {
        return v.longValue();
    }
    public Long toLong(Float v) {
        return v.longValue();
    }
    public Long toLong(Integer v) {
        return v.longValue();
    }
    public Long toLong(Long v) {
        return v.longValue();
    }


    // Recommend using Math: equivalents
    public Double max(Double a, Double b) { return a > b ? a : b; }
    public Double min(Double a, Double b) { return a < b ? a : b; }
    public Integer max(Integer a, Integer b) { return a > b ? a : b; }
    public Integer min(Integer a, Integer b) { return a < b ? a : b; }

    public Number MB2(Number bytes) {
        return bytes.doubleValue() / 1000000;
    }
    public Number KB2(Number bytes) {
        return bytes.doubleValue() / 1000;
    }
    public Number GB2(Number bytes) {
        return bytes.doubleValue() / 1000000000L;
    }
    public Number TB2(Number bytes) {
        return bytes.doubleValue() / 1000000000000L ;
    }

}
