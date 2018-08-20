/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.

 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.

 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.groundwork.foundation.dao;

import java.io.Serializable;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

/**
 * The FilterCriteria classes wraps the hibernate Restrictions class and hides its implementation from the client.
 * We are unable to derive from the class because it does not allow instantiation (private constructor)
 *
 */
public class FilterCriteria extends Criteria implements Serializable
{
	// Criterion
	private Criterion _criterion = null;	
	
	// Property / Value Pairs
	private Map<String, Object> _propValuePairs = new Hashtable<String, Object>(5);
	
	/*************************************************************************/
	/* Constructors */
	/*************************************************************************/
	private FilterCriteria (Criterion criterion)
	{		
		_criterion = criterion;		
	}	
	
	private FilterCriteria (String propertyName, Criterion criterion)
	{		
		_criterion = criterion;
		
		_criteriaAliases.addAll(CriteriaAlias.createAliases(propertyName));
	}	
	
	/**
	 * This constructor is used to save the property / value pair b/c there is no way
	 * to extract this information from the Criterion class.  It is only used with the
	 * equals (eq) operator and is a workaround.
	 * 
	 * We may need to rework the FilterCriteria class so it is not coupled with
	 * the Criterion class.  This will enable us to more effectively use FilterCriteria
	 * outside of Hibernate Criteria Queries (e.g. StatisticEntity Queries)
	 * 
	 * @param propertyName
	 * @param criterion
	 * @param value
	 */
	private FilterCriteria (String propertyName, Criterion criterion, Object value)
	{		
		if (value != null)
			_propValuePairs.put(propertyName, value);
		
		_criterion = criterion;
		
		_criteriaAliases.addAll(CriteriaAlias.createAliases(propertyName));
	}	
	
	private FilterCriteria (String propertyName, String propertyName2, Criterion criterion)
	{		
		_criterion = criterion;
		
		_criteriaAliases.addAll(CriteriaAlias.createAliases(propertyName));
		_criteriaAliases.addAll(CriteriaAlias.createAliases(propertyName2));
	}	

	private FilterCriteria (Collection<CriteriaAlias> aliases, Criterion criterion)
	{		
		_criterion = criterion;
		
		_criteriaAliases.addAll(aliases);
	}		
	
	/*************************************************************************/
	/* Public Methods */
	/*************************************************************************/
	
	public Map<String, Object> getPropertyValuePairs ()
	{
		return _propValuePairs;
	}
	
	/*************************************************************************/
	/* Public Static Methods */
	/*************************************************************************/
	
	/**
	 * Return the conjuction of two expressions
	 *
	 * @param lhs
	 * @param rhs
	 * @return Criterion
	 */
	public void and(FilterCriteria criteria)
	{
		if (criteria == null)
			throw new IllegalArgumentException("Invalid null FilterCriteria parameter.");
		
		if (_criterion == null) {
			_criterion = criteria.getCriterion();
		}
		else 
		{
			_criterion = Restrictions.and(_criterion, criteria.getCriterion());
		}		
		
		// Add criteria properties to "this" set of properties
		_criteriaAliases.addAll(criteria.getCriteriaAliases());
		
		// Add Property value pairs
		_propValuePairs.putAll(criteria.getPropertyValuePairs());
	}
	
	/**
	 * Return the disjuction of two expressions
	 *
	 * @param lhs
	 * @param rhs
	 * @return Criterion
	 */
	public void or(FilterCriteria criteria)
	{
		if (criteria == null)
			throw new IllegalArgumentException("Invalid null FilterCriteria parameter.");
		
		if (_criterion == null) {
			_criterion = criteria.getCriterion();
		}
		else 
		{
			_criterion = Restrictions.or(_criterion, criteria.getCriterion());
		}		
		
		// Add criteria properties to "this" set of properties
		_criteriaAliases.addAll(criteria.getCriteriaAliases());		
		
		// Add Property value pairs
		_propValuePairs.putAll(criteria.getPropertyValuePairs());
	}
	
