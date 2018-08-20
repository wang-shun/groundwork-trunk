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
package org.jboss.portal.core.aspects.controller.node;

import org.apache.log4j.Logger;
import org.jboss.portal.Mode;
import org.jboss.portal.WindowState;
import org.jboss.portal.api.PortalRuntimeContext;
import org.jboss.portal.api.node.PortalNode;
import org.jboss.portal.api.node.event.PageRenderEvent;
import org.jboss.portal.api.node.event.PortalNodeEvent;
import org.jboss.portal.api.node.event.WindowActionEvent;
import org.jboss.portal.api.node.event.WindowEvent;
import org.jboss.portal.api.node.event.WindowNavigationEvent;
import org.jboss.portal.api.node.event.WindowRenderEvent;
import org.jboss.portal.common.invocation.InvocationException;
import org.jboss.portal.common.util.LazyMap;
import org.jboss.portal.common.util.MapAccessor;
import org.jboss.portal.common.util.ParameterMap;
import org.jboss.portal.core.controller.CommandRedirectionException;
import org.jboss.portal.core.controller.ControllerCommand;
import org.jboss.portal.core.controller.ControllerInterceptor;
import org.jboss.portal.core.controller.ControllerResponse;
import org.jboss.portal.core.event.PortalEventListenerRegistry;
import org.jboss.portal.core.impl.api.node.PortalNodeEventContextImpl;
import org.jboss.portal.core.impl.api.node.PortalNodeImpl;
import org.jboss.portal.core.model.portal.PortalObjectId;
import org.jboss.portal.core.model.portal.command.WindowCommand;
import org.jboss.portal.core.model.portal.command.action.InvokePortletWindowActionCommand;
import org.jboss.portal.core.model.portal.command.action.InvokePortletWindowRenderCommand;
import org.jboss.portal.core.model.portal.command.render.RenderPageCommand;
import org.jboss.portal.core.model.portal.command.render.RenderWindowCommand;
import org.jboss.portal.core.model.portal.navstate.WindowNavigationalState;
import org.jboss.portal.core.navstate.NavigationalStateKey;
import org.jboss.portal.portlet.ParametersStateString;
import org.jboss.portal.portlet.StateString;

import java.util.Map;

/** @author <a href="mailto:julien@jboss.org">Julien Viet</a> */
public class EventBroadcasterInterceptor extends ControllerInterceptor
{

   /** . */
   private static Logger log = Logger.getLogger(EventBroadcasterInterceptor.class);

   /** . */
   private PortalEventListenerRegistry listenerRegistry;

   public PortalEventListenerRegistry getListenerRegistry()
   {
      return listenerRegistry;
   }

   public void setListenerRegistry(PortalEventListenerRegistry listenerRegistry)
   {
      this.listenerRegistry = listenerRegistry;
   }

   public ControllerResponse invoke(ControllerCommand cmd) throws Exception, InvocationException
   {
      // Get the current node from the thread local
      PortalNodeImpl node = Navigation.getCurrentNode();

      // The next event that will optionally replace this one
      PortalNodeEvent nextEvent = null;

      // Create an event from the current node
      PortalNodeEvent event = createEvent(cmd, node);

      //
      if (event != null)
      {
         try
         {
            // Get runtime context from the thread local
            PortalRuntimeContext runtimeContext = Navigation.getPortalRuntimeContext();

            //
            PortalNodeEventContextImpl nodeEventContext = new PortalNodeEventContextImpl(listenerRegistry, node, event, runtimeContext);

            // Fire the event
            nextEvent = nodeEventContext.dispatch();
         }
         catch (Exception e)
         {
            log.error("Error when dispatching pre event " + event, e);
         }
      }

      //
      if (nextEvent != null)
      {
         ControllerCommand redirection = createCommand(nextEvent);

         //
         if (redirection != null)
         {
            throw new CommandRedirectionException(redirection);
         }
      }

      //
      return (ControllerResponse)cmd.invokeNext();
   }

