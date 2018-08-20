package com.groundworkopensource.portal.statusviewer.common.actions;

/**
 * This enum holds various options for the 'Schedule Downtime' action command.
 * 
 * @author shivangi_walvekar
 */
public enum ScheduledDowntimeCommandEnum {

    /** Do nothing with child hosts. */
    DO_NOTHING_WITH_CHILD_HOSTS(
            ActionCommandsConstants.DO_NOTHING_WITH_CHILD_HOSTS,
            NagiosCommandsConstants.SCHEDULE_HOST_DOWNTIME),

    /** Schedule triggered downtime for all child hosts. */
    SCHEDULE_AND_PROPAGATE_TRIGGERED_HOST_DOWNTIME(
            ActionCommandsConstants.SCHEDULE_AND_PROPAGATE_TRIGGERED_HOST_DOWNTIME,
            NagiosCommandsConstants.SCHEDULE_AND_PROPAGATE_TRIGGERED_HOST_DOWNTIME),

    /** Schedule non-triggered downtime for all child hosts. */
    SCHEDULE_AND_PROPAGATE_HOST_DOWNTIME(
            ActionCommandsConstants.SCHEDULE_AND_PROPAGATE_HOST_DOWNTIME,
            NagiosCommandsConstants.SCHEDULE_AND_PROPAGATE_HOST_DOWNTIME);

    /**
     * Gets the action command.
     * 
     * @return actionCommand
     */
    public String getActionCommand() {
        return actionCommand;
    }

    /**
     * Sets the action command.
     * 
     * @param actionCommand
     *            the action command
     */
    public void setActionCommand(String actionCommand) {
        this.actionCommand = actionCommand;
    }

    /**
     * Gets the nagios command.
     * 
     * @return nagiosCommand
     */
    public String getNagiosCommand() {
        return nagiosCommand;
    }

    /**
     * Sets the nagios command.
     * 
     * @param nagiosCommand
     *            the nagios command
     */
    public void setNagiosCommand(String nagiosCommand) {
        this.nagiosCommand = nagiosCommand;
    }

    /** String property for action command to be displayed on UI. */
    private String actionCommand;

    /** String property for command to be sent to Nagios. */
    private String nagiosCommand;

    /**
     * Instantiates a new scheduled downtime command enum.
     * 
     * @param actionCommand
     *            the action command
     * @param nagiosCommand
     *            the nagios command
     */
    private ScheduledDowntimeCommandEnum(String actionCommand,
            String nagiosCommand) {
        this.actionCommand = actionCommand;
        this.nagiosCommand = nagiosCommand;
    }
}
