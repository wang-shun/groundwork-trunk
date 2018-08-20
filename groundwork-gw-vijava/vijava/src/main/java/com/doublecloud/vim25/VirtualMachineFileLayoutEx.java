/*================================================================================
Copyright (c) 2013 Steve Jin. All Rights Reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, 
this list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice, 
this list of conditions and the following disclaimer in the documentation 
and/or other materials provided with the distribution.

* Neither the names of copyright holders nor the names of its contributors may be used
to endorse or promote products derived from this software without specific prior 
written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.
================================================================================*/

package com.doublecloud.vim25;
import java.util.Calendar;

/**
* @author Steve Jin (http://www.doublecloud.org)
* @version 5.1
*/

@SuppressWarnings("all")
public class VirtualMachineFileLayoutEx extends DynamicData {
  public VirtualMachineFileLayoutExFileInfo[] file;
  public VirtualMachineFileLayoutExDiskLayout[] disk;
  public VirtualMachineFileLayoutExSnapshotLayout[] snapshot;
  public Calendar timestamp;

  public VirtualMachineFileLayoutExFileInfo[] getFile() {
    return this.file;
  }

  public VirtualMachineFileLayoutExDiskLayout[] getDisk() {
    return this.disk;
  }

  public VirtualMachineFileLayoutExSnapshotLayout[] getSnapshot() {
    return this.snapshot;
  }

  public Calendar getTimestamp() {
    return this.timestamp;
  }

  public void setFile(VirtualMachineFileLayoutExFileInfo[] file) {
    this.file=file;
  }

  public void setDisk(VirtualMachineFileLayoutExDiskLayout[] disk) {
    this.disk=disk;
  }

  public void setSnapshot(VirtualMachineFileLayoutExSnapshotLayout[] snapshot) {
    this.snapshot=snapshot;
  }

  public void setTimestamp(Calendar timestamp) {
    this.timestamp=timestamp;
  }
}