	/*************************************************************************/
	/* Public Static Methods */
	/*************************************************************************/
	
	/**
	 * Apply an "equal" constraint to the identifier property
	 * @param value
	 * @return Criterion
	 */
	public static FilterCriteria idEq(Object value)
	{
		return new FilterCriteria(Restrictions.idEq(value));				
	}

	/**
	 * Apply a case insensitive "equal" constraint to the named property
	 * @param propertyName
	 * @param value
	 * @return Criterion
	 */
	public static FilterCriteria ieq(String propertyName, Object value)
	{					
		// For Now, we only add equal filters to property value collection
		// This mechanism is a workaround because we not able to extract 
		// filter information from the Hibernate Criterion class
		return new FilterCriteria(propertyName, 
				Restrictions.eq(getCriterionAlias(propertyName), value).ignoreCase(),
				value);
	}

    /**
     * Apply an "equal" constraint to the named property
     * @param propertyName
     * @param value
     * @return Criterion
     */
    public static FilterCriteria eq(String propertyName, Object value)
    {
        // For Now, we only add equal filters to property value collection
        // This mechanism is a workaround because we not able to extract
        // filter information from the Hibernate Criterion class
        return new FilterCriteria(propertyName,
                Restrictions.eq(getCriterionAlias(propertyName), value),
                value);
    }

    /**
	 * Apply a "not equal" constraint to the named property
	 * @param propertyName
	 * @param value
	 * @return Criterion
	 */
	public static FilterCriteria ne(String propertyName, Object value)
	{
		return new FilterCriteria(propertyName, 
								  Restrictions.ne(getCriterionAlias(propertyName), value));
	}
	
	/**
	 * Apply a "like" constraint to the named property
	 * @param propertyName
	 * @param value
	 * @return Criterion
	 */
	public static FilterCriteria like(String propertyName, Object value) 
	{
		return new FilterCriteria(propertyName, 
								  Restrictions.like(getCriterionAlias(propertyName), value));
	}
	
	/**
	 * Apply a "like" constraint to the named property
	 * @param propertyName
	 * @param value
	 * @return Criterion
	 */
	public static FilterCriteria like(String propertyName, String value, MatchType matchType)
	{
		return new FilterCriteria(propertyName,
								  Restrictions.like(getCriterionAlias(propertyName), value, matchType.getMatchMode()));
	}
	
	/**
	 * A case-insensitive "like", similar to Postgres <tt>ilike</tt>
	 * operator
	 *
	 * @param propertyName
	 * @param value
	 * @return Criterion
	 */
	public static FilterCriteria ilike(String propertyName, String value,  MatchType matchType)
	{
		return new FilterCriteria(propertyName, Restrictions.ilike(getCriterionAlias(propertyName), value, matchType.getMatchMode()));
	}
	
	/**
	 * A case-insensitive "like", similar to Postgres <tt>ilike</tt>
	 * operator
	 *
	 * @param propertyName
	 * @param value
	 * @return Criterion
	 */
	public static FilterCriteria ilike(String propertyName, Object value) 
	{
		return new FilterCriteria(propertyName, 
								  Restrictions.ilike(getCriterionAlias(propertyName), 
								  value));
	}
	
	/**
	 * Apply a "greater than" constraint to the named property
	 * @param propertyName
	 * @param value
	 * @return Criterion
	 */
	public static FilterCriteria gt(String propertyName, Object value)
	{
		return new FilterCriteria(propertyName,
								  Restrictions.gt(getCriterionAlias(propertyName), 
								  value));
	}
	
	/**
	 * Apply a "less than" constraint to the named property
	 * @param propertyName
	 * @param value
	 * @return Criterion
	 */
	public static FilterCriteria lt(String propertyName, Object value)
	{
		return new FilterCriteria(propertyName,
								  Restrictions.lt(getCriterionAlias(propertyName),
								  value));
	}
	
