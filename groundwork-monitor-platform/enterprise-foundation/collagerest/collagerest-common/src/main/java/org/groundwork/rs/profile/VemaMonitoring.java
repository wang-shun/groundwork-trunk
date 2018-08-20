package org.groundwork.rs.profile;

import java.io.Serializable;

@Deprecated
public class VemaMonitoring implements Serializable{
	
	private Hypervisor hypervisor = null;
	
	private VM vm = null;

	public Hypervisor getHypervisor() {
		return hypervisor;
	}

	public void setHypervisor(Hypervisor hypervisor) {
		this.hypervisor = hypervisor;
	}

	public VM getVm() {
		return vm;
	}

	public void setVm(VM vm) {
		this.vm = vm;
	}
	

}
