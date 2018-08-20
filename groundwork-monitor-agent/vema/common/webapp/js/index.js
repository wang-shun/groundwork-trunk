function newConnection(){
	
	var vmware = document.getElementById("vmware");
	
	if(vmware.checked) {
		location.href="/cloudhub/vmware/testConnection";
	}
	else
	{
		location.href="/cloudhub/rhev/testConnection";
	}
	
	return false;
}