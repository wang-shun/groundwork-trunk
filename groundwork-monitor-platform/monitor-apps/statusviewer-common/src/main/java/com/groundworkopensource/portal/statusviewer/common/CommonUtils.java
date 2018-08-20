/*
 * 
 * Copyright 2007 GroundWork Open Source, Inc. ("GroundWork") All rights
 * reserved. This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package com.groundworkopensource.portal.statusviewer.common;


import com.groundworkopensource.portal.common.ws.ApplicationTypeHelper;
import org.groundwork.foundation.ws.model.impl.ServiceStatus;
import org.groundwork.rs.dto.DtoApplicationType;

import java.util.List;

/**
 * Common Utility class
 *
 * @author manish_kjain
 */
public class CommonUtils {


    /**
     * Wrap long String which does not contains any spaces or white space.
     *
     * @param str
     * @param maxCols
     * @return String
     */
    public static String getWrapString(String str, int maxCols) {
        StringBuffer wrapStr = new StringBuffer();

        try {
            String[] splitWithBr = str.split(Constant.BR);
            for (int i = 0; i < splitWithBr.length; i++) {
                if (splitWithBr[i].length() > maxCols) {
                    wrapStr.append(getSubWrapString(splitWithBr[i], maxCols));
                } else {
                    wrapStr.append(splitWithBr[i]);
                    wrapStr.append(Constant.BR);
                }
            }
        } catch (Exception e) {
            // ingore exception
            return str;
        }
        return wrapStr.toString();
    }

    /**
     * Wrap long String which does not contains any spaces or white space.
     *
     * @param str
     * @param maxCols
     * @return String
     */
    public static String getSubWrapString(String str, int maxCols) {
        String strSoftHyphen = "&#8203;";
        StringBuffer wrapString = new StringBuffer();
        String[] splitString = str.split(Constant.SPACE);
        for (int i = 0; i < splitString.length; i++) {
            if (splitString[i].length() > maxCols) {
                String stringToWrap = splitString[i];
                while (stringToWrap.length() > maxCols) {
                    wrapString.append(stringToWrap.substring(0, maxCols));
                    wrapString.append(strSoftHyphen);
                    stringToWrap = stringToWrap.substring(maxCols, stringToWrap
                            .length());
                }
                wrapString.append(stringToWrap);
                wrapString.append(Constant.SPACE);
            } else {
                wrapString.append(splitString[i]);
                wrapString.append(Constant.SPACE);
            }
        }
        return wrapString.toString();
    }

    /**
     * Protected Constructor - Rationale: Instantiating utility classes does not
     * make sense. Hence the constructors should either be private or (if you
     * want to allow sub-classing) protected. <br>
     * <p/>
     * Refer to "HideUtilityClassConstructor" section in
     * http://checkstyle.sourceforge.net/config_design.html.
     */
    protected CommonUtils() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }

    /**
     * Helper to Get application type from ID
     *
     * @param appTypeID
     * @return
     */
    public static String getApplicationNameByID(int appTypeID) {
        List<DtoApplicationType> appTypes = ApplicationTypeHelper.getApplicationTypes();
        String output = null;
        for (DtoApplicationType appType : appTypes) {
            if (appType.getId() == appTypeID) {
                output = appType.getName();
                break;
            } // end if
        }  // end for
        return output;
    }

    /**
     * Checks if atleast one service is Nagios
     * @param services
     * @return
     */
    public static boolean isAtleastOnceNagiosService(ServiceStatus[] services) {
        boolean result = false;

        for (ServiceStatus service : services) {
            String appType = CommonUtils.getApplicationNameByID(service.getApplicationTypeID());
            if ((appType != null) && appType.equalsIgnoreCase(Constant.APP_TYPE_NAGIOS)) {
                result = true;
                break;
            }
        }
        return result;
    }
}
