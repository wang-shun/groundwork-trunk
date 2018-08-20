package com.groundworkopensource.portal.statusviewer.common.actions;

/**
 * This enum defines the visibility of the UI components to be displayed on the
 * intermediate screens of actions portlet.
 * 
 * @author shivangi_walvekar
 * 
 */
public enum PopupComponentsEnum {
    /**
     * Enum for the visibility of components for the menu 'Acknowledge This Host
     * Problem'
     */
    ACK_HOST_PROB(false, false, false, true, true, true, false, true, true,
            false, false, false, false, false, false, false, false, false,
            false, false, false, false,
            CommandDescriptionConstants.ACK_HOST_PROB),
    /**
     * Enum for the visibility of components for the menu 'Remove Acknowledgment
     * of Problem'
     */
    REM_ACK_PROB(false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false,
            false, false, false, false,
            CommandDescriptionConstants.REM_ACK_PROB),
    /**
     * Enum for the visibility of components for the menu 'Acknowledge Problem'
     * for a service
     */
    ACK_SVC_PROB(true, false, false, false, true, true, false, true, true,
            false, false, false, false, false, false, false, false, false,
            false, false, false, true, CommandDescriptionConstants.ACK_SVC_PROB),
    /**
     * Enum for the visibility of components for the menu 'Remove Problem
     * Acknowledgment' for a service
     */
    REM_SVC_ACK(true, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false,
            false, false, false, true, CommandDescriptionConstants.REM_SVC_ACK),
    /**
     * Enum for the visibility of components for the menu 'enable notifications
     * for all Hosts' in host group
     */
    ENABLE_HOSTGROUP_HOST_NOTIFICATIONS(false, false, false, false, false,
            false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false,
            CommandDescriptionConstants.ENABLE_HOSTGROUP_HOST_NOTIFICATIONS),
    /**
     * Enum for the visibility of components for the menu 'Disable notifications
     * for all Hosts' in host group
     */
    DISABLE_HOSTGROUP_HOST_NOTIFICATIONS(false, false, false, false, false,
            false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false,
            CommandDescriptionConstants.DISABLE_HOSTGROUP_HOST_NOTIFICATIONS),
    /**
     * Enum for the visibility of components for the menu 'Enable notifications
     * for all Services' in host group
     */
    ENABLE_HOSTGROUP_SVC_NOTIFICATIONS(false, false, true, false, false, false,
            false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false,
            CommandDescriptionConstants.ENABLE_HOSTGROUP_SVC_NOTIFICATIONS),
    /**
     * Enum for the visibility of components for the menu 'Disable notifications
     * for all Services' in host group
     */
    DISABLE_HOSTGROUP_SVC_NOTIFICATIONS(false, false, false, false, false,
            false, false, false, false, false, false, false, false, false,
            false, true, false, false, false, false, false, false,
            CommandDescriptionConstants.DISABLE_HOSTGROUP_SVC_NOTIFICATIONS),
    /**
     * Enum for the visibility of components for the menu 'Enable Notifications'
     * for host.
     */
    ENABLE_HOST_NOTIFICATIONS(false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false,
            CommandDescriptionConstants.ENABLE_HOST_NOTIFICATIONS),
    /**
     * Enum for the visibility of components for the menu 'Delay next
     * Notification' for host.
     */
    DELAY_HOST_NOTIFICATION(false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false,
            true, false, false, false, false, false,
            CommandDescriptionConstants.DELAY_HOST_NOTIFICATION),
    /**
     * Enum for the visibility of components for the menu 'Disable Notifications
     * for All Services on Host'
     */
    DISABLE_HOST_SVC_NOTIFICATIONS(false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false,
            true, false, false, false, false, false, false,
            CommandDescriptionConstants.DISABLE_HOST_SVC_NOTIFICATIONS),
    /**
     * Enum for the visibility of components for the menu 'Disable
     * Notifications' for Host'
     */
    DISABLE_HOST_NOTIFICATIONS(false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false,
            CommandDescriptionConstants.DISABLE_HOST_NOTIFICATIONS),
    /**
     * Enum for the visibility of components for the menu 'Enable Notifications
     * for All Services on Host'
     */
    ENABLE_HOST_SVC_NOTIFICATIONS(false, false, true, false, false, false,
            false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false,
            CommandDescriptionConstants.ENABLE_HOST_SVC_NOTIFICATIONS),
    /**
     * Enum for the visibility of components for the menu 'Enable notifications
     * for all Services' in Service group
     */
    ENABLE_SERVICEGROUP_SVC_NOTIFICATIONS(false, false, false, false, false,
            false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false,
            CommandDescriptionConstants.ENABLE_SERVICEGROUP_SVC_NOTIFICATIONS),
    /**
     * Enum for the visibility of components for the menu 'Disable notifications
     * for all Services' in Service group
     */
    DISABLE_SERVICEGROUP_SVC_NOTIFICATIONS(false, false, false, false, false,
            false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false,
            CommandDescriptionConstants.DISABLE_SERVICEGROUP_SVC_NOTIFICATIONS),
    /**
     * Enum for the visibility of components for the menu 'Disable
     * Notifications' for Service
     */
    DISABLE_SVC_NOTIFICATIONS(true, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, true,
            CommandDescriptionConstants.DISABLE_SVC_NOTIFICATIONS),
    /**
     * Enum for the visibility of components for the menu 'Delay Next
     * Notification' for Service
     */
    DELAY_SVC_NOTIFICATION(true, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false,
            true, false, false, false, false, true,
            CommandDescriptionConstants.DELAY_SVC_NOTIFICATION),
    /**
     * Enum for the visibility of components for the menu 'Enable Notifications'
     * for Service
     */
    ENABLE_SVC_NOTIFICATIONS(true, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, true,
            CommandDescriptionConstants.ENABLE_SVC_NOTIFICATIONS),
    /**
     * Enum for the visibility of components for the menu 'Schedule Downtime For
     * All Hosts' in a host group
     */
    SCHEDULE_HOSTGROUP_HOST_DOWNTIME(false, false, false, false, false, false,
            false, true, true, false, true, true, true, true, false, false,
            false, false, false, false, false, false,
            CommandDescriptionConstants.SCHEDULE_HOSTGROUP_HOST_DOWNTIME),
    /**
     * Enum for the visibility of components for the menu 'Schedule Downtime For
     * All Services' in a host group
     */
    SCHEDULE_HOSTGROUP_SVC_DOWNTIME(false, false, false, false, false, false,
            false, true, true, false, true, true, true, true, false, false,
            false, true, false, false, false, false,
            CommandDescriptionConstants.SCHEDULE_HOSTGROUP_SVC_DOWNTIME),

