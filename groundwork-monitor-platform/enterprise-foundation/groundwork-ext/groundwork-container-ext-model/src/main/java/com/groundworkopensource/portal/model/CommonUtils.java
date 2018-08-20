package com.groundworkopensource.portal.model;

import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;

public class CommonUtils {
	
	/**Extended Role ATT constant */
	public static final String EXTENDED_ROLE_ATT = "com.gwos.portal.ext_role_atts";
	
	private static final String HAS_BEEN_VALIDATED = "hasbeenvalidated";


	/**
	 * Converts HG Beans to String
	 * 
	 * @param hgBeans
	 * @return
	 */
	public static String convert2HGString(List<String> hgList) {
		StringBuilder sb = new StringBuilder();
		if (hgList != null) {
			int count = 0;
			for (String hg : hgList) {				
					sb.append(hg);
					if (++count != hgList.size()) {
						sb.append(",");
					} // end if
				} // end for
			} // end if
		return sb.toString();
	}

	/**
	 * Converts String to HGBEans
	 * 
	 * @param hgList
	 * @return
	 */
	public static List<String> convert2HGList(String hgString) {
		List<String> hgList = null;
		if (hgString != null && !hgString.equalsIgnoreCase("")) {
			StringTokenizer stkn = new StringTokenizer(hgString, ",");
			hgList = new ArrayList<String>();
			while (stkn.hasMoreTokens()) {
				hgList.add(stkn.nextToken());
			} // end while
		} // end if
		return hgList;
	}
	
	/**
	 * Converts String to HGBEans
	 * 
	 * @param hgList
	 * @return
	 */
	public static List<String> convert2SGList(String sgString) {
		List<String> sgList = null;
		if (sgString != null && !sgString.equalsIgnoreCase("")) {
			StringTokenizer stkn = new StringTokenizer(sgString, ",");
			sgList = new ArrayList<String>();
			while (stkn.hasMoreTokens()) {
				sgList.add(stkn.nextToken());
			} // end while
		} // end if
		return sgList;
	}



	/**
	 * Converts String to SGBeans
	 * 
	 * @param sgList
	 * @return
	 */
	public static String convert2SGString(List<String> sgList) {
		StringBuilder sb = new StringBuilder();
		if (sgList != null) {
			int count = 0;
			for (String sg : sgList) {				
					sb.append(sg);
					if (++count != sgList.size()) {
						sb.append(",");
					} // end if
				} // end for
			} // end if
		return sb.toString();
	}

}
