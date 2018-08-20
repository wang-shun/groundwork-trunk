package org.groundwork.agents.monitor;

public enum ConnectionState
{
    NASCENT,       // -> connecting
    CONNECTING,    // -> connected | timedout
    CONNECTED,     // -> disconnected
    TIMEDOUT,      // -> timedout | failed
    FAILED,        // -> failed
    DISCONNECTED,  // -> connecting
    SEMICONNECTED  // -> connected   (in multihost case)
};