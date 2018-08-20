<f:view xmlns:h="http://java.sun.com/jsf/html"
	xmlns:f="http://java.sun.com/jsf/core"
	xmlns:ice="http://www.icesoft.com/icefaces/component">
	<ice:outputDeclaration doctypeRoot="HTML"
		doctypePublic="-//W3C//DTD HTML 4.01 Transitional//EN"
		doctypeSystem="http://www.w3.org/TR/html4/loose.dtd" />

	<html>
	<head>

	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"></meta>
	<title>GW Console 5.2</title>
	<link href="resources/gwconsole.css" rel="stylesheet" type="text/css" />
	<link href="resources/reset.css" rel="styleSheet" type="text/css"
		media="screen" />
	<link href="resources/global.css" rel="styleSheet" type="text/css"
		media="screen" />
	<link href="resources/buttons.css" rel="styleSheet" type="text/css"
		media="screen" />
	<link href="resources/masthead.css" rel="styleSheet" type="text/css"
		media="screen" />
	<link href="resources/navigation.css" rel="styleSheet" type="text/css"
		media="screen" />
	<link href="resources/statusBar.css" rel="styleSheet" type="text/css"
		media="screen" />
	<link href="resources/sideBar.css" rel="styleSheet" type="text/css"
		media="screen" />
	<link href="resources/console.css" rel="styleSheet" type="text/css"
		media="screen" />
	<link href="resources/modules.css" rel="styleSheet" type="text/css"
		media="screen" />
	<link href="resources/tabs.css" rel="styleSheet" type="text/css"
		media="screen" />
	<link href="resources/text.css" rel="styleSheet" type="text/css"
		media="screen" />
	<link href="resources/tree.css" rel="styleSheet" type="text/css"
		media="screen" />
	<link href="resources/table.css" rel="styleSheet" type="text/css"
		media="screen" />
	<link href="resources/contextMenu.css" rel="styleSheet" type="text/css"
		media="screen" />
	<link href="resources/footer.css" rel="styleSheet" type="text/css"
		media="screen" />
		<f:loadBundle basename="#{locale.baseName}" var="msg" />
	</head>
	<body>

	<div class="bodyWrapper clearfix"><ice:panelGroup
		styleClass="#{console.layoutWrapperStyleClass}">

		<ice:panelGroup styleClass="sideBarWrapper">
			<ice:form id="naviPanel" partialSubmit="true">
				<!--		<div id="filterSideBar" class="sideBarWrapper">-->
				<!--						<div class="sideBarPanelWrapper">-->
				<ice:panelGroup styleClass="sideBarPanelWrapper">

					<div class="treeWrapper"><ice:panelGroup
						effect="#{console.effect}">
						<ice:panelCollapsible id="panelSystemFilter" expanded="true"
							styleClass="navPnlClpsbl">
							<f:facet name="header">
								<ice:panelGroup>
									<ice:outputText id="panelHeaderSystem" value="#{msg.com_groundwork_console_navigation_system_filters}" />
								</ice:panelGroup>
							</f:facet>
							<ice:panelGroup>
								<ice:commandLink value="#{msg.com_groundwork_console_content_tab_default}"
									action="#{consoleMgr.populateAllOpenEvents}"
									styleClass="#{console.allEventsStyleClass}" />
								<ice:tree id="systemFilterTree" value="#{filterTreeBean.model}"
									var="item" hideRootNode="false" hideNavigation="false">
									<ice:treeNode>
										<f:facet name="icon">
											<ice:panelGroup style="display: inline">
												<h:graphicImage value="#{item.userObject.icon}" />
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
									<ice:outputText id="panelHeaderPub" value="#{msg.com_groundwork_console_navigation_public_filters}" />
								</ice:panelGroup>
							</f:facet>
							<ice:panelGroup width="100%" height="1000px">
								<ice:tree id="publicFilterTree"
									value="#{publicFilterTreeBean.model}" var="item"
									hideRootNode="false" hideNavigation="false">
									<ice:treeNode>
										<f:facet name="icon">
											<ice:panelGroup style="display:inline;">
												<h:graphicImage value="#{item.userObject.icon}" />

												<ice:commandLink value="#{item.userObject.text}"
													styleClass="#{item.userObject.styleClass}"
													actionListener="#{item.userObject.nodeClicked}" />
											</ice:panelGroup>
										</f:facet>
									</ice:treeNode>
								</ice:tree>
							</ice:panelGroup>
						</ice:panelCollapsible>
					</ice:panelGroup></div>

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
		<div class="consoleTitle"><ice:outputText id="consoleTitle" value="#{msg.com_groundwork_console_content_title}" /></div>
		<div class="consoleTabsWrapper"><ice:form id="contentPanel"
			partialSubmit="true">
			<ice:panelTabSet id="icepnltabset" var="currentTab"
				value="#{tabset.tabs}" selectedIndex="#{tabset.tabIndex}"
				tabChangeListener="#{tabset.tabSelection}">

				<ice:panelTab id="icepnltab" label="#{currentTab.label}">
					<div class="tabContent"><ice:panelGroup
						styleClass="tabContentHeader" style="#{console.searchPanelStyle}">
						<ice:panelGroup style="float:right">
							<ice:commandButton id="closeTab" image="images/tabClose.gif"
								actionListener="#{tabset.closeTab}" />
						</ice:panelGroup>
						<div class="tabContentForm searchEventsForm">
						<div class="formHeader">
						<p><label for="searchEvents_hosts"><ice:outputText id="search_title" value="#{msg.com_groundwork_console_content_search_title}" /></label></p>
						</div>

						<div class="formLine"><label for="searchEvents_hosts"><ice:outputText id="device" value="#{msg.com_groundwork_console_content_search_device}" /></label>
						<ice:inputText id="searchEvents_hosts"
							value="#{currentTab.searchCriteria.host}"
							styleClass="text" partialSubmit="true"
							action="#{consoleMgr.performSearch}" /></div>
						<div class="formLine"><label for="searchEvents_messages"><ice:outputText id="messages" value="#{msg.com_groundwork_console_content_search_messages}" /></label>
						<ice:inputText id="searchEvents_messages"
							value="#{currentTab.searchCriteria.message}"
							styleClass="text" partialSubmit="true"
							action="#{consoleMgr.performSearch}">
						</ice:inputText></div>
						</div>

						<div class="tabContentForm dateRangeForm">
						<div class="formHeader">
						<p><ice:outputText id="datetimerange" value="#{msg.com_groundwork_console_content_search_datetime}" /></p>
						</div>

						<div class="formLine"><ice:selectOneRadio
							id="dateTimeRange_preset"
							value="#{currentTab.searchCriteria.ageType}"
							layout="pageDirection" partialSubmit="true">
							<f:selectItem itemValue="preset" itemLabel="#{msg.com_groundwork_console_content_search_preset }" />
							<f:selectItem itemValue="custom" itemLabel="#{msg.com_groundwork_console_content_search_custom}" />
						</ice:selectOneRadio></div>
						<!--						</div>--> <!--<div class="formLineSmall"></div>
						-->

						<table>


							<tr>
								<td><br />
								<div class="formLine"><ice:selectOneMenu id="ageValue1"
									value="#{currentTab.searchCriteria.presetValue}">
									<f:selectItem itemValue="none" itemLabel="NONE" />
									<f:selectItem itemValue="last10min" itemLabel="LAST 10 MINS" />
									<f:selectItem itemValue="last30min" itemLabel="LAST 30 MINS" />
									<f:selectItem itemValue="lasthr" itemLabel="LAST HOUR" />
									<f:selectItem itemValue="last6hr" itemLabel="LAST 6 HOURS" />
									<f:selectItem itemValue="last12hr" itemLabel="LAST 12 HOURS" />
									<f:selectItem itemValue="last24hr" itemLabel="LAST 24 HOURS" />
								</ice:selectOneMenu></div>
								</td>
								
								
								
							</tr>

							<tr>
								<td>
								<div class="formLine"><ice:selectInputDate id="date1"
									value="#{currentTab.searchCriteria.ageValueFrom}"
									imageDir="./xmlhttp/css/xp/css-images/" renderAsPopup="true"
									styleClass="text" /> <ice:selectInputDate id="date2"
									value="#{currentTab.searchCriteria.ageValueTo}"
									imageDir="./xmlhttp/css/xp/css-images/" renderAsPopup="true"
									styleClass="text" /></div>
								</td>
								
							</tr>
							
						</table>
						<div class="tabContentForm updateLabelForm">
						<div class="formHeader">
						<p><ice:outputText id="updatelabelheader" value="#{msg.com_groundwork_console_content_search_header_updatelabel}" /></p>
						</div>

					<div class="formLine">
									<ice:inputText id="update_label"
										value="#{currentTab.label}" size="20"
										styleClass="text" partialSubmit="true"
										/>
										<div class="formButton cancel">
						<div class="formButtonLeft"></div>
						<div class="formButtonMiddle">
						<p><ice:commandLink id="updateLabel" type="submit" value="#{msg.com_groundwork_console_content_search_button_updatelabel }"	 /></p>
						</div>
						<div class="formButtonRight"></div>
						</div>
								
								</div>
								</div>
						
						

						<div class="buttonRow">
						<div class="formButton cancel">
						<div class="formButtonLeft"></div>
						<div class="formButtonMiddle">
						<p><ice:commandLink id="BLRset" type="submit" value="#{msg.com_groundwork_console_content_search_button_reset}"
							action="#{consoleMgr.clearSearch}" /></p>
						</div>
						<div class="formButtonRight"></div>
						</div>
						<div class="formButton search">
						<div class="buttonActiveLeft"></div>
						<div class="buttonActiveMiddle">
						<p><ice:commandLink id="BLCmdBtn" type="submit" value="#{msg.com_groundwork_console_content_search_button}"
							action="#{consoleMgr.performSearch}" /></p>
						</div>
						<div class="buttonActiveRight"></div>
						</div>
						</div>
						</div>
						
					</ice:panelGroup> 
					
					
					
					<ice:panelGroup
						rendered="#{currentTab.rendered}">
						<div class="horizDivider"><ice:commandButton name="collapseButton"
							id="hideSearch" image="#{console.searchPanelImage}"
							actionListener="#{console.hideSearchPanel}" border="0"
							title="Show/Hide Search Panel" /></div>
					
						<div class="tableStatus"><span class="tableStatusLabel">Showing:</span>
						<ice:dataPaginator id="displayInfo" for="eventTableID"
							rowsCountVar="rowsCount"
							displayedRowsCountVar="displayedRowsCountVar"
							firstRowIndexVar="firstRowIndex" lastRowIndexVar="lastRowIndex"
							pageCountVar="pageCount" pageIndexVar="pageIndex">

							<ice:outputFormat value="{1} of {0}">
								<f:param value="#{rowsCount}" />
								<f:param value="#{displayedRowsCountVar}" />
							</ice:outputFormat>

						</ice:dataPaginator></div>


						<div class="shortcutBar">
						
								<div class="plainButton active">
								<div class="plainButtonLeft"></div>
								<div class="plainButtonMiddle">
								<ice:panelGrid columns="2">
									<ice:graphicImage
										url="images/button_select_all.gif"
										width="16px" height="16px" styleClass="buttonIcon" title="Select/Deselect"/>
								<p><ice:commandLink id="toggleSelections"
									value="#{currentTab.msgSelector.selectAllButtonText}"
									actionListener="#{currentTab.msgSelector.toggleAllSelected}"
									immediate="true" /></p>
									</ice:panelGrid>	
								</div>
								<div class="plainButtonRight"></div>
								</div>

								
								<div class="plainButton active">
								<div class="plainButtonLeft"></div>
								<div class="plainButtonMiddle">
								<ice:menuBar id="menu" orientation="horizontal" displayOnClick="true">
									<ice:menuItems
										value="#{currentTab.actionBean.menuModel}" />
								</ice:menuBar>
								</div>
								<div class="plainButtonRight"></div>
								
								
								</div>

								<div class="plainButton active">
								<div class="plainButtonLeft"></div>
								<div class="plainButtonMiddle"><ice:panelGrid columns="2">
									<ice:graphicImage
										url="#{currentTab.freezeBean.pauseButtonImage}"
										width="14px" height="14px" styleClass="buttonIcon" />
									<p><ice:commandLink id="toggleFreeze"
										value="#{currentTab.freezeBean.freezeButtonText}"
										actionListener="#{currentTab.freezeBean.toggleButton}"
										immediate="true" /></p>
								</ice:panelGrid></div>
								<div class="plainButtonRight"></div>
								</div>
								
						</div>
						
						 
						<div class="tableWrapper"><ice:dataTable id="eventTableID"
							value="#{tabset.tabs[tabset.tabIndex].dataTableBean}"
							binding="#{tabset.tabs[tabset.tabIndex].dataTableBean.eventDataTable}"
							var="event" styleClass="text"
							sortColumn="#{tabset.tabs[tabset.tabIndex].dataTableBean.sortColumnName}"
							sortAscending="#{tabset.tabs[tabset.tabIndex].dataTableBean.ascending}"
							rowClasses="odd,list-row-even">
							
						</ice:dataTable> <ice:panelGroup>
							<ice:dataPaginator id="eventsPagerBottom" for="eventTableID"
								fastStep="10" pageCountVar="pageCount" pageIndexVar="pageIndex"
								paginator="true" paginatorMaxPages="10" styleClass="text"
								actionListener="#{consoleMgr.paginatorClicked}"
								renderFacetsIfSinglePage="false">
								<f:facet name="first">
									<ice:graphicImage url="assets/icons/icon_page_first.gif"
										style="border:none;" title="First Page" />
								</f:facet>
								<f:facet name="last">
									<ice:graphicImage url="assets/icons/icon_page_last.gif"
										style="border:none;" title="Last Page" />
								</f:facet>
								<f:facet name="previous">
									<ice:graphicImage url="assets/icons/icon_page_prev.gif"
										style="border:none;" title="Previous Page" />
								</f:facet>
								<f:facet name="next">
									<ice:graphicImage url="assets/icons/icon_page_next.gif"
										style="border:none;" title="Next page" />
								</f:facet>
								<f:facet name="fastforward">
									<ice:graphicImage url="assets/icons/icon_double_next.gif"
										style="border:none;" title="Fast Forward" />
								</f:facet>
								<f:facet name="fastrewind">
									<ice:graphicImage url="assets/icons/icon_double_prev.gif"
										style="border:0;" title="Fast Rewind" />
								</f:facet>
							</ice:dataPaginator>
						</ice:panelGroup></div>
						
					</ice:panelGroup></div>
					<div class="consoleWrapperBottom" />
				</ice:panelTab>

			</ice:panelTabSet>
		</ice:form></div>

		</div>

	</ice:panelGroup></div>

	<jsp:directive.include file="panelPopup.jsp" />

	</body>
	</html>
</f:view>