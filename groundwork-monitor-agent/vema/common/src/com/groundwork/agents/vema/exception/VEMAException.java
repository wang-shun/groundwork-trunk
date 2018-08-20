/*
 * Copyright 2012 GroundWork , Inc. ("GroundWork") 
 * All rights reserved. 
*/

package com.groundwork.agents.vema.exception;

public class VEMAException extends RuntimeException
{
    public VEMAException() 
    {
    }

    public VEMAException(String message)
    {
        super(message);
    }

    public VEMAException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public VEMAException(Throwable cause)
    {
        super(cause);
    }

}