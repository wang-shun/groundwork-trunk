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

package com.doublecloud.vim25.mo;

import java.rmi.RemoteException;

import com.doublecloud.vim25.FileFault;
import com.doublecloud.vim25.InvalidBundle;
import com.doublecloud.vim25.InvalidState;
import com.doublecloud.vim25.ManagedObjectReference;
import com.doublecloud.vim25.MismatchedBundle;
import com.doublecloud.vim25.RuntimeFault;

/**
 * The managed object class corresponding to the one defined in VI SDK API reference.
 * @author Steve JIN (http://www.doublecloud.org)
 */

public class HostFirmwareSystem extends ManagedObject 
{

  public HostFirmwareSystem(ServerConnection serverConnection, ManagedObjectReference mor) 
  {
    super(serverConnection, mor);
  }
  
  public String backupFirmwareConfiguration() throws RuntimeFault, RemoteException 
  {
    return getVimService().backupFirmwareConfiguration(getMOR());
  }
  
  public String queryFirmwareConfigUploadURL() throws RuntimeFault, RemoteException 
  {
    return getVimService().queryFirmwareConfigUploadURL(getMOR());
  }
  
  public void resetFirmwareToFactoryDefaults() throws InvalidState, RuntimeFault, RemoteException 
  {
    getVimService().resetFirmwareToFactoryDefaults(getMOR());
  }
  
  public void restoreFirmwareConfiguration(boolean force) throws InvalidBundle, MismatchedBundle, FileFault, InvalidState, RuntimeFault, RemoteException 
  {
    getVimService().restoreFirmwareConfiguration(getMOR(), force);
  }

}
