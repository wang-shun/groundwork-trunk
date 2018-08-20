package com.groundworkopensource.portal.statusviewer.common.actions;

/**
 * This enum defines all the menu items for the Service context.
 * 
 * @author shivangi_walvekar
 * 
 */
public enum ServiceActionEnum {
    /**
     * This semicolon is required as part of enum syntax in case of nested
     * enums. Removing this give compilation errors.
     */
    ;
    /**
     * This child enum defines menu items for Acknowledge menu for Service
     * context.
     * 
     * @author shivangi_walvekar
     * 
     */
    public enum Acknowledge {
        /**
         * Acknowledge this service problem
         */
        ACK_SVC_PROB(ActionCommandsConstants.ACKNOWLEDGE_SVC_PROBLEM,
                NagiosCommandsConstants.ACKNOWLEDGE_SVC_PROBLEM),
        /**
         * Remove Acknowledgment of this service problem
         */
        REM_SVC_ACK(ActionCommandsConstants.REMOVE_SVC_ACKNOWLEDGEMENT,
                NagiosCommandsConstants.REMOVE_SVC_ACKNOWLEDGEMENT);
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
        private Acknowledge(String actionCommand, String nagiosCommand) {
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
     * This child enum defines menu items for Downtime menu for Host context.
     * 
     * @author shivangi_walvekar
     * 
     */
    public enum Downtime {
        /**
         * Schedule Downtime For This Service
         */
        SCHEDULE_SVC_DOWNTIME(ActionCommandsConstants.SCHEDULE_SVC_DOWNTIME,
                NagiosCommandsConstants.SCHEDULE_SVC_DOWNTIME);
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
     * This child enum defines menu items for Notifications menu for Host
     * context.
     * 
     * @author shivangi_walvekar
     * 
     */
    public enum Notifications {
        /**
         * Disable Notifications for this service
         */
        DISABLE_SVC_NOTIFICATIONS(
                ActionCommandsConstants.DISABLE_SVC_NOTIFICATIONS,
                NagiosCommandsConstants.DISABLE_SVC_NOTIFICATIONS),
        /**
         * Delay Next Notification for this service
         */
        DELAY_SVC_NOTIFICATION(ActionCommandsConstants.DELAY_SVC_NOTIFICATION,
                NagiosCommandsConstants.DELAY_SVC_NOTIFICATION),
        /**
         * Enable Notifications for this service
         */
        ENABLE_SVC_NOTIFICATIONS(
                ActionCommandsConstants.ENABLE_SVC_NOTIFICATIONS,
                NagiosCommandsConstants.ENABLE_SVC_NOTIFICATIONS);
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
     * This child enum defines menu items for Settings menu for Host context.
     * 
     * @author shivangi_walvekar
     * 
     */
    public enum Settings {
        /**
         * Disable Checks On This Service
         */
        DISABLE_SVC_CHECK(ActionCommandsConstants.DISABLE_SVC_CHECK,
                NagiosCommandsConstants.DISABLE_SVC_CHECK),
        /**
         * Enable Checks On This Service
         */
        ENABLE_SVC_CHECK(ActionCommandsConstants.ENABLE_SVC_CHECK,
                NagiosCommandsConstants.ENABLE_SVC_CHECK),
        /**
         * Disable Passive Checks for this service
         */
        DISABLE_PASSIVE_SVC_CHECKS(
                ActionCommandsConstants.DISABLE_PASSIVE_SVC_CHECKS,
                NagiosCommandsConstants.DISABLE_PASSIVE_SVC_CHECKS),
        /**
         * Enable Passive Checks for this service
         */
        ENABLE_PASSIVE_SVC_CHECKS(
                ActionCommandsConstants.ENABLE_PASSIVE_SVC_CHECKS,
                NagiosCommandsConstants.ENABLE_PASSIVE_SVC_CHECKS),
        /**
         * Disable Flap Detection for this service
         */
        DISABLE_SVC_FLAP_DETECTION(
                ActionCommandsConstants.DISABLE_SVC_FLAP_DETECTION,
                NagiosCommandsConstants.DISABLE_SVC_FLAP_DETECTION),
        /**
         * Enable Flap Detection for this service
         */
        ENABLE_SVC_FLAP_DETECTION(
                ActionCommandsConstants.ENABLE_SVC_FLAP_DETECTION,
                NagiosCommandsConstants.ENABLE_SVC_FLAP_DETECTION);
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

    /**
     * This child enum defines menu items for EventHandlers menu for Host
     * context.
     * 
     * @author shivangi_walvekar
     * 
     */
    public enum EventHandlers {
        /**
         * Disable Event Handler for this service
         */
        DISABLE_SVC_EVENT_HANDLER(
                ActionCommandsConstants.DISABLE_SVC_EVENT_HANDLER,
                NagiosCommandsConstants.DISABLE_SVC_EVENT_HANDLER),

        /**
         * Enable Event Handler for this service
         */
        ENABLE_SVC_EVENT_HANDLER(
                ActionCommandsConstants.ENABLE_SVC_EVENT_HANDLER,
                NagiosCommandsConstants.ENABLE_SVC_EVENT_HANDLER);
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
        private EventHandlers(String actionCommand, String nagiosCommand) {
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
     * This child enum defines menu items for CheckResults menu for Host
     * context.
     * 
     * @author shivangi_walvekar
     * 
     */
    public enum CheckResults {
        /**
         * Submit Passive Check Result for this service
         */
        PROCESS_SERVICE_CHECK_RESULT(
                ActionCommandsConstants.PROCESS_SERVICE_CHECK_RESULT,
                NagiosCommandsConstants.PROCESS_SERVICE_CHECK_RESULT),
        /**
         * Reschedule Next Check for this service
         */
        SCHEDULE_SVC_CHECK(ActionCommandsConstants.SCHEDULE_SVC_CHECK,
                NagiosCommandsConstants.SCHEDULE_SVC_CHECK);

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
        private CheckResults(String actionCommand, String nagiosCommand) {
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