	/**
	 * Apply a "less than or equal" constraint to the named property
	 * @param propertyName
	 * @param value
	 * @return Criterion
	 */
	public static FilterCriteria le(String propertyName, Object value) 
	{
		return new FilterCriteria(propertyName,
								  Restrictions.le(getCriterionAlias(propertyName),
								  value));
	}
	
	/**
	 * Apply a "greater than or equal" constraint to the named property
	 * @param propertyName
	 * @param value
	 * @return Criterion
	 */
	public static FilterCriteria ge(String propertyName, Object value) 
	{
		return new FilterCriteria(propertyName,
								  Restrictions.ge(getCriterionAlias(propertyName),
								  value));
	}
	
	/**
	 * Apply a "between" constraint to the named property
	 * @param propertyName
	 * @param lo value
	 * @param hi value
	 * @return Criterion
	 */
	public static FilterCriteria between(String propertyName, Object lo, Object hi)
	{
		return new FilterCriteria(propertyName,
								  Restrictions.between(getCriterionAlias(propertyName),
								  lo,
								  hi));
	}
	
	/**
	 * Apply an "in" constraint to the named property
	 * @param propertyName
	 * @param values
	 * @return Criterion
	 */
	public static FilterCriteria in(String propertyName, Object[] values)
	{
		return new FilterCriteria(propertyName,
								  Restrictions.in(getCriterionAlias(propertyName),
								  values));
	}
	
	/**
	 * Apply an "in" constraint to the named property
	 * @param propertyName
	 * @param values
	 * @return Criterion
	 */
	public static FilterCriteria in(String propertyName, Collection values)
	{
		return new FilterCriteria(propertyName,
								  Restrictions.in(getCriterionAlias(propertyName),
								  values));
	}
	
	/**
	 * Apply an "is null" constraint to the named property
	 * @return Criterion
	 */
	public static FilterCriteria isNull(String propertyName)
	{
		return new FilterCriteria(propertyName,
								  Restrictions.isNull(getCriterionAlias(propertyName)));
	}
	
	/**
	 * Apply an "equal" constraint to two properties
	 */
	public static FilterCriteria eqProperty(String propertyName, String otherPropertyName)
	{
		return new FilterCriteria(propertyName, 
								  otherPropertyName,
								  Restrictions.eqProperty(getCriterionAlias(propertyName), 
								  getCriterionAlias(propertyName)));
	}
	
	/**
	 * Apply a "not equal" constraint to two properties
	 */
	public static FilterCriteria neProperty(String propertyName, String otherPropertyName)
	{		
		return new FilterCriteria(propertyName,
								  otherPropertyName, 
								  Restrictions.neProperty(getCriterionAlias(propertyName), 
								  getCriterionAlias(propertyName)));
	}
	
	/**
	 * Apply a "less than" constraint to two properties
	 */
	public static FilterCriteria ltProperty(String propertyName, String otherPropertyName)
    {
		return new FilterCriteria(propertyName, 
								  otherPropertyName, 
								  Restrictions.ltProperty(getCriterionAlias(propertyName),
								  getCriterionAlias(propertyName)));
	}
	
	/**
	 * Apply a "less than or equal" constraint to two properties
	 */
	public static FilterCriteria leProperty(String propertyName, String otherPropertyName)
	{
		return new FilterCriteria(propertyName,
								  otherPropertyName,
								  Restrictions.leProperty(getCriterionAlias(propertyName), 
								  getCriterionAlias(propertyName)));
	}
	
	/**
	 * Apply a "greater than" constraint to two properties
	 */
	public static FilterCriteria gtProperty(String propertyName, String otherPropertyName)
	{
		return new FilterCriteria(propertyName, 
								  otherPropertyName,
								  Restrictions.gtProperty(getCriterionAlias(propertyName),
								  getCriterionAlias(propertyName)));
	}
	
