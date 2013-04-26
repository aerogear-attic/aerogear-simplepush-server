/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.simplepush.server.undertow;

import static org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil.fromJson;
import static org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil.toJson;
import io.undertow.websockets.api.AbstractAssembledFrameHandler;
import io.undertow.websockets.api.WebSocketFrameHeader;
import io.undertow.websockets.api.WebSocketSession;

import org.jboss.aerogear.simplepush.protocol.HandshakeResponse;
import org.jboss.aerogear.simplepush.protocol.MessageType;
import org.jboss.aerogear.simplepush.protocol.impl.HandshakeImpl;
import org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil;
import org.jboss.aerogear.simplepush.server.SimplePushServer;

public class PushNotificationFrameHandler extends AbstractAssembledFrameHandler {
    
    private SimplePushServer simplePushServer = new SimplePushServer();
    
    @Override
    public void onTextFrame(final WebSocketSession session, final WebSocketFrameHeader header, final CharSequence payload) {
        final MessageType messageType = JsonUtil.parseFrame(payload.toString());
        System.out.println(messageType.getMessageType());
        session.sendText("bajja", null);
        
        switch (messageType.getMessageType()) {
        case HELLO:
                final HandshakeResponse response = simplePushServer.handleHandshake(fromJson(payload.toString(), HandshakeImpl.class));
                session.sendText(toJson(response), null);
            break;
        case REGISTER:
            break;
        case UNREGISTER:
            break;
        case ACK:
            break;
        case NOTIFICATION:
            break;
        default:
            break;
        }
    }

}