    /**
     * Enum for the visibility of components for the menu 'Schedule Downtime'
     * for host
     */
    SCHEDULE_HOST_DOWNTIME(false, false, false, false, false, false, false,
            true, true, true, true, true, true, true, true, false, false,
            false, false, false, false, false,
            CommandDescriptionConstants.SCHEDULE_HOST_DOWNTIME),
    /**
     * Enum for the visibility of components for the menu 'Schedule Downtime For
     * All Services' in service group
     */
    SCHEDULE_SERVICEGROUP_SVC_DOWNTIME(false, false, false, false, false,
            false, false, true, true, false, true, true, true, true, false,
            false, false, false, false, false, false, false,
            CommandDescriptionConstants.SCHEDULE_SERVICEGROUP_SVC_DOWNTIME),
    /**
     * Enum for the visibility of components for the menu 'Schedule Downtime For
     * This Service'
     */
    SCHEDULE_SVC_DOWNTIME(true, false, false, false, false, false, false, true,
            true, true, true, true, true, true, false, false, false, false,
            false, false, false, true,
            CommandDescriptionConstants.SCHEDULE_SVC_DOWNTIME),
    /**
     * Enum for the visibility of components for the menu 'Enable checks for all
     * Services' in host group
     */
    ENABLE_HOSTGROUP_SVC_CHECKS(false, false, true, false, false, false, false,
            false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false,
            CommandDescriptionConstants.ENABLE_HOSTGROUP_SVC_CHECKS),
    /**
     * Enum for the visibility of components for the menu 'Disable active checks
     * for all Services' in host group
     */
    DISABLE_HOSTGROUP_SVC_CHECKS(false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false,
            true, false, false, false, false, false, false,
            CommandDescriptionConstants.DISABLE_HOSTGROUP_SVC_CHECKS),
    /**
     * Enum for the visibility of components for the menu 'Disable Checks On
     * This Host'
     */
    DISABLE_HOST_CHECK(false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false,
            false, false, false, false, false,
            CommandDescriptionConstants.DISABLE_HOST_CHECK),
    /**
     * Enum for the visibility of components for the menu 'Enable Checks On This
     * Host'
     */
    ENABLE_HOST_CHECK(false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false,
            false, false, false, false, false,
            CommandDescriptionConstants.ENABLE_HOST_CHECK),
    /**
     * Enum for the visibility of components for the menu 'Enable Passive
     * Checks' for this host
     */
    ENABLE_PASSIVE_HOST_CHECKS(false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false,
            CommandDescriptionConstants.ENABLE_PASSIVE_HOST_CHECKS),
    /**
     * Enum for the visibility of components for the menu 'Disable Passive
     * Checks' for this host
     */
    DISABLE_PASSIVE_HOST_CHECKS(false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false,
            CommandDescriptionConstants.DISABLE_PASSIVE_HOST_CHECKS),
    /**
     * Enum for the visibility of components for the menu 'Disable Active Checks
     * for All Services on Host'
     */
    DISABLE_HOST_SVC_CHECKS(false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, true,
            false, false, false, false, false, false,
            CommandDescriptionConstants.DISABLE_HOST_SVC_CHECKS),
    /**
     * Enum for the visibility of components for the menu 'Enable Active Checks
     * for All Services on Host'
     */
    ENABLE_HOST_SVC_CHECKS(false, false, true, false, false, false, false,
            false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false,
            CommandDescriptionConstants.ENABLE_HOST_SVC_CHECKS),

