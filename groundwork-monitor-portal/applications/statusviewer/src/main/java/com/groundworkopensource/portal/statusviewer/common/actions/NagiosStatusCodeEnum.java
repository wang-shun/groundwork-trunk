package com.groundworkopensource.portal.statusviewer.common.actions;

/**
 * This enum defines all the monitor status to be displayed on the actions
 * portlet UI for the action command 'Submit Passive Check Result' for Service
 * and Host context. Each enum has a statusName field which maps to the label to
 * be displayed on the UI and nagiosCode field maps to the code to be passed to
 * the nagios for the corresponding statusName.
 * 
 * @author shivangi_walvekar
 * 
 */
public enum NagiosStatusCodeEnum {
    ;
    /**
     * Defines enum for the host context
     * 
     * @author shivangi_walvekar
     * 
     */
    public enum Host {
        /**
         * Indicates monitor status UP. Corresponding nagios code is 0.
         */
        UP("UP", 0),
        /**
         * Indicates monitor status DOWN. Corresponding nagios code is 1.
         */
        DOWN("DOWN", 1),
        /**
         * Indicates monitor status UNREACHABLE. Corresponding nagios code is 2.
         */
        UNREACHABLE("UNREACHABLE", 2);
        /**
         * @return statusName
         */
        public String getStatusName() {
            return statusName;
        }

        /**
         * @param statusName
         */
        public void setStatusName(String statusName) {
            this.statusName = statusName;
        }

        /**
         * @return statusValue
         */
        public int getNagiosCode() {
            return nagiosCode;
        }

        /**
         * @param nagiosCode
         */
        public void setNagiosCode(int nagiosCode) {
            this.nagiosCode = nagiosCode;
        }

        /**
         * This field which maps to the label to be displayed on the UI.
         */
        private String statusName;
        /**
         * This field maps to the code to be passed to the nagios for the
         * corresponding statusName.
         */
        private int nagiosCode;

        /**
         * Constructor
         * 
         * @param statusName
         * @param nagiosCode
         */
        private Host(String statusName, int nagiosCode) {
            this.statusName = statusName;
            this.nagiosCode = nagiosCode;
        }
    }

    /**
     * Defines enum for the service context
     * 
     * @author shivangi_walvekar
     * 
     */
    public enum Service {
        /**
         * Indicates monitor status OK. Corresponding nagios code is 0.
         */
        OK("OK", 0),
        /**
         * Indicates monitor status WARNING. Corresponding nagios code is 1.
         */
        DOWN("WARNING", 1),
        /**
         * Indicates monitor status CRITICAL. Corresponding nagios code is 2.
         */
        CRITICAL("CRITICAL", 2),
        /**
         * Indicates monitor status UNKNOWN. Corresponding nagios code is 3.
         */
        UNREACHABLE("UNKNOWN", 3);

        /**
         * Constructor
         * 
         * @param statusName
         * @param nagiosCode
         */
        private Service(String statusName, int nagiosCode) {
            this.statusName = statusName;
            this.nagiosCode = nagiosCode;
        }

        /**
         * @return statusName
         */
        public String getStatusName() {
            return statusName;
        }

        /**
         * @param statusName
         */
        public void setStatusName(String statusName) {
            this.statusName = statusName;
        }

        /**
         * @return statusValue
         */
        public int getNagiosCode() {
            return nagiosCode;
        }

        /**
         * @param nagiosCode
         */
        public void setNagiosCode(int nagiosCode) {
            this.nagiosCode = nagiosCode;
        }

        /**
         * This field which maps to the label to be displayed on the UI.
         */
        private String statusName;
        /**
         * This field maps to the code to be passed to the nagios for the
         * corresponding statusName.
         */
        private int nagiosCode;
    }

}