	/**
	 * Apply a "greater than or equal" constraint to two properties
	 */
	public static FilterCriteria geProperty(String propertyName, String otherPropertyName)
	{
		return new FilterCriteria(propertyName,
								  otherPropertyName,
								  Restrictions.geProperty(getCriterionAlias(propertyName),
								  getCriterionAlias(propertyName)));
	}
	
	/**
	 * Apply an "is not null" constraint to the named property
	 * @return Criterion
	 */
	public static FilterCriteria isNotNull(String propertyName)
	{
		return new FilterCriteria(propertyName,
								  Restrictions.isNotNull(getCriterionAlias(propertyName)));
	}
	
	/**
	 * Negates current filter criteria
	 *
	 * @param expression
	 * @return Criterion
	 */
	public static FilterCriteria not(FilterCriteria criteria)
	{
		return new FilterCriteria(criteria.getCriteriaAliases(),
								  Restrictions.not(criteria.getCriterion()));
	}

	/**
	 * Apply an "equals" constraint to each property in the
	 * key set of a <tt>Map</tt>
	 *
	 * @param propertyNameValues a map from property names to values
	 * @return Criterion
	 */
	public static FilterCriteria allEq(Map<String, Object> propertyNameValues)
	{
		return new FilterCriteria(CriteriaAlias.createAliases(propertyNameValues.keySet()),
								Restrictions.allEq(getCriterionAliases(propertyNameValues)));
	}
	
	/**
	 * Constrain a collection valued property to be empty
	 */
	public static FilterCriteria isEmpty(String propertyName)
	{
		return new FilterCriteria(propertyName,
							Restrictions.isEmpty(getCriterionAlias(propertyName)));
	}

	/**
	 * Constrain a collection valued property to be non-empty
	 */
	public static FilterCriteria isNotEmpty(String propertyName)
	{
		return new FilterCriteria(propertyName,
							Restrictions.isNotEmpty(getCriterionAlias(propertyName)));
	}
	
	/**
	 * Constrain a collection valued property by size
	 */
	public static FilterCriteria sizeEq(String propertyName, int size)
	{
		return new FilterCriteria(propertyName, 
						Restrictions.sizeEq(getCriterionAlias(propertyName),
						size));
	}
	
	/**
	 * Constrain a collection valued property by size
	 */
	public static FilterCriteria sizeNe(String propertyName, int size) 
	{
		return new FilterCriteria(propertyName, 
						Restrictions.sizeNe(getCriterionAlias(propertyName),
						size));
	}
	
	/**
	 * Constrain a collection valued property by size
	 */
	public static FilterCriteria sizeGt(String propertyName, int size)
	{
		return new FilterCriteria(propertyName,
								Restrictions.sizeGt(getCriterionAlias(propertyName),
								size));
	}
	
	/**
	 * Constrain a collection valued property by size
	 */
	public static FilterCriteria sizeLt(String propertyName, int size)
	{
		return new FilterCriteria(propertyName,
								  Restrictions.sizeLt(getCriterionAlias(propertyName),
								  size));
	}
	
	/**
	 * Constrain a collection valued property by size
	 */
	public static FilterCriteria sizeGe(String propertyName, int size)
	{
		return new FilterCriteria(propertyName,
								  Restrictions.sizeGe(getCriterionAlias(propertyName),
								  size));
	}
	
	/**
	 * Constrain a collection valued property by size
	 */
	public static FilterCriteria sizeLe(String propertyName, int size)
	{
		return new FilterCriteria(propertyName,
								  Restrictions.sizeLe(getCriterionAlias(propertyName),
								  size));
	}
	
	/*************************************************************************/
	/* Protected Methods */
	/*************************************************************************/	
	protected Criterion getCriterion ()
	{
		return _criterion;
	}	
	
	/*************************************************************************/
	/* Private Methods */
	/*************************************************************************/		
}