    // /**
    // * Enum for the visibility of components for the menu 'Start Obsessing
    // Over
    // * This Host'
    // */
    // START_OBSESSING_OVER_HOST(false, false, false, false, false, false,
    // false,
    // false, false, false, false, false, false, false, false, false,
    // false, false, false, false, false, false,
    // CommandDescriptionConstants.START_OBSESSING_OVER_HOST),
    // /**
    // * Enum for the visibility of components for the menu 'Stop Obsessing Over
    // * This Host'
    // */
    // STOP_OBSESSING_OVER_HOST(false, false, false, false, false, false, false,
    // false, false, false, false, false, false, false, false, false,
    // false, false, false, false, false, false,
    // CommandDescriptionConstants.STOP_OBSESSING_OVER_HOST),

    /**
     * Enum for the visibility of components for the menu 'Enable Flap
     * Detection' for this host
     */
    ENABLE_HOST_FLAP_DETECTION(false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false,
            CommandDescriptionConstants.ENABLE_HOST_FLAP_DETECTION),
    /**
     * Enum for the visibility of components for the menu 'Disable Flap
     * Detection' for this host
     */
    DISABLE_HOST_FLAP_DETECTION(false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false,
            CommandDescriptionConstants.DISABLE_HOST_FLAP_DETECTION),
    /**
     * Enum for the visibility of components for the menu 'Enable checks for all
     * Services' in this service group
     */
    ENABLE_SERVICEGROUP_SVC_CHECKS(false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false,
            CommandDescriptionConstants.ENABLE_SERVICEGROUP_SVC_CHECKS),
    /**
     * Enum for the visibility of components for the menu 'Disable checks for
     * all Services' in this service group
     */
    DISABLE_SERVICEGROUP_SVC_CHECKS(false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false,
            CommandDescriptionConstants.DISABLE_SERVICEGROUP_SVC_CHECKS),
    /**
     * Enum for the visibility of components for the menu 'Disable Checks On
     * This Service'
     */
    DISABLE_SVC_CHECK(true, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false,
            false, false, false, false, true,
            CommandDescriptionConstants.DISABLE_SVC_CHECK),
    /**
     * Enum for the visibility of components for the menu 'Enable Checks' for
     * this service
     */
    ENABLE_SVC_CHECK(true, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false,
            false, false, false, false, true,
            CommandDescriptionConstants.ENABLE_SVC_CHECK),
    /**
     * Enum for the visibility of components for the menu 'Disable Passive
     * Checks' for this service
     */
    DISABLE_PASSIVE_SVC_CHECKS(true, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, true,
            CommandDescriptionConstants.DISABLE_PASSIVE_SVC_CHECKS),
    /**
     * Enum for the visibility of components for the menu 'Enable Passive
     * Checks' for this service
     */
    ENABLE_PASSIVE_SVC_CHECKS(true, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, true,
            CommandDescriptionConstants.ENABLE_PASSIVE_SVC_CHECKS),
    /**
     * Enum for the visibility of components for the menu 'Disable Flap
     * Detection' for this service
     */
    DISABLE_SVC_FLAP_DETECTION(true, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, true,
            CommandDescriptionConstants.DISABLE_SVC_FLAP_DETECTION),
    /**
     * Enum for the visibility of components for the menu 'Enable Flap
     * Detection' for this service
     */
    ENABLE_SVC_FLAP_DETECTION(true, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, true,
            CommandDescriptionConstants.ENABLE_SVC_FLAP_DETECTION),
    /**
     * Enum for the visibility of components for the menu 'Enable Event Handler'
     * for this host
     */
    ENABLE_HOST_EVENT_HANDLER(false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false,
            CommandDescriptionConstants.ENABLE_HOST_EVENT_HANDLER),
    /**
     * Enum for the visibility of components for the menu 'Disable Event
     * Handler' for this host
     */
    DISABLE_HOST_EVENT_HANDLER(false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false,
            CommandDescriptionConstants.DISABLE_HOST_EVENT_HANDLER),
    /**
     * Enum for the visibility of components for the menu 'Disable Event
     * Handler' for this service
     */
    DISABLE_SVC_EVENT_HANDLER(true, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, true,
            CommandDescriptionConstants.DISABLE_SVC_EVENT_HANDLER),
    /**
     * Enum for the visibility of components for the menu 'Enable Event Handler'
     * for this service
     */
    ENABLE_SVC_EVENT_HANDLER(true, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, true,
            CommandDescriptionConstants.ENABLE_SVC_EVENT_HANDLER),
    /**
     * Enum for the visibility of components for the menu 'Re-Schedule the Next
     * Check' for this host
     */
    SCHEDULE_HOST_CHECK(false, true, false, false, false, false, true, false,
            false, false, false, false, false, false, false, false, false,
            false, false, false, false, false,
            CommandDescriptionConstants.SCHEDULE_HOST_CHECK),
    /**
     * Enum for the visibility of components for the menu 'Schedule Check For
     * All Services Of This Host'
     */
    SCHEDULE_HOST_SVC_CHECKS(false, true, false, false, false, false, true,
            false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false,
            CommandDescriptionConstants.SCHEDULE_HOST_SVC_CHECKS),
    /**
     * Enum for the visibility of components for the menu 'Submit Passive Check
     * Result' for this host
     */
    PROCESS_HOST_CHECK_RESULT(false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false,
            false, false, true, true, true, false,
            CommandDescriptionConstants.PROCESS_HOST_CHECK_RESULT),
    /**
     * Enum for the visibility of components for the menu 'Submit Passive Check
     * Result' for this service
     */
    PROCESS_SERVICE_CHECK_RESULT(true, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false,
            false, false, false, true, true, true, true,
            CommandDescriptionConstants.PROCESS_SERVICE_CHECK_RESULT),
    /**
     * Enum for the visibility of components for the menu 'Reschedule Next
     * Check' for this service
     */
    SCHEDULE_SVC_CHECK(true, true, false, false, false, false, true, false,
            false, false, false, false, false, false, false, false, false,
            false, false, false, false, true,
            CommandDescriptionConstants.SCHEDULE_SVC_CHECK);

