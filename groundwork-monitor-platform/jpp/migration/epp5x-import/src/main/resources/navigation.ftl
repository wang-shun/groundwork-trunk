<?xml version="1.0" encoding="UTF-8"?>
<node-navigation xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.gatein.org/xml/ns/gatein_objects_1_0 http://www.gatein.org/xml/ns/gatein_objects_1_0"
    xmlns="http://www.gatein.org/xml/ns/gatein_objects_1_0">
    <priority>1</priority>

    <page-nodes>
    <#list pages as page>
        <node>   
            <name>${page.node.name}</name>      
          <#list page.node.label?keys as locale>
            <label xml:lang="${locale}">${page.node.label[locale]}</label>
          </#list> 
            <visibility>${page.node.visibility}</visibility>  
            <page-reference>${page.node.pageReference}</page-reference> 
         <#list page.eppPageList as subPage>
            <node>     
                <name>${subPage.node.name}</name>     
              <#list subPage.node.label?keys as locale>
                <label xml:lang="${locale}">${subPage.node.label[locale]}</label>
              </#list>   
                <visibility>${subPage.node.visibility}</visibility>  
                <page-reference>${subPage.node.pageReference}</page-reference> 
            </node>
         </#list> 
      
     </node>   
    </#list>
    
    </page-nodes>
</node-navigation>