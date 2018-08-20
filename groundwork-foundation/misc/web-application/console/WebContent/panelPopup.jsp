<ice:form id="popupform">
	<ice:panelPopup id="popupDiv" rendered="true"
		visible="#{popup.showModalPanel}" modal="true"
		style="z-index: 1000; top: 300px; left: 700px; position: absolute; width: 300px; height: 150px;">

		<f:facet name="header">
			<ice:outputText styleClass="popupTitle" value="#{popup.title}" />

		</f:facet>

		<f:facet name="body">
			<ice:panelGrid id="modalPanelGrid" width="100%" cellpadding="0"
				cellspacing="0" columns="1" styleClass="popupModalBody">

				<table align="center">
					<tr>
						<td align="left"><ice:outputText value="#{popup.message}"
							styleClass="popupText" /></td>
					</tr>
					<tr>
						<td align="left"></td>
					</tr>
					<tr>
						<td align="center"><ice:commandButton id="closeModal"
							actionListener="#{popup.closeModalPopup}" value="OK"
							style="z-index:3;bottom: 30px;cursor: pointer;" /></td>
					</tr>
				</table>


			</ice:panelGrid>
		</f:facet>
	</ice:panelPopup>
	<ice:panelPopup id="draggablePP" draggable="true" rendered="true"
		visible="#{popup.showDraggablePanel}"
		style="z-index: 1000; top: 300px; left: 700px; position: absolute; width: 300px; height:auto;">

		<f:facet name="header">
			<ice:outputText styleClass="popupTitle" value="#{popup.title}" />

		</f:facet>

		<f:facet name="body">
			<ice:panelGrid id="draggablePanelGrid" width="100%" cellpadding="0"
				cellspacing="0" columns="1" styleClass="popupModalBody">
			<ice:panelBorder id="panelBorder"                     
                     renderNorth="true"
                     renderSouth="true"
                     renderCenter="true"
                     renderWest="false"
                     renderEast="false">

        <f:facet name="north">
           <ice:panelGrid columns="2" cellspacing="10">
                    <ice:outputLabel for="labelDevice"  value="Device: " style="font-weight: bold;"/>
                       <ice:outputText id ="labelDevice" value="#{popup.host.hostName}"
							styleClass="popupText" />  
                    <ice:outputLabel for="labelStatus"  value="Status: " style="font-weight: bold;"/>
                       <ice:outputText id ="labelStatus" value="#{popup.host.status}"
							styleClass="popupText" />  
							 <ice:outputLabel for="labelCheckTime"  value="LastCheckTime: " style="font-weight: bold;"/>
                       <ice:outputText id ="labelCheckTime" value="#{popup.host.lastCheckTime}"
							styleClass="popupText" />  
				</ice:panelGrid>
        </f:facet>

        <f:facet name="center">
            <ice:panelGroup>
						<ice:dataTable id="data_service" var="service"
							value="#{popup.host.serviceStatus}" scrollable="true" 
							rowClasses="odd,list-row-even" styleClass="tableWrapper">
							<ice:column>
								<f:facet name="header">
									<ice:outputText id="column1" value="Service" />
								</f:facet>
								<ice:outputText id="description" value="#{service.description}" />
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
</ice:form>