    /**
     * String property for command description to be displayed on UI.
     */
    private String commandDesc;

    /**
     * @return commandDesc
     */
    public String getCommandDesc() {
        return commandDesc;
    }

    /**
     * @param commandDesc
     */
    public void setCommandDesc(String commandDesc) {
        this.commandDesc = commandDesc;
    }

    /**
     * @return checkTime
     */
    public boolean isCheckTime() {
        return checkTime;
    }

    /**
     * @param checkTime
     */
    public void setCheckTime(boolean checkTime) {
        this.checkTime = checkTime;
    }

    /**
     * @return enableForHostsToo
     */
    public boolean isEnableForHostsToo() {
        return enableForHostsToo;
    }

    /**
     * @param enableForHostsToo
     */
    public void setEnableForHostsToo(boolean enableForHostsToo) {
        this.enableForHostsToo = enableForHostsToo;
    }

    /**
     * @return ackHostServiceToo
     */
    public boolean isAckHostServiceToo() {
        return ackHostServiceToo;
    }

    /**
     * @param ackHostServiceToo
     */
    public void setAckHostServiceToo(boolean ackHostServiceToo) {
        this.ackHostServiceToo = ackHostServiceToo;
    }

    /**
     * @return sendNotification
     */
    public boolean isSendNotification() {
        return sendNotification;
    }

