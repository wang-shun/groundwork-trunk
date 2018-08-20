package com.groundwork.agents.vema.base;

import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;

public class VemaBaseObjectTree
{
	public class VBOTleaf
	{
		private String         name            = null;
		private String         parent          = null;
		private VemaBaseObject object          = null;
		private String         applicationType = null;
		private String         objectType      = null;
		private String         collectedBy     = null;

        VBOTleaf(String name, String parent, VemaBaseObject object, String applicationType, String objectType, String collectedBy )
        {
			this.name            = name;
			this.parent          = parent;
			this.object          = object;

            this.applicationType = applicationType;
            this.objectType      = objectType;
            this.collectedBy     = collectedBy;
		}
		
		VBOTleaf(){}  // IS OK to make them without stuff inside, too.
		
		public String         getName()             { return this.name;            }
		public String         getParent()           { return this.parent;          }
		public VemaBaseObject getObject()           { return this.object;          }
        public String         getApplicationType()  { return this.applicationType; }
        public String         getObjectType()       { return this.objectType;      }
        public String         getCollectedBy()      { return this.collectedBy;     }
	}

	// INTERNALLY - in order to keep a list of UNIQUE identifiers, we use a hash.
	// This is less efficient than just a "list", but in the end there's never a
	// need to remove duplicates from the list.  HOWEVER - we return it in the
	// list format though, so that upper level callers have an easier coding 
	// sequence.
	//
	private ConcurrentHashMap<String, ConcurrentHashMap<String, String>>parentTree = 
		new ConcurrentHashMap<String, ConcurrentHashMap<String, String>>();

	private ConcurrentHashMap<String, VBOTleaf> objectHash = 
		new ConcurrentHashMap<String, VBOTleaf>();
	
	private String ourApplicationType = null;
	private String ourCollectedBy     = null;
	
	public VemaBaseObjectTree( String applicationType, String collectedBy )
	{
		ourApplicationType = applicationType;
		ourCollectedBy     = collectedBy;
	}
	
    public ConcurrentHashMap<String, ArrayList<String>> getMembersOf()
    {
        ConcurrentHashMap<String, ArrayList<String>> results = new 
        ConcurrentHashMap<String, ArrayList<String>>();

        for( String key : parentTree.keySet() )
            results.put( key, new ArrayList<String>(parentTree.get( key ).keySet()) );

        return results;
    }

    public ConcurrentHashMap<String, ArrayList<String>> getMembersOf( String prefix )
    {
        List<String> prefixes = new ArrayList<String>( );  // this is a LITTLE silly, but 
        prefixes.add( prefix );                            // the overhead is pretty low
        return getMembersOf( prefixes );
    }

    public ConcurrentHashMap<String, ArrayList<String>> getMembersOf( List<String> prefixes )
    {
        ConcurrentHashMap<String, ArrayList<String>> results = new
        ConcurrentHashMap<String, ArrayList<String>>();

        for( String key : parentTree.keySet() )
            for( String prefix : prefixes )
                if( key.startsWith( prefix ) )
                {
                    if( !results.contains( key ) )
                    	results.put( key, new ArrayList<String>() );

                    results.get( key ).addAll( parentTree.get( key ).keySet() );

                    break;
                }

        return results;
    }

    // the reason we continue to pass in applicationType, objectType and collectedBy 
    // information is because we're potentially going to manage a larger list than
    // "just our information".
    
    public void addMember( String name, String parent, VemaBaseObject object, String applicationType, String objectType, String collectedBy )
    {
        if( !parentTree.contains( parent ) )
            parentTree.put( parent, new ConcurrentHashMap<String, String>() );
        
        parentTree.get( parent ).put( name, "" );
        
        if( !objectHash.contains( name ) )
        	objectHash.put( name, new VBOTleaf( name, parent, object, applicationType, objectType, collectedBy) );

    }
    
    public void delMember( String name, String parent, VemaBaseObject object, String applicationType, String objectType, String collectedBy )
    {
    	if( !parentTree.contains( parent ) )
    		return;
    	
    	if( parentTree.get( parent ).contains( name ) )
    		parentTree.get( parent ).remove( name );
    	
    	if( parentTree.get( parent ).keySet().size() == 0 )
    		parentTree.remove( parent );
    	
    	if( objectHash.contains( name ) )
    		objectHash.remove( name );
    }
    
    public ArrayList<VemaBaseObject> getOurVMList()           { return getOurListByOT( "VM" ); } 
    public ArrayList<VemaBaseObject> getOurHostList()         { return getOurListByOT( "HOST" ); } 
    public ArrayList<VemaBaseObject> getOurNetworkList()      { return getOurListByOT( "NETWORK" ); } 
    public ArrayList<VemaBaseObject> getOurStorageList()      { return getOurListByOT( "STORAGE" ); } 
    public ArrayList<VemaBaseObject> getOurResourcePoolList() { return getOurListByOT( "RESOURCE" ); } 
    public ArrayList<VemaBaseObject> getOurDatacenterList()   { return getOurListByOT( "DATACENTER" ); } 

    private ArrayList<VemaBaseObject> getListByOT( String objectType )
    {
    	ArrayList<VemaBaseObject> results = new ArrayList<VemaBaseObject>();
    	
    	for( VBOTleaf leaf : objectHash.values() )
    		if( leaf.objectType.equals( objectType ) )
    			results.add( leaf.getObject() );
    	
    	return results;
    }
    
    private ArrayList<VemaBaseObject> getOurListByOT( String objectType )
    {
        return getRestrictedList( objectType, ourApplicationType, ourCollectedBy );
    }
    
    private ArrayList<VemaBaseObject> getRestrictedList( String objectType, String applicationType, String collectedBy )
    {
    	ArrayList<VemaBaseObject> results = new ArrayList<VemaBaseObject>();
    	
    	for( VBOTleaf leaf : objectHash.values() )
    		if( leaf.objectType     .equals( objectType ) 
    		&&  leaf.applicationType.equals( applicationType )
    		&&  leaf.collectedBy    .equals( collectedBy )
    		)
    		{
    			results.add( leaf.getObject() );
    		}
    	
    	return results;
    }
}
