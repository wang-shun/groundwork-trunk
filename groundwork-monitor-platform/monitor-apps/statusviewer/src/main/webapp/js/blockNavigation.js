function blockNavigation() {
	jQuery.unblockUI();
	jQuery.blockUI( {
		message :'<h1>Please wait, processing your request ...</h1>'
	});
}

function unBlockNavigation() {
	alert('in unBlockNavigation');
	jQuery.unblockUI();
}