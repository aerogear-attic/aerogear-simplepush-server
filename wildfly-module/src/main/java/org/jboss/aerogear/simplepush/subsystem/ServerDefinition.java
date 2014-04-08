/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.aerogear.simplepush.subsystem;

import java.util.HashMap;
import java.util.Map;

import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

public class ServerDefinition extends SimpleResourceDefinition {

    public static final String SERVER = "server";
    public static final PathElement SERVER_PATH = PathElement.pathElement(SERVER);

    public enum Element {
        UNKNOWN(null),
        SERVER_NAME("name"),
        SOCKET_BINDING("socket-binding"),
        PASSWORD("password"),
        REAPER_TIMEOUT("useragent-reaper-timeout"),
        ENDPOINT_PREFIX("endpoint-prefix"),
        ENDPOINT_TLS("endpoint-tls"),
        ENDPOINT_ACK_INTERVAL("endpoint-ack-interval"),
        ENDPOINT_SOCKET_BINDING("endpoint-socket-binding"),
        NOTIFIER_MAX_THREADS("notifier-max-threads"),
        SOCKJS_PREFIX("sockjs-prefix"),
        SOCKJS_COOKIES_NEEDED("sockjs-cookies-needed"),
        SOCKJS_URL("sockjs-url"),
        SOCKJS_SESSION_TIMEOUT("sockjs-session-timeout"),
        SOCKJS_HEARTBEAT_INTERVAL("sockjs-heartbeat-interval"),
        SOCKJS_MAX_STREAMING_BYTES_SIZE("sockjs-max-streaming-bytes-size"),
        SOCKJS_TLS("sockjs-tls"),
        SOCKJS_KEYSTORE("sockjs-keystore"),
        SOCKJS_KEYSTORE_PASSWORD("sockjs-keystore-password"),
        SOCKJS_ENABLE_WEBSOCKET("sockjs-websocket-enable"),
        SOCKJS_WEBSOCKET_HEARTBEAT_INTERVAL("sockjs-websocket-heartbeat-interval"),
        SOCKJS_WEBSOCKET_PROTOCOLS("sockjs-websocket-protocols");

        private final String name;

        private Element(final String name) {
            this.name = name;
        }

        public String localName() {
            return name;
        }

        private static final Map<String, Element> MAP;

        static {
            final Map<String, Element> map = new HashMap<String, Element>();
            for (Element element : values()) {
                final String name = element.localName();
                if (name != null) map.put(name, element);
            }
            MAP = map;
        }

        public static Element of(final String localName) {
            final Element element = MAP.get(localName);
            return element == null ? UNKNOWN : element;
        }

    }

    protected static final SimpleAttributeDefinition SERVER_NAME_ATTR = new SimpleAttributeDefinition(Element.SERVER_NAME.localName(), ModelType.STRING, true);
    protected static final SimpleAttributeDefinition SOCKET_BINDING_ATTR = new SimpleAttributeDefinition(Element.SOCKET_BINDING.localName(), ModelType.STRING, false);
    protected static final SimpleAttributeDefinition PASSWORD_ATTR = new SimpleAttributeDefinition(Element.PASSWORD.localName(), ModelType.STRING, true);
    protected static final SimpleAttributeDefinition ENDPOINT_TLS_ATTR = new SimpleAttributeDefinition(Element.ENDPOINT_TLS.localName(), ModelType.BOOLEAN, true);
    protected static final SimpleAttributeDefinition REAPER_TIMEOUT_ATTR = new SimpleAttributeDefinition(Element.REAPER_TIMEOUT.localName(), new ModelNode(604800000), ModelType.LONG, true);
    protected static final SimpleAttributeDefinition ENDPOINT_PREFIX_ATTR = new SimpleAttributeDefinition(Element.ENDPOINT_PREFIX.localName(), new ModelNode("/update"), ModelType.STRING, true);
    protected static final SimpleAttributeDefinition ENDPOINT_ACK_INTERVAL_ATTR = new SimpleAttributeDefinition(Element.ENDPOINT_ACK_INTERVAL.localName(), new ModelNode(60000), ModelType.LONG, true);
    protected static final SimpleAttributeDefinition ENDPOINT_SOCKET_BINDING_ATTR = new SimpleAttributeDefinition(Element.ENDPOINT_SOCKET_BINDING.localName(), ModelType.STRING, true);
    protected static final SimpleAttributeDefinition NOTIFIER_MAX_THREADS = new SimpleAttributeDefinition(Element.NOTIFIER_MAX_THREADS.localName(), ModelType.INT, true);
    protected static final SimpleAttributeDefinition SOCKJS_PREFIX_ATTR = new SimpleAttributeDefinition(Element.SOCKJS_PREFIX.localName(), new ModelNode("/simplepush"), ModelType.STRING, false);
    protected static final SimpleAttributeDefinition SOCKJS_COOKIES_NEEDED_ATTR = new SimpleAttributeDefinition(Element.SOCKJS_COOKIES_NEEDED.localName(), new ModelNode(true), ModelType.BOOLEAN, true);
    protected static final SimpleAttributeDefinition SOCKJS_URL_ATTR = new SimpleAttributeDefinition(Element.SOCKJS_URL.localName(), ModelType.STRING, true);
    protected static final SimpleAttributeDefinition SOCKJS_SESSION_TIMEOUT_ATTR = new SimpleAttributeDefinition(Element.SOCKJS_SESSION_TIMEOUT.localName(), new ModelNode(5000), ModelType.LONG, true);
    protected static final SimpleAttributeDefinition SOCKJS_HEARTBEAT_INTERVAL_ATTR = new SimpleAttributeDefinition(Element.SOCKJS_HEARTBEAT_INTERVAL.localName(), new ModelNode(25000), ModelType.LONG, true);
    protected static final SimpleAttributeDefinition SOCKJS_MAX_STREAMING_BYTES_SIZE_ATTR = new SimpleAttributeDefinition(Element.SOCKJS_MAX_STREAMING_BYTES_SIZE.localName(), new ModelNode(131072), ModelType.LONG, true);
    protected static final SimpleAttributeDefinition SOCKJS_TLS_ATTR = new SimpleAttributeDefinition(Element.SOCKJS_TLS.localName(), new ModelNode(false), ModelType.BOOLEAN, true);
    protected static final SimpleAttributeDefinition SOCKJS_KEYSTORE_ATTR = new SimpleAttributeDefinition(Element.SOCKJS_KEYSTORE.localName(), ModelType.STRING, true);
    protected static final SimpleAttributeDefinition SOCKJS_KEYSTORE_PASSWORD_ATTR = new SimpleAttributeDefinition(Element.SOCKJS_KEYSTORE_PASSWORD.localName(), ModelType.STRING, true);
    protected static final SimpleAttributeDefinition SOCKJS_ENABLE_WEBSOCKET_ATTR = new SimpleAttributeDefinition(Element.SOCKJS_ENABLE_WEBSOCKET.localName(), ModelType.BOOLEAN, true);
    protected static final SimpleAttributeDefinition SOCKJS_WEBSOCKET_HEARTBEAT_INTERVAL_ATTR = new SimpleAttributeDefinition(Element.SOCKJS_WEBSOCKET_HEARTBEAT_INTERVAL.localName(), new ModelNode(180000L), ModelType.LONG, true);
    protected static final SimpleAttributeDefinition SOCKJS_WEBSOCKET_PROTOCOLS = new SimpleAttributeDefinition(Element.SOCKJS_WEBSOCKET_PROTOCOLS.localName(), new ModelNode("push-notification"), ModelType.STRING, true);

