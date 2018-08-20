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
package org.jboss.portal.core.model.portal.navstate;

import org.jboss.portal.Mode;
import org.jboss.portal.WindowState;
import org.jboss.portal.common.invocation.AttributeResolver;
import org.jboss.portal.core.model.portal.Window;
import org.jboss.portal.core.navstate.NavigationalStateKey;
import org.jboss.portal.portlet.StateString;

import java.io.Serializable;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 10960 $
 */
public final class WindowNavigationalState implements Serializable
{

   /** . */
   private static final WindowNavigationalState DEFAULT = new WindowNavigationalState();

   /** . */
   private final WindowState windowState;

   /** . */
   private final Mode mode;

   /** . */
   private final StateString contentState;

   /** . */
   private final StateString publicContentState;

   public static WindowNavigationalState create()
   {
      return DEFAULT;
   }

   public WindowNavigationalState(WindowState windowState, Mode mode, StateString contentState, StateString publicContentState)
   {
      if (windowState == null)
      {
         throw new IllegalArgumentException("No null window state accepted");
      }
      if (mode == null)
      {
         throw new IllegalArgumentException("No null mode accepted");
      }
      this.windowState = windowState;
      this.mode = mode;
      this.contentState = contentState;
      this.publicContentState = publicContentState;
   }

   private WindowNavigationalState()
   {
      this(WindowState.NORMAL, Mode.VIEW, null, null);
   }

   public WindowState getWindowState()
   {
      return windowState;
   }

   public Mode getMode()
   {
      return mode;
   }

   public StateString getContentState()
   {
      return contentState;
   }

   public StateString getPublicContentState()
   {
      return publicContentState;
   }

   public static WindowState getWindowState(AttributeResolver resolver, Object id)
   {
      NavigationalStateKey key = new NavigationalStateKey(WindowNavigationalState.class, id);

      //
      WindowNavigationalState wns = (WindowNavigationalState)resolver.getAttribute(key);

      //
      return wns != null ? wns.getWindowState() : null;
   }

   public static void setWindowState(AttributeResolver resolver, Object id, WindowState windowState)
   {
      setWindowState(resolver, new NavigationalStateKey(WindowNavigationalState.class, id), windowState);
   }

   public static void setWindowState(AttributeResolver resolver, NavigationalStateKey key, WindowState windowState)
   {
      if (resolver == null)
      {
         throw new IllegalArgumentException("No null resolver");
      }
      if (key == null)
      {
         throw new IllegalArgumentException("No null key");
      }
      if (windowState == null)
      {
         throw new IllegalArgumentException("No null window state");
      }

      //
      WindowNavigationalState wns = (WindowNavigationalState)resolver.getAttribute(key);

      //
      if (wns == null)
      {
         wns = new WindowNavigationalState(windowState, Mode.VIEW, null, null);
      }
      else
      {
         wns = new WindowNavigationalState(windowState, wns.getMode(), wns.getContentState(), wns.getPublicContentState());
      }

      //
      resolver.setAttribute(key, wns);
   }

   public static Mode getMode(AttributeResolver resolver, Object id)
   {
      NavigationalStateKey key = new NavigationalStateKey(WindowNavigationalState.class, id);

      //
      WindowNavigationalState wns = (WindowNavigationalState)resolver.getAttribute(key);

      //
      return wns != null ? wns.getMode() : null;
   }

   public static void setMode(AttributeResolver context, Object id, Mode mode)
   {
      setMode(context, new NavigationalStateKey(WindowNavigationalState.class, id), mode);
   }

   public static void setMode(AttributeResolver resolver, NavigationalStateKey key, Mode mode)
   {
      if (resolver == null)
      {
         throw new IllegalArgumentException("No null resolver");
      }
      if (key == null)
      {
         throw new IllegalArgumentException("No null key");
      }
      if (mode == null)
      {
         throw new IllegalArgumentException("No null mode");
      }

      //
      WindowNavigationalState wns = (WindowNavigationalState)resolver.getAttribute(key);

      //
      if (wns == null)
      {
         wns = new WindowNavigationalState(WindowState.NORMAL, mode, null, null);
      }
      else
      {
         wns = new WindowNavigationalState(wns.getWindowState(), mode, wns.getContentState(), wns.getPublicContentState());
      }

      //
      resolver.setAttribute(key, wns);
   }

