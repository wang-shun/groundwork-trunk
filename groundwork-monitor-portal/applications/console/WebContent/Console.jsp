<!--
   Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork)
   All rights reserved. Use is subject to GroundWork commercial license terms.
-->

<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page"
	xmlns:f="http://java.sun.com/jsf/core"
	xmlns:h="http://java.sun.com/jsf/html"
	xmlns:ice="http://www.icesoft.com/icefaces/component">
	<f:view>
		<ice:outputDeclaration doctypeRoot="HTML"
			doctypePublic="-//W3C//DTD HTML 4.01 Transitional//EN"
			doctypeSystem="http://www.w3.org/TR/html4/loose.dtd" />

		<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"></meta>
<title>GW Console</title>
<f:loadBundle basename="#{locale.baseName}" var="msg" />
</head>
<body>

	<ice:portlet>

		<div id="jquery_jplayer"></div>

		<div class="bodyWrapper clearfix">
			<ice:panelGroup styleClass="#{console.layoutWrapperStyleClass}">

				<ice:panelGroup styleClass="sideBarWrapper">
					<ice:form id="naviPanel" partialSubmit="true">
						<!--		<div id="filterSideBar" class="sideBarWrapper">-->
						<!--						<div class="sideBarPanelWrapper">-->
						<ice:panelGroup styleClass="sideBarPanelWrapper">

							<div class="treeWrapper">
								<ice:panelGroup effect="#{console.effect}">
									<ice:panelCollapsible id="panelSystemFilter" expanded="true"
										styleClass="navPnlClpsbl">
										<f:facet name="header">
											<ice:panelGroup>
												<ice:outputText id="panelHeaderSystem"
													value="#{msg.com_groundwork_console_navigation_system_filters}" />
											</ice:panelGroup>
										</f:facet>
										<ice:panelGroup>
											<ice:commandLink
												value="#{msg.com_groundwork_console_content_tab_default}"
												action="#{consoleMgr.populateAllOpenEvents}"
												styleClass="#{console.allEventsStyleClass}" />
											<ice:tree id="systemFilterTree"
												value="#{filterTreeBean.model}" var="item"
												hideRootNode="false" hideNavigation="false">
												<ice:treeNode>
													<f:facet name="icon">
														<ice:panelGroup style="display: inline">
															<ice:graphicImage value="#{item.userObject.icon}" />
															<ice:commandLink value="#{item.userObject.text}"
																styleClass="#{item.userObject.styleClass}"
																actionListener="#{item.userObject.nodeClicked}" />

														</ice:panelGroup>
													</f:facet>
												</ice:treeNode>
											</ice:tree>
										</ice:panelGroup>
									</ice:panelCollapsible>
									<br />
									<ice:panelCollapsible id="PanelPublicFilter" expanded="true"
										styleClass="navPnlClpsbl">
										<f:facet name="header">
											<ice:panelGroup>
												<ice:outputText id="panelHeaderPub"
													value="#{msg.com_groundwork_console_navigation_public_filters}" />
											</ice:panelGroup>
										</f:facet>

										<ice:panelGroup width="100%" height="1000px">
											<ice:tree id="publicFilterTree"
												value="#{publicFilterTreeBean.model}" var="item"
												hideRootNode="false" hideNavigation="false">
												<ice:treeNode>
													<f:facet name="icon">
														<ice:panelGroup style="display:inline;">
															<ice:graphicImage value="#{item.userObject.icon}" />
															<ice:commandLink value="#{item.userObject.text}"
																styleClass="#{item.userObject.styleClass}"
																actionListener="#{item.userObject.nodeClicked}" />
															<ice:commandLink
																action="#{publicFilterTreeBean.refreshFilter}"
																rendered="#{item.userObject.text eq 'Filter Events'}">
																	   (  <ice:graphicImage
																	value="#{publicFilterTreeBean.refreshImg}"
																	title="header=[] body=[Reload Filters]"
																	style="margin:0px 0px -4px 0px; " />
																<ice:outputText id="refreshPubFilters"
																	value="#{msg.com_groundwork_console_navigation_public_refresh_filters}"
																	style="font-weight:bold;" />  )
																	    </ice:commandLink>
														</ice:panelGroup>
													</f:facet>
												</ice:treeNode>
											</ice:tree>
										</ice:panelGroup>
									</ice:panelCollapsible>
									<br />
									<ice:panelCollapsible id="EventTile" expanded="true"
										styleClass="navPnlClpsbl">
										<f:facet name="header">
											<ice:panelGroup>
												<ice:outputText id="panelHeaderTile"
													value="#{msg.com_groundwork_console_navigation_show_eventtile}" />
											</ice:panelGroup>
										</f:facet>
										<ice:panelGroup>
											<ice:commandLink
												value="#{msg.com_groundwork_console_navigation_show_eventtile_popup}"
												actionListener="#{consoleMgr.showEventTile}" />
										</ice:panelGroup>
									</ice:panelCollapsible>
								</ice:panelGroup>
							</div>

							<div class="sideBarPanelWrapperLowerRight"></div>

						</ice:panelGroup>

						<!--				</div>-->
						<div class="sideBarBgUpperRight"></div>
						<div class="sideBarBgLowerRight"></div>
						<ice:panelGroup styleClass="sideBarCollapseIcon">

							<ice:commandButton id="hideTree" image="#{console.sideBarArrow}"
								actionListener="#{console.hideTree}" border="0"
								title="#{console.tooltip}" />

						</ice:panelGroup>

					</ice:form>
				</ice:panelGroup>
				<div class="consoleWrapper">
					<div class="consoleTabsWrapper">
						<ice:form id="contentPanel" style="overflow:auto;"
							partialSubmit="true">

							<ice:inputHidden value="#{tabset.hiddenField}"></ice:inputHidden>
							<ice:panelTabSet id="icepnltabset" var="currentTab"
								value="#{tabset.tabs}" selectedIndex="#{tabset.tabIndex}"
								tabChangeListener="#{tabset.tabSelection}">

								<ice:panelTab id="icepnltab" title="#{currentTab.label}">
									<f:facet name="label">
										<ice:panelGrid columns="2">
											<ice:outputText value="#{currentTab.label}"></ice:outputText>
											<ice:commandButton
												style="background:none;border:none;margin:0 0 0 5px;"
												disabled="#{tabset.tabSize == 2}" image="/images/delete.png"
												actionListener="#{tabset.closeTab}"
												value="#{currentTab.tabId}"
												rendered="#{tabset.tabSize > 2 and msg.com_groundwork_console_content_tab_new != currentTab.label }">
											</ice:commandButton>
										</ice:panelGrid>
									</f:facet>
									<div class="tabContent">
										<ice:panelGroup styleClass="tabContentHeader"
											style="#{console.searchPanelStyle}">

											<div class="tabContentForm searchEventsForm">
												<div class="formHeader">
													<p>
														<label for="searchEvents_hosts"><ice:outputText
																id="search_title"
																value="#{msg.com_groundwork_console_content_search_title}" /></label>
													</p>
												</div>

												<div class="formLine">
													<label for="searchEvents_hosts"><ice:outputText
															id="device"
															value="#{msg.com_groundwork_console_content_search_device}" /></label>
													<ice:inputText id="searchEvents_hosts"
														value="#{currentTab.searchCriteria.host}"
														styleClass="text" partialSubmit="true"
														action="#{consoleMgr.performSearch}" maxlength="500" />
												</div>
												<div class="formLine">
													<label for="searchEvents_messages"><ice:outputText
															id="messages"
															value="#{msg.com_groundwork_console_content_search_messages}" /></label>
													<ice:inputText id="searchEvents_messages"
														value="#{currentTab.searchCriteria.message}"
														styleClass="text" partialSubmit="true"
														action="#{consoleMgr.performSearch}" maxlength="500">
													</ice:inputText>
												</div>
												<div class="formLine">
													<label for="searchEvents_severity"><ice:outputText
															id="severity"
															value="#{msg.com_groundwork_console_content_search_severity}" /></label>
													<ice:selectOneMenu id="searchEvents_severity"
														value="#{currentTab.searchCriteria.severity}"
														partialSubmit="true">
														<f:selectItems id="SlctSeverityItms"
															value="#{currentTab.searchCriteria.severityItems}" />
													</ice:selectOneMenu>
												</div>
												<div class="formLine">
													<label for="searchEvents_opStatus"><ice:outputText
															id="opStatus"
															value="#{msg.com_groundwork_console_content_search_opStatus}" /></label>
													<ice:selectOneMenu id="searchEvents_opStatus"
														value="#{currentTab.searchCriteria.opStatus}"
														partialSubmit="true">
														<f:selectItems id="SlctOpStatusItms"
															value="#{currentTab.searchCriteria.opStatusItems}" />
													</ice:selectOneMenu>
												</div>
												<div class="formLine">
													<label for="searchEvents_monStatus"><ice:outputText
															id="monStatus"
															value="#{msg.com_groundwork_console_content_search_monStatus}" /></label>
													<ice:selectOneMenu id="searchEvents_monStatus"
														value="#{currentTab.searchCriteria.monStatus}"
														partialSubmit="true">
														<f:selectItems id="SlctMonStatusItms"
															value="#{currentTab.searchCriteria.monitorStatusItems}" />
													</ice:selectOneMenu>
												</div>


											</div>

											<div class="tabContentForm dateRangeForm">
												<div class="formHeader">
													<p>
														<ice:outputText id="datetimerange"
															value="#{msg.com_groundwork_console_content_search_datetime}" />
													</p>
												</div>

												<div class="formLine" style="width: 30%;">
													<ice:selectOneRadio id="dateTimeRange_preset"
														value="#{currentTab.searchCriteria.ageType}"
														layout="pageDirection" partialSubmit="true">
														<f:selectItem itemValue="preset"
															itemLabel="#{msg.com_groundwork_console_content_search_preset }" />
														<f:selectItem itemValue="custom"
															itemLabel="#{msg.com_groundwork_console_content_search_custom}" />
													</ice:selectOneRadio>
												</div>
												<!--						</div>-->
												<!--<div class="formLineSmall"></div>
						-->

												<table>


													<tr>
														<td><br />
															<div class="formLine">
																<ice:selectOneMenu id="ageValue1"
																	value="#{currentTab.searchCriteria.presetValue}">
																	<f:selectItem itemValue="none" itemLabel="NONE" />
																	<f:selectItem itemValue="last10min"
																		itemLabel="LAST 10 MINS" />
																	<f:selectItem itemValue="last30min"
																		itemLabel="LAST 30 MINS" />
																	<f:selectItem itemValue="lasthr" itemLabel="LAST HOUR" />
																	<f:selectItem itemValue="last6hr"
																		itemLabel="LAST 6 HOURS" />
																	<f:selectItem itemValue="last12hr"
																		itemLabel="LAST 12 HOURS" />
																	<f:selectItem itemValue="last24hr"
																		itemLabel="LAST 24 HOURS" />
																</ice:selectOneMenu>
															</div></td>



													</tr>

													<tr>
														<td>
															<div class="formLine">
																<ice:selectInputDate id="date1"
																	value="#{currentTab.searchCriteria.ageValueFrom}"
																	imageDir="/xmlhttp/css/xp/css-images/"
																	renderAsPopup="true" styleClass="text" />
																<ice:selectInputDate id="date2"
																	value="#{currentTab.searchCriteria.ageValueTo}"
																	imageDir="/xmlhttp/css/xp/css-images/"
																	renderAsPopup="true" styleClass="text" />
															</div>
														</td>

													</tr>

												</table>





												<div class="tabContentForm updateLabelForm">
													<div class="formHeader">
														<p>
															<ice:outputText id="updatelabelheader"
																value="#{msg.com_groundwork_console_content_search_header_updatelabel}" />
														</p>
													</div>

													<div class="formLine">
														<ice:inputText id="update_label"
															value="#{currentTab.label}" size="20" styleClass="text"
															partialSubmit="true" />
														<div class="formButton cancel">
															<div class="formButtonLeft"></div>
															<div class="formButtonMiddle">
																<p>
																	<ice:commandLink id="updateLabel" type="submit"
																		value="#{msg.com_groundwork_console_content_search_button_updatelabel }"
																		action="#{consoleMgr.updateLabel}" />
																</p>
															</div>
															<div class="formButtonRight"></div>
														</div>

													</div>
												</div>



												<div class="buttonRow">

													<div class="formButton search">
														<div class="formButtonLeft"></div>
														<div class="formButtonMiddle">
															<p>
																<ice:commandLink id="BLCmdBtn" type="submit"
																	value="#{msg.com_groundwork_console_content_search_button}"
																	action="#{consoleMgr.performSearch}" />
															</p>
														</div>
														<div class="formButtonRight"></div>
													</div>
													<div class="formButton cancel">
														<div class="formButtonLeft"></div>
														<div class="formButtonMiddle">
															<p>
																<ice:commandLink id="BLRset" type="submit"
																	value="#{msg.com_groundwork_console_content_search_button_reset}"
																	action="#{consoleMgr.clearSearch}" />
															</p>
														</div>
														<div class="formButtonRight"></div>
													</div>

												</div>
											</div>

										</ice:panelGroup>
										<ice:panelGroup rendered="#{currentTab.rendered}">
											<div class="horizDivider">
												<ice:commandButton name="collapseButton" id="hideSearch"
													image="#{console.searchPanelImage}"
													actionListener="#{console.hideSearchPanel}" border="0"
													title="Show/Hide Search Panel" />
											</div>

											<div class="tableStatus">
												<span class="tableStatusLabel">Showing:</span>
												<ice:dataPaginator id="displayInfo" for="eventTableID"
													rowsCountVar="rowsCount"
													displayedRowsCountVar="displayedRowsCountVar"
													firstRowIndexVar="firstRowIndex"
													lastRowIndexVar="lastRowIndex" pageCountVar="pageCount"
													pageIndexVar="pageIndex">

													<ice:outputFormat value="{1} of {0}">
														<f:param value="#{rowsCount}" />
														<f:param value="#{displayedRowsCountVar}" />
													</ice:outputFormat>
												</ice:dataPaginator>
											</div>


											<div class="shortcutBar">

												<div class="plainButton active">
													<div class="plainButtonLeft"></div>
													<div class="plainButtonMiddle">
														<ice:panelGrid columns="2">
															<ice:graphicImage url="/images/button_select_all.gif"
																width="16px" height="16px" styleClass="buttonIcon"
																title="Select/Deselect" />
															<p>
																<ice:commandLink id="toggleSelections"
																	value="#{currentTab.msgSelector.selectAllButtonText}"
																	actionListener="#{currentTab.msgSelector.toggleAllSelected}"
																	immediate="true" />
															</p>
														</ice:panelGrid>
													</div>
													<div class="plainButtonRight"></div>
												</div>


												<div class="plainButton active">
													<div class="plainButtonLeft"></div>
													<div class="plainButtonMiddle">
														<ice:menuBar id="menu" orientation="horizontal"
															displayOnClick="true">
															<ice:menuItems value="#{currentTab.actionBean.menuModel}" />
														</ice:menuBar>
													</div>
													<div class="plainButtonRight"></div>


												</div>

												<div class="plainButton active">
													<div class="plainButtonLeft"></div>
													<div class="plainButtonMiddle">
														<ice:panelGrid columns="2">
															<ice:graphicImage
																url="#{currentTab.freezeBean.pauseButtonImage}"
																width="14px" height="14px" styleClass="buttonIcon" />
															<p>
																<ice:commandLink id="toggleFreeze"
																	value="#{currentTab.freezeBean.freezeButtonText}"
																	actionListener="#{currentTab.freezeBean.toggleButton}"
																	immediate="true" />
															</p>
														</ice:panelGrid>
													</div>

													<div class="plainButtonRight"></div>
												</div>


												<ice:panelGroup styleClass="plainButton active"
													rendered="#{tabset.enableAlarm}">
													<div class="plainButtonLeft"></div>
													<div class="plainButtonMiddle">
														<p>
															<ice:commandLink title="#{consoleMgr.alarmButtonText}"
																value="#{consoleMgr.alarmButtonText}" immediate="true"
																actionListener="#{consoleMgr.toggleSilenceAlarm}">
																<ice:graphicImage url="#{consoleMgr.muteButtonImage}"
																	width="14px" height="14px" styleClass="buttonIcon" />
															</ice:commandLink>
														</p>

													</div>
													<div class="plainButtonRight"></div>
												</ice:panelGroup>


												<ice:panelGroup styleClass="plainButton active"
													rendered="true">
													<div class="plainButtonLeft"></div>
													<div class="plainButtonMiddle">
														<p>
															<ice:commandLink title="Open Log Message"
																value="Open Log Message" immediate="true"
																actionListener="#{currentTab.actionBean.updateOperationStatus}">

															</ice:commandLink>
														</p>

													</div>
													<div class="plainButtonRight"></div>
												</ice:panelGroup>

												<ice:panelGroup styleClass="plainButton active"
													rendered="true">
													<div class="plainButtonLeft"></div>
													<div class="plainButtonMiddle">
														<p>
															<ice:commandLink title="Close Log Message"
																value="Close Log Message" immediate="true"
																actionListener="#{currentTab.actionBean.updateOperationStatus}">

															</ice:commandLink>
														</p>

													</div>
													<div class="plainButtonRight"></div>
												</ice:panelGroup>

												<ice:panelGroup styleClass="plainButton active"
													rendered="true">
													<div class="plainButtonLeft"></div>
													<div class="plainButtonMiddle">
														<p>
															<ice:commandLink title="Accept Log Message"
																value="Accept Log Message" immediate="true"
																actionListener="#{currentTab.actionBean.updateOperationStatus}">

															</ice:commandLink>
														</p>

													</div>
													<div class="plainButtonRight"></div>
												</ice:panelGroup>

												<ice:panelGroup styleClass="plainButton active"
													rendered="true">
													<div class="plainButtonLeft"></div>
													<div class="plainButtonMiddle">
														<p>
															<ice:commandLink title="Notify Log Message"
																value="Notify Log Message" immediate="true"
																actionListener="#{currentTab.actionBean.updateOperationStatus}">

															</ice:commandLink>
														</p>

													</div>
													<div class="plainButtonRight"></div>
												</ice:panelGroup>
											</div>

											<div class="tableWrapper">
												<ice:dataTable id="eventTableID"
													value="#{tabset.tabs[tabset.tabIndex].dataTableBean}"
													binding="#{tabset.tabs[tabset.tabIndex].dataTableBean.eventDataTable}"
													var="event" styleClass="text"
													sortColumn="#{tabset.tabs[tabset.tabIndex].dataTableBean.sortColumnName}"
													sortAscending="#{tabset.tabs[tabset.tabIndex].dataTableBean.ascending}"
													rowClasses="odd,list-row-even" resizable="true">

												</ice:dataTable>
												<ice:panelGroup>
													<ice:dataPaginator id="eventsPagerBottom"
														for="eventTableID" fastStep="10" pageCountVar="pageCount"
														pageIndexVar="pageIndex" paginator="true"
														paginatorMaxPages="10" styleClass="text"
														actionListener="#{consoleMgr.paginatorClicked}"
														renderFacetsIfSinglePage="false">
														<f:facet name="first">
															<ice:graphicImage url="/assets/icons/icon_page_first.gif"
																style="border:none;" title="First Page" />
														</f:facet>
														<f:facet name="last">
															<ice:graphicImage url="/assets/icons/icon_page_last.gif"
																style="border:none;" title="Last Page" />
														</f:facet>
														<f:facet name="previous">
															<ice:graphicImage url="/assets/icons/icon_page_prev.gif"
																style="border:none;" title="Previous Page" />
														</f:facet>
														<f:facet name="next">
															<ice:graphicImage url="/assets/icons/icon_page_next.gif"
																style="border:none;" title="Next page" />
														</f:facet>
														<f:facet name="fastforward">
															<ice:graphicImage
																url="/assets/icons/icon_double_next.gif"
																style="border:none;" title="Fast Forward" />
														</f:facet>
														<f:facet name="fastrewind">
															<ice:graphicImage
																url="/assets/icons/icon_double_prev.gif"
																style="border:0;" title="Fast Rewind" />
														</f:facet>
													</ice:dataPaginator>
												</ice:panelGroup>
											</div>

										</ice:panelGroup>
									</div>
									<div class="consoleWrapperBottom" />
								</ice:panelTab>

							</ice:panelTabSet>

						</ice:form>
					</div>

				</div>

			</ice:panelGroup>
		</div>

		<jsp:directive.include file="panelPopup.jsp" />
		<jsp:directive.include file="eventTile.jspx" />

	</ice:portlet>

</body>
		</html>
	</f:view>
</jsp:root>