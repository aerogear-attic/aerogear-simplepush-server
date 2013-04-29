(function() {
    var broadcastRequest,broadcastEndpoint, mailEndpoint, mailRequest, fooEndpoint, fooRequest, broadcastEndpoint;

    getTextAreaElement().value = "Web Socket opened!";

    broadcastRequest = navigator.push.register();
    broadcastRequest.onsuccess = function( event ) {
        broadcastEndpoint = event.target.result;
        broadcastRequest.registerWithPushServer( "broadcast", broadcastEndpoint );
        appendTextArea("Subscribed to BroadCast messages on " + broadcastEndpoint.channelID);
    };

    mailRequest = navigator.push.register();
    mailRequest.onsuccess = function( event ) {
        mailEndpoint = event.target.result;
        mailRequest.registerWithPushServer( "mail", mailEndpoint );
        appendTextArea("Subscribed to Mail messages on " + mailEndpoint.channelID);
    };

    fooRequest = navigator.push.register();
    fooRequest.onsuccess = function( event ) {
        fooEndpoint = event.target.result;
        fooRequest.registerWithPushServer( "foo", fooEndpoint );
        appendTextArea("Subscribed to Foo messages on " + fooEndpoint.channelID);
    };

    navigator.setMessageHandler( "push", function( message ) {
        if ( message.channelID === mailEndpoint.channelID )
            appendTextArea("Mail Notification - " + message.version);
        else if ( message.channelID === fooEndpoint.channelID )
            appendTextArea("Foo Notification - " + message.version);
        else if ( message.channelID === broadcastEndpoint.channelID )
            appendTextArea("Broadcast Notification - " + message.version);
			
        // Broadcast messages are subscribed by default and can be acted on as well
        // TODO: figure out broadcast
    });

    function appendTextArea(newData) {
        var el = getTextAreaElement();
        el.value = el.value + '\n' + newData;
    }

    function getTextAreaElement() {
        return document.getElementById('responseText');
    }
})();
