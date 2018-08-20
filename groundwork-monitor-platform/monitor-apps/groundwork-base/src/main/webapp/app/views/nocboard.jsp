<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<portlet:defineObjects />
<portlet:resourceURL var="nocboard" id="nocboard" escapeXml="false" />
<portlet:resourceURL var="addComment" id="addComment" escapeXml="false" />
<portlet:resourceURL var="deleteComment" id="deleteComment" escapeXml="false" />
<portlet:resourceURL var="postAck" id="postAck" escapeXml="false" />

<div class="panel panel-default relative" style="margin-bottom: 0px" ng-controller="NocBoardController as noc" ng-init="init('<%=renderResponse.encodeURL(nocboard.toString())%>')">
    <div class="loading-overlay" ng-show="isLoading">Loading...</div>
    <div class="form-group" style="margin-bottom: 0px">
        <div>
            <alert ng-repeat="alert in alerts" type="alert.type" close="closeAlert($index)"><strong>{{alert.msg}}</strong></alert>
        </div>
    </div>
    <accordion close-others="true" class="panel-group panel-group-noc" aria-multiselectable="true">
        <accordion-group class="panel panel-noc" is-open="isOpen" id="noc-group-<%=renderRequest.getWindowID()%>" gwp-accordion-state>
            <accordion-heading class="panel-heading">
              <span class="panel-heading-cell panel-heading-cell-expander" ng-style="{'background': getBackgroundColor()}">
                <i class="fa fa-plus"></i>
              </span>
              <span class="panel-heading-cell">
                {{serviceData.prefs.title}} - Hosts: {{hostStatusCount()}} [
                    <span><span class="host-up">{{hostUpCount()}}</span> UP<span ng-show="hostDownCount() || hostWarningCount() || hostPendingCount()">,</span></span>
                    <span><span class="host-down">{{hostDownCount()}}</span> DOWN<span ng-show="hostWarningCount() || hostPendingCount()">,</span></span>
                    <span ng-show="hostWarningCount()"><span class="host-warning">{{hostWarningCount()}}</span> WARNING<span ng-show="hostPendingCount()">,</span></span>
                    <span ng-show="hostPendingCount()"><span class="host-pending">{{hostPendingCount()}}</span> PENDING</span>
                ]
                &nbsp;&nbsp;
                Services: {{serviceStatusCount()}} [
                    <span><span class="host-up">{{serviceUpCount()}}</span> OK<span ng-show="serviceCriticalCount() || serviceWarningCount() || servicePendingCount() || serviceUnknownCount()">,</span></span>
                    <span><span class="host-down">{{serviceCriticalCount()}}</span> CRITICAL<span ng-show="serviceWarningCount() || servicePendingCount() || serviceUnknownCount()">,</span></span>
                    <span ng-show="serviceWarningCount()"><span class="host-warning">{{serviceWarningCount()}}</span> WARNING<span ng-show="servicePendingCount() || serviceUnknownCount()">,</span></span>
                    <span ng-show="servicePendingCount()"><span class="host-pending">{{servicePendingCount()}}</span> PENDING<span ng-show="serviceUnknownCount()">,</span></span>
                    <span ng-show="serviceUnknownCount()"><span class="service-unknown">{{serviceUnknownCount()}}</span> UNKNOWN</span>
                ]
                &nbsp;&nbsp;
                SLA: [<span class="sla-met" ng-style="{'color': getSLAColor()}" ng-hide="slaMet">{{isSlaMet()}}</span>MET]: {{serviceData.slaPercent| number:0}}%
              </span>
            </accordion-heading>
            <div class="panel-collapse collapse in">
                <div class="panel-body">
                    <accordion class="panel-group panel-group-filter">
                        <accordion-group class="panel panel-filter" is-open="noc.showFilters" id="noc-filter-group-<%=renderRequest.getWindowID()%>" gwp-accordion-state-special>
                            <accordion-heading class="panel-heading" ng-style="{'background': getBackgroundColor()}">
                                <span class="panel-heading-cell">
                                  SERVICE PROBLEMS: {{serviceProblemCount()}}   <button>Click to {{getShowHideFilters()}} filters</button>
                                </span>
                            </accordion-heading>
                            <div class="problem-header">
                                <div class="problem-title">SERVICE PROBLEMS</div>
                                <div class="problem-count problem-service-count" ng-style="{'font-size': getFontSize() + 'px', 'background': getBackgroundColor()}">{{serviceProblemCount()}}</div>
                            </div>
                            <div class="problem-description">
                                <div class="problem-description-title">Showing:</div>
                                <table>
                                    <tr><td>Service Status:</td><td>{{serviceStatuses()}}</td></tr>
                                    <tr><td>Downtime:</td><td>{{downtimeStatuses()}}</td></tr>
                                    <tr><td>Pending/Expired Downtime Window:</td><td>{{serviceData.prefs.downtimeHours}} hours</td></tr>
                                    <tr><td>Acknowledgment:</td><td>{{acknowledgementStatuses()}}</td></tr>
                                    <tr><td>Availability Window:</td><td>{{serviceData.prefs.availabilityHours}} hours</td></tr>
                                    <tr><td>SLA:</td><td>{{serviceData.prefs.percentageSLA}}% [<span class="sla-met" ng-style="{'color': getSLAColor()}" ng-hide="slaMet">{{isSlaMet()}}</span>MET] Actual: {{serviceData.slaPercent| number:0}}%</td></tr>
                                </table>
                            </div>
                            </accordion-group>
                        </accordion>
                    <div class="col-sm-12 no-padding">
                        <div class="gridStyleServices" ui-grid="noc.gridOptionsServices" ui-grid-pagination ui-grid-save-state></div>
                    </div>
                </div>
            </div>
        </accordion-group>
    </accordion>
