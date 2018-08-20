package com.groundworkopensource.portal.statusviewer.bean.action;

import java.io.Serializable;

import com.groundworkopensource.portal.statusviewer.common.actions.PopupComponentsEnum;

/**
 * This managed bean is used on UI to set the visibility for the various
 * components to be displayed on intermediate screen for actions portlet.
 * 
 * @author shivangi_walvekar
 * 
 */
public class VisibilityBean implements Serializable {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 4475567430279428779L;

    /**
     * property to indicate visibility of UI component serviceDesc
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
     * property to indicate visibility of panel grid for enableForHostsToo
     * components
     */
    private boolean enableForHostsToo;

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
     * property to indicate visibility of panel grid for ackHostServiceToo
     * components
     */
    private boolean ackHostServiceToo;

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
     * property to indicate visibility of panel grid for sendNotification
     * components
     */
    private boolean sendNotification;

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
     * property to indicate visibility of panel grid for persistentComment
     * components
     */
    private boolean persistentComment;

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
     * property to indicate visibility of panel grid for forceCheck components
     */
    private boolean forceCheck;

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
     * property to indicate visibility of panel grid for author components
     */
    private boolean author;

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
     * property to indicate visibility of panel grid for comment components
     */
    private boolean comment;

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
     * property to indicate visibility of panel grid for triggeredBy components
     */
    private boolean triggeredBy;

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
     * property to indicate visibility of panel grid for startTime components
     */
    private boolean startTime;

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
     * property to indicate visibility of panel grid for endTime components
     */
    private boolean endTime;

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
     * property to indicate visibility of panel grid for type components
     */
    private boolean type;

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
     * property to indicate visibility of panel grid for duration components
     */
    private boolean duration;

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
     * property to indicate visibility of panel grid for childHosts components
     */
    private boolean childHosts;

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
     * property to indicate visibility of panel grid for childHosts components
     */
    private boolean notificationDelay;

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
     * property to indicate visibility of UI component disabledForHostsToo
     */
    private boolean disabledForHostsToo;

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

    /**
     * property to indicate visibility of UI component schDowntimeForHostsToo
     */
    private boolean schDowntimeForHostsToo;

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
     * property to indicate visibility of UI component checkResult
     */
    private boolean checkResult;

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
     * property to indicate visibility of UI component checkOutput
     */
    private boolean checkOutput;

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
     * property to indicate visibility of UI component performanceData
     */
    private boolean performanceData;

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
     * This method copies the data from PopupComponentsEnum to the visibility
     * bean.
     * 
     * @param popupEnum
     */
    public void copyEnumData(PopupComponentsEnum popupEnum) {
        setCheckTime(popupEnum.isCheckTime());
        setEnableForHostsToo(popupEnum.isEnableForHostsToo());
        setAckHostServiceToo(popupEnum.isAckHostServiceToo());
        setSendNotification(popupEnum.isSendNotification());
        setPersistentComment(popupEnum.isPersistentComment());
        setForceCheck(popupEnum.isForceCheck());
        setAuthor(popupEnum.isAuthor());
        setComment(popupEnum.isComment());
        setTriggeredBy(popupEnum.isTriggeredBy());
        setStartTime(popupEnum.isStartTime());
        setEndTime(popupEnum.isEndTime());
        setType(popupEnum.isType());
        setDuration(popupEnum.isDuration());
        setChildHosts(popupEnum.isChildHosts());
        setDisabledForHostsToo(popupEnum.isDisabledForHostsToo());
        setNotificationDelay(popupEnum.isNotificationDelay());
        setSchDowntimeForHostsToo(popupEnum.isSchDowntimeForHostsToo());
        setCheckResult(popupEnum.isCheckResult());
        setCheckOutput(popupEnum.isCheckOutput());
        setPerformanceData(popupEnum.isPerformanceData());
        setHostName(popupEnum.isHostName());
        setCommandDesc(popupEnum.getCommandDesc());
    }
}