   public static StateString getState(AttributeResolver resolver, Object id)
   {
      NavigationalStateKey key = new NavigationalStateKey(WindowNavigationalState.class, id);

      //
      WindowNavigationalState wns = (WindowNavigationalState)resolver.getAttribute(key);

      //
      return wns != null ? wns.getContentState() : null;
   }

   public static void setState(AttributeResolver resolver, Object id, StateString state)
   {
      setState(resolver, new NavigationalStateKey(WindowNavigationalState.class, id), state);
   }

   public static StateString getPublicState(AttributeResolver resolver, Object id)
   {
      NavigationalStateKey key = new NavigationalStateKey(WindowNavigationalState.class, id);

      //
      WindowNavigationalState wns = (WindowNavigationalState)resolver.getAttribute(key);

      //
      return wns != null ? wns.getPublicContentState() : null;
   }

   public static void setPublicState(AttributeResolver resolver, Object id, StateString state)
   {
      setPublicState(resolver, new NavigationalStateKey(WindowNavigationalState.class, id), state);
   }

   public static void setState(AttributeResolver resolver, NavigationalStateKey key, StateString state)
   {
      if (resolver == null)
      {
         throw new IllegalArgumentException("No null resolver");
      }
      if (key == null)
      {
         throw new IllegalArgumentException("No null key");
      }
      if (state == null)
      {
         throw new IllegalArgumentException("No null state");
      }

      //
      WindowNavigationalState wns = (WindowNavigationalState)resolver.getAttribute(key);

      //
      if (wns == null)
      {
         wns = new WindowNavigationalState(WindowState.NORMAL, Mode.VIEW, state, null);
      }
      else
      {
         wns = new WindowNavigationalState(wns.getWindowState(), wns.getMode(), state, wns.getPublicContentState());
      }

      //
      resolver.setAttribute(key, wns);
   }

   public static void setPublicState(AttributeResolver resolver, NavigationalStateKey key, StateString publicState)
   {
      if (resolver == null)
      {
         throw new IllegalArgumentException("No null resolver");
      }
      if (key == null)
      {
         throw new IllegalArgumentException("No null key");
      }
      if (publicState == null)
      {
         throw new IllegalArgumentException("No null public state");
      }

      //
      WindowNavigationalState wns = (WindowNavigationalState)resolver.getAttribute(key);

      //
      if (wns == null)
      {
         wns = new WindowNavigationalState(WindowState.NORMAL, Mode.VIEW, null, publicState);
      }
      else
      {
         wns = new WindowNavigationalState(wns.getWindowState(), wns.getMode(), wns.getContentState(), publicState);
      }

      //
      resolver.setAttribute(key, wns);
   }

   public static void setState(AttributeResolver resolver, NavigationalStateKey key, StateString state, Window window)
   {
      if (resolver == null)
      {
         throw new IllegalArgumentException("No null resolver");
      }
      if (key == null)
      {
         throw new IllegalArgumentException("No null key");
      }
      if (state == null)
      {
         throw new IllegalArgumentException("No null state");
      }
      if (window == null)
      {
         throw new IllegalArgumentException("No null window");
      }

      //
      WindowNavigationalState wns = (WindowNavigationalState)resolver.getAttribute(key);

      //
      if (wns == null)
      {
         wns = new WindowNavigationalState(window.getInitialWindowState(), window.getInitialMode(), state, null);
      }
      else
      {
         wns = new WindowNavigationalState(wns.getWindowState(), wns.getMode(), state, wns.getPublicContentState());
      }

      //
      resolver.setAttribute(key, wns);
   }

   public static WindowNavigationalState bilto(WindowNavigationalState oldNS, WindowState windowState, Mode mode, StateString contentState)
   {
      StateString newState = oldNS != null ? oldNS.getContentState() : null;
      WindowState newWindowState = oldNS != null ? oldNS.getWindowState() : WindowState.NORMAL;
      Mode newMode = oldNS != null ? oldNS.getMode() : Mode.VIEW;

      //
      if (contentState != null)
      {
         newState = contentState;
      }

      //
      if (mode != null)
      {
         newMode = mode;
      }

      //
      if (windowState != null)
      {
         newWindowState = windowState;
      }

      // Create new NS
      return new WindowNavigationalState(newWindowState, newMode, newState, oldNS.getPublicContentState());
   }
   
}
