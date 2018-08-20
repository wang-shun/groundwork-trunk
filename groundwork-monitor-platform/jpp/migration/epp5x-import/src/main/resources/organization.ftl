<?xml version="1.0" encoding="ISO-8859-1"?>
<!--

    Copyright (C) 2009 eXo Platform SAS.
    
    This is free software; you can redistribute it and/or modify it
    under the terms of the GNU Lesser General Public License as
    published by the Free Software Foundation; either version 2.1 of
    the License, or (at your option) any later version.
    
    This software is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
    Lesser General Public License for more details.
    
    You should have received a copy of the GNU Lesser General Public
    License along with this software; if not, write to the Free
    Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
    02110-1301 USA, or see the FSF site: http://www.fsf.org.

-->

<configuration
   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
   xsi:schemaLocation="http://www.exoplaform.org/xml/ns/kernel_1_0.xsd http://www.exoplaform.org/xml/ns/kernel_1_0.xsd"
   xmlns="http://www.exoplaform.org/xml/ns/kernel_1_0.xsd">
  <external-component-plugins>
    <target-component>org.exoplatform.services.organization.OrganizationService</target-component>
    <component-plugin>
      <name>init.service.listener</name>
      <set-method>addListenerPlugin</set-method>
      <type>org.exoplatform.services.organization.OrganizationDatabaseInitializer</type>
      <description>this listener populate organization data for the first launch</description>
      <init-params>      
        <value-param>
          <name>checkDatabaseAlgorithm</name>
          <description>check database</description>
          <value>entry</value>
        </value-param>      
        <value-param>
          <name>printInformation</name>
          <description>Print information init database</description>
          <value>false</value>
        </value-param> 
        <object-param>
          <name>configuration</name>
          <description>description</description>
          <object type="org.exoplatform.services.organization.OrganizationConfig">
            <field  name="membershipType">
              <collection type="java.util.ArrayList">
                <value>
                  <object type="org.exoplatform.services.organization.OrganizationConfig$MembershipType">
                    <field  name="type"><string>manager</string></field>
                    <field  name="description"><string>manager membership type</string></field>
                  </object>
                </value>
                <value>
                  <object type="org.exoplatform.services.organization.OrganizationConfig$MembershipType">
                    <field  name="type"><string>member</string></field>
                    <field  name="description"><string>member membership type</string></field>
                  </object>
                </value>                
                <value>
                  <object type="org.exoplatform.services.organization.OrganizationConfig$MembershipType">
                    <field  name="type"><string>validator</string></field>
                    <field  name="description"><string>validator membership type</string></field>
                  </object>
                </value>
              </collection>
            </field>

            <field  name="group">
              <collection type="java.util.ArrayList">             
                <value>
                  <object type="org.exoplatform.services.organization.OrganizationConfig$Group">
                    <field  name="name"><string>groundwork</string></field>
                    <field  name="parentId"><string></string></field>
                    <field  name="description"><string>the /groundwork group</string></field>
                    <field  name="label"><string>Groundwork</string></field>                    
                  </object>
                </value>
               
                <value>
                  <object type="org.exoplatform.services.organization.OrganizationConfig$Group">
                    <field  name="name"><string>platform</string></field>
                    <field  name="parentId"><string></string></field>
                    <field  name="description"><string>the /platform group</string></field>
                    <field  name="label"><string>Platform</string></field>                    
                  </object>
                </value>
                <value>
                  <object type="org.exoplatform.services.organization.OrganizationConfig$Group">
                    <field  name="name"><string>administrators</string></field>
                    <field  name="parentId"><string>/platform</string></field>
                    <field  name="description"><string>the /platform/administrators group</string></field>
                    <field  name="label"><string>Administrators</string></field>
                  </object>
                </value>
                <value>
                  <object type="org.exoplatform.services.organization.OrganizationConfig$Group">
                    <field  name="name"><string>users</string></field>
                    <field  name="parentId"><string>/platform</string></field>
                    <field  name="description"><string>the /platform/users group</string></field>
                    <field  name="label"><string>Users</string></field>
                  </object>
                </value>
                <value>
                  <object type="org.exoplatform.services.organization.OrganizationConfig$Group">
                    <field  name="name"><string>guests</string></field>
                    <field  name="parentId"><string>/platform</string></field>
                    <field  name="description"><string>the /platform/guests group</string></field>
                    <field  name="label"><string>Guests</string></field>
                  </object>
                </value>
                <value>
                  <object type="org.exoplatform.services.organization.OrganizationConfig$Group">
                    <field  name="name"><string>organization</string></field>
                    <field  name="parentId"><string></string></field>
                    <field  name="description"><string>the organization group</string></field>
                    <field  name="label"><string>Organization</string></field>
                  </object>
                </value>
                <value>
                  <object type="org.exoplatform.services.organization.OrganizationConfig$Group">
                    <field  name="name"><string>management</string></field>
                    <field  name="parentId"><string>/organization</string></field>
                    <field  name="description"><string>the /organization/management group</string></field>
                    <field  name="label"><string>Management</string></field>
                  </object>
                </value>
                <value>
                  <object type="org.exoplatform.services.organization.OrganizationConfig$Group">
                    <field  name="name"><string>executive-board</string></field>
                    <field  name="parentId"><string>/organization/management</string></field>
                    <field  name="description"><string>the /organization/management/executive-board group</string></field>
                    <field  name="label"><string>Executive Board</string></field>
                  </object>
                </value>                
              </collection>
            </field>

            <field  name="user">
              <collection type="java.util.ArrayList">
                <value>
                  <object type="org.exoplatform.services.organization.OrganizationConfig$User">
                    <field  name="userName"><string>root</string></field>
                    <field  name="password"><string>gtn</string></field>
                    <field  name="firstName"><string>Root</string></field>
                    <field  name="lastName"><string>Root</string></field>
                    <field  name="email"><string>root@localhost</string></field>
                    <field  name="groups">
                      <string>
                        manager:/groundwork,manager:/platform/administrators,member:/platform/users,
                        member:/organization/management/executive-board
                      </string>
                    </field>
                  </object>
                </value>
                                       
              </collection>
            </field>
          </object>
        </object-param>
      </init-params>
    </component-plugin>
           
    <component-plugin>
      <name>new.user.event.listener</name>
      <set-method>addListenerPlugin</set-method>
      <type>org.exoplatform.services.organization.impl.NewUserEventListener</type>
      <description>this listener assign group and membership to a new created user</description>
      <init-params>
        <object-param>
          <name>configuration</name>
          <description>description</description>
          <object type="org.exoplatform.services.organization.impl.NewUserConfig">
            <field  name="group">
              <collection type="java.util.ArrayList">
                <value>
                  <object type="org.exoplatform.services.organization.impl.NewUserConfig$JoinGroup">
                    <field  name="groupId"><string>/platform/users</string></field>
                    <field  name="membership"><string>member</string></field>
                  </object>
                </value>               
              </collection>
            </field>
            <field  name="ignoredUser">
              <collection type="java.util.HashSet">
                <value><string>root</string></value>
                <value><string>john</string></value>
                <value><string>mary</string></value>
                <value><string>demo</string></value>
              </collection>
            </field>
          </object>
        </object-param>
      </init-params>
    </component-plugin>

  </external-component-plugins>
</configuration>
