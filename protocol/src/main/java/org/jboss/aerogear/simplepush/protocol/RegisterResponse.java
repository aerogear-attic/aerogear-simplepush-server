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

/**
 * Represents the Register response message, 'register' message type, in the 
 * <a href="https://wiki.mozilla.org/WebAPI/SimplePush/Protocol">SimplePush specification protocol</a>
 * 
 * This message is sent from the PushServer to the UserAgent with the result of a registration attempt
 *
 */
public interface RegisterResponse extends RegisterMessage {

    String STATUS_FIELD = "status";
    String PUSH_ENDPOINT__FIELD = "pushEndpoint";

    /**
     * Returns the result of the Register call
     * 
     * @return {@code String} the channelId.
     */
    Status getStatus();

    /**
     * Returns the push endpoint for this channel. 
     * 
     * This is the endpoint URL that is passed back to the UserAgent upon registering a channel. The UserAgent 
     * will then update the server side application of this endpoint, which the server side application will 
     * then use when it wants to trigger a notification.
     * 
     * @return {@code String} the endpoint which can be used to trigger notifications.
     */
    String getPushEndpoint();

}
