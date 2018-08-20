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

/**
 * Common Utility class
 * 
 * @author manish_kjain
 * 
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
     * 
     * Refer to "HideUtilityClassConstructor" section in
     * http://checkstyle.sourceforge.net/config_design.html.
     */
    protected CommonUtils() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }
}
