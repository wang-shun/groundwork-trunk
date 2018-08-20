<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/ 

class EventCheckBox extends InputCheckBox
{
    private $eventId;

    public function setEventId($eventId)
    {
        $this->eventId = $eventId;
    }
    
    public function getEventId()
    {
        return $this->eventId;
    }
}
?>
