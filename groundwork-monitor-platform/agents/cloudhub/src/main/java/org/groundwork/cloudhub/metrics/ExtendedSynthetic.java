package org.groundwork.cloudhub.metrics;

/**
 * Created by dtaylor on 11/12/14.
 */
public class ExtendedSynthetic extends BaseSynthetic {

    public enum SyntheticOperation {
        add,
        subtract,
        divide,
        multiply,
        none
    }

    ;

    private SyntheticOperation operation = SyntheticOperation.none;
    private double constant = 0;

    public ExtendedSynthetic(
            String handle,
            String lookup1,
            double factor1,
            String lookup2,
            boolean fromTop,
            boolean toPercent,
            SyntheticOperation operation,
            double constant) {
        super(handle, lookup1, factor1, lookup2, fromTop, toPercent);
        this.operation = operation;
        this.constant = constant;
    }

    public ExtendedSynthetic(
            String handle,
            String lookup1,
            double factor1,
            String lookup2,
            boolean fromTop,
            boolean toPercent) {
        super(handle, lookup1, factor1, lookup2, fromTop, toPercent);
    }

    public ExtendedSynthetic(
            String handle,
            String lookup1,
            SyntheticOperation operation,
            double constant) {
        this(handle, lookup1, 1.0, "", false, false, operation, constant);
    }

    public int computeWithConstant(String v1, String v2) {
        double value1;
        double x;

        try {
            value1 = Double.parseDouble(v1);
        } catch (Exception e) {
            value1 = 0;
        }
        switch (operation) {
            case multiply:
                value1 = value1 * constant;
                break;
            case divide:
                value1 = value1 / constant;
                break;
            case add:
                value1 = value1 + constant;
                break;
            case subtract:
                value1 = value1 - constant;
                break;
        }
        return (int) value1;
    }

    public int compute(String v1) {
        return compute(v1, null);
    }

    public int compute(String v1, String v2) {

        if (operation != SyntheticOperation.none)
            return computeWithConstant(v1, v2);

        return super.compute(v1, v2);
    }

}
