/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2006, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.jboss.portal.core.impl.model.instance.persistent;

import org.jboss.portal.common.i18n.LocalizedString;
import org.jboss.portal.common.i18n.LocalizedString.Value;
import org.jboss.portal.core.impl.model.instance.AbstractInstanceDefinition;
import org.jboss.portal.core.impl.model.instance.InstanceContainerContext;
import org.jboss.portal.core.model.instance.metadata.InstanceMetaData;
import org.jboss.portal.jems.hibernate.ContextObject;
import org.jboss.portal.security.RoleSecurityBinding;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 10228 $
 */
class PersistentInstanceDefinition extends AbstractInstanceDefinition implements ContextObject
{

   // Persistent fields

   protected Long key;
   protected String instanceId;
   protected boolean mutable;
   protected Map relatedSecurityBindings;
   protected Map relatedCustomizations;
   protected Map<Locale, String> displayNames;

   // Runtime fields

   /** . */
   protected InstanceContainerContext containerContext;

   public PersistentInstanceDefinition()
   {
      this.mutable = false;
      this.portletRef = null;
      this.instanceId = null;
      this.relatedSecurityBindings = null;
      this.relatedCustomizations = null;
      this.state = null;
   }

   public PersistentInstanceDefinition(InstanceContainerContext containerContext, String id, String portletRef)
   {
      this.containerContext = containerContext;
      this.mutable = false;
      this.portletRef = portletRef;
      this.instanceId = id;
      this.relatedSecurityBindings = new HashMap();
      this.relatedCustomizations = new HashMap();
      this.displayNames = new HashMap();
      this.state = null;
   }

   public PersistentInstanceDefinition(InstanceContainerContext containerContext, InstanceMetaData instanceMD)
   {
      this.containerContext = containerContext;
      this.mutable = false;
      this.portletRef = instanceMD.getPortletRef();
      this.instanceId = instanceMD.getId();
      this.displayNames = getDisplayNamesMap(instanceMD.getDisplayName());
      this.relatedSecurityBindings = new HashMap();
      this.relatedCustomizations = new HashMap();
      this.state = null;
   }

   private Map<Locale, String> getDisplayNamesMap(LocalizedString lString)
   {
      Map<Locale, String> map = new HashMap<Locale, String>();
      if (lString != null)
      {
         Map<Locale, Value> values = lString.getValues();
         for (Locale locale: values.keySet())
         {
            map.put(locale, values.get(locale).getString());         
         }
      }
      return map;
   }

   public LocalizedString getDisplayName()
   {
      return new LocalizedString(displayNames, Locale.ENGLISH);
   }

   public void setDisplayName(LocalizedString localizedString)
   {
      if (localizedString == null)
      {
         throw new IllegalArgumentException("No null display name accepted");
      }

      displayNames = new HashMap();
      
      Map map = localizedString.getValues();
      Iterator it = map.values().iterator();
      while (it.hasNext())
      {
         LocalizedString.Value value = (LocalizedString.Value)it.next();
         displayNames.put(value.getLocale(), value.getString());
      }
   }

   // Hibernate ********************************************************************************************************

   public Long getKey()
   {
      return key;
   }

   public void setKey(Long key)
   {
      this.key = key;
   }

   public String getInstanceId()
   {
      return instanceId;
   }

   public void setInstanceId(String instanceId)
   {
      this.instanceId = instanceId;
   }

   public Map getRelatedSecurityBindings()
   {
      return relatedSecurityBindings;
   }

   public void setRelatedSecurityBindings(Map relatedSecurityBindings)
   {
      this.relatedSecurityBindings = relatedSecurityBindings;
   }

   public Map getRelatedCustomizations()
   {
      return relatedCustomizations;
   }

   public void setDisplayNames(Map displayNames)
   {
      this.displayNames = displayNames;
   }

   public Map getDisplayNames()
   {
      return displayNames;
   }

   public void setRelatedCustomizations(Map relatedCustomizations)
   {
      this.relatedCustomizations = relatedCustomizations;
   }

   public boolean isMutable()
   {
      return mutable;
   }

   public void setMutable(boolean mutable)
   {
      this.mutable = mutable;
   }

   // Instance implementation ******************************************************************************************

   public String getId()
   {
      return instanceId;
   }

   // AbstractInstanceDefinition implementation ************************************************************************

   public Collection getCustomizations()
   {
      return relatedCustomizations.values();
   }

   public Set getSecurityBindings()
   {
      Set constraints = new HashSet();
      for (Iterator i = relatedSecurityBindings.values().iterator(); i.hasNext();)
      {
         PersistentRoleSecurityBinding isc = (PersistentRoleSecurityBinding)i.next();
         RoleSecurityBinding sc = new RoleSecurityBinding(isc.getActions(), isc.getRole());
         constraints.add(sc);
      }
      return constraints;
   }

   // ContextObject implementation *************************************************************************************

   public void setContext(Object context)
   {
      this.containerContext = (InstanceContainerContext)context;
   }

   protected InstanceContainerContext getContainerContext()
   {
      return containerContext;
   }
}
