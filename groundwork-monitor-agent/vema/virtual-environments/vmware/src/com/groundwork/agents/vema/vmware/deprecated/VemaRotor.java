package com.groundwork.agents.vema.vmware.deprecated;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class VemaRotor
{
	private class VRObject
	{
		private Object   VRO_object;
		private long     VRO_interval;
		private long     VRO_created;
		private long     VRO_lastrun;
        private long     VRO_elapsed;
        private boolean  VRO_isrunning;
		
		VRObject(Object object, long repInterval)
		{
			VRO_object    = object; 
			VRO_interval  = repInterval;    // in sleep() units (millisec)
			VRO_created   = java.lang.System.currentTimeMillis();
			VRO_lastrun   = VRO_created;
            VRO_elapsed   = 0;
            VRO_isrunning = false;
		}

		public Object  getObject()   { return VRO_object;    }
		public long    getLastrun()  { return VRO_lastrun;   }
		public long    getCreated()  { return VRO_created;   }
		public long    getInterval() { return VRO_interval;  }
		public long    getElapsed()  { return VRO_elapsed;  }
        public boolean isRunning()   { return VRO_isrunning; }

        public long    untilAlarm()  // in milliseconds
        {
            long now = java.lang.System.currentTimeMillis();
            double xtime;

            if( VRO_interval <= 0 )   // if we're NOT supposed to wait
                return (long)0;       // ... then don't wait.

            xtime = ( now - VRO_created ) / VRO_interval;   // num intervals since create
            xtime = Math.floor( xtime );                    // get rid of fractional part
            xtime += 1.0;                                   // jump to NEXT interval
            xtime *= VRO_interval;                          // put it back to milliseconds;

            return ((long)xtime);
        }

        public void kill()
        {
            if( !VRO_isrunning )
            {
                // place to put the KILL THREAD code
                return;
            }

            VRO_isrunning = false;
        }

        public void run()  // the blocking version
        {
            long now = java.lang.System.currentTimeMillis();

            if( VRO_isrunning )
                return;

            VRO_lastrun = now;
            VRO_isrunning = true;

            try { java.lang.Thread.sleep( untilAlarm() ); }
            catch ( InterruptedException e )
            {
                // VRO_object.run();
                VRO_isrunning = false;
            }

            now = java.lang.System.currentTimeMillis();
            VRO_elapsed = now - VRO_lastrun;
            VRO_lastrun = now;
        }
	}

	List             <VRObject>         vrolist;  // object list to step through
    ConcurrentHashMap<VRObject,Boolean> running;  // if exists, is running.

    VemaRotor() // constructor
    {
    }

    public void add( Object obj, long interval )
    {
        vrolist.add( new VRObject( obj, interval ) );
    }
    
    public Object get( int index )
    {
        if( index < 0 || index >= vrolist.size() )
            return null;

        return vrolist.get(index);
    }

    // thoughts...
    // this of course needs to be non-blocking,
    // but also result in creating a thread,
    // waiting the interval, then letting the object
    // run.  "do its thing". 
    //
    // it also probably should block an attempt
    // at running the object simultaneously.  
    //
    public void schedule( int index ) throws Exception
    {
        VRObject vro = vrolist.get( index );

        if( vro == null )
            throw new Exception( "schedule(" + index + "): arg out of range" );

        if( running.get( vro ).booleanValue() )
            return;            // good spot for 'kill duplicate thread' stuff

        running.put( vro, Boolean.TRUE );
    }

    public void scheduleAll()
    {
        int index;
        for( index = 0; index < vrolist.size(); index++ )
        {
            try 
            { 
                schedule( index );
            }
            catch( Exception e )
            {
                System.out.println( e );
            }
            finally
            {
                // instigate a riot or something; contemplate infinity; eat a bagel.
            }
        }
    }
}
