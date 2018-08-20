/*
 * JOSSO: Java Open Single Sign-On
 *
 * Copyright 2004-2009, Atricore, Inc.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package org.josso.gateway.ws._1_2.wsdl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.josso.Lookup;
import org.josso.gateway.MutableSSOContext;
import org.josso.gateway.SSOGateway;

/**
 * @author <a href="mailto:sgonzalez@atricore.org">Sebastian Gonzalez Oyuela</a>
 * @version $Rev: 1530 $ $Date: 2009-09-16 16:08:28 -0300 (Wed, 16 Sep 2009) $
 */
public class BaseSSOService {

    private static Log logger = LogFactory.getLog(BaseSSOService.class);

    /**
     * Prepares a SSO Context based on the given security token (session id or assertion id)
     *
     * @param tokenType
     * @param tokenValue
     */
    protected void prepareCtx(String tokenType, String tokenValue) {
        try {

            SSOGateway gwy = Lookup.getInstance().lookupSSOGateway();
            MutableSSOContext ctx = null;

            if (tokenValue == null || "".equals(tokenValue)) {
                logger.warn("No security token recieved, using default SSO Context");
                ctx = (MutableSSOContext) gwy.prepareDefaultSSOContext();
            } else {
                ctx = (MutableSSOContext) gwy.prepareSSOContext(tokenType, tokenValue);
            }

            ctx.setUserLocation("soap-client");


        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }


    }

    /**
     * This prepares a SSO Context based on the given security domain name
     * @param securityDomain
     */
    protected void prepareCtx(String securityDomain) {
        try {

            MutableSSOContext ctx = (MutableSSOContext) Lookup.getInstance().lookupSSOGateway().prepareSSOContext(securityDomain);
            ctx.setUserLocation("soap-client");

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }
}
