package com.doublecloud.vim25.mo;

import com.doublecloud.vim25.ManagedObjectReference;

public class HostDirectoryStore extends HostAuthenticationStore
{
  public HostDirectoryStore(ServerConnection sc, ManagedObjectReference mor) 
  {
    super(sc, mor);
  }
}