var TextMessages = function () {

    var service = {

        messages_en: {
            notFound: 'Message not found',
            serverFailure:
                'We\'re sorry, but we had trouble contacting our Monitor server. Please contact support for further assistance. Message: %s status: %s',
            serverSuccess: 'Server is up and running',
            serverFailed: 'Bad status. Status: %s',
            prefsUpdated: 'Your preferences have been updated.'
        },

        get: function (key, etc) {
            // TODO: localize
            var message = service.messages_en[key];
            if (message === undefined)
                return service.messages_en['notFound'];
            if (arguments.length <= 1)
                return message;
            var args = Array.prototype.slice.call(arguments, 1);
            args.unshift(message);
            return service.sprintf.apply(service, args);
        },

        sprintf: function(format, etc) {
            var arg = arguments;
            var i = 1;
            return format.replace(/%((%)|s)/g, function (m) { return m[2] || arg[i++] })
        }

    }

    return service;
}
