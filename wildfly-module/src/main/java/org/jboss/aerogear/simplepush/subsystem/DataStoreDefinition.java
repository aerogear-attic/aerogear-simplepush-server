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
import org.jboss.dmr.ModelType;

public class DataStoreDefinition extends SimpleResourceDefinition {

    public enum Element {
        UNKNOWN(null),
        JPA("jpa"),
        DATASOURCE("datasource-jndi-name"),
        PERSISTENCE_UNIT("persistence-unit"),
        REDIS("redis"),
        COUCHDB("couchdb"),
        HOST("host"),
        PORT("port"),
        URL("url"),
        DB_NAME("database-name");

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

    public static final SimpleAttributeDefinition DATASOURCE_ATTR = new SimpleAttributeDefinition(Element.DATASOURCE.localName(), ModelType.STRING, true);
    public static final SimpleAttributeDefinition PERSISTENCE_UNIT_ATTR = new SimpleAttributeDefinition(Element.PERSISTENCE_UNIT.localName(), ModelType.STRING, true);
    public static final SimpleAttributeDefinition HOST_ATTR = new SimpleAttributeDefinition(Element.HOST.localName(), ModelType.STRING, true);
    public static final SimpleAttributeDefinition PORT_ATTR = new SimpleAttributeDefinition(Element.PORT.localName(), ModelType.STRING, true);
    public static final SimpleAttributeDefinition URL_ATTR = new SimpleAttributeDefinition(Element.URL.localName(), ModelType.STRING, true);
    public static final SimpleAttributeDefinition DB_NAME_ATTR = new SimpleAttributeDefinition(Element.DB_NAME.localName(), ModelType.STRING, true);

    public static final String DATASTORE = "datastore";

    public static final PathElement DATASTORE_PATH = PathElement.pathElement(DATASTORE);
    public static final DataStoreDefinition INSTANCE = new DataStoreDefinition();

    private DataStoreDefinition() {
        super(DATASTORE_PATH,
                SimplePushExtension.getResourceDescriptionResolver(ServerDefinition.SERVER + "." + DATASTORE),
                DataStoreAdd.INSTANCE,
                DataStoreRemove.INSTANCE);
    }

    @Override
    public void registerAttributes(final ManagementResourceRegistration resourceRegistration) {
        super.registerAttributes(resourceRegistration);
        resourceRegistration.registerReadWriteAttribute(DATASOURCE_ATTR, null, SimplePushSocketBindingHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(PERSISTENCE_UNIT_ATTR, null, SimplePushSocketBindingHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(HOST_ATTR, null, SimplePushSocketBindingHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(PORT_ATTR, null, SimplePushSocketBindingHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(URL_ATTR, null, SimplePushSocketBindingHandler.INSTANCE);
        resourceRegistration.registerReadWriteAttribute(DB_NAME_ATTR, null, SimplePushSocketBindingHandler.INSTANCE);
    }

    @Override
    public void registerOperations(ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);
    }

    @Override
    public void registerChildren(ManagementResourceRegistration resourceRegistration) {
        super.registerChildren(resourceRegistration);
    }

}
