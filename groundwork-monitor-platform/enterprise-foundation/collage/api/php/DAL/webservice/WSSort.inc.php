<?php
class Sort {
    private $SortItem = array();
    
    public function __construct($sortItem) 
    {
        array_push($this->SortItem, $sortItem);
    }
    
    public function addSortItem($sortItem)
    {
        array_push($this->SortItem, $sortItem);
    }
    
    public function getSortItem()
    {
        return $this->SortItem;
    }
    
}

class SortItem 
{
    private $SortAscending;
    private $PropertyName;
    
    public function __construct($sortAsc, $propName)
    {
        $this->SortAscending = $sortAsc;
        $this->PropertyName = $propName;
    }
    
    public function getSortAscending()
    {
        return $this->SortAscending;
    }
    
    public function getPropertyName()
    {
        return $this->PropertyName;
    }
}
?>