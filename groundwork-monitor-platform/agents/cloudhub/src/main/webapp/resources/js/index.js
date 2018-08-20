function newConnection(){
    var vmware = document.getElementById("vmware");
    var openstack = document.getElementById("openstack");
    if (vmware.checked) {
        location.href="/cloudhub/mvc/vmware2/navigateCreateConnection";
    }
    else
    {
        if (openstack.checked) {
            location.href="/cloudhub/mvc/openstack/navigateCreateConnection";
        }
        else {
            location.href="/cloudhub/mvc/rhev/navigateCreateConnection";
        }
    }
    return false;
}

function confirmDelete(e) {
	var r=confirm("Deletion will remove the configuration and all \nthe hosts and services for the configuration.\nThe delete action cannot be recovered back.\n\nAre you sure you want to delete the configuration?");
	
	if (r==true)
	{
		while(e.id != "deleteHiddenBtn") {
			e = e.nextSibling;
		}
		e.onclick();
	}
	else
	{
		document.getElementById("result").innerHTML = "";
		return false;
	} 	
}

$(document).ready(function() {
 	$('#example').dataTable();
 } );
 

 function toggle(e, fileName, filePath) {
	 var label = e.value;
	 
	 var currentStatus = (label.indexOf('Start')!=-1)?"start":"end";
	 var url = '/cloudhub/mvc/changeServerStatus?currentStatus=' + currentStatus + "&fileName=" + fileName + "&filePath=" + filePath;
     $("body").css("cursor", "progress");
     $("form :input").attr("disabled", true);
	 $.ajax(
             {
                 type: "GET",
                 url: url,
                 success: function (data) {
                     $("body").css("cursor", "default");
                     $("form :input").attr( "disabled", false );
                	 if (data === "success") {
                         if (label.indexOf('Start') != -1) {
                             e.value = 'Stop';
                             var startStopServerImg = prev(e);
                             startStopServerImg.className = "greencircle";
                         }
                         else {
                             e.value = 'Start';
                             var startStopServerImg = prev(e);
                             startStopServerImg.className = "redcircle";
                         }
                     }
                     else {
                         $("body").css("cursor", "default");
                         $("form :input").attr("disabled", false);
                         alert("Cannot start agent. Please test connection from configuration screen. Message: " + data);
                     }
                 },
                 error: function (msg, url, line) {
                     alert("Sorry some error occurred while starting the server.\nPlease check if the configuration is correct and the profile has been created.");

                 }
             });        	 
 }
 
 function next(elem) {
	    do {
	        elem = elem.nextSibling;
	    } while (elem && elem.nodeType !== 1);
	    return elem;        
	}

 function prev(elem) {
	    do {
	        elem = elem.previousSibling;
	    } while (elem && elem.nodeType !== 1);
	    return elem;        
	}

 function setEditedConfigObj(obj) {        	 
	 document.getElementById("configObg").value = obj;
 }