//
//Copyright 2007 GroundWork Open Source, Inc. ("GroundWork")
//All rights reserved. Use is subject to GroundWork commercial license terms. 
//


var refreshXMLhttp = createRequestObject();
var target;
var table;
var obj;
//document.onkeypress = keyhandler;
//document.LogfileTypeForm.newLogfileType.onkeypress = keyhandler;

//window.onkeydown = keyhandler;

if(refreshXMLhttp != undefined) {
//	alert("Successfully Created Refresh XML Object For Window.");
	//refreshXMLhttp.overrideMimeType('text/xml');
}
else {
	alert("Failed to create Refresh XML Object For Window.");
} 			 				


function  check_submit(event,myForm,struct,action,options){
	//alert("fired check_submit");
	if(event && event.which == 13){
		sendDataReq(struct,action,options);
	}
	else{
		return true;
	}
}

function refreshGrid(){
		//alert("Refreshing...");
        obj.setStatusProperty("code", "loading"); 
        obj.refresh();
        var randomNum = Math.floor(Math.random()*1000000)
        myURL = "/log-reporting/bin/reports.pl?struct=LogMessageFilter&action=listXML&nocache=" + randomNum
  		table.setURL(myURL);
        table.request();
        obj.setStatusProperty("code", ""); 
        obj.refresh(); 
}

function setComponentOptions(componentArray) {

document.forms['messageFilterForm'].myComponents.options.length = 0;
var selectBox = document.forms['messageFilterForm'].myComponents;

var i;
if(componentArray != null){
for(i = 0;i< componentArray.length;i++){
	selectBox.options[i] = new Option(componentArray[i]);
	}
}
}


function addComponent(option){
    myComps = 	document.forms['messageFilterForm'].myComponents;
    allComps = document.forms['messageFilterForm'].AllComponents;
	myComps.options[myComps.options.length] = new Option(allComps.options[allComps.selectedIndex].value);
}

function removeComponent(option){
    myComps = 	document.forms['messageFilterForm'].myComponents;
    allComps = document.forms['messageFilterForm'].AllComponents;
    
	myComps.options[myComps.selectedIndex] =  null;

}

function editLMT(){
var selectBox = document.forms['LogMessageTypeDel'].selection;
var selectedLMT = selectBox.options[selectBox.options.selectedIndex].value;
sendDataReq('LogMessageTypeEdit','edit',selectedLMT);
}
function editLFT(){
var selectBox = document.forms['LogfileTypeDel'].selection;
var selectedLFT = selectBox.options[selectBox.options.selectedIndex].value;
 
sendDataReq('lftFilter','edit',selectedLFT);
}

function editLMC(){
var selectBox = document.forms['LogMessageClassDel'].selection;
var selectedLMT = selectBox.options[selectBox.options.selectedIndex].value;
sendDataReq('LogMessageClassEdit','edit',selectedLMT);
}

 

function saveLMT(){
var selectBox = document.forms['LogMessageTypeDel'].selection;
var selectedLMT = selectBox.options[selectBox.options.selectedIndex].value;
var sendVals = selectedLMT + ',' + document.forms['LMTEditForm'].persistence.value + ',' + document.forms['LMTEditForm'].grouping.value; 
sendDataReq('LogMessageTypeEdit','save',sendVals);
}



function saveLMC(){
var selectBox = document.forms['LogMessageClassDel'].selection;
var selectedLMC = selectBox.options[selectBox.options.selectedIndex].value;
var sendVals = selectedLMC + ',' + document.forms['LMCEditForm'].persistence.value + ',' + document.forms['LMCEditForm'].grouping.value; 
sendDataReq('LogMessageClassEdit','save',sendVals);
}

function keyhandler(e){

	var targ;
	if (!e) var e = window.event;
	if (e.target) targ = e.target;
	else if (e.srcElement) targ = e.srcElement;
	if (targ.nodeType == 3) // defeat Safari bug
		targ = targ.parentNode;
		
	alert("target: " + targ.name);
 
}
 	 
function setTitle(){
     
	var selectBox = document.forms['LogMessageTypeDel'].selection;
	document.getElementById('messageTypeTitle').innerHTML =   selectBox.options[selectBox.options.selectedIndex].value;
	document.getElementById('LMTEditBox').style.display = '';

}

 function getlistOpt(){
 	var myComps = document.forms['messageFilterForm'].myComponents.options;
	var myOpts;
 	if(myComps.length > 0){ 
 		myOpts = myComps[0].text;
 	 }
	for(var i=1;i<myComps.length;i++){
	    if(myComps[i].text != ''){
			myOpts = myOpts + ',' + myComps[i].text;
			}
 		}
 		
	 listOpt =      document.forms['messageFilterForm'].isEnabled.value + 'ZZZ' +	 	
			document.forms['messageFilterForm'].parsingRuleID.value + 'ZZZ' +
		    document.forms['messageFilterForm'].regex.value + 'ZZZ' +
    	 	 myOpts + 'ZZZ' +
    	 	document.forms['messageFilterForm'].messageTypes.value + 'ZZZ' +
    	 	document.forms['messageFilterForm'].logTypes.value + 'ZZZ' +
    	 	document.forms['messageFilterForm'].filterName.value + 'ZZZ' + 
    	 	document.forms['messageFilterForm'].severity.value;
    	 	}
 	
