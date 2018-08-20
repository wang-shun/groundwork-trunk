package com.groundworkopensource.portal.statusviewer.common.actions;

/**
 * This enum defines all the menu items for the Service Group context.
 * 
 * @author shivangi_walvekar
 * 
 */
public enum ServiceGroupActionEnum {
    /**
     * This semicolon is required as part of enum syntax in case of nested
     * enums. Removing this give compilation errors.
     */
    ;
    /**
     * This child enum defines menu items for Notifications menu for Host
     * context.
     * 
     * @author shivangi_walvekar
     * 
     */
    public enum Notifications {
        /**
         * Enable notifications for all Services in this service group
         */
        ENABLE_SERVICEGROUP_SVC_NOTIFICATIONS(
                ActionCommandsConstants.ENABLE_SERVICEGROUP_SVC_NOTIFICATIONS,
                NagiosCommandsConstants.ENABLE_SERVICEGROUP_SVC_NOTIFICATIONS),
        /**
         * Disable notifications for all Services in this service group
         */
        DISABLE_SERVICEGROUP_SVC_NOTIFICATIONS(
                ActionCommandsConstants.DISABLE_SERVICEGROUP_SVC_NOTIFICATIONS,
                NagiosCommandsConstants.DISABLE_SERVICEGROUP_SVC_NOTIFICATIONS);
        /**
         * String property for action command to be displayed on UI.
         */
        private String actionCommand;
        /**
         * String property for command to be sent to Nagios.
         */
        private String nagiosCommand;

        /**
         * Constructor
         */
        private Notifications(String actionCommand, String nagiosCommand) {
            this.actionCommand = actionCommand;
            this.nagiosCommand = nagiosCommand;
        }

        /**
         * @return actionCommand
         */
        public String getActionCommand() {
            return actionCommand;
        }

        /**
         * @param actionCommand
         */
        public void setActionCommand(String actionCommand) {
            this.actionCommand = actionCommand;
        }

        /**
         * @return nagiosCommand
         */
        public String getNagiosCommand() {
            return nagiosCommand;
        }

        /**
         * @param nagiosCommand
         */
        public void setNagiosCommand(String nagiosCommand) {
            this.nagiosCommand = nagiosCommand;
        }
    }

    /**
     * This child enum defines menu items for Downtime menu for Service context.
     * 
     * @author shivangi_walvekar
     * 
     */
    public enum Downtime {
        /**
         * Schedule Downtime For All Services in this service group
         */
        SCHEDULE_SERVICEGROUP_SVC_DOWNTIME(
                ActionCommandsConstants.SCHEDULE_SERVICEGROUP_SVC_DOWNTIME,
                NagiosCommandsConstants.SCHEDULE_SERVICEGROUP_SVC_DOWNTIME);
        /**
         * String property for action command to be displayed on UI.
         */
        private String actionCommand;
        /**
         * String property for command to be sent to Nagios.
         */
        private String nagiosCommand;

        /**
         * Constructor
         */
        private Downtime(String actionCommand, String nagiosCommand) {
            this.actionCommand = actionCommand;
            this.nagiosCommand = nagiosCommand;
        }

        /**
         * @return actionCommand
         */
        public String getActionCommand() {
            return actionCommand;
        }

        /**
         * @param actionCommand
         */
        public void setActionCommand(String actionCommand) {
            this.actionCommand = actionCommand;
        }

        /**
         * @return nagiosCommand
         */
        public String getNagiosCommand() {
            return nagiosCommand;
        }

        /**
         * @param nagiosCommand
         */
        public void setNagiosCommand(String nagiosCommand) {
            this.nagiosCommand = nagiosCommand;
        }
    }

    /**
     * This child enum defines menu items for Settings menu for Service context.
     * 
     * @author shivangi_walvekar
     * 
     */
    public enum Settings {
        /**
         * Enable checks for all Services in this service group
         */
        ENABLE_SERVICEGROUP_SVC_CHECKS(
                ActionCommandsConstants.ENABLE_SERVICEGROUP_SVC_CHECKS,
                NagiosCommandsConstants.ENABLE_SERVICEGROUP_SVC_CHECKS),
        /**
         * Disable checks for all Services in this service group
         */
        DISABLE_SERVICEGROUP_SVC_CHECKS(
                ActionCommandsConstants.DISABLE_SERVICEGROUP_SVC_CHECKS,
                NagiosCommandsConstants.DISABLE_SERVICEGROUP_SVC_CHECKS);
        /**
         * String property for action command to be displayed on UI.
         */
        private String actionCommand;
        /**
         * String property for command to be sent to Nagios.
         */
        private String nagiosCommand;

        /**
         * Constructor
         */
        private Settings(String actionCommand, String nagiosCommand) {
            this.actionCommand = actionCommand;
            this.nagiosCommand = nagiosCommand;
        }

        /**
         * @return actionCommand
         */
        public String getActionCommand() {
            return actionCommand;
        }

        /**
         * @param actionCommand
         */
        public void setActionCommand(String actionCommand) {
            this.actionCommand = actionCommand;
        }

        /**
         * @return nagiosCommand
         */
        public String getNagiosCommand() {
            return nagiosCommand;
        }

        /**
         * @param nagiosCommand
         */
        public void setNagiosCommand(String nagiosCommand) {
            this.nagiosCommand = nagiosCommand;
        }
    }
}
