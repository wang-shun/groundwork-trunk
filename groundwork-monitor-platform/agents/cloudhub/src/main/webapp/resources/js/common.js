function setFormAction(form, action) {
    checkHostNameChange(form, action, successFormAction);
}

function successFormAction(form, action) {
    form = "#" + form;
    $('input').each(function(idx, field){
        //var trimmed = field.value.trim();
        if (field.type == "hidden") {
            // console.log("skipping hidden " + field.id);
        }
        else {
            var trimmed = field.value.replace(/^\s+|\s+$/gm, '');
            field.value = trimmed;
        }
    });
    $(form).attr("action", action);
    $(form).submit();
}

function testConnection(form, action) {
    var testConWaitMsg = document.getElementById("testConWaitMsg");
    testConWaitMsg.style.display = "block";
    var testConResultMsg = document.getElementById("testConResultMsg");
    testConResultMsg.style.display = "none";
    var testConnectionBtn = document.getElementById("testConnectionBtn");
    testConnectionBtn.className = "disabledHubButton";
    var saveButton = document.getElementById("saveButton");
    saveButton.disabled = true;
    successFormAction(form, action);
    $("body").css("cursor", "progress");
    $("form :input").attr( "disabled", true );
}

function hypMonitorOnChangeCheckbox (checkbox) {
    var id = checkbox.id;
    var index = id.substring(id.lastIndexOf("_")+1,id.length);
    document.getElementById('hyp_graphed_'+index).checked =
        (checkbox.checked ? 'checked' : false);
}
function vmMonitorOnChangeCheckbox (checkbox, prefix) {
    var id = checkbox.id;
    prefix = prefix || 'vm';
    var index = id.substring(id.lastIndexOf("_")+1,id.length);
    document.getElementById(prefix + '_graphed_'+index).checked =
        (checkbox.checked ? 'checked' : false);
}

function hypGraphOnChangeCheckbox (checkbox) {
    var id = checkbox.id;
    var index = id.substring(id.lastIndexOf("_")+1,id.length);
    if (checkbox.checked)
        document.getElementById('hyp_monitored_'+index).checked = 'checked';
}

function vmGraphOnChangeCheckbox (checkbox, prefix) {
    var id = checkbox.id;
    prefix = prefix || 'vm';
    var index = id.substring(id.lastIndexOf("_")+1,id.length);
    if (checkbox.checked)
        document.getElementById(prefix + '_monitored_'+index).checked = 'checked';
}

function checkHostNameChange(form, action, callback) {
    var hostName = $('#groundwork\\.server\\.name').val();
    hostName = hostName.replace(/^\s+|\s+$/gm, '');
    if (hostName == null || hostName.length === 0) {
        // short circuit this check, let backend handle validation
        callback(form, action);
        return;
    }
    var url = '/cloudhub/mvc/hostNameChanged?hostName=' + hostName;
    $.ajax(
        {
            type: "GET",
            url: url,
            success: function (data) {
                if (data === "<<nochange>>") {
                    $("body").css("cursor", "progress");
                    callback(form, action);
                    $("form :input").attr( "disabled", true );
                }
                else {
                    if (confirm("You are modifying the Groundwork Host Name. This can cause orphaned records in monitoring database." +
                          " Are you sure you want to modify the host name from " + data + " to " + hostName + "?")) {
                        $("body").css("cursor", "progress");
                        callback(form, action);
                        $("form :input").attr( "disabled", true );
                    }
                }
            },
            error: function (msg, url, line) {
                alert("Sorry some error occurred while retrieving hostName verification.");
            }
        });
}

