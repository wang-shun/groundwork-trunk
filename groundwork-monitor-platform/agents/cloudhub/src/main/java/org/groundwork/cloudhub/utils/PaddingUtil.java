package org.groundwork.cloudhub.utils;

public final class PaddingUtil {
    private static int padmax = 0;

    /*
     * aPad( lookfor, message ))
     *
     * AUTO-pads <message> prior to <lookfor> with spaces, based on a
     * series of strings with <lookfor> in various columns.  Essentially
     * this makes output quite readable when a character such as "]" is
     * in every string from a long list of strings, but where the stuff
     * before the "]" can also be variable length.
     */
    public static String pad(String character, String message) {
        int charAt = 0;

        if (character == null)
            return message;

        if ((charAt = message.indexOf(character)) > 0) {
            if (charAt >= padmax) {
                padmax = charAt;
                return message;
            } else {
                int padAmount = padmax - charAt;
                StringBuffer buildup = new StringBuffer(250);
                buildup.append(message.substring(0, charAt));
                buildup.append(String.format("%" + padAmount + "s", ""));
                buildup.append(message.substring(charAt));
                return buildup.toString();
            }
        }
        return message;
    }

}
