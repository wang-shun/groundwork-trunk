// Configuration and some usefull methods

/**
 * Debug/Verbose
 * ----------------------------------------------------------------------------
 */
var debug_mode = !!casper.cli.get('verbose');
if (debug_mode) {
    debug_mode = true;
    casper.options.verbose = true;
    casper.options.logLevel = 'debug';
}

/**
 * The view
 * ----------------------------------------------------------------------------
 */

// The viewport size
casper.options.viewportSize = {
    width: 1024,
    height: 768
};

/**
 * Utils, XPath, FileSystem
 * ----------------------------------------------------------------------------
 */
var utils   = require('utils');
var x       = casper.selectXPath;
var fs      = require('fs');

/**
 * URLs
 * ----------------------------------------------------------------------------
 */
var url = casper.cli.get("url");
if (!/\/$/.test(url)) {
    // We haven't trailing slash: add it
    url = url + '/';
}

// Done for the test file
// ----------------------------------------------------------------------------
casper.test.done();

/**
 * Tear down and set up
 * ----------------------------------------------------------------------------
 */

// Tear down:
// - clear cookies
// - reset captures counter
casper.test.tearDown(function () {

    // Clear cookies
    casper.clearCookies();

    // Reset captures counter
    captures_counter = 0;
});

// Set up: nothing
casper.test.setUp(function () {});

/**
 * Steps
 * ----------------------------------------------------------------------------
 */

// AutoSteps for Resurrectio
var autostep = casper.cli.get("autostep");
//this.echo ("STEP" + captures_counter + ": " + Date.now())
// On step start
casper.on("step.start", function() {
    casper.capturePage();
});


/**
 * Tools and cool methods :')
 * ----------------------------------------------------------------------------
 */

// Clear cookies
casper.clearCookies = function () {
    casper.test.info("Clear cookies");
    casper.page.clearCookies();
};


// Print the current page title
casper.printTitle = function () {
    this.echo('### ' + casper.getTitle() + ' ###', 'INFO_BAR');
};

// Capture the current test page
var captures_counter = 0;
casper.capturePage = function (debug_name) {
    var directory = 'captures/' + casper.test.currentSuite.name;
    if (captures_counter > 0) {
        var previous = directory + '/step-' + (captures_counter-1) + '.jpg';
        if (debug_name) {
            var current = directory + '/step-' + captures_counter + '-' + debug_name + '.jpg';
        } else {
            var current = directory + '/step-' + captures_counter + '.jpg';
        }
        casper.capture(current);

        // If previous is same as current (and no debug_name), remove current
        if (!debug_name && fs.isFile(previous) && fs.read(current) === fs.read(previous)) {
            fs.remove(current);
            captures_counter--;
            casper.log('Capture removed because same as previous', 'warning');
        }
    }
    captures_counter++;
};
