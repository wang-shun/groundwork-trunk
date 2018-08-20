        <container id="OuterContainer" template="system:/groovy/portal/webui/container/UIContainer.gtmpl">
          <access-permissions><#list page.accessPermissions as access>${access}</#list></access-permissions>
          <container id="TopContainer" template="system:/groovy/portal/webui/container/UIContainer.gtmpl">
            <access-permissions><#list page.accessPermissions as access>${access}</#list></access-permissions>
          <#assign portlets=page.porltetsForTopRegion>
          <#list portlets as portlet>
            <#include "portlet.ftl">
          </#list>
          </container> 
          <container id="TopContainer" template="system:/groovy/portal/webui/container/UIContainer.gtmpl">
            <access-permissions><#list page.accessPermissions as access>${access}</#list></access-permissions>
            <container id="Center1Container" template="system:/groovy/portal/webui/container/UITableAutofitColumnContainer.gtmpl" >
              <access-permissions><#list page.accessPermissions as access>${access}</#list></access-permissions>
              <#assign portlets=page.porltetsForCenterCol1Region>
              <#list portlets as portlet>
              <#include "portlet.ftl">
              </#list>
            </container>
            <container id="Center2Container" template="system:/groovy/portal/webui/container/UITableAutofitColumnContainer.gtmpl" >
              <access-permissions><#list page.accessPermissions as access>${access}</#list></access-permissions>
              <#assign portlets=page.porltetsForCenterCol2Region>
              <#list portlets as portlet>
              <#include "portlet.ftl">
              </#list>
            </container>
          </container>
          <container id="BottomContainer" template="system:/groovy/portal/webui/container/UIContainer.gtmpl">
            <access-permissions><#list page.accessPermissions as access>${access}</#list></access-permissions>
          <#assign portlets=page.porltetsForDashBottomRegion>
          <#list portlets as portlet>
            <#include "portlet.ftl">
          </#list>
          </container>      
        </container>