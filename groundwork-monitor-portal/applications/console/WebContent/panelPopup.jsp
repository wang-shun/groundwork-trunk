<!--
   Copyright (C) 2009 GroundWork Open Source, Inc. (GroundWork)
   All rights reserved. Use is subject to GroundWork commercial license terms.
-->

<ice:form id="popupform">
	<ice:panelPopup id="popupDiv" rendered="true"
		visible="#{popup.showModalPanel}" modal="true">

		<f:facet name="header">
			<ice:outputText styleClass="popupTitle" value="#{popup.title}" />

		</f:facet>

		<f:facet name="body">
			<ice:panelGrid id="modalPanelGrid" width="100%" cellpadding="0"
				cellspacing="0" columns="1" styleClass="popupModalBody">

				<table align="center">
					<tr>
						<td align="left"><ice:outputText value="#{popup.message}"
								styleClass="popupText" />
						</td>
					</tr>
					<tr>
						<td align="left"></td>
					</tr>
					<tr>
						<td align="center"><ice:commandButton id="closeModal"
								actionListener="#{popup.closeModalPopup}" value="OK"
								style="z-index:3;bottom: 30px;cursor: pointer;" />
						</td>
					</tr>
				</table>


			</ice:panelGrid>
		</f:facet>
	</ice:panelPopup>
	<ice:panelPopup id="popupInputModalDiv" rendered="true"
		visible="#{popup.showModalInputPanel}" modal="true" autoCentre="true">

		<f:facet name="header">
			<ice:outputText styleClass="popupTitle" value="#{popup.title}" />

		</f:facet>

		<f:facet name="body">
			<ice:panelGrid id="modalInputPanelGrid" width="100%" cellpadding="0"
				cellspacing="0" columns="1" styleClass="popupModalBody">

				<table align="center">
					<tr>
						<td align="left"><ice:outputText value="#{popup.message}"
								styleClass="popupText" />
						</td>
					</tr>
					<tr>
						<td align="left"><ice:inputTextarea
								value="#{popup.inputText}" styleClass="popupText" rows="3"
								cols="30" visible="#{popup.buttonValue == 'Submit'}" />
						</td>
					</tr>
					<tr>
						<td align="left"></td>
					</tr>
					<tr>
						<td align="center"><ice:commandButton id="closeInputModal"
								actionListener="#{popup.closeInputModalPopup}"
								value="#{popup.buttonValue}"
								style="z-index:3;bottom: 30px;cursor: pointer;" />
						</td>
					</tr>
				</table>


			</ice:panelGrid>
		</f:facet>
	</ice:panelPopup>
	<ice:panelPopup id="draggablePP" draggable="false" rendered="true"
		autoCentre="true" visible="#{popup.showDraggablePanel}"
		styleClass="icePnlPopTbl">

		<f:facet name="header">
			<ice:outputText styleClass="popupTitle" value="#{popup.title}" />

		</f:facet>

		<f:facet name="body">
			<ice:panelGrid id="draggablePanelGrid" width="100%" cellpadding="0"
				cellspacing="0" columns="1" styleClass="popupModalBody">
				<ice:panelBorder id="panelBorder" style="width:95%"
					renderNorth="true" renderSouth="true" renderCenter="true"
					renderWest="false" renderEast="false">

					<f:facet name="north">
						<ice:panelGrid columns="2" cellspacing="10">
							<ice:outputLabel for="labelDevice" value="Device: "
								style="font-weight: bold;" />
							<ice:outputText id="labelDevice" value="#{popup.host.hostName}"
								styleClass="popupText" />
							<ice:outputLabel for="labelStatus" value="Status: "
								style="font-weight: bold;" />
							<ice:outputText id="labelStatus" value="#{popup.host.status}"
								styleClass="popupText" />
							<ice:outputLabel for="labelCheckTime" value="LastCheckTime: "
								style="font-weight: bold;" />
							<ice:outputText id="labelCheckTime"
								value="#{popup.host.lastCheckTime}" styleClass="popupText" />
						</ice:panelGrid>
					</f:facet>

					<f:facet name="center">
						<ice:panelGroup style="width:100%">
							<ice:dataTable id="data_service" var="service" style="width:95%"
								value="#{popup.host.serviceStatus}" scrollable="true"
								rowClasses="odd,list-row-even" styleClass="tableWrapper">
								<ice:column>
									<f:facet name="header">
										<ice:outputText id="column1" value="Service" />
									</f:facet>
									<ice:outputLink id="ServiceLink"
										value="#{popup.host.baseURL}host=#{popup.host.correctHostName}&amp;service=#{service.description}"
										rendered="#{popup.linksEnabled}" onclick="blockNavigation();">
										<ice:outputText id="description"
											value="#{service.description}" />
									</ice:outputLink>
									<ice:outputText rendered="#{!popup.linksEnabled}"
										id="txt_description" value="#{service.description}" />
								</ice:column>
								<ice:column>
									<f:facet name="header">
										<ice:outputText id="column2" value="Status" />
									</f:facet>
									<ice:outputText id="serviceName"
										value="#{service.monitorStatus.name}" />
								</ice:column>
							</ice:dataTable>
						</ice:panelGroup>
					</f:facet>

					<f:facet name="south">
						<ice:panelGroup>
							<ice:commandButton id="closeDraggable"
								actionListener="#{popup.closeDraggablePopup}" value="OK"
								style="cursor: pointer;margin-left:120px;margin-bottom:30px;" />
						</ice:panelGroup>
					</f:facet>
				</ice:panelBorder>
			</ice:panelGrid>
		</f:facet>

	</ice:panelPopup>
	<ice:panelPopup id="dynamicPropPopupPP" draggable="false"
		rendered="true" autoCentre="true" visible="#{popup.showDynamicProps}"
		styleClass="icePnlPopTbl"	>

		<f:facet name="header">
			<ice:outputText styleClass="popupTitle" value="#{popup.title}" />
		</f:facet>

		<f:facet name="body">
			<ice:panelGrid id="dynamicPropPopupPanelGrid" width="100%"
				cellpadding="0" cellspacing="0" columns="1"
				styleClass="popupModalBody">
				<ice:dataTable value="#{popup.dynaPropKeys}" var="var1"
					id="dynamicProp" border="1">
					<ice:column>
						<f:facet name="header">
							<ice:outputText id="header1" value="Property" />
						</f:facet>
						<ice:outputText id="value1" value="#{var1}" />
					</ice:column>
					<ice:column>
						<f:facet name="header">
							<ice:outputText id="header2" value="Value" />
						</f:facet>
						<ice:outputText id="value2" value="#{popup.dynaPropMap[var1]}" />
					</ice:column>
				</ice:dataTable>
				<ice:panelGroup>
							<ice:commandButton id="closeDynamicProp"
								actionListener="#{popup.closeDynamicPropPopup}" value="OK"
								style="cursor: pointer;margin-left:180px;margin-bottom:30px;" />
						</ice:panelGroup>
			</ice:panelGrid>
		</f:facet>

	</ice:panelPopup>
</ice:form>