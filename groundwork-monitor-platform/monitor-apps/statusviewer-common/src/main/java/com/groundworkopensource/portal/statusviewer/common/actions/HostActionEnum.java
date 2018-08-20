package com.groundworkopensource.portal.statusviewer.common.actions;

/**
 * This enum defines all the menu items for the Host context.
 * 
 * @author shivangi_walvekar
 * 
 */
public enum HostActionEnum {
    /**
     * This semicolon is required as part of enum syntax in case of nested
     * enums. Removing this give compilation errors.
     */
    ;
    /**
     * This child enum defines menu items for Acknowledge menu for Host context.
     * 
     * @author shivangi_walvekar
     * 
     */
    public enum Acknowledge {
        /**
         * Acknowledge This Host Problem - CompositeCommand (ACK_HOST_PROB +
         * ACK_SERVICES_TOO)
         */
        ACK_HOST_PROB(ActionCommandsConstants.ACK_HOST_PROB,
                NagiosCommandsConstants.ACK_HOST_PROB),

        /**
         * Remove Acknowledgment of this host problem
         */
        REM_ACK_PROB(ActionCommandsConstants.REMOVE_HOST_ACKNOWLEDGEMENT,
                NagiosCommandsConstants.REMOVE_HOST_ACKNOWLEDGEMENT);

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
         * 
         * @param actionCommand
         * @param nagiosCommand
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
         * Schedule Downtime for this Host
         */
        SCHEDULE_HOST_DOWNTIME(ActionCommandsConstants.SCHEDULE_HOST_DOWNTIME,
                NagiosCommandsConstants.SCHEDULE_HOST_DOWNTIME);
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
         * Enable Notifications for this host
         */
        ENABLE_HOST_NOTIFICATIONS(
                ActionCommandsConstants.ENABLE_HOST_NOTIFICATIONS,
                NagiosCommandsConstants.ENABLE_HOST_NOTIFICATIONS),
        /**
         * Delay next Notification for this host
         */
        DELAY_HOST_NOTIFICATION(
                ActionCommandsConstants.DELAY_HOST_NOTIFICATION,
                NagiosCommandsConstants.DELAY_HOST_NOTIFICATION),

        /**
         * Disable Notifications for All Services on Host
         */
        DISABLE_HOST_SVC_NOTIFICATIONS(
                ActionCommandsConstants.DISABLE_HOST_SVC_NOTIFICATIONS,
                NagiosCommandsConstants.DISABLE_HOST_SVC_NOTIFICATIONS),
        /**
         * Disable Notifications for this Host
         */
        DISABLE_HOST_NOTIFICATIONS(
                ActionCommandsConstants.DISABLE_HOST_NOTIFICATIONS,
                NagiosCommandsConstants.DISABLE_HOST_NOTIFICATIONS),
        /**
         * Enable Notifications for All Services on Host
         */
        ENABLE_HOST_SVC_NOTIFICATIONS(
                ActionCommandsConstants.ENABLE_HOST_SVC_NOTIFICATIONS,
                NagiosCommandsConstants.ENABLE_HOST_SVC_NOTIFICATIONS);
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
         * Disable Checks On This Host
         */
        DISABLE_HOST_CHECK(ActionCommandsConstants.DISABLE_HOST_CHECK,
                NagiosCommandsConstants.DISABLE_HOST_CHECK),
        /**
         * Enable Checks On This Host
         */
        ENABLE_HOST_CHECK(ActionCommandsConstants.ENABLE_HOST_CHECK,
                NagiosCommandsConstants.ENABLE_HOST_CHECK),
        /**
         * Enable Passive Checks On This Host
         */
        ENABLE_PASSIVE_HOST_CHECKS(
                ActionCommandsConstants.ENABLE_PASSIVE_HOST_CHECKS,
                NagiosCommandsConstants.ENABLE_PASSIVE_HOST_CHECKS),
        /**
         * Disable Passive Checks On This Host
         */
        DISABLE_PASSIVE_HOST_CHECKS(
                ActionCommandsConstants.DISABLE_PASSIVE_HOST_CHECKS,
                NagiosCommandsConstants.DISABLE_PASSIVE_HOST_CHECKS),
        /**
         * Disable Active Checks for All Services on Host
         */
        DISABLE_HOST_SVC_CHECKS(
                ActionCommandsConstants.DISABLE_HOST_SVC_CHECKS,
                NagiosCommandsConstants.DISABLE_HOST_SVC_CHECKS),
        /**
         * Enable Active Checks for All Services on Host
         */
        ENABLE_HOST_SVC_CHECKS(ActionCommandsConstants.ENABLE_HOST_SVC_CHECKS,
                NagiosCommandsConstants.ENABLE_HOST_SVC_CHECKS),

        // /**
        // * Start Obsessing Over This Host
        // */
        // START_OBSESSING_OVER_HOST(
        // ActionCommandsConstants.START_OBSESSING_OVER_HOST,
        // NagiosCommandsConstants.START_OBSESSING_OVER_HOST),
        // /**
        // * Stop Obsessing Over This Host
        // */
        // STOP_OBSESSING_OVER_HOST(
        // ActionCommandsConstants.STOP_OBSESSING_OVER_HOST,
        // NagiosCommandsConstants.STOP_OBSESSING_OVER_HOST),
        /**
         * Enable Flap Detection for This Host
         */
        ENABLE_HOST_FLAP_DETECTION(
                ActionCommandsConstants.ENABLE_HOST_FLAP_DETECTION,
                NagiosCommandsConstants.ENABLE_HOST_FLAP_DETECTION),
        /**
         * Disable Flap Detection for this host
         */
        DISABLE_HOST_FLAP_DETECTION(
                ActionCommandsConstants.DISABLE_HOST_FLAP_DETECTION,
                NagiosCommandsConstants.DISABLE_HOST_FLAP_DETECTION);
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
         * Enable Event Handler for this host
         */
        ENABLE_HOST_EVENT_HANDLER(
                ActionCommandsConstants.ENABLE_HOST_EVENT_HANDLER,
                NagiosCommandsConstants.ENABLE_HOST_EVENT_HANDLER),
        /**
         * Disable Event Handler for this host
         */
        DISABLE_HOST_EVENT_HANDLER(
                ActionCommandsConstants.DISABLE_HOST_EVENT_HANDLER,
                NagiosCommandsConstants.DISABLE_HOST_EVENT_HANDLER);
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
         * Re-Schedule the Next Check for this host
         */
        SCHEDULE_HOST_CHECK(ActionCommandsConstants.SCHEDULE_HOST_CHECK,
                NagiosCommandsConstants.SCHEDULE_HOST_CHECK),

        /**
         * Schedule Check For All Services Of This Host
         */
        SCHEDULE_HOST_SVC_CHECKS(
                ActionCommandsConstants.SCHEDULE_HOST_SVC_CHECKS,
                NagiosCommandsConstants.SCHEDULE_HOST_SVC_CHECKS),

        /**
         * Submit Passive Check Result for This Host
         */
        PROCESS_HOST_CHECK_RESULT(
                ActionCommandsConstants.PROCESS_HOST_CHECK_RESULT,
                NagiosCommandsConstants.PROCESS_HOST_CHECK_RESULT);

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
