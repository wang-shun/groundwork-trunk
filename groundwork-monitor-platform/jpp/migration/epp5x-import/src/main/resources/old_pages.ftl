<?xml version="1.0" encoding="UTF-8"?>
<page-set xmlns="http://www.gatein.org/xml/ns/gatein_objects_1_0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.gatein.org/xml/ns/gatein_objects_1_0 http://www.gatein.org/xml/ns/gatein_objects_1_0">

<#list pages as page>
  <page>
      <name>${page.displayName}</name>
      <title>${page.title}</title>
      <access-permissions><#list page.accessPermissions as access>${access}</#list></access-permissions>
      <edit-permission>${page.editPermissions}</edit-permission>
      <show-max-window>${page.showMaxWindow?string}</show-max-window>
      <#assign layout = page.layout>
      <#if layout = "TroubleMap">
      <!--- !!!!!!!!! ${layout} -->
      <#include "trouble_layout.ftl">
      <#else>
      <!--- ${layout} !!!!!!!!!!!!!!!-->
      <container id="OuterContainer" template="system:/groovy/portal/webui/container/UITableAutofitColumnContainer.gtmpl">
         <access-permissions><#list page.accessPermissions as access>${access}</#list></access-permissions>
         <container id="LeftContainer" template="system:/groovy/portal/webui/container/UIContainer.gtmpl" />
         <container id="RightContainer" template="system:/groovy/portal/webui/container/UIContainer.gtmpl">
            <access-permissions><#list page.accessPermissions as access>${access}</#list></access-permissions>
         <#list page.portlets as portlet>
            <portlet-application>
               <portlet>
                  <application-ref>${portlet.applicationRef}</application-ref>
                  <portlet-ref>${portlet.portletRef}</portlet-ref>
               </portlet>
               <title>${portlet.title}</title>
               <access-permissions><#list portlet.accessPermissions as access>${access}</#list></access-permissions>
               <show-info-bar>${portlet.showInfoBar?string}</show-info-bar>
               <show-application-state>${portlet.showApplicationState?string}</show-application-state>
               <show-application-mode>${portlet.showApplicationMode?string}</show-application-mode>
               <description>${portlet.description}</description>
               <icon>${portlet.icon}</icon>
            </portlet-application>
            </#list>
         </container>
      </container>
      </#if>
  </page>
  
  
  
  <#list page.eppPageList as subPage>
  <page>
      <name>${subPage.displayName}</name>
      <title>${subPage.title}</title>
      <access-permissions><#list subPage.accessPermissions as access>${access}</#list></access-permissions>
      <edit-permission>${subPage.editPermissions}</edit-permission>
      <show-max-window>${subPage.showMaxWindow?string}</show-max-window>
      <#assign layout = subPage.layout>
      <#if layout = "TroubleMap">
      <#include "trouble_layout.ftl">
      <#elseif layout = "default-dashboard">
      <#include "default_dashboard.ftl">
      <#elseif layout = "svlayout">
      <!--- !!!!!!!!! ${layout} -->
      <#include "sv_layout.ftl">
      <#else>
      <!-- o ${layout} o -->
      <container id="OuterContainer" template="system:/groovy/portal/webui/container/UITableAutofitColumnContainer.gtmpl">
         <access-permissions><#list subPage.accessPermissions as access>${access}</#list></access-permissions>
         <container id="LeftContainer" template="system:/groovy/portal/webui/container/UIContainer.gtmpl" />
         <container id="RightContainer" template="system:/groovy/portal/webui/container/UIContainer.gtmpl">
            <access-permissions><#list subPage.accessPermissions as access>${access}</#list></access-permissions>
         <#list subPage.portlets as portlet>
            <portlet-application>
               <portlet>
                  <application-ref>${portlet.applicationRef}</application-ref>
                  <portlet-ref>${portlet.portletRef}</portlet-ref>
               </portlet>
               <title>${portlet.title}</title>
               <access-permissions><#list portlet.accessPermissions as access>${access}</#list></access-permissions>
               <show-info-bar>${portlet.showInfoBar?string}</show-info-bar>
               <show-application-state>${portlet.showApplicationState?string}</show-application-state>
               <show-application-mode>${portlet.showApplicationMode?string}</show-application-mode>
               <description>${portlet.description}</description>
               <icon>${portlet.icon}</icon>
            </portlet-application>
            </#list>
         </container>
      </container>
      </#if>
  </page>
  </#list>
  
</#list>

</page-set>