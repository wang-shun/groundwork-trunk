/*
 * Copyright 2012 GroundWork , Inc. ("GroundWork") 
 * All rights reserved. 
*/

package org.groundwork.cloudhub.exceptions;

public class RateExceededException extends ConnectorException
{
    public RateExceededException()
    {
    }

    public RateExceededException(String message)
    {
        super(message);
    }

    public RateExceededException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public RateExceededException(Throwable cause)
    {
        super(cause);
    }

}