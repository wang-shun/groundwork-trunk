package com.groundworkopensource.portal.statusviewer.common.actions;

/**
 * This enum defines all the menu items for the Host Group context.
 * 
 * @author shivangi_walvekar
 * 
 */
public enum HostGroupActionEnum {
    /**
     * This semicolon is required as part of enum syntax in case of nested
     * enums. Removing this give compilation errors.
     */
    ;
    /**
     * This child enum defines menu items for Downtime menu for Host Group
     * context.
     * 
     * @author shivangi_walvekar
     * 
     */
    public enum Downtime {
        /**
         * Schedule Downtime For All Hosts in this host group
         */
        SCHEDULE_HOSTGROUP_HOST_DOWNTIME(
                ActionCommandsConstants.SCHEDULE_HOSTGROUP_HOST_DOWNTIME,
                NagiosCommandsConstants.SCHEDULE_HOSTGROUP_HOST_DOWNTIME),
        /**
         * Schedule Downtime For All Services in this host group
         */
        SCHEDULE_HOSTGROUP_SVC_DOWNTIME(
                ActionCommandsConstants.SCHEDULE_HOSTGROUP_SVC_DOWNTIME,
                NagiosCommandsConstants.SCHEDULE_HOSTGROUP_SVC_DOWNTIME);
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
     * This child enum defines menu items for Notifications menu for Host Group
     * context.
     * 
     * @author shivangi_walvekar
     * 
     */
    public enum Notifications {
        /**
         * Enable notifications for all Hosts in host group
         */
        ENABLE_HOSTGROUP_HOST_NOTIFICATIONS(
                ActionCommandsConstants.ENABLE_HOSTGROUP_HOST_NOTIFICATIONS,
                NagiosCommandsConstants.ENABLE_HOSTGROUP_HOST_NOTIFICATIONS),
        /**
         * Disable notifications for all Hosts in host group
         */
        DISABLE_HOSTGROUP_HOST_NOTIFICATIONS(
                ActionCommandsConstants.DISABLE_HOSTGROUP_HOST_NOTIFICATIONS,
                NagiosCommandsConstants.DISABLE_HOSTGROUP_HOST_NOTIFICATIONS),
        /**
         * Enable notifications for all Services in host group
         */
        ENABLE_HOSTGROUP_SVC_NOTIFICATIONS(
                ActionCommandsConstants.ENABLE_HOSTGROUP_SVC_NOTIFICATIONS,
                NagiosCommandsConstants.ENABLE_HOSTGROUP_SVC_NOTIFICATIONS),

        /**
         * Disable notifications for all Services in host group
         */
        DISABLE_HOSTGROUP_SVC_NOTIFICATIONS(
                ActionCommandsConstants.DISABLE_HOSTGROUP_SVC_NOTIFICATIONS,
                NagiosCommandsConstants.DISABLE_HOSTGROUP_SVC_NOTIFICATIONS);

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
     * This child enum defines menu items for Settings menu for Host Group
     * context.
     * 
     * @author shivangi_walvekar
     * 
     */
    public enum Settings {
        /**
         * Enable checks for all Services in this host group
         */
        ENABLE_HOSTGROUP_SVC_CHECKS(
                ActionCommandsConstants.ENABLE_HOSTGROUP_SVC_CHECKS,
                NagiosCommandsConstants.ENABLE_HOSTGROUP_SVC_CHECKS),
        /**
         * Enable checks for all Services in this host group
         */
        DISABLE_HOSTGROUP_SVC_CHECKS(
                ActionCommandsConstants.DISABLE_HOSTGROUP_SVC_CHECKS,
                NagiosCommandsConstants.DISABLE_HOSTGROUP_SVC_CHECKS);
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