</div>

<script type="text/ng-template" id="commentsModal.html">
    <div class="modal-header">
        <h3 class="modal-title" id="modal-title-comments">Comments</h3>
    </div>
    <div class="modal-body" id="modal-body-comments">
        <p ng-hide="!comments.length" ng-repeat="comment in comments"><span class="comment-author">{{comment.commentUser}}</span>&nbsp;{{comment.commentText}}<span class="comment-date">{{comment.commentDate}}</span><button class="btn btn-warning" type="button" ng-hide="!comment.commentID" ng-click="deleteComment('<%=renderResponse.encodeURL(deleteComment.toString())%>', comment.commentID)">Delete</button></p>
        <p ng-show="!comments.length">No comments yet.</p>
    </div>
    <div class="modal-footer">
        <input type="text" ng-model="comment.notes" size="40" class="comment-box" placeholder="Enter New Comment" />&nbsp;
        <button class="btn btn-primary" type="button" ng-click="postComment('<%=renderResponse.encodeURL(addComment.toString())%>')" ng-disabled="!comment.notes.length">Post</button>
        <button class="btn btn-warning" type="button" ng-click="cancel()">Close</button>
        <div class="col-md-10 col-sm-10 padding-left0">
            <alert ng-repeat="alert in alerts" type="alert.type" close="closeAlert($index)"><strong>{{alert.msg}}</strong></alert>
        </div>
    </div>
</script>

<script type="text/ng-template" id="ackModal.html">
    <div class="modal-header">
        <h3 class="modal-title" id="modal-title-ack">Acknowledge</h3>
    </div>
    <div class="modal-footer">
        <input type="text" ng-model="comment.notes" size="40" class="comment-box" placeholder="Enter Acknowledge Comment" />&nbsp;
        <button class="btn btn-primary" type="button" ng-click="postAck('<%=renderResponse.encodeURL(postAck.toString())%>')" ng-disabled="!comment.notes.length">Acknowledge</button>
        <button class="btn btn-warning" type="button" ng-click="cancel()">Close</button>
        <div class="col-md-10 col-sm-10 padding-left0">
            <alert ng-repeat="alert in alerts" type="alert.type" close="closeAlert($index)"><strong>{{alert.msg}}</strong></alert>
        </div>
    </div>
</script>