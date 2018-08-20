package org.groundwork.cloudhub.connectors.cloudera;

/**
 * Created by dtaylor on 6/19/17.
 */
public class MetricName implements Comparable<MetricName> {

    private String name;
    private String lowerCaseName;
    private String canonicalName;

    /**
     * Construct autcomplete name.
     *
     * @param name autocomplete name
     */
    public MetricName(String name) {
        if (name == null) {
            throw new NullPointerException("Name cannot be null");
        }
        this.name = name;
        this.lowerCaseName = name.toLowerCase();
    }

    /**
     * Construct autcomplete name for alias.
     *
     * @param name autocomplete alias name
     * @param canonicalName autocomplete canonical name
     */
    public MetricName(String name, String canonicalName) {
        this(name);
        this.canonicalName = canonicalName;
    }


    /**
     * Comparable compareTo implementation: compare folded names.
     *
     * @param other compare to other
     * @return comparison
     */
    @Override
    public int compareTo(MetricName other) {
        return lowerCaseName.compareTo(other.lowerCaseName);
    }

    /**
     * Object hashCode implementation: hash folded name.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return lowerCaseName.hashCode();
    }

    /**
     * Object equals implementation: test folded names.
     *
     * @return equals
     */
    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other instanceof MetricName) {
            return lowerCaseName.equals(((MetricName) other).lowerCaseName);
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public String getLowerCaseName() {
        return lowerCaseName;
    }

    public String getCanonicalName() {
        return canonicalName;
    }
}