function postTestConnection(form, action, appType, callback) {
    var testConWaitMsg = document.getElementById("testConWaitMsg");
    testConWaitMsg.style.display = "block";
    var testConResultMsg = document.getElementById("testConResultMsg");
    testConResultMsg.style.display = "none";
    var saveButton = document.getElementById("saveButton");
    saveButton.disabled = true;
    $("#next").attr("disabled", true);

    form = "#" + form;
    $('input').each(function(idx, field){
        //var trimmed = field.value.trim();
        if (field.type == "hidden") {
            // console.log("skipping hidden " + field.id);
        }
        else {
            var trimmed = field.value.replace(/^\s+|\s+$/gm, '');
            field.value = trimmed;
        }
    });
    $("body").css("cursor", "progress");
    $(form).attr("action", action);
    $.ajax(
        {
            type: "POST",
            url: action,
            data: $(form).serialize(),
            success: function (data) {
                var testConWaitMsg = document.getElementById("testConWaitMsg");
                testConWaitMsg.style.display = "none";
                var testConResultMsg = document.getElementById("testConResultMsg");
                testConResultMsg.style.display = "block";

                $("body").css("cursor", "default");
                $("form :input").attr( "disabled", false );
                $("#common\\.createProfileDisabled").val("false");
                var request = jQuery.parseJSON(data);
                var container = $("#testConResultMsg");
                container.empty();
                if (request.result === "success") {
                    $("#next").attr("disabled", false);
                    var msgDiv = $( "<div id='messages' class='message'><p class='message'>Connection successful! " + request.errorMessage + "</p></div>" );
                    $( "#testConResultMsg" ).append( msgDiv );
                }
                else if (request.result == "gwoserror") {
                    $("#next").attr("disabled", true);
                    var msgDiv = $( "<div id='messages' class='redMessage'><p>GWOS connection failed!</p><p>"
                    + request.errorMessage + "</p></div>");
                    $( "#testConResultMsg" ).append( msgDiv );
                }
                else {
                    var msgDiv = $( "<div id='messages' class='redMessage'><p>" + appType + " server connection failed!</p><p>"
                    + request.errorMessage + "</p></div>");
                    $( "#testConResultMsg" ).append( msgDiv );
                }
                if (callback) {
                    callback();
                }
            },
            error: function (msg, url, line) {
                var testConWaitMsg = document.getElementById("testConWaitMsg");
                testConWaitMsg.style.display = "none";
                var testConResultMsg = document.getElementById("testConResultMsg");
                testConResultMsg.style.display = "block";
                $("body").css("cursor", "default");
                $("form :input").attr( "disabled", false );
                $("#next").attr("disabled", true);
                $("#common\\.createProfileDisabled").val("false");
                var container = $("#testConResultMsg");
                container.empty();
                var msgDiv = $( "<div id='messages' class='redMessage'><p>Server connection failed! Ensure that Groundwork server is up and running.</p></div>");
                $( "#testConResultMsg" ).append( msgDiv );
                if (callback) {
                    callback();
                }
            }
        });
}

function collectProfileStateStringRow(name, obj) {
    var rowString = "";
    obj.find("input[name^='" + name + "']").each(function() {
        if ($(this).attr("type") === "checkbox") {
            var checked = $(this).attr("checked");
            if (checked === "checked" || checked === true) {
                rowString += "true|";
            } else {
                rowString += "false|";
            }
        } else {
            rowString += $(this).val() + "|";
        }
    });
    if (rowString != "") {
        rowString += "&";
    }
    return rowString;
}

function collectProfileStateString() {
    var stateString = "";
    $("tr").each(function() {
        stateString += collectProfileStateStringRow("hypervisorMetrics[", $(this));
    });
    stateString += "$";
    $("tr").each(function() {
        stateString += collectProfileStateStringRow("vmMetrics[", $(this));
    });                                                        n
    $("#submitForm > #extraState").val(stateString);
}

function initCustomNameValidation(usePrefix) {
    var customNames = $('input.customName'),
        allNames = $('input.customName, input.metricName'),
        ERROR_MESSAGES = ['Invalid Name: only allow Alphanumeric, underscore, (no prefix required)',
            'Invalid Name: name entered is not unique for this profile'];

    customNames.each(function()
    {
        var input = $(this);

        // input.bind("paste", function(e) {
        //     var pastedData = e.originalEvent.clipboardData.getData('text');
        //     var result = isValidCustomName(pastedData, allNames, usePrefix, this);
        //     if (result > 0) {
        //         event.preventDefault();
        //         $("#" + this.id + "_error").text(ERROR_MESSAGES[result-1]);
        //         return;
        //     }
        //     else {
        //         $("#" + this.id + "_error").text('');
        //     }
        // });

        input.keyup(function(event)
        {
            var text = this.value;// + String.fromCharCode(event.which);
            var result = isValidCustomName(text, allNames, usePrefix, this);

            if (result > 0) {
                $("#" + this.id + "_error").text(ERROR_MESSAGES[result - 1]);
                input.addClass("invalid");
            }
            else {
                $("#" + this.id + "_error").text('');
                input.removeClass("invalid");
            }

            if(customNames.filter(".invalid").length) {
                $("#btn-save").attr("disabled", "disabled");
            }
            else {
                $("#btn-save").removeAttr("disabled", "disabled");
            }
        });

        input.trigger("keyup");
    });
}

function isValidCustomName(customName, allNames, usePrefix, widget) {
    var regexp = /^[\w\-_]+$/;

    if (customName === '') {
        return 0;
    }

    if (customName.search(regexp) == -1) {
        return 1;
    }

    var result = 0;

    allNames.not(widget).each(function()
    {
        var val = this.value;

        if (usePrefix) {
            var pos = val.indexOf(".");

            if (pos > -1) {
                val = val.substring(pos + 1);
            }
        }

        if(val == customName && val !== '') {
            result = 2;
            return 2;
        }
    });

    return result;
}