function getOpt(){
	 listOpt =      	 	
			document.forms['dbControlForm'].dbName.value + 'ZZZ' +
		    document.forms['dbControlForm'].host.value + 'ZZZ' +
    	 	document.forms['dbControlForm'].user.value + 'ZZZ' +
    	 	document.forms['dbControlForm'].password.value;
    	 	}
 	
function createRequestObject() {
	try {
		xmlObject=new ActiveXObject("Msxml2.XMLHTTP");
	} catch (error) {
		//alert("Failed to create Msxml2.XMLHTTP Object For IE");
		try {
			xmlObject=new ActiveXObject("Microsoft.XMLHTTP");
		} catch (error) {
		//	alert("Failed to Create Microsoft.XMLHTTP Object");
			xmlObject=null;
		}
	}
	if(!xmlObject && typeof XMLHttpRequest != "undefined")
		xmlObject = new XMLHttpRequest();
		if(!xmlObject){alert("couldnt do this either");}
	return xmlObject;
    
}
    

function selectValueSet(SelectName, Value) {
  eval('SelectObject = document.forms["messageFilterForm"].' + 
     SelectName + ';');
 //alert('selectValue = ' + Value);
   // SelectObject = document.forms['messageFilterForm'].messageTypes;
    //alert('selectLength = ' + SelectObject.length);
   // alert('document.forms["messageFilterForm"].' + SelectName + ';');
  for(index = 0; 
    index < SelectObject.length; 
    index++) {
    //alert(SelectObject[index].value + '=' + Value);
   if(SelectObject[index].value == Value){
	     SelectObject.selectedIndex = index;
	     }
   }
}


function URLencode(sStr) {
    return escape(sStr).
             replace(/\+/g, '%2B').
                replace(/\"/g,'%22').
                   replace(/\'/g, '%27'); //.
                     //.replace(/\//g,'%2F');
  }

function sendDataReq(struct,action,option) {
		//alert("Starting View Refresh Request.");
		//alert('option =' + option);
		
		//Random num for IE Caching BS
		result = Math.random();
		myRand = Math.floor(result * 9999999) + 1;
		
	 	var send = '';
		send = URLencode(option);
		request = "reports.pl?struct=" + struct + "&action=" + action + "&listOption=" + send + "&nocache=" + myRand;
		 //alert('request= ' + request);
		target = struct;
 		refreshXMLhttp.open('GET', request, true);
		refreshXMLhttp.onreadystatechange = handleResponse;                
        refreshXMLhttp.setRequestHeader("Referer","/log-reporting");
		refreshXMLhttp.send(null);
}
 
function handleResponse() {
   if(refreshXMLhttp.readyState == 4){
				responseHTML = refreshXMLhttp.responseText
			if(refreshXMLhttp.status == 200) {
			//	alert("Successfully received Component Refresh Response XML.");
				  if(!responseHTML) { alert("No response from the Server. Please try again or refer to the error logs.");}
				  if(element = document.getElementById(target)) {
				//  	element.parentNode.innerHTML = responseHTML;
				// 	alert(element.innerHTML);
				 // 	alert("Attempting to fill " + element.nodeName + " node with content: " + responseHTML);
					element.innerHTML = responseHTML;
				  }
				  else {
					  alert('Unable to find element with id: ' + target);
				  }
			//	if(table){table.request();}
			 }
			else{
				alert("An Error has Occurred: " + refreshXMLhttp.status + responseHTML);
			}
		}
}

function toggleModified(ROW_NAME)
{
 
var row = document.getElementById(ROW_NAME); 
row.style.display = '';
//row.style.display == 'none' ? row.style.display ='': row.style.display = 'none';
}  

function handleThis()
{ 
//var index = this.getSelectionProperty("index");
  //  var row = src.getRowProperty("index");
  	var row = obj.getCurrentRow();
  	var index = row.getItemProperty("index");
    var count = row.getItemProperty("count");
    var components = row.getItemProperty("text",row,5);
    var regex = row.getItemProperty("text", row, 4);
    var name = row.getItemProperty("text",row,2);
    var mType = row.getItemProperty("text",row,1);
    var fType = row.getItemProperty("text",row,0);
    var parsingRuleID = row.getItemProperty("text",row,3);
    document.forms['messageFilterForm'].parsingRuleID.value = parsingRuleID;
    document.forms['messageFilterForm'].regex.value = regex;
    //document.forms['messageFilterForm'].components.value = components;
  selectValueSet('messageTypes',mType);
  selectValueSet('logTypes',fType);
//   document.forms['messageFilterForm'].messageTypes.value = mType;
  // document.forms['messageFilterForm'].logTypes.value = fType;
  
    document.forms['messageFilterForm'].filterName.value = name;
    
   // sendDataReq('LogMessageFilter','getRegex',ruleID);"
   //select * from ParsingRule_ComponentType where parsingRuleID = 1 


}

 

function pop(url, w, h, scroll) {
if (scroll != 1) { scroll = 0; }
openWin = window.open(url,"pop","toolbar=0,scrollbars=" + scroll +
",status=yes,width=" + w + ",height=" + h + ",resizable=yes");
openWin.focus();
}
