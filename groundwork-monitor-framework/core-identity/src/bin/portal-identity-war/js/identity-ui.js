 <!-- JQuery to show/hide a partial list -->
 var jbpnamespace = '_jbpns_2fgroundwork_2dmonitor_2fadmin_2fusers_2fIdentityAdminPortletWindowsnpbj:j_id';

 function showHideList(seq, formname, val) {		
	   if (val != 'P') {					
			document.getElementById(jbpnamespace + seq + ':' + formname + ':partialList').style.display="none";
		}
		else	{			
			document.getElementById(jbpnamespace + seq + ':' + formname + ':partialList').style.display="";
		} // end if
		return true;
	 }
 
 function onLoadShowHideList(seq, formname)
 {
	  if (document.getElementById(jbpnamespace + seq + ':' + formname + ':restrictionradio:1').checked == 1) {
		 // alert(document.getElementById(jbpnamespace + seq + ':' + formname
			// + ':partialList'));
		 // alert(document.getElementById(jbpnamespace + seq + ':' + formname
			// + ':partialList').style.display);
		 document.getElementById(jbpnamespace + seq + ':' + formname + ':partialList').style.display="";
	 }
	 else	 {
		 document.getElementById(jbpnamespace + seq + ':' + formname + ':partialList').style.display="none";
	 }
 }
 
 function selectAll(seq,formname)
 {
	 if (document.getElementById(jbpnamespace + seq + ':' + formname + ':restrictionradio:1').checked == 1) {
		 var str= "";
		 var elem = document.getElementById(jbpnamespace + seq + ':' + formname).elements;		
		 for(var i = 0; i < elem.length; i++) { 
			 //str += "Type: "  + elem[i].type + " "; 
			 //str += "ID: " + elem[i].id + " "; 
			 //str += "Value: "  + elem[i].value + " "; 
			 //str += "<BR>"; 
			 var type = elem[i].type;
			 if (type == 'checkbox') {
				 var id = elem[i].id;
				 if (document.getElementById(id).checked == 1)	{
					 document.getElementById(id).checked="";
				 } 
				 else {
					 document.getElementById(id).checked="checked";
				 } // end if
			 } // end if
		 }  // end for		
	 }
 }
 
 // Array contains helper method
 Array.prototype.contains = function(obj) {
	    var i = this.length;
	    while (i--) {
	        if (this[i] == obj) {
	            return true;
	        }
	    }
	    return false;
	}

 
 function validateRoleInfo(seq, formname) {		
	 //alert('validate!');
	 if (document.getElementById(jbpnamespace + seq + ':' + formname + ':restrictionradio:1').checked == 1) {
		 var str= "";
		 var elem = document.getElementById(jbpnamespace + seq + ':' + formname).elements;
		
		 var hgSelectedCount= 0;
		 var sgSelectedCount = 0;
		 var hgSelected=new Array();
		 var sgSelected=new Array();
		 var hgDefaultSelected="";
		 var sgDefaultSelected="";
		 for(var i = 0; i < elem.length; i++) { 
			 //str += "Type: "  + elem[i].type + " "; 
			 //str += "ID: " + elem[i].id + " "; 
			 //str += "Value: "  + elem[i].value + " "; 
			 //str += "<BR>"; 
			 var id = elem[i].id;
			 var type = elem[i].type;
			 if (type == 'checkbox' && id.indexOf('hgmultiselect') != -1) {				 
				 if (document.getElementById(id).checked == 1)	{
					 hgSelected[hgSelectedCount] = elem[i].value;
					 hgSelectedCount = hgSelectedCount + 1;
				 } // end if
			 } // end if
			 if (type == 'checkbox' && id.indexOf('sgmultiselect') != -1) {				 
				 if (document.getElementById(id).checked == 1)	{
					 sgSelected[sgSelectedCount] = elem[i].value;
					 sgSelectedCount = sgSelectedCount + 1;
				 } // end if
			 } // end if
			 if (type == 'radio' && id.indexOf('hgradio') != -1) {				 
				 if (document.getElementById(id).checked == 1)	{
					 hgDefaultSelected = elem[i].value;					 
				 } // end if
			 } // end if
			 if (type == 'radio' && id.indexOf('sgradio') != -1) {				 
				 if (document.getElementById(id).checked == 1)	{
					 sgDefaultSelected = elem[i].value;	
				 } // end if
			 } // end if
		 }  // end for
		 if (hgSelectedCount >= 1 || sgSelectedCount>=1) {			 
			 if (hgSelectedCount >=1 && !hgSelected.contains(hgDefaultSelected))
			 {
				 alert("Please select a default hostgroup!") ;
				 return false;
			 } // end if
			 if (sgSelectedCount >=1 && !sgSelected.contains(sgDefaultSelected))
			 {
				 alert("Please select a default servicegroup!") ;
				 return false;
			 } // end if
			 return true;
		 }
		 else {
			 alert("Atleast one HostGroup or ServiceGroup should be selected for Partial Restriction Type!"); 
			 return false;
		 } // end if	 
	 }
	 return true;
	 }
 
