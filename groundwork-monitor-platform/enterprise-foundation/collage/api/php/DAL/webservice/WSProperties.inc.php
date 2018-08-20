<?php
// we have to have one property class for each property type in fwsmodel
class Property {
    private $name;
    private $value;
    
    public function __construct($prop_name, $prop_value) {
        $this->name = $prop_name;
        $this->value = $prop_value;
    }
}

?>