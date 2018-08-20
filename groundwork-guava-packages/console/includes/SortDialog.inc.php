<?php
/*
Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
All rights reserved. Use is subject to GroundWork commercial license terms. 
*/ 

/**
 * Provides the Dialog for advanced sorting for the console
 * 
 * @author Robin Dandridge
 */
class SortDialog extends Dialog {
	
	// select boxes that contain the possible columns to select from
	private $sortColOne;
	private $sortColTwo;
	// button to submit selections
	private $submitButton;
	// button to cancel sort
	private $cancelButton;
	// pointer to console
	private $console;
	// names of the columns
	private $colNames;
	// the current selection
	private $totalSelection = "SORT BY ";
	// text of column One
	private $colOneName;
	// text of column two
	private $colTwoName;
	
	public function __construct($console) {
		parent::__construct();

		$this->console = $console;
		$this->submitButton = new Button("Submit");
		$this->submitButton->addClickListener("click", $this, "submitClickHandler");
		$this->cancelButton = new Button("Cancel");
		$this->cancelButton->addClickListener("click", $this, "cancelClickHandler");
		$this->sortColOne = new Select();
		$this->sortColOne->addClickListener("click", $this, "colOneClickHandler");
		$this->sortColTwo = new Select();
		$this->sortColTwo->addClickListener("click", $this, "colTwoClickHandler");
		$this->colNames = $this->console->getColumnNames();
		$counter = 0;
		foreach($this->colNames as $name) {
			$this->sortColOne->AddOption($counter, $name);
			$this->sortColTwo->AddOption($counter, $name);
			$counter++;
		}
	}
	
	public function unregister() {
		parent::unregister();
		$this->submitButton->unregister();
		$this->cancelButton->unregister();
		$this->sortColOne->unregister();
		$this->sortColTwo->unregister();
	}
	
	public function submitClickHandler() {
		$this->hide();
		$this->console->sort();
		$this->unregister();
	}
	
	public function cancelClickHandler() {
		$this->hide();
		$this->console->sort();
		$this->unregister();
	}
	
	public function getFirstSortOption() {
		return $this->colNames[$this->sortColOne->getValue()];
	}
	
	public function getSecondSortOption() {
		return $this->colNames[$this->sortColTwo->getValue()];
	}
	
	public function colOneClickHandler() {
		$this->colOneName = $this->colNames[$this->sortColOne->getValue()];
		$this->totalSelection .= $this->colOneName ." ";
	}
	
	public function colTwoClickHandler() {
		$this->colTwoName = $this->colNames[$this->sortColTwo->getValue()];
		$this->totalSelection .= $this->colTwoName;
	}
	
	public function Draw() {
		?>
		<table border="0" style="width:480px; margin:24px 16px 20px;" cellpadding="10" cellspacing="0">
			<tr>
			<tr>
				<td colspan="3" style="padding-left:0px;">
					<h1>Advanced Sorting Options</h1>
				</td>
			</tr>
			
			<tr style="
					">
					
				<td style="
					width:33%;
					height:164px;
					border-top:1px solid #cccccc;
					border-left:1px solid #cccccc;
					border-bottom:1px solid #cccccc;
				">
				Select the column you would like to sort by:
				</td>
				
				<td style="
					width:33%;
					border-top:1px solid #cccccc;
					border-bottom:1px solid #cccccc;
				">
				Primary:
				<?php $this->sortColOne->Draw(); ?>
				</td>
				<td style="
					width:33%;
					border-top:1px solid #cccccc;
					border-bottom:1px solid #cccccc;
					border-right:1px solid #cccccc;
				">
				Secondary:
				<?php $this->sortColTwo->Draw(); ?>
				</td>
			</tr>
			<tr>
				<td colspan="3" style="text-align:right; padding-right:0px;">
					<?php $this->submitButton->Draw(); ?>
					<?php $this->cancelButton->Draw(); ?>
				</td>
			</tr>
		</table>
		<?php
	}
	
}	
?>