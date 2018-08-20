package org.groundwork.cloudhub.synthetics;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlException;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.JexlScript;
import org.apache.log4j.Logger;
import org.groundwork.agents.utils.StringUtils;
import org.groundwork.cloudhub.exceptions.CloudHubException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class Synthetics {

    private static Logger log = Logger.getLogger(Synthetics.class);

    public static final String FAILED_TO_PROCESS_SYNTHETIC = "Failed to process synthetic: [";
    public static final String FAILED_TO_EVALUATE_SYNTHETIC = "Failed to evaluate synthetic: [";
    public static final String FAILED_TO_CONVERT_SYNTHETIC = "Failed to convert synthetic to Number: [";
    public static final String FAILED_TO_EVALUATE_FORMAT = "Failed to format metric: [";

    protected JexlEngine engine;
    protected JexlBuilder builder;

    @Value("${synthetics.expressions.cacheSize}")
    protected int cacheSize = 128;
    @Value("${synthetics.expressions.cacheThreshold}")
    protected int cacheThreshold = 256;

    /**
     * Spring post construct initializer, creates and configures the expression evaluator and its expression cache.
     * We also inject the two namespaces here: Groundwork Functions and Java Math functions
     *
     */
    @PostConstruct
    protected void init() {
        Map<String, Object> namespaces = new HashMap<String, Object>();
        namespaces.put("GW", new GroundworkFunctions());
        namespaces.put("Math", Math.class);
        builder = new JexlBuilder().cache(cacheSize).cacheThreshold(cacheThreshold)
                .silent(false).strict(true)
                .namespaces(namespaces)
                .debug(false);
        engine = builder.create();
    }

    /**
     * Create a synthetic expression context for all metric properties of a given resource. This context is reused
     * for all metric properties on one resource. Note that expressions are cached, see <code>init</code> method above
     *
     * @param properties the map of metric query names to metric sample values
     * @return a new expression context
     */
    public SyntheticContext createContext(Map<String,Object> properties) {
        return new SyntheticContext(properties);
    }

    /**
     * Evaluate an expression for a set of metric sample values of a given resource. Expressions are cached, so the
     * createExpression method can return a cached instance of an expression, which can improve performance.
     * Expression evaluation can fail. When this happens, the expression string is added to a CloudHub exception as
     * additional information. This is useful for consolidating error handling, so we don't overrun the logs with
     * repeated expression evaluation errors across all evaluated instance's in a metric collection run
     *
     * @param context the synthetic expression context, holding all metric value samples
     * @param expression the string expression to be evaluated
     * @return The mathematical result of an expression evaluation, a subclass of <code>Number</code> such as Integer, Long, Double
     * @throws CloudHubException to normalize all variants of exceptions. The expression is added to the exception as additional information
     */
    public Number evaluate(SyntheticContext context, String expression) throws CloudHubException {
        try {
            JexlExpression jexlExpression = engine.createExpression(expression);  // uses cache
            Object result = jexlExpression.evaluate(context.getJexlContext());
            if (result == null) {
                String message = FAILED_TO_EVALUATE_SYNTHETIC + expression + "] ";
                if (log.isDebugEnabled()) log.debug(message);
                throw new CloudHubException(message, expression);
            }
            // Should always be a Number (Long,Integer,Double)
            if (result instanceof Number) {
                return (Number) result;
            }
        }
        catch(JexlException e) {
            // Jexl Syntax error
            String message = e.getMessage();
            int index = message.lastIndexOf("?:");
            message = (index == -1) ? message : message.substring(index + 1);
            String fullMessage = FAILED_TO_EVALUATE_SYNTHETIC + expression + "] -" + message;
            if (log.isDebugEnabled()) log.debug(fullMessage);
            throw new CloudHubException(fullMessage, expression, e);
        }
        catch(Exception e) {
            // this should not happen, need to log it
            String message = FAILED_TO_EVALUATE_SYNTHETIC + expression + "] -" + e.getMessage();
            log.warn(message, e);
            throw new CloudHubException(message, e);
        }
        // unknown data type
        String message = FAILED_TO_CONVERT_SYNTHETIC + expression  + "] ";
        log.error(message);
        throw new CloudHubException(message, expression);

    }

    /**
     * The format method is called after the expression is evaluated. The responsibility of matching expression return values
     * with formatting strings is up to the end user. For example, if <code>evaluate</code> returns an Integer, the
     * corresponding format parameter must have a match Java format statement such as "%d", similar for double values "%f"
     *
     * The format statement is a Java format string. It should have only one value substitution. Examples:
     *
     * For example, evaluate returns an Integer percentage, the format statement substitutes the value and adds the percent sign:
     *      <code>%d%%</code>
     * For example, evaluate returns a Double number as MB, the format statement substitutes the value ands MB postfix:
     *      <code>%f.2MB</code>
     *
     * @param value the Number subclassed value such as Integer, Long, Double to be formatted
     * @param format a Java formatting expression for a single value plus additional formatting. Should have only one value substitution
     * @return the formatted string created by applying parameter <code>format</code> to parameter <code>value</code>
     * @throws CloudHubException to normalize all variants of exceptions. The format string is added to the exception as additional information
     */
    public String format(Number value, String format) {
        try {
            if (StringUtils.isEmpty(format)) {
                return value.toString();
            }
            return String.format(format, value);
        }
        catch (Exception e) {
            String message = FAILED_TO_EVALUATE_FORMAT + format + "] - " + e.getMessage();
            if (log.isDebugEnabled()) log.debug(message);
            throw new CloudHubException(message, e);
        }
    }

    public List<String> extractVariables(String expression) {
        List<String> result = new ArrayList<>();
        JexlScript script = engine.createScript(expression);
        Set<List<String>> variables = script.getVariables();
        for (List<String> part : variables) {
            StringBuffer variable = new StringBuffer();
            int count = 0;
            for (String subvar : part) {
                if (count > 0) variable.append(".");
                variable.append(subvar);
                count++;
            }
            result.add(variable.toString());
        }
        return result;
    }

    private List<String> groundworkFunctions = null;
    private Object buildingFunctionsList = new Object();

    public List<String> listGroundworkFunction() {
        if (groundworkFunctions != null) {
            return groundworkFunctions;
        }
        synchronized(buildingFunctionsList) {
            Set<String> names = new HashSet<>();
            Method[] allMethods = GroundworkFunctions.class.getDeclaredMethods();
            for (Method method : allMethods) {
                if (Modifier.isPublic(method.getModifiers())) {
                    StringBuffer signature = new StringBuffer();
                    signature.append("GW:");
                    signature.append(method.getName());
                    signature.append("(");
                    int size = method.getParameterTypes().length;
                    for (int ix = 1; ix <= size; ix++) {
                        signature.append("arg" + ix);
                        if (ix < size) {
                            signature.append(",");
                        }
                    }
                    signature.append(")");
                    names.add(signature.toString());
                }
            }
            groundworkFunctions = new ArrayList(names);
            Collections.sort(groundworkFunctions);
            return groundworkFunctions;
        }
    }
}