    /**
     * @param sendNotification
     */
    public void setSendNotification(boolean sendNotification) {
        this.sendNotification = sendNotification;
    }

    /**
     * @return persistentComment
     */
    public boolean isPersistentComment() {
        return persistentComment;
    }

    /**
     * @param persistentComment
     */
    public void setPersistentComment(boolean persistentComment) {
        this.persistentComment = persistentComment;
    }

    /**
     * @return forceCheck
     */
    public boolean isForceCheck() {
        return forceCheck;
    }

    /**
     * @param forceCheck
     */
    public void setForceCheck(boolean forceCheck) {
        this.forceCheck = forceCheck;
    }

    /**
     * @return author
     */
    public boolean isAuthor() {
        return author;
    }

    /**
     * @param author
     */
    public void setAuthor(boolean author) {
        this.author = author;
    }

    /**
     * @return comment
     */
    public boolean isComment() {
        return comment;
    }

    /**
     * @param comment
     */
    public void setComment(boolean comment) {
        this.comment = comment;
    }

    /**
     * @return triggeredBy
     */
    public boolean isTriggeredBy() {
        return triggeredBy;
    }

    /**
     * @param triggeredBy
     */
    public void setTriggeredBy(boolean triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    /**
     * @return startTime
     */
    public boolean isStartTime() {
        return startTime;
    }

    /**
     * @param startTime
     */
    public void setStartTime(boolean startTime) {
        this.startTime = startTime;
    }

    /**
     * @return endTime
     */
    public boolean isEndTime() {
        return endTime;
    }

    /**
     * @param endTime
     */
    public void setEndTime(boolean endTime) {
        this.endTime = endTime;
    }

    /**
     * @return type
     */
    public boolean isType() {
        return type;
    }

    /**
     * @param type
     */
    public void setType(boolean type) {
        this.type = type;
    }

    /**
     * @return duration
     */
    public boolean isDuration() {
        return duration;
    }

    /**
     * @param duration
     */
    public void setDuration(boolean duration) {
        this.duration = duration;
    }

    /**
     * @return childHosts
     */
    public boolean isChildHosts() {
        return childHosts;
    }

    /**
     * @param childHosts
     */
    public void setChildHosts(boolean childHosts) {
        this.childHosts = childHosts;
    }

    /**
     * @return notificationDelay
     */
    public boolean isNotificationDelay() {
        return notificationDelay;
    }

    /**
     * @param notificationDelay
     */
    public void setNotificationDelay(boolean notificationDelay) {
        this.notificationDelay = notificationDelay;
    }

    /**
     * @return schDowntimeForHostsToo
     */
    public boolean isSchDowntimeForHostsToo() {
        return schDowntimeForHostsToo;
    }

    /**
     * @param schDowntimeForHostsToo
     */
    public void setSchDowntimeForHostsToo(boolean schDowntimeForHostsToo) {
        this.schDowntimeForHostsToo = schDowntimeForHostsToo;
    }

    /**
     * property to indicate visibility of UI component txtServiceDesc
     */
    private boolean serviceDesc;

    /**
     * property to indicate visibility of UI component hostName
     */
    private boolean hostName;

    /**
     * @return hostName
     */
    public boolean isHostName() {
        return hostName;
    }

    /**
     * @param hostName
     */
    public void setHostName(boolean hostName) {
        this.hostName = hostName;
    }

    /**
     * @return serviceDesc
     */
    public boolean isServiceDesc() {
        return serviceDesc;
    }

    /**
     * @param serviceDesc
     */
    public void setServiceDesc(boolean serviceDesc) {
        this.serviceDesc = serviceDesc;
    }

    /**
     * property to indicate visibility of panel grid for checkTime components
     */
    private boolean checkTime;

    /**
     * property to indicate visibility of panel grid for enableForHostsToo
     * components
     */
    private boolean enableForHostsToo;

    /**
     * property to indicate visibility of panel grid for ackHostServiceToo
     * components
     */
    private boolean ackHostServiceToo;

    /**
     * property to indicate visibility of panel grid for sendNotification
     * components
     */
    private boolean sendNotification;

    /**
     * property to indicate visibility of panel grid for persistentComment
     * components
     */
    private boolean persistentComment;

    /**
     * property to indicate visibility of panel grid for forceCheck components
     */
    private boolean forceCheck;

    /**
     * property to indicate visibility of panel grid for author components
     */
    private boolean author;

    /**
     * property to indicate visibility of panel grid for comment components
     */
    private boolean comment;

    /**
     * property to indicate visibility of panel grid for triggeredBy components
     */
    private boolean triggeredBy;

    /**
     * property to indicate visibility of panel grid for startTime components
     */
    private boolean startTime;

    /**
     * property to indicate visibility of panel grid for endTime components
     */
    private boolean endTime;

    /**
     * property to indicate visibility of panel grid for type components
     */
    private boolean type;

    /**
     * property to indicate visibility of panel grid for duration components
     */
    private boolean duration;

    /**
     * property to indicate visibility of panel grid for childHosts components
     */
    private boolean childHosts;
    /**
     * property to indicate visibility of panel grid for notificationDelay
     * components
     */
    private boolean notificationDelay;

    /**
     * property to indicate visibility of UI component disabledForHostsToo
     */
    private boolean disabledForHostsToo;

    /**
     * property to indicate visibility of UI component schDowntimeForHostsToo
     */
    private boolean schDowntimeForHostsToo;

    /**
     * property to indicate visibility of UI component checkResult
     */
    private boolean checkResult;

    /**
     * property to indicate visibility of UI component checkOutput
     */
    private boolean checkOutput;

    /**
     * property to indicate visibility of UI component performanceData
     */
    private boolean performanceData;

    /**
     * @return checkResult
     */
    public boolean isCheckResult() {
        return checkResult;
    }

    /**
     * @param checkResult
     */
    public void setCheckResult(boolean checkResult) {
        this.checkResult = checkResult;
    }

    /**
     * @return checkOutput
     */
    public boolean isCheckOutput() {
        return checkOutput;
    }

    /**
     * @param checkOutput
     */
    public void setCheckOutput(boolean checkOutput) {
        this.checkOutput = checkOutput;
    }

    /**
     * @return performanceData
     */
    public boolean isPerformanceData() {
        return performanceData;
    }

    /**
     * @param performanceData
     */
    public void setPerformanceData(boolean performanceData) {
        this.performanceData = performanceData;
    }

    /**
     * 
     * @param serviceDesc
     * @param checkTime
     * @param enableForHostsToo
     * @param ackHostServiceToo
     * @param sendNotification
     * @param persistentComment
     * @param forceCheck
     * @param author
     * @param comment
     * @param triggeredBy
     * @param startTime
     * @param endTime
     * @param type
     * @param duration
     * @param childHosts
     * @param disabledForHostsToo
     * @param notificationDelay
     * @param schDowntimeForHostsToo
     * @param checkResult
     * @param checkOutput
     * @param performanceData
     * @param hostName
     * @param commandDesc
     */
    private PopupComponentsEnum(boolean serviceDesc, boolean checkTime,
            boolean enableForHostsToo, boolean ackHostServiceToo,
            boolean sendNotification, boolean persistentComment,
            boolean forceCheck, boolean author, boolean comment,
            boolean triggeredBy, boolean startTime, boolean endTime,
            boolean type, boolean duration, boolean childHosts,
            boolean disabledForHostsToo, boolean notificationDelay,
            boolean schDowntimeForHostsToo, boolean checkResult,
            boolean checkOutput, boolean performanceData, boolean hostName,
            String commandDesc) {
        this.serviceDesc = serviceDesc;
        this.checkTime = checkTime;
        this.enableForHostsToo = enableForHostsToo;
        this.ackHostServiceToo = ackHostServiceToo;
        this.sendNotification = sendNotification;
        this.persistentComment = persistentComment;
        this.forceCheck = forceCheck;
        this.author = author;
        this.comment = comment;
        this.triggeredBy = triggeredBy;
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type;
        this.duration = duration;
        this.childHosts = childHosts;
        this.disabledForHostsToo = disabledForHostsToo;
        this.notificationDelay = notificationDelay;
        this.schDowntimeForHostsToo = schDowntimeForHostsToo;
        this.checkResult = checkResult;
        this.checkOutput = checkOutput;
        this.performanceData = performanceData;
        this.hostName = hostName;
        this.commandDesc = commandDesc;
    }

    /**
     * @return disabledForHostsToo
     */
    public boolean isDisabledForHostsToo() {
        return disabledForHostsToo;
    }

    /**
     * @param disabledForHostsToo
     */
    public void setDisabledForHostsToo(boolean disabledForHostsToo) {
        this.disabledForHostsToo = disabledForHostsToo;
    }

}
