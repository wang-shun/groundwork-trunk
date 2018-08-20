package com.groundwork.agents.vema.base;

/**
 * Just an object to hold login-to-VMware server information;
 */

public class VemaBaseHostURI
{
	private String  uri;
	private String  username;
	private String  password;
	private String  vm;
	
	public VemaBaseHostURI( String theUri, String theUser, String thePassword, String theVm )
	{
		uri      = theUri;
		username = theUser;
		password = thePassword;
		vm       = theVm;
	}
	
	public void   setUri(      String theUri      )  { uri      = theUri;      }
	public void   setUser(     String theUser     )  { username = theUser;     }
	public void   setPassword( String thePassword )  { password = thePassword; }
	public void   setVm(       String theVm       )  { vm       = theVm;       }
	
	public String getUri()                           { return uri;             }
	public String getUser()                          { return username;        }
	public String getPassword()                      { return password;        }
	public String getVm()                            { return vm;              }
}
