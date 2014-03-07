/**
 * JBoss, Home of Professional Open Source Copyright Red Hat, Inc., and individual contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.jboss.aerogear.simplepush.server;

import org.jboss.aerogear.simplepush.protocol.Ack;

/**
 * Notification is data container for notification information
 * to be sent to a UserAgent.
 *
 * This is a separate class and not part of the protcol as it contains information
 * that must not be exposed. Currently this is only the UserAgent ID (uaid) that we
 * need to pass from the SimpelPushServer to the network application layer.
 */
public class Notification {

    private final String uaid;
    private final Ack ack;

    public Notification(final String uaid, final Ack ack) {
        this.uaid = uaid;
        this.ack = ack;
    }

    public String uaid() {
        return uaid;
    }

    public Ack ack() {
        return ack;
    }

}