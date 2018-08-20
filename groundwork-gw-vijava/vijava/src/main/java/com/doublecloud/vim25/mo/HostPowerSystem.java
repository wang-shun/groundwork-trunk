package com.doublecloud.vim25.mo;

import java.rmi.RemoteException;

import com.doublecloud.vim25.HostConfigFault;
import com.doublecloud.vim25.PowerSystemCapability;
import com.doublecloud.vim25.PowerSystemInfo;
import com.doublecloud.vim25.RuntimeFault;

public class HostPowerSystem extends ManagedObject
{
  public PowerSystemCapability getCapability()
  {
    return (PowerSystemCapability) getCurrentProperty("capability");
  }
  
  public PowerSystemInfo getInfo()
  {
    return (PowerSystemInfo) getCurrentProperty("info");
  }
  
  public void configurePowerPolicy(int key) throws HostConfigFault, RuntimeFault, RemoteException
  {
    getVimService().configurePowerPolicy(getMOR(), key);
  }
}