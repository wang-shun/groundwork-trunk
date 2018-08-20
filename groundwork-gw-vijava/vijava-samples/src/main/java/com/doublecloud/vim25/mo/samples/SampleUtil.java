package com.doublecloud.vim25.mo.util;

import com.gwos.GwosServerConfiguration;
import com.doublecloud.vim25.FileInfo;
import com.doublecloud.vim25.FileQueryFlags;
import com.doublecloud.vim25.HostDatastoreBrowserSearchResults;
import com.doublecloud.vim25.HostDatastoreBrowserSearchSpec;
import com.doublecloud.vim25.ManagedObjectReference;
import com.doublecloud.vim25.VirtualMachineCapability;
import com.doublecloud.vim25.VirtualMachineConfigInfo;
import com.doublecloud.vim25.VirtualMachineFileLayout;
import com.doublecloud.vim25.VirtualMachineFileLayoutSnapshotLayout;
import com.doublecloud.vim25.mo.Folder;
import com.doublecloud.vim25.mo.HostDatastoreBrowser;
import com.doublecloud.vim25.mo.HostSystem;
import com.doublecloud.vim25.mo.InventoryNavigator;
import com.doublecloud.vim25.mo.ManagedEntity;
import com.doublecloud.vim25.mo.ServiceInstance;
import com.doublecloud.vim25.mo.Task;
import com.doublecloud.vim25.mo.VirtualMachine;
import com.doublecloud.vim25.mo.VirtualMachineSnapshot;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;


public class SampleUtil
{
    public static void main(String[] args) throws Exception
    {
        long start = System.currentTimeMillis();
        //ServiceInstance si = new ServiceInstance(new URL("https://8.8.8.8/sdk"), "root", "password", true);
        ServiceInstance si = new ServiceInstance(new URL(GwosServerConfiguration.GROUNDWORK_VMWARE_SERVER), GwosServerConfiguration.GROUNDWORK_VMWARE_USERNAME, GwosServerConfiguration.GROUNDWORK_VMWARE_PASSWORD, true);
        Folder rootFolder = si.getRootFolder();
        String name = rootFolder.getName();
        System.out.println("root:" + name);
        ManagedEntity[] mes = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");
        if(mes==null || mes.length ==0)
        {
            return;
        }
        for (int ix=0; ix < mes.length; ix++) {
            VirtualMachine vv = (VirtualMachine) mes[ix];
            if (vv.getName().equals("qa-testlink")) {
                System.out.println("found: " + ix);
            }
        }

        VirtualMachine vm = (VirtualMachine) mes[0];
        VirtualMachineConfigInfo vminfo = vm.getConfig();
        VirtualMachineCapability vmc = vm.getCapability();

        vm.getResourcePool();
        System.out.println("Hello " + vm.getName());
        System.out.println("GuestOS: " + vminfo.getGuestFullName());
        System.out.println("Multiple snapshot supported: " + vmc.isMultipleSnapshotsSupported());

        for (int ix = 0; ix < mes.length; ix++) {
            VirtualMachine vmx = (VirtualMachine) mes[ix];
            VirtualMachineConfigInfo vmi = vmx.getConfig();
            System.out.println("-- vm: " + vmi.getName() + "/" + vmx.getName());

            long s2 = System.currentTimeMillis();
            HashMap<String, Long> snapshots = getMapOfVmSnapShotSizes(vmx, si);
            System.out.println("\nruntime: " + (System.currentTimeMillis() - s2));
//            for (Map.Entry e : snapshots.entrySet()) {
//                System.out.println("\t\t key: " + e.getKey() + " : " + e.getValue());
//            }
        }

        si.getServerConnection().logout();
        System.out.println("time taken:" + (System.currentTimeMillis() - start));

    }

    public static HashMap<String, Long> getMapOfVmSnapShotSizes(VirtualMachine vm_in_path, ServiceInstance s1)
            throws IOException
    {
        HashMap<String, Long> return_map = new HashMap<String, Long>();

        ManagedObjectReference hs_mor = vm_in_path.getRuntime().getHost();

        HostSystem hs = new HostSystem(s1.getServerConnection(), hs_mor);

        VirtualMachineFileLayout k = vm_in_path.getLayout();

        if (k != null)
        {
            VirtualMachineFileLayoutSnapshotLayout[] x = k.snapshot;

            if (x != null && x.length > 0)
            {
                HashSet<String> files_seen_so_far = new HashSet<String>();

                for (VirtualMachineFileLayoutSnapshotLayout snapshot_lay : x)
                {
                    VirtualMachineSnapshot p = new VirtualMachineSnapshot(s1.getServerConnection(), snapshot_lay.key);

                    String[] snap_shot_files = snapshot_lay.getSnapshotFile();

                    System.out.println("==========================");
                    System.out.println("Snapshot: " + p.toString() + " " +p.hashCode());
                    System.out.println("==========================");

                    long total_file_size = 0;

                    for (String j : snap_shot_files)
                    {
                        if (!files_seen_so_far.contains(j))
                        {
                            long file_size = SampleUtil.length(j,s1,hs);

                            total_file_size += file_size;

                            double current_file_size = (double)file_size / (double)SampleUtil.number_of_bytes_in_one_gb;

                            //System.out.printf("File: " + j + ". Size: %.6f GBn\n", current_file_size);
                            System.out.printf("File: " + j + ". Size: %d GBn\n", file_size);

                            files_seen_so_far.add(j);
                        }
                    }

                    return_map.put(p.getMOR().type + p.getMOR().val, new Long(total_file_size));
                }
            }
        }
        return return_map;
    }

    public static long length(String path, ServiceInstance si, HostSystem hs) throws IOException
    {
        String dsPath = path;
        String[] splitPath = dsPath.split("/");
        String fileName = splitPath[splitPath.length - 1];
        String filePath = path.substring(0, path.length() - fileName.length());

        HostDatastoreBrowser hdb = hs.getDatastoreBrowser();
        HostDatastoreBrowserSearchSpec searchSpec = new HostDatastoreBrowserSearchSpec();
        searchSpec.setMatchPattern(new String[]{fileName});
        FileQueryFlags queryFlags = new FileQueryFlags();
        //fileOwner has to be set (true or false) because of a bug in VI Java
        queryFlags.setFileOwner(false);
        queryFlags.setFileSize(true);
        queryFlags.setModification(true);
        queryFlags.setFileType(true);
        searchSpec.setDetails(queryFlags);
        Task task = hdb.searchDatastore_Task(filePath, searchSpec);
        try {
            task.waitForTask();
        } catch (Exception ex) {
            throw new IOException("Could not search file " + path + " ");
        }

        long file_size = 0;

        try
        {
            HostDatastoreBrowserSearchResults searchResults =
                    (HostDatastoreBrowserSearchResults) task.getTaskInfo().getResult();

            if (searchResults != null) {
                FileInfo[] fileInfo = searchResults.getFile();
                if (fileInfo == null || fileInfo.length == 0) {
                    throw (new IOException("File not found " + path));
                }
                file_size = fileInfo[0].fileSize;
            }
        }
        catch(java.rmi.RemoteException e)
        {
            // this could be a bug in the Java VI library

            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }

        return file_size;
    }


    public static final long number_of_bytes_in_one_gb = 102410241024L;
}