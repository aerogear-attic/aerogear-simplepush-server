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
package org.jboss.aerogear.simplepush.protocol;

import java.util.Set;
import java.util.UUID;

/**
 * Represents the Handshake message, 'hello' message type, in the 
 * <a href="https://wiki.mozilla.org/WebAPI/SimplePush/Protocol">SimplePush specification protocol</a>
 * 
 * This message is sent after the initial WebSocket handshake has been completed and is the handshake for 
 * between the UserAgent and the SimplePush Server.
 *
 */
public interface HandshakeMessage extends MessageType {
    
    String CHANNEL_IDS_FIELD = "channelIDs";
    String UAID_FIELD = "uaid";
    
    /**
     * A globally unique identifier for a UserAgent created by the SimplePush Server.
     * 
     * @return {@code UUID} a globally unique id for a UserAgent, or an empty String if the UserAgent has not
     *         been assigned a UAID yet or wants to reset it, which will create a new one.
     */
    UUID getUAID();
    
    /**
     * Channel identifiers are created on the UserAgent side and are stored by the SimplePush Server
     * and associated with the UserAgent. Every channelId has a version and an endpoint associated 
     * with it.  
     * 
     * @return {@code Set<String>} a set of channelIds sent from the UserAgent, or an empty list if no channel 
     *         ids were sent.
     */
    Set<String> getChannelIds();

}
