package com.groundwork.agents.vema.base;

public class VemaBaseObject
{
	public enum VemaObjectEnum { HOST, VM, BAG, }
	private String         name;
	private VemaObjectEnum type;
	VemaBaseHost           hosto;
	VemaBaseVM             vmo  ;
	VemaBaseGroup          groupo ;
	
    // ----------------------------------------------------------------------
    // this has to be RETIRED so that the following definition can work.
    // ----------------------------------------------------------------------
	public VemaBaseObject( String theName, VemaObjectEnum theType, VemaBaseHost vbh, VemaBaseVM vbvm )
	{
		this.name   = theName;
		this.type   = theType;
        this.hosto  = vbh;
        this.vmo    = vbvm;
	}

    // ----------------------------------------------------------------------
    // this will allow nesting of groups, and so on.
    // ----------------------------------------------------------------------
    public VemaBaseObject(String theName, VemaObjectEnum theType, VemaBaseHost vbh, VemaBaseVM vbvm, VemaBaseGroup vbg )
	{
		this.name   = theName;
		this.type   = theType;
        this.hosto  = vbh;
        this.vmo    = vbvm;
        this.groupo = vbg;
	}

	public VemaObjectEnum getType()   { return this.type;   }
	public VemaBaseHost   getHost()   { return this.hosto;  }
	public VemaBaseVM     getVM()     { return this.vmo;    }
    public VemaBaseGroup  getGroup()  { return this.groupo; }
	public String         getName()   { return this.name;   }
}