   /**
    * @param event
    * @return
    */
   private ControllerCommand createCommand(PortalNodeEvent event)
   {
      if (event instanceof WindowEvent)
      {
         WindowEvent we = (WindowEvent)event;

         //
         PortalNodeImpl nextNode = (PortalNodeImpl)we.getNode();
         PortalObjectId nodeRef = nextNode.getObjectId();
         Mode mode = we.getMode();
         WindowState windowState = we.getWindowState();

         //
         if (event instanceof WindowActionEvent)
         {
            WindowActionEvent wae = (WindowActionEvent)event;
            Map<String, String[]> params = wae.getParameters();

            //
            if (params != null)
            {
               return new InvokePortletWindowActionCommand(
                  nodeRef,
                  mode,
                  windowState,
                  null,
                  ParametersStateString.create(params),
                  null);
            }
         }
         else if (event instanceof WindowNavigationEvent)
         {
            WindowNavigationEvent wne = (WindowNavigationEvent)event;

            //
            Map<String, String[]> params = wne.getParameters();
            StateString state = params != null ? ParametersStateString.create(params) : null;

            //
            return new InvokePortletWindowRenderCommand(nodeRef, mode, windowState, state);
         }
      }

      //
      return null;
   }

   /**
    * Returns portal node event corresponding to the command or null.
    *
    * @param command
    * @param node
    * @return a portal node event
    */
   private PortalNodeEvent createEvent(ControllerCommand command, PortalNode node)
   {
      if (command instanceof WindowCommand)
      {
         WindowCommand wc = (WindowCommand)command;

         //
         if (command instanceof InvokePortletWindowActionCommand)
         {
            InvokePortletWindowActionCommand iwac = (InvokePortletWindowActionCommand)wc;

            // Get form parameters
            ParameterMap formParameters = iwac.getFormParameters();

            // Get interaction parameters
            Map<String, String[]> interactionParameters = null;
            if (iwac.getInteractionState() instanceof ParametersStateString)
            {
               interactionParameters = ((ParametersStateString)iwac.getInteractionState()).getParameters();
            }

            // Build a map that represents the parameters of the action
            Map<String, String[]> actionParameters;
            if (interactionParameters != null)
            {
               if (formParameters != null)
               {
                  actionParameters = new ActionParameterMap(interactionParameters, formParameters);
               }
               else
               {
                  actionParameters = interactionParameters;
               }
            }
            else
            {
               if (formParameters != null)
               {
                  actionParameters = formParameters;
               }
               else
               {
                  actionParameters = new ParameterMap();
               }
            }

            // Populate and return the window action event
            WindowActionEvent action = new WindowActionEvent(node);
            action.setMode(iwac.getMode());
            action.setWindowState(iwac.getWindowState());
            action.setParameters(actionParameters);
            return action;
         }
         else if (command instanceof InvokePortletWindowRenderCommand)
         {
            InvokePortletWindowRenderCommand iwrc = (InvokePortletWindowRenderCommand)wc;

            //
            WindowNavigationEvent event = new WindowNavigationEvent(node);
            event.setMode(iwrc.getMode());
            event.setWindowState(iwrc.getWindowState());

            //
            StateString navigationalState = iwrc.getNavigationalState();
            if (navigationalState instanceof ParametersStateString)
            {
               Map<String, String[]> params = ((ParametersStateString)navigationalState).getParameters();
               event.setParameters(params);
            }

            //
            return event;
         }
         else if (command instanceof RenderWindowCommand)
         {
            WindowRenderEvent event = new WindowRenderEvent(node);

            //
            RenderWindowCommand rwc = (RenderWindowCommand)command;
            NavigationalStateKey key = new NavigationalStateKey(WindowNavigationalState.class, rwc.getTargetId());
            WindowNavigationalState navstate = (WindowNavigationalState)command.getControllerContext().getAttribute(ControllerCommand.NAVIGATIONAL_STATE_SCOPE, key);
            if (navstate != null)
            {
               event.setMode(navstate.getMode());
               event.setWindowState(navstate.getWindowState());

               StateString parametersState = navstate.getContentState();
               if (parametersState instanceof ParametersStateString)
               {
                  Map<String, String[]> params = ((ParametersStateString)parametersState).getParameters();
                  event.setParameters(params);
               }
            }

            //
            return event;
         }
      }
      else if (command instanceof RenderPageCommand)
      {
         return new PageRenderEvent(node);
      }

      //
      return null;
   }

   /**
    *
    */
   private static class ActionParameterMap extends LazyMap<String, String[]>
   {
      public ActionParameterMap(final Map<String, String[]> interactionParams, final Map<String, String[]> formParams)
      {
         super(new MapAccessor<String, String[]>()
               {
                  /** . */
                  private ParameterMap params;

                  public Map<String, String[]> getMap(boolean writable)
                  {
                     if (params == null)
                     {
                        params = new ParameterMap(interactionParams);
                        params.append(formParams);
                     }
                     return params;
                  }
               });
      }
   }
}