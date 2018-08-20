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
      <#include "trouble_layout.ftl">
      <#elseif layout = "default-dashboard">
      <#include "default_dashboard.ftl">
      <#elseif layout = "svlayout">
      <#include "sv_layout.ftl">
      <#else>
         <container id="OneColumnContainer" template="system:/groovy/portal/webui/container/UIContainer.gtmpl">
            <access-permissions><#list page.accessPermissions as access>${access}</#list></access-permissions>
         <#list page.portlets as portlet>
          <#include "portlet.ftl">
          </#list>
         </container>
      </#if>
  </page>  
</#list>

</page-set>