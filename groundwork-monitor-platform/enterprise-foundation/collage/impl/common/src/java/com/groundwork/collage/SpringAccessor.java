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

package com.groundwork.collage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.groundwork.collage.exception.CollageException;

/**
 *
 * SpringAcessor Spring static accessor singleton to initialize the Spring
 * environment and for getting instances of API objects
 *
 * @author <a href="mailto:rruttimann@itgroundwork.com"> Roger Ruttimann </a>
 * @version $Id: SpringAccessor.java 6397 2007-04-02 21:27:40Z glee $
 */
public class SpringAccessor {

    private static Log log = LogFactory.getLog(SpringAccessor.class);

    /*
     * Use the Application Context to load multiple assemby files. The assembly
     * files have to be in the classpath (typically in the META-INF diectory of
     * the jar package). Each implementation package (query/admin)
     * should include an assembly file.
     */

    private static BeanFactory springFactory = null;

    public static BeanFactory getFactory() {
        return springFactory;
    }

    /**
     * getBean
     * Returns a bean for a given name. If the bean is not found it returns null.
     * @param beanName given name
     * @return bean object
     */
    static Object getBean(String beanName) {
        if (springFactory == null) {
            log.warn("Bean Factory has no assemblies loaded. Call loadAssembly() before requesting beans");
            return null;
        }
        return springFactory.getBean(beanName);
    }

    /**
     * Add a new assembly to the existing context. This allows to add new beans at runtime
     * to the BeanFactory
     *
     * @param assemblyPath path to assembly xml
     * @throws CollageException on failure to load assembly
     */
    static void loadAssembly(String assemblyPath) throws CollageException {
        if (log.isDebugEnabled()) log.debug("Loading assembly: " + assemblyPath);
        String[] addContext = new String[]{assemblyPath};
        ApplicationContext context;

        try {
            if (springFactory == null) {
                context = new ClassPathXmlApplicationContext (addContext);
            } else {
                context = new ClassPathXmlApplicationContext (addContext, (ApplicationContext)springFactory);
            }

            if (log.isInfoEnabled()) log.info("Loaded assembly " + assemblyPath + " into BeanFactory");

            // Re-assign updated context to bean factory
            springFactory = context;
        } catch (BeansException be) {
            // Model assembly not available -- stop!
            log.error("Unable to load: " + assemblyPath, be);
            throw new CollageException("Error! Failed to load assembly " + assemblyPath, be);
        }
    }

    static void unloadAssembly() {
        ApplicationContext context = (ApplicationContext)springFactory;
        do {
            if (log.isDebugEnabled()) log.debug("Closing AppContext: " + context.getApplicationName() + " " + context.getDisplayName() + " " + context.getId());
            ApplicationContext parent = context.getParent();
            ((ClassPathXmlApplicationContext) context).close();
            context = parent;
        } while (context != null);
        springFactory = null;
    }

}