    public static final ServerDefinition INSTANCE = new ServerDefinition();

    private ServerDefinition() {
        super(SERVER_PATH,
                SimplePushExtension.getResourceDescriptionResolver(SERVER),
                ServerAdd.INSTANCE,
                ServerRemove.INSTANCE);
    }

    @Override
    public void registerAttributes(final ManagementResourceRegistration resourceRegistration) {
        super.registerAttributes(resourceRegistration);
        resourceRegistration.registerReadWriteAttribute(SERVER_NAME_ATTR, null, SimplePushSocketBindingHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(SOCKET_BINDING_ATTR, null, SimplePushSocketBindingHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(PASSWORD_ATTR, null, SimplePushSocketBindingHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(REAPER_TIMEOUT_ATTR, null, SimplePushSocketBindingHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(ENDPOINT_PREFIX_ATTR, null, SimplePushSocketBindingHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(ENDPOINT_TLS_ATTR, null, SimplePushSocketBindingHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(ENDPOINT_ACK_INTERVAL_ATTR, null, SimplePushSocketBindingHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(ENDPOINT_SOCKET_BINDING_ATTR, null, SimplePushSocketBindingHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(NOTIFIER_MAX_THREADS, null, SimplePushSocketBindingHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(SOCKJS_PREFIX_ATTR, null, SimplePushSocketBindingHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(SOCKJS_COOKIES_NEEDED_ATTR, null, SimplePushSocketBindingHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(SOCKJS_URL_ATTR, null, SimplePushSocketBindingHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(SOCKJS_SESSION_TIMEOUT_ATTR, null, SimplePushSocketBindingHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(SOCKJS_HEARTBEAT_INTERVAL_ATTR, null, SimplePushSocketBindingHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(SOCKJS_MAX_STREAMING_BYTES_SIZE_ATTR, null, SimplePushSocketBindingHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(SOCKJS_TLS_ATTR, null, SimplePushSocketBindingHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(SOCKJS_KEYSTORE_ATTR, null, SimplePushSocketBindingHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(SOCKJS_KEYSTORE_PASSWORD_ATTR, null, SimplePushSocketBindingHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(SOCKJS_ENABLE_WEBSOCKET_ATTR, null, SimplePushSocketBindingHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(SOCKJS_WEBSOCKET_HEARTBEAT_INTERVAL_ATTR, null, SimplePushSocketBindingHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(SOCKJS_WEBSOCKET_PROTOCOLS, null, SimplePushSocketBindingHandler.INSTANCE);
    }

    @Override
    public void registerChildren(ManagementResourceRegistration resourceRegistration) {
        super.registerChildren(resourceRegistration);
        resourceRegistration.registerSubModel(DataStoreDefinition.INSTANCE);
    }



}
