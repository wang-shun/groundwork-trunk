package com.groundworkopensource.portal.statusviewer.common.actions;

/**
 * This enum defines all the composite commands.
 * 
 * @author shivangi_walvekar
 * 
 */
public enum CompositeCommandsEnum {
    /**
     * nagios command = 'Acknowledge This Host Problem' , 'Acknowledge This Host
     * Problem - services too'
     */
    ACK_HOST_PROB(ActionCommandsConstants.ACK_HOST_PROB, new String[] {
            ActionCommandsConstants.ACK_HOST_PROB,
            ActionCommandsConstants.ACK_SERVICES_TOO }, new String[] {
            NagiosCommandsConstants.ACK_HOST_PROB,
            NagiosCommandsConstants.ACK_SERVICES_TOO }, true),
    /**
     * Composite command for - 'Enable notifications for all Services' , 'Enable
     * notifications for all Hosts' in host group
     */
    ENABLE_HOSTGROUP_SVC_NOTIFICATIONS(
            ActionCommandsConstants.ENABLE_HOSTGROUP_SVC_NOTIFICATIONS,
            new String[] {
                    ActionCommandsConstants.ENABLE_HOSTGROUP_SVC_NOTIFICATIONS,
                    ActionCommandsConstants.ENABLE_HOSTGROUP_HOST_NOTIFICATIONS },
            new String[] {
                    NagiosCommandsConstants.ENABLE_HOSTGROUP_SVC_NOTIFICATIONS,
                    NagiosCommandsConstants.ENABLE_HOSTGROUP_HOST_NOTIFICATIONS },
            false),
    /**
     * nagios command = 'Disable notifications for all Services' ,'Disable
     * notifications for all Hosts' in host group
     */
    DISABLE_HOSTGROUP_SVC_NOTIFICATIONS(
            ActionCommandsConstants.DISABLE_HOSTGROUP_SVC_NOTIFICATIONS,
            new String[] {
                    ActionCommandsConstants.DISABLE_HOSTGROUP_SVC_NOTIFICATIONS,
                    ActionCommandsConstants.DISABLE_HOSTGROUP_HOST_NOTIFICATIONS },
            new String[] {
                    NagiosCommandsConstants.DISABLE_HOSTGROUP_SVC_NOTIFICATIONS,
                    NagiosCommandsConstants.DISABLE_HOSTGROUP_HOST_NOTIFICATIONS },
            false),
    /**
     * nagios command = 'Disable Notifications for All Services on Host'
     * ,'Disable Notifications for host'
     */
    DISABLE_HOST_SVC_NOTIFICATIONS(
            ActionCommandsConstants.DISABLE_HOST_SVC_NOTIFICATIONS,
            new String[] {
                    ActionCommandsConstants.DISABLE_HOST_SVC_NOTIFICATIONS,
                    ActionCommandsConstants.DISABLE_HOST_NOTIFICATIONS },
            new String[] {
                    NagiosCommandsConstants.DISABLE_HOST_SVC_NOTIFICATIONS,
                    NagiosCommandsConstants.DISABLE_HOST_NOTIFICATIONS }, false),
    /**
     * nagios command = 'Disable Notifications for All Services on Host'
     * ,'Disable Notifications for host'
     */
    ENABLE_HOST_SVC_NOTIFICATIONS(
            ActionCommandsConstants.ENABLE_HOST_SVC_NOTIFICATIONS,
            new String[] {
                    ActionCommandsConstants.ENABLE_HOST_SVC_NOTIFICATIONS,
                    ActionCommandsConstants.ENABLE_HOST_NOTIFICATIONS },
            new String[] {
                    NagiosCommandsConstants.ENABLE_HOST_SVC_NOTIFICATIONS,
                    NagiosCommandsConstants.ENABLE_HOST_NOTIFICATIONS }, false),
    /**
     * nagios command = 'Schedule Downtime For All Services ' ,'Schedule
     * Downtime For All Hosts' in host group
     */
    SCHEDULE_HOSTGROUP_SVC_DOWNTIME(
            ActionCommandsConstants.SCHEDULE_HOSTGROUP_SVC_DOWNTIME,
            new String[] {
                    ActionCommandsConstants.SCHEDULE_HOSTGROUP_SVC_DOWNTIME,
                    ActionCommandsConstants.SCHEDULE_HOSTGROUP_HOST_DOWNTIME },
            new String[] {
                    NagiosCommandsConstants.SCHEDULE_HOSTGROUP_SVC_DOWNTIME,
                    NagiosCommandsConstants.SCHEDULE_HOSTGROUP_HOST_DOWNTIME },
            false),
    /**
     * nagios command = 'Enable checks for all Services' ,'Enable checks for all
     * hosts' in host group
     */
    ENABLE_HOSTGROUP_SVC_CHECKS(
            ActionCommandsConstants.ENABLE_HOSTGROUP_SVC_CHECKS, new String[] {
                    ActionCommandsConstants.ENABLE_HOSTGROUP_SVC_CHECKS,
                    ActionCommandsConstants.ENABLE_HOSTGROUP_HOST_CHECKS },
            new String[] { NagiosCommandsConstants.ENABLE_HOSTGROUP_SVC_CHECKS,
                    NagiosCommandsConstants.ENABLE_HOSTGROUP_HOST_CHECKS },
            false),
    /**
     * nagios command = 'Disable checks for all Services' ,'Disable checks for
     * all hosts' in host group
     */
    DISABLE_HOSTGROUP_SVC_CHECKS(
            ActionCommandsConstants.DISABLE_HOSTGROUP_SVC_CHECKS, new String[] {
                    ActionCommandsConstants.DISABLE_HOSTGROUP_SVC_CHECKS,
                    ActionCommandsConstants.DISABLE_HOSTGROUP_HOST_CHECKS },
            new String[] {
                    NagiosCommandsConstants.DISABLE_HOSTGROUP_SVC_CHECKS,
                    NagiosCommandsConstants.DISABLE_HOSTGROUP_HOST_CHECKS },
            false),
    /**
     * nagios command = 'Disable Active Checks for All Services on Host'
     * ,'Disable Checks On This Host'
     */
    DISABLE_HOST_SVC_CHECKS(ActionCommandsConstants.DISABLE_HOST_SVC_CHECKS,
            new String[] { ActionCommandsConstants.DISABLE_HOST_SVC_CHECKS,
                    ActionCommandsConstants.DISABLE_HOST_CHECK }, new String[] {
                    NagiosCommandsConstants.DISABLE_HOST_SVC_CHECKS,
                    NagiosCommandsConstants.DISABLE_HOST_CHECK }, false),
    /**
     * nagios command = 'Enable Active Checks for All Services on Host' ,'Enable
     * Checks On This Host'
     */
    ENABLE_HOST_SVC_CHECKS(ActionCommandsConstants.ENABLE_HOST_SVC_CHECKS,
            new String[] { ActionCommandsConstants.ENABLE_HOST_SVC_CHECKS,
                    ActionCommandsConstants.ENABLE_HOST_CHECK }, new String[] {
                    NagiosCommandsConstants.ENABLE_HOST_SVC_CHECKS,
                    NagiosCommandsConstants.ENABLE_HOST_CHECK }, false),
    /**
     * nagios command = 'Re-Schedule the Next Check' ,'Re-Schedule the Next
     * Check - Forced'
     */
    SCHEDULE_HOST_CHECK(ActionCommandsConstants.SCHEDULE_HOST_CHECK,
            new String[] { ActionCommandsConstants.SCHEDULE_HOST_CHECK,
                    ActionCommandsConstants.SCHEDULE_FORCED_HOST_CHECK },
            new String[] { NagiosCommandsConstants.SCHEDULE_HOST_CHECK,
                    NagiosCommandsConstants.SCHEDULE_FORCED_HOST_CHECK }, false),
    /**
     * nagios command = 'Schedule Check For All Services Of This Host'
     * ,'Schedule Check For All Services Of This Host-Forced'
     */
    SCHEDULE_HOST_SVC_CHECKS(ActionCommandsConstants.SCHEDULE_HOST_SVC_CHECKS,
            new String[] {
                    ActionCommandsConstants.SCHEDULE_FORCED_HOST_SVC_CHECKS,
                    ActionCommandsConstants.SCHEDULE_FORCED_HOST_CHECK },
            new String[] { NagiosCommandsConstants.SCHEDULE_HOST_SVC_CHECKS,
                    NagiosCommandsConstants.SCHEDULE_FORCED_HOST_SVC_CHECKS },
            false),
    /**
     * nagios command = 'Reschedule Next Check' ,'Reschedule Next Check-Forced'
     * for this service
     */
    SCHEDULE_SVC_CHECK(ActionCommandsConstants.SCHEDULE_SVC_CHECK,
            new String[] { ActionCommandsConstants.SCHEDULE_SVC_CHECK,
                    ActionCommandsConstants.SCHEDULE_FORCED_SVC_CHECK },
            new String[] { NagiosCommandsConstants.SCHEDULE_SVC_CHECK,
                    NagiosCommandsConstants.SCHEDULE_FORCED_SVC_CHECK }, false);
    /**
     * Constructor
     * 
     * @param nagiosCommand
     * @param compositeCommands
     */
    private CompositeCommandsEnum(String command, String[] compositeCommands,
            String[] nagiosCommands, boolean applyToEachService) {
        this.command = command;
        this.compositeCommands = compositeCommands;
        this.nagiosCommands = nagiosCommands;
        this.applyToEachService = applyToEachService;

    }

