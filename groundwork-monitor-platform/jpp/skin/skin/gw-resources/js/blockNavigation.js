function blockNavigation() {
	// jQuery.blockUI.defaults.applyPlatformOpacityRules = false;
	jQuery.unblockUI();
	jQuery.blockUI( {
		message :'<h1>Please wait, processing your request ...</h1>'
	});
}