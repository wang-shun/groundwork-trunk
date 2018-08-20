package com.groundwork.collage.query;

import com.groundwork.collage.model.PropertyType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.groundwork.foundation.bs.metadata.MetadataService;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryTranslator {

    public static final String HOST_KEY = "host";
    public static final String HOSTGROUP_KEY = "hostGroup";
    public static final String DEVICE_KEY = "device";
    public static final String EVENT_KEY = "event";
    public static final String SERVICE_KEY = "serviceStatus";
    public static final String CATEGORY_KEY = "category";
    public static final String PROPERTY_TYPE_KEY = "propertyType";
    public static final String ENTITY_TYPE_KEY = "entityType";
    public static final String APPLICATION_TYPE_KEY = "applicationType";
    public static final String CONSOLIDATION_TYPE_KEY = "consolidation";
    public static final String AUDIT_LOG_KEY = "auditLog";
    public static final String HOST_IDENTITY_KEY = "hostIdentity";
    public static final String HOST_BLACKLIST_KEY = "hostBlacklist";
    public static final String DEVICE_TEMPLATE_PROFILE_KEY = "deviceTemplateProfile";

    public static final char WHITESPACE = ' ';
    public static final char LEFT_PAREN = '(';
    public static final char RIGHT_PAREN = ')';
    public static final char SINGLE_QUOTE = '\'';
    public static final String DOT = ".";
    public static final String PROPERTY_PREFIX = "property.";
    public static final String HQL_AND = "and";
    public static final String HQL_EQUALS  = "=";
    public static final String NAME_PROPERTY = ".name";
    public static final String TYPEID_PROPERTY = ".propertyTypeId";
    public static final String VALUE_STRING_PROPERTY = ".valueString";
    public static final String VALUE_DATE_PROPERTY = ".valueDate";
    public static final String VALUE_BOOLEAN_PROPERTY = ".valueBoolean";
    public static final String VALUE_INTEGER_PROPERTY = ".valueInteger";
    public static final String VALUE_LONG_PROPERTY = ".valueLong";
    public static final String VALUE_DOUBLE_PROPERTY = ".valueDouble";
    public static final String IDENTIFIER_REGEX = "^([_A-Za-z][_A-Za-z0-9]*?_*)";
    public static final String IDENTIFIER_CHAR_REGEX = "[_A-Za-z0-9]";
    public static final String FUNCTION_REGEX = "^([_A-Za-z][_A-Za-z0-9]*?_*)\\(([_.A-Za-z0-9]*?_*)\\)";
    private static final String CATEGORY_NAME_PREFIX = "category.name";
    private static final String CATEGORY_PREFIX = "category";
    private static final String SERVICEGROUP_PREFIX = "servicegroup";
    private static final String SQL_ORDER_BY = "order by";
    public static final String PASSTHROUGH = "pass:";

    protected static Log log = LogFactory.getLog(QueryTranslator.class);

    private final MetadataService metadataService;
    private final Map<String, QueryTranslatorInfo> identifierToPropertyMap = new ConcurrentHashMap<String, QueryTranslatorInfo>();
    private final Pattern literalPattern;
    private final Pattern literalCharPattern;
    private final Pattern functionPattern;

    public QueryTranslator(MetadataService metadataService) {
        this.metadataService = metadataService;
        identifierToPropertyMap.put(HOST_KEY, new HostQueryTranslatorInfo());
        identifierToPropertyMap.put(HOSTGROUP_KEY, new HostGroupQueryTranslatorInfo());
        identifierToPropertyMap.put(DEVICE_KEY, new DeviceQueryTranslatorInfo());
        identifierToPropertyMap.put(EVENT_KEY, new EventQueryTranslatorInfo());
        identifierToPropertyMap.put(SERVICE_KEY, new ServiceQueryTranslatorInfo());
        identifierToPropertyMap.put(CATEGORY_KEY, new CategoryQueryTranslatorInfo());
        identifierToPropertyMap.put(PROPERTY_TYPE_KEY, new PropertyTypeQueryTranslatorInfo());
        identifierToPropertyMap.put(ENTITY_TYPE_KEY, new EntityTypeQueryTranslatorInfo());
        identifierToPropertyMap.put(APPLICATION_TYPE_KEY, new ApplicationTypeQueryTranslatorInfo());
        identifierToPropertyMap.put(CONSOLIDATION_TYPE_KEY, new ConsolidationQueryTranslatorInfo());
        identifierToPropertyMap.put(AUDIT_LOG_KEY, new AuditLogQueryTranslatorInfo());
        identifierToPropertyMap.put(HOST_IDENTITY_KEY, new HostIdentityQueryTranslatorInfo());
        identifierToPropertyMap.put(HOST_BLACKLIST_KEY, new HostBlacklistQueryTranslatorInfo());
        identifierToPropertyMap.put(DEVICE_TEMPLATE_PROFILE_KEY, new DeviceTemplateProfileQueryTranslatorInfo());
        literalPattern = Pattern.compile(IDENTIFIER_REGEX);
        literalCharPattern = Pattern.compile(IDENTIFIER_CHAR_REGEX);
        functionPattern = Pattern.compile(FUNCTION_REGEX);
    }

    public boolean isPassThrough(String query) {
        return query.startsWith(PASSTHROUGH);
    }
    
    /**
     * Given a query string, translate user-friendly expressions to hibernate-compatible expressions
     * Property example:
     *          property.executionTime > 1000
     * is translated to
     *          host.hostStatus.propertyValues.name == ' executionTime ' and host.hostStatus.propertyValues.valueDouble > 1000
     *
     * Basic Property shortcut example:
     *         monitorStatus
     * is translated to:
     *         host.hostStatus.hostMonitorStatus.name
     *
     * @param query the original query string in user-friendly expression language
     * @param entityType the query entity defined by constants in this service
     * @return a QueryTranslation including the translated query string in hibernate compatible property expression language
     *         and a substitution list for post processing of the translated query
     */
    public QueryTranslation translate(String query, String entityType) {

        if (log.isDebugEnabled()) {
            log.debug(String.format("Translating query %s for entityType %s ...", query, entityType));
        }
        QueryTranslatorInfo info = identifierToPropertyMap.get(entityType);
        if (info == null)
            throw new RuntimeException("Failed to find entity query model for entityType " + entityType);

        if (query.startsWith(PASSTHROUGH)) {
            String passThrough = query.substring(PASSTHROUGH.length());
            QueryTranslation qt = new QueryTranslation(passThrough);
            qt.setHql(passThrough);
            return qt;
        }

        // Aliases for properties, maps from propertyName to alias
        Map<String, String> aliases = new ConcurrentHashMap<String, String>();
        QueryTranslation queryTranslation = new QueryTranslation(query);
        boolean hasProperties = false;

        // 1. First pass - tokenize query string, taking into account non-trivial cases such as spaces in string literals and parenthesis in expression
        List<String> tokens = new LinkedList<String>();
        boolean inStringLiteral = false;
        boolean inFunction = false;
        StringBuffer token = new StringBuffer();
        for (int ix = 0; ix < query.length(); ix++) {
            if (isParenthesis(query.charAt(ix))) {
                if (query.charAt(ix) == RIGHT_PAREN && inFunction)  {
                    token.append(query.charAt(ix));
                    tokens.add(token.toString());
                    token = new StringBuffer();
                    inFunction = false;
                    continue;
                }
                if (inStringLiteral) {
                    token.append(query.charAt(ix));
                    continue;
                }
                if (query.charAt(ix) == LEFT_PAREN && ix > 0 && isIdentifierChar(String.valueOf(query.charAt(ix-1)))) {
                    // function
                    inFunction = true;
                    token.append(query.charAt(ix));
                    continue;
                }
                if (token.length() > 0) {
                    tokens.add(token.toString());
                }
                tokens.add(String.valueOf(query.charAt(ix)));
                token = new StringBuffer();
                continue;
            }
            else if (Character.isWhitespace(query.charAt(ix))) {
                if (inStringLiteral) {
                    token.append(query.charAt(ix));
                    continue;
                }
                if (token.length() > 0) {
                    tokens.add(token.toString());
                }
                token = new StringBuffer();
                continue;
            }
            else if (isOperator(query.charAt(ix))) {
                if (inStringLiteral) {
                    token.append(query.charAt(ix));
                    continue;
                }
                if (token.length() > 0) {
                    tokens.add(token.toString());
                }
                token = new StringBuffer();
                token.append(query.charAt(ix));
                if (ix < query.length() - 1 && isOperator(query.charAt(ix+1))) {
                    token.append(query.charAt(ix+1));
                    ix = ix + 1;
                }
                tokens.add(token.toString());
                token = new StringBuffer();
                continue;
            }
            else if (query.charAt(ix) == SINGLE_QUOTE) {
                inStringLiteral = !inStringLiteral;
            }
            token.append(query.charAt(ix));
        }
        if (token.length() > 0) {
            tokens.add(token.toString());
        }

        // 2. Second pass - translated tokens starting with "property." into hibernate expressions
        //    translate basic properties shortcuts, but also support full property paths
        //    basic properties are only considered for translation if they are simple identifers (no dot notation)
        List<String> newTokens = new LinkedList<String>();
        boolean isPastOrderBy = false;
        for (int ix = 0 ; ix < tokens.size() ; ix++) {
            String oldToken = tokens.get(ix);
            if (isFunction(oldToken)) {
                int start = oldToken.indexOf(LEFT_PAREN) + 1;
                int end = oldToken.indexOf(RIGHT_PAREN);
                String subToken = oldToken.substring(start, end);
                String function = oldToken.substring(0, start);
                if (subToken.toLowerCase().startsWith(PROPERTY_PREFIX)) {
                    String propertyName = subToken.substring(subToken.indexOf(DOT) + 1);
                    String alias = calculateAlias(propertyName, info.getPropertiesAlias(), aliases);
                    PropertyType propertyType = metadataService.getPropertyTypeByName(propertyName);
                    if (propertyType == null) {
                        throw new RuntimeException("Invalid Property name " + propertyName + " provided to query translator.");
                    }
                    String valuePropertyType = lookupPropertyValueType(propertyType, propertyName);
                    if (!isPastOrderBy) {
                        newTokens.add(alias + TYPEID_PROPERTY);
                        newTokens.add(HQL_EQUALS);
                        newTokens.add(Integer.toString(propertyType.getPropertyTypeId()));
                        newTokens.add(HQL_AND);
                    }
                    newTokens.add(function);
                    newTokens.add(alias + valuePropertyType);
                    newTokens.add(String.valueOf(RIGHT_PAREN));
                    hasProperties = true;
                }
                else {
                    if (isIdentifier(subToken)) {
                        subToken = basicPropertyTranslate(subToken, info);
                    }
                    newTokens.add(function);
                    newTokens.add(subToken);
                    newTokens.add(String.valueOf(RIGHT_PAREN));
                }
            }
            else if (oldToken.toLowerCase().startsWith(PROPERTY_PREFIX)) {
                String propertyName = oldToken.substring(oldToken.indexOf(DOT) + 1);
                String alias = calculateAlias(propertyName, info.getPropertiesAlias(), aliases);
                PropertyType propertyType = metadataService.getPropertyTypeByName(propertyName);
                if (propertyType == null) {
                    throw new RuntimeException("Invalid Property name " + propertyName + " provided to query translator.");
                }
                String valuePropertyType = lookupPropertyValueType(propertyType, propertyName);
                if (!isPastOrderBy) {
                    newTokens.add(alias + TYPEID_PROPERTY);
                    newTokens.add(HQL_EQUALS);
                    newTokens.add(Integer.toString(propertyType.getPropertyTypeId()));
                    newTokens.add(HQL_AND);
                    newTokens.add(alias + valuePropertyType);
                }
                else {
                    newTokens.add(alias + valuePropertyType);
                }
                hasProperties = true;
            }
            else if (oldToken.toLowerCase().equals(CATEGORY_PREFIX) || oldToken.toLowerCase().equals(SERVICEGROUP_PREFIX)) {
                // replace category prefix with actual join field - requires queryTranslatorInfo configuration
                oldToken = basicPropertyTranslate(oldToken, info);
                // read ahead to next token replace with in clause
                QuerySubstitution.QuerySubstitutionType substitutionType = QuerySubstitution.QuerySubstitutionType.CATEGORY_EQUAL;
                if (ix+1 < tokens.size())  {
                    String nextToken = tokens.get(ix + 1);
                    substitutionType =  (nextToken.equals("=")) ? QuerySubstitution.QuerySubstitutionType.CATEGORY_EQUAL :
                            QuerySubstitution.QuerySubstitutionType.CATEGORY_IN;
                    tokens.set(ix+1, "in");
                }
                if (ix+2 < tokens.size()) {
                    int pos = ix + 2;
                    String value = tokens.get(ix+2);
                    if (value.equals("(")) {
                        if (ix+3 < tokens.size()) {
                            value = tokens.get(ix+3);
                            pos = ix + 3;
                        }
                        else {
                            throw new RuntimeException("Invalid category query. Failed to parse in clause " + query);
                        }
                    }
                    QuerySubstitution substitution = queryTranslation.add(substitutionType, value);
                    tokens.set(pos, substitution.getPlaceHolder());
                }
                newTokens.add(oldToken);
            }
            else {
                if (oldToken.equalsIgnoreCase("order") && tokens.get(ix+1).equalsIgnoreCase("by")) {
                    isPastOrderBy = true;
                }
                else {
                    // determine if basic Property translation, only translate simple properties
                    if (isIdentifier(oldToken)) {
                        oldToken = basicPropertyTranslate(oldToken, info);
                    }
                }
                newTokens.add(oldToken);
            }
        }

        // 3. Third pass - build the where clause
        StringBuffer where = new StringBuffer();
        for (int ix = 0; ix < newTokens.size(); ix++) {
            String tok = newTokens.get(ix);
            where.append(tok);
            if (tok.charAt(0) != LEFT_PAREN) {
                if ((ix + 1) < newTokens.size()) {
                    String next = newTokens.get(ix + 1);
                    if (next.charAt(0) != RIGHT_PAREN && !tok.endsWith(String.valueOf(LEFT_PAREN)))
                        where.append(WHITESPACE);
                }
            }
        }

        // 4. Build the full query string
        String selectQuery = buildSelectQuery(info, hasProperties, aliases, where, newTokens.size());
        String countQuery = buildCountQuery(info,  hasProperties, aliases, where, newTokens.size());
        if (log.isDebugEnabled()) {
            log.debug(String.format("...Query Translation completed: %s", selectQuery));
        }
        queryTranslation.setHql(selectQuery);
        queryTranslation.setCountHql(countQuery);
        return queryTranslation;
    }

    private String buildSelectQuery(QueryTranslatorInfo info, boolean hasProperties,
                                  Map<String, String> aliases, StringBuffer where, int size) {
        StringBuffer fullQuery = new StringBuffer();
        fullQuery.append(info.getSelectClause());
        if (hasProperties) { // build join for properties
            for (String alias : aliases.values()) {
                fullQuery.append(",");
                fullQuery.append(alias);
            }
        }
        addOrderByProjections(fullQuery, where.toString(), info);
        fullQuery.append(info.getFromClause());
        if (hasProperties) { // build join for properties
            for (String alias : aliases.values()) {
                fullQuery.append(" inner join ");
                fullQuery.append(info.getPropertiesPath());
                fullQuery.append(" as ");
                fullQuery.append(alias);
                fullQuery.append(" ");
            }
        }
        if (size > 0) {
            String orderByCheck = (where.toString());
            if (!orderByCheck.toLowerCase().startsWith(SQL_ORDER_BY)) {
                fullQuery.append(" where ");
            }
            fullQuery.append(where);
        }
        String finalQuery = fullQuery.toString();
        return finalQuery;
    }

    private void addOrderByProjections(StringBuffer fullQuery, String where, QueryTranslatorInfo info) {
        // projections on order by required for distinct queries on postgresql
        String propertiesAlias = (info.getPropertiesAlias() == null) ? "" : info.getPropertiesAlias();
        String temp = where.toString().toLowerCase();
        int index = temp.indexOf(SQL_ORDER_BY);
        if (index > -1) {
            StringTokenizer tokenizer = new StringTokenizer(where.substring(index + SQL_ORDER_BY.length()), ",");
            while (tokenizer.hasMoreTokens()) {
                String alias = tokenizer.nextToken().trim();
                if (!isPropertyAlias(alias, propertiesAlias)) {
                    String translated = info.translateName(alias);
                    alias = (translated == null) ? alias : translated;
                    if (isJoined(alias)) {
                        fullQuery.append(",");
                        fullQuery.append(alias);
                    }
                }
            }
        }
    }

    private boolean isPropertyAlias(String alias, String propertiesAlias) {
        if (alias.startsWith(propertiesAlias) && alias.length() > 3) {
            if (alias.charAt(2) == '.')
                return true;
        }
        return false;
    }

    private boolean isJoined(String alias) {
        int delimCount = 0;
        String field = new String(alias);
        if (field.length() > 2) {
            if (field.charAt(1) == '.')
                field = alias.substring(2);
        }
        for (int ix = 0; ix < field.length(); ix++) {
            if (field.charAt(ix) == '.')
                delimCount++;
        }
        return delimCount >= 1;
    }

    private String buildCountQuery(QueryTranslatorInfo info, boolean hasProperties,
                                  Map<String, String> aliases, StringBuffer where, int size) {
        StringBuffer fullQuery = new StringBuffer();
        fullQuery.append(info.getCountQuery());
        if (hasProperties) { // build join for properties
            for (String alias : aliases.values()) {
                fullQuery.append("inner join ");
                fullQuery.append(info.getPropertiesPath());
                fullQuery.append(" as ");
                fullQuery.append(alias);
                fullQuery.append(" ");
            }
        }
        if (size > 0) {
            String orderByCheck = (where.toString());
            if (!orderByCheck.toLowerCase().startsWith(SQL_ORDER_BY)) {
                fullQuery.append(" where ");
            }
            fullQuery.append(where);
        }
        String finalQuery = fullQuery.toString();
        String countQueryString = finalQuery.toString().toLowerCase();
        int index = countQueryString.indexOf(SQL_ORDER_BY);
        if (index > -1) {
            finalQuery = finalQuery.substring(0, index);
        }
        return finalQuery;
    }

    public String substitute(QueryTranslation translation, QuerySubstitution sub, String replacement) {
        return translation.getHql().replaceAll("\\" + sub.getPlaceHolder(), replacement);
    }

    private String basicPropertyTranslate(String shortCut, QueryTranslatorInfo info) {
        //if (shortCut.indexOf(DOT) == -1)  { (not necessary, regex doesn't allow anything but simple identifiers
        String fullPropertyName = info.translateName(shortCut);
        return (fullPropertyName == null) ? shortCut : fullPropertyName;
    }

    private boolean isIdentifier(String token) {
        Matcher matcher = literalPattern.matcher(token);
        return matcher.matches();
    }

    private boolean isIdentifierChar(String ch) {
        Matcher matcher = literalPattern.matcher(ch);
        return matcher.matches();
    }

    private boolean isFunction(String token) {
        Matcher matcher = functionPattern.matcher(token);
        return matcher.matches();
    }

    private boolean isOperator(char ch) {
        return (ch == '=' || ch == '!' || ch == '>' || ch == '<');
    }

    private boolean isParenthesis(char ch) {
        return (ch == LEFT_PAREN || ch == RIGHT_PAREN) ;
    }

    private String calculateAlias(String propertyName, String prefix, Map<String, String> aliases) {
        String alias = aliases.get(propertyName);
        if (alias == null) {
            alias = prefix + aliases.size();
            aliases.put(propertyName, alias);
        }
        return alias;
    }

    public String lookupPropertyValueType(PropertyType propertyType, String propertyName) {
        if (propertyType.isString())
            return VALUE_STRING_PROPERTY;
        else if (propertyType.isDouble())
            return VALUE_DOUBLE_PROPERTY;
        else if (propertyType.isInteger())
            return VALUE_INTEGER_PROPERTY;
        else if (propertyType.isLong())
            return VALUE_LONG_PROPERTY;
        else if (propertyType.isBoolean())
            return VALUE_BOOLEAN_PROPERTY;
        else if (propertyType.isDate())
            return VALUE_DATE_PROPERTY;
        throw new RuntimeException("Could not map Property name '" + propertyName + "' to a data type.");
    }
}
