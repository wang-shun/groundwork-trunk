/*
 * Copyright 2012 GroundWork , Inc. ("GroundWork") 
 * All rights reserved. 
*/

package org.groundwork.cloudhub.exceptions;

public class ConnectorException extends RuntimeException
{
    public ConnectorException()
    {
    }

    public ConnectorException(String message)
    {
        super(message);
    }

    public ConnectorException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ConnectorException(Throwable cause)
    {
        super(cause);
    }

}