    /**
     * Command
     */
    private String command;

    /**
     * @return command
     */
    public String getCommand() {
        return command;
    }

    /**
     * @param command
     */
    public void setCommand(String command) {
        this.command = command;
    }

    /**
     * @return compositeCommands
     */
    public String[] getCompositeCommands() {
        return compositeCommands;
    }

    /**
     * @param compositeCommands
     */
    public void setCompositeCommands(String[] compositeCommands) {
        this.compositeCommands = compositeCommands;
    }

    /**
     * Array of composite actions commands.
     */
    private String[] compositeCommands;

    /**
     * Array of composite nagios commands.
     */
    private String[] nagiosCommands;

    /**
     * @return nagiosCommands
     */
    public String[] getNagiosCommands() {
        return nagiosCommands;
    }

    /**
     * @param nagiosCommands
     */
    public void setNagiosCommands(String[] nagiosCommands) {
        this.nagiosCommands = nagiosCommands;
    }

    /**
     * This parameter indicates if the nagios command is to be applied for all
     * the services for a host. e.g. if for a host there are 4 services ,4
     * commands will be sent to nagios along with the parent command.
     */
    private boolean applyToEachService;

    /**
     * @return applyToEachService
     */
    public boolean isApplyToEachService() {
        return applyToEachService;
    }

    /**
     * @param applyToEachService
     */
    public void setApplyToEachService(boolean applyToEachService) {
        this.applyToEachService = applyToEachService;
    }
}
