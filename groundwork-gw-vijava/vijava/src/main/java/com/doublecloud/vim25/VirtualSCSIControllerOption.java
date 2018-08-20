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

/**
* @author Steve Jin (http://www.doublecloud.org)
* @version 5.1
*/

@SuppressWarnings("all")
public class VirtualSCSIControllerOption extends VirtualControllerOption {
  public IntOption numSCSIDisks;
  public IntOption numSCSICdroms;
  public IntOption numSCSIPassthrough;
  public VirtualSCSISharing[] sharing;
  public int defaultSharedIndex;
  public BoolOption hotAddRemove;
  public int scsiCtlrUnitNumber;

  public IntOption getNumSCSIDisks() {
    return this.numSCSIDisks;
  }

  public IntOption getNumSCSICdroms() {
    return this.numSCSICdroms;
  }

  public IntOption getNumSCSIPassthrough() {
    return this.numSCSIPassthrough;
  }

  public VirtualSCSISharing[] getSharing() {
    return this.sharing;
  }

  public int getDefaultSharedIndex() {
    return this.defaultSharedIndex;
  }

  public BoolOption getHotAddRemove() {
    return this.hotAddRemove;
  }

  public int getScsiCtlrUnitNumber() {
    return this.scsiCtlrUnitNumber;
  }

  public void setNumSCSIDisks(IntOption numSCSIDisks) {
    this.numSCSIDisks=numSCSIDisks;
  }

  public void setNumSCSICdroms(IntOption numSCSICdroms) {
    this.numSCSICdroms=numSCSICdroms;
  }

  public void setNumSCSIPassthrough(IntOption numSCSIPassthrough) {
    this.numSCSIPassthrough=numSCSIPassthrough;
  }

  public void setSharing(VirtualSCSISharing[] sharing) {
    this.sharing=sharing;
  }

  public void setDefaultSharedIndex(int defaultSharedIndex) {
    this.defaultSharedIndex=defaultSharedIndex;
  }

  public void setHotAddRemove(BoolOption hotAddRemove) {
    this.hotAddRemove=hotAddRemove;
  }

  public void setScsiCtlrUnitNumber(int scsiCtlrUnitNumber) {
    this.scsiCtlrUnitNumber=scsiCtlrUnitNumber;
  }
}