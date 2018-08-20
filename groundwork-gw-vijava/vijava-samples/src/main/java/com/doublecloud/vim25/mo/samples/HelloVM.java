/*================================================================================
Copyright (c) 2008 VMware, Inc. All Rights Reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, 
this list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice, 
this list of conditions and the following disclaimer in the documentation 
and/or other materials provided with the distribution.

* Neither the name of VMware, Inc. nor the names of its contributors may be used
to endorse or promote products derived from this software without specific prior 
written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL VMWARE, INC. OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.
================================================================================*/

package com.doublecloud.vim25.mo.samples;

import com.doublecloud.vim25.VirtualMachineCapability;
import com.doublecloud.vim25.VirtualMachineConfigInfo;
import com.doublecloud.vim25.VirtualMachineQuickStats;
import com.doublecloud.vim25.mo.Folder;
import com.doublecloud.vim25.mo.InventoryNavigator;
import com.doublecloud.vim25.mo.ManagedEntity;
import com.doublecloud.vim25.mo.ServiceInstance;
import com.doublecloud.vim25.mo.VirtualMachine;
import com.doublecloud.vim25.mo.util.PropertyCollectorUtil;
import com.gwos.GwosServerConfiguration;

import java.net.URL;
import java.util.Hashtable;

public class HelloVM 
{
  public static void main(String[] args) throws Exception
  {
    long start = System.currentTimeMillis();
    //ServiceInstance si = new ServiceInstance(new URL("https://8.8.8.8/sdk"), "root", "password", true);
    ServiceInstance si = new ServiceInstance(new URL(GwosServerConfiguration.GROUNDWORK_VMWARE_SERVER), GwosServerConfiguration.GROUNDWORK_VMWARE_USERNAME, GwosServerConfiguration.GROUNDWORK_VMWARE_PASSWORD, true);
    long end = System.currentTimeMillis();
    System.out.println("time taken:" + (end-start));
    Folder rootFolder = si.getRootFolder();
    String name = rootFolder.getName();
    System.out.println("root:" + name);

    start = System.currentTimeMillis();
    String[][] props = new String[][] { {"VirtualMachine", "name" }, };
    String[] props2 = new String[] { "name", "summary.quickStats.balloonedMemory", "summary.quickStats.overallCpuUsage"};
    ManagedEntity[] mes = new InventoryNavigator(rootFolder).searchManagedEntities(props, true);
    if(mes==null || mes.length ==0)
    {
      return;
    }
    Hashtable[] result = PropertyCollectorUtil.retrieveProperties(mes, "VirtualMachine", props2);
    VirtualMachine vm = (VirtualMachine) mes[0];
    System.out.println("time taken:" + (System.currentTimeMillis() - start));

    String name2 = vm.getName();
    String name3 = (String)vm.getPropertyByPath("name");
    VirtualMachineConfigInfo vminfo = vm.getConfig();
    VirtualMachineCapability vmc = vm.getCapability();
    VirtualMachineQuickStats quikStats = vm.getSummary().getQuickStats();
    vm.getResourcePool();
    System.out.println("Hello " + vm.getName());
    System.out.println("GuestOS: " + vminfo.getGuestFullName());
    System.out.println("Multiple snapshot supported: " + vmc.isMultipleSnapshotsSupported());

    si.getServerConnection().logout();
  }

}
