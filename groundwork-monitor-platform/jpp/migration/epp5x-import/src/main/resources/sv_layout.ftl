<container id="OuterContainer"  template="system:/groovy/portal/webui/container/UITableAutofitColumnContainer.gtmpl">
  <access-permissions><#list page.accessPermissions as access>${access}</#list></access-permissions>
  <container id="LeftContainer" template="system:/groovy/portal/webui/container/UIContainer.gtmpl">
    <access-permissions><#list page.accessPermissions as access>${access}</#list></access-permissions>
  <#assign portlets=page.porltetsForLeftRegion>
  <#list portlets as portlet>
    <#include "portlet.ftl">
  </#list>
  </container> 
  <container id="BottomContainer" template="system:/groovy/portal/webui/container/UIContainer.gtmpl">
    <access-permissions><#list page.accessPermissions as access>${access}</#list></access-permissions>
      <#assign portlets=page.porltetsForBottomRegion>
      <#list portlets as portlet>
      <#include "portlet.ftl">
      </#list>
  </container>      
</container>