           <portlet-application>
               <portlet>
                  <application-ref>${portlet.applicationRef}</application-ref>
                  <portlet-ref>${portlet.portletRef}</portlet-ref>
                  <#if portlet.hasPreferences>
                   <preferences>
                 <#list portlet.preferences?keys as key>
                   <preference>
                    <name>${key}</name>
                    <value>${portlet.preferences[key]}</value>
                   </preference>
                 </#list>
                 </preferences>
               </#if>
               </portlet>
               <title>${portlet.title}</title>
               <access-permissions><#list portlet.accessPermissions as access>${access}</#list></access-permissions>
               <show-info-bar>${portlet.showInfoBar?string}</show-info-bar>
               <show-application-state>${portlet.showApplicationState?string}</show-application-state>
               <show-application-mode>${portlet.showApplicationMode?string}</show-application-mode>
               <description>${portlet.description}</description>
               <icon>${portlet.icon}</icon>
            </portlet-application>
            