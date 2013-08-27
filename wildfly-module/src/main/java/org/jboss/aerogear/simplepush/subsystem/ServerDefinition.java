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

import static org.jboss.aerogear.simplepush.subsystem.SimplePushExtension.SERVER;
import static org.jboss.aerogear.simplepush.subsystem.SimplePushExtension.SERVER_PATH;

import java.util.HashMap;
import java.util.Map;

import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelType;

public class ServerDefinition extends SimpleResourceDefinition {

    public enum Element {
        UNKNOWN(null),
        SOCKET_BINDING("socket-binding"),
        DATASOURCE("datasource-jndi-name"),
        TOKEN_KEY("token-key"),
        ENDPOINT_TLS("endpoint-tls");

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

    protected static final SimpleAttributeDefinition SOCKET_BINDING_ATTR = new SimpleAttributeDefinition(Element.SOCKET_BINDING.localName(), ModelType.STRING, false);
    protected static final SimpleAttributeDefinition DATASOURCE_ATTR = new SimpleAttributeDefinition(Element.DATASOURCE.localName(), ModelType.STRING, true);
    protected static final SimpleAttributeDefinition TOKEN_KEY_ATTR = new SimpleAttributeDefinition(Element.TOKEN_KEY.localName(), ModelType.STRING, true);
    protected static final SimpleAttributeDefinition ENDPOINT_TLS_ATTR = new SimpleAttributeDefinition(Element.ENDPOINT_TLS.localName(), ModelType.BOOLEAN, true);

    public static final ServerDefinition INSTANCE = new ServerDefinition();

    private ServerDefinition() {
        super(SERVER_PATH,
                SimplePushExtension.getResourceDescriptionResolver(SERVER),
                ServerAdd.INSTANCE,
                ServerRemove.INSTANCE);
    }

    @Override
    public void registerAttributes(final ManagementResourceRegistration resourceRegistration) {
        resourceRegistration.registerReadWriteAttribute(SOCKET_BINDING_ATTR, null, SimplePushSocketBindingHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(DATASOURCE_ATTR, null, SimplePushSocketBindingHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(TOKEN_KEY_ATTR, null, SimplePushSocketBindingHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(ENDPOINT_TLS_ATTR, null, SimplePushSocketBindingHandler.INSTANCE);
    }
}
