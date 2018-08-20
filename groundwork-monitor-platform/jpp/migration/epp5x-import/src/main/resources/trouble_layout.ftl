<container id="OuterContainer" template="system:/groovy/portal/webui/container/UIContainer.gtmpl">

         <access-permissions><#list page.accessPermissions as access>${access}</#list></access-permissions>
         <#if (page.portlets?size > 0)>
           <container id="RowOneContainer" template="system:/groovy/portal/webui/container/UITableAutofitColumnContainer.gtmpl" >
           <#assign portlet = page.portlets[0]>
           <access-permissions><#list page.accessPermissions as access>${access}</#list></access-permissions>
             <#include "portlet.ftl">
         </container>
         </#if>
         <#if (page.portlets?size > 1)>
           <container id="RowTwoContainer" template="system:/groovy/portal/webui/container/UITableAutofitColumnContainer.gtmpl" >
           <#assign portlet = page.portlets[1]>
           <access-permissions><#list page.accessPermissions as access>${access}</#list></access-permissions>
             <#include "portlet.ftl">
             <#if (page.portlets?size > 2)>
                <#assign portlet = page.portlets[2]>
                <#include "portlet.ftl">
             </#if>
         </container>
         </#if>
         
         <#if (page.portlets?size > 3)>
           <container id="RowThreeContainer" template="system:/groovy/portal/webui/container/UITableAutofitColumnContainer.gtmpl" >
           <#assign portlet = page.portlets[3]>
           <access-permissions><#list page.accessPermissions as access>${access}</#list></access-permissions>
             <#include "portlet.ftl">
             <#if (page.portlets?size > 4)>
                <#assign portlet = page.portlets[4]>
                <#include "portlet.ftl">
             </#if>
         </container>
         </#if>
         
       
      </container>