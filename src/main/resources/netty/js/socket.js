(function() {
    var Sock = function() {
        var socket;
        if (!window.WebSocket) {
            window.WebSocket = window.MozWebSocket;
        }

        if (window.WebSocket) {
            socket = new WebSocket("ws://localhost:8080/simplepush", "push-notification");
            socket.onopen = onopen;
            socket.onmessage = onmessage;
            socket.onclose = onclose;
        } else {
            alert("Your browser does not support Web Socket.");
        }

        function onopen(event) {
            getTextAreaElement().value = "Web Socket opened!";
        }

        function onmessage(event) {
            appendTextArea(event.data);
        }
        function onclose(event) {
            appendTextArea("Web Socket closed");
        }

        function appendTextArea(newData) {
            var el = getTextAreaElement();
            el.value = el.value + '\n' + newData;
        }

        function getTextAreaElement() {
            return document.getElementById('responseText');
        }

        function sendHello(event) {
            send(event, '{"messageType": "hello"}');
        }
        
        function sendRegister(event) {
            send(event, '{"messageType": "register", "channelID": "'.concat(event.target.message.value, '"}'));
        }
        function sendUnregister(event) {
            send(event, '{"messageType": "unregister", "channelID": "'.concat(event.target.message.value, '"}'));
        }
        //send(event.target.message.value);
        
        function send(event, body) {
            event.preventDefault();
            if (window.WebSocket) {
                if (socket.readyState == WebSocket.OPEN) {
                    socket.send(body);
                } else {
                    alert("The socket is not open.");
                }
            }
        }
        
        
        document.forms.hello.addEventListener('submit', sendHello, false);
        document.forms.register.addEventListener('submit', sendRegister, false);
        document.forms.unregister.addEventListener('submit', sendUnregister, false);
    }
    window.addEventListener('load', function() { new Sock(); }, false);
})();
