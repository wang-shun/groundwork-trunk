<?php
class Filter {
    private $Operator = null;
    private $LeftFilter = null;
    private $RightFilter = null;

    public function __construct($operator=null)
    {
        $this->Operator = $operator;
    }
    
    public function setLeftFilter($filter)
    {
        $this->LeftFilter = $filter;
    }
    
    public function setRightFilter($filter)
    {
        $this->RightFilter = $filter;
    }
}

class StringFilter {
	private $Operator = NULL;
	private $StringProperty = null;
    
	public function __construct($operator) {
	    $this->Operator = $operator;
	}
	
	public function setStringProperty($property) {
	    $this->StringProperty = $property;
	}
	
}

class IntegerFilter {
	private $Operator = NULL;
	private $IntegerProperty = null;
    
	public function __construct($operator) {
	    $this->Operator = $operator;
	}
	
	public function setIntegerProperty($property) {
	    $this->IntegerProperty = $property;
	}
}

class DateFilter {
	private $Operator = NULL;
	private $DateProperty = null;
    
	public function __construct($operator) {
	    $this->Operator = $operator;
	}
	
	public function setDateProperty($property) {
	    $this->DateProperty = $property;
	}    
}

class BooleanFilter {
	private $Operator = NULL;
	private $BooleanProperty = null;
    
	public function __construct($operator) {
	    $this->Operator = $operator;
	}
	
	public function setBooleanProperty($property) {
	    $this->BooleanProperty = $property;
	}    
}
?>