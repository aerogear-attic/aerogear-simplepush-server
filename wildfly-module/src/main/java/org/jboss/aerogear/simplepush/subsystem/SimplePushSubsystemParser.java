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

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;
import static org.jboss.as.controller.parsing.ParseUtils.unexpectedAttribute;
import static org.jboss.as.controller.parsing.ParseUtils.unexpectedElement;

import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

public class SimplePushSubsystemParser implements XMLStreamConstants, XMLElementReader<List<ModelNode>>, XMLElementWriter<SubsystemMarshallingContext> {

    @Override
    public void readElement(final XMLExtendedStreamReader reader, final List<ModelNode> list) throws XMLStreamException {
        final ModelNode address = new ModelNode();
        address.add(SUBSYSTEM,  SimplePushExtension.SUBSYSTEM_NAME);
        address.protect();

        final ModelNode subsystem = new ModelNode();
        subsystem.get(OP).set(ADD);
        subsystem.get(OP_ADDR).set(address);
        list.add(subsystem);

        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            if (reader.isStartElement()) {
                readServerType(reader, list, address);
            }
        }
    }

    private void readServerType(final XMLExtendedStreamReader reader, final List<ModelNode> modelNodes, final ModelNode parentAddress) throws XMLStreamException {
        final ModelNode node = new ModelNode();
        node.get(OP).set(ADD);
        modelNodes.add(node);

        readServerAttributes(reader, node, parentAddress);
        readServerElements(reader, node, modelNodes);
        ParseUtils.requireNoContent(reader);
    }

    private void readServerAttributes(final XMLExtendedStreamReader reader, final ModelNode node, final ModelNode parentAddress) throws XMLStreamException {
        String serverName = "simplepush";
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            final String name = reader.getAttributeLocalName(i);
            final String value = reader.getAttributeValue(i);
            switch (ServerDefinition.Element.of(name)) {
                case SERVER_NAME:
                    serverName = value;
                    break;
                case SOCKET_BINDING:
                    ServerDefinition.SOCKET_BINDING_ATTR.parseAndSetParameter(value, node, reader);
                    break;
                case TOKEN_KEY:
                    ServerDefinition.TOKEN_KEY_ATTR.parseAndSetParameter(value, node, reader);
                    break;
                case REAPER_TIMEOUT:
                    ServerDefinition.REAPER_TIMEOUT_ATTR.parseAndSetParameter(value, node, reader);
                    break;
                case NOTIFICATION_PREFIX:
                    ServerDefinition.NOTIFICATION_PREFIX_ATTR.parseAndSetParameter(value, node, reader);
                    break;
                case NOTIFICATION_TLS:
                    ServerDefinition.NOTIFICATION_TLS_ATTR.parseAndSetParameter(value, node, reader);
                    break;
                case NOTIFICATION_ACK_INTERVAL:
                    ServerDefinition.NOTIFICATION_ACK_INTERVAL_ATTR.parseAndSetParameter(value, node, reader);
                    break;
                case NOTIFICATION_SOCKET_BINDING:
                    ServerDefinition.NOTIFICATION_SOCKET_BINDING_ATTR.parseAndSetParameter(value, node, reader);
                    break;
                case SOCKJS_PREFIX:
                    ServerDefinition.SOCKJS_PREFIX_ATTR.parseAndSetParameter(value, node, reader);
                    break;
                case SOCKJS_COOKIES_NEEDED:
                    ServerDefinition.SOCKJS_COOKIES_NEEDED_ATTR.parseAndSetParameter(value, node, reader);
                    break;
                case SOCKJS_URL:
                    ServerDefinition.SOCKJS_URL_ATTR.parseAndSetParameter(value, node, reader);
                    break;
                case SOCKJS_SESSION_TIMEOUT:
                    ServerDefinition.SOCKJS_SESSION_TIMEOUT_ATTR.parseAndSetParameter(value, node, reader);
                    break;
                case SOCKJS_HEARTBEAT_INTERVAL:
                    ServerDefinition.SOCKJS_HEARTBEAT_INTERVAL_ATTR.parseAndSetParameter(value, node, reader);
                    break;
                case SOCKJS_MAX_STREAMING_BYTES_SIZE:
                    ServerDefinition.SOCKJS_MAX_STREAMING_BYTES_SIZE_ATTR.parseAndSetParameter(value, node, reader);
                    break;
                case SOCKJS_TLS:
                    ServerDefinition.SOCKJS_TLS_ATTR.parseAndSetParameter(value, node, reader);
                    break;
                case SOCKJS_KEYSTORE:
                    ServerDefinition.SOCKJS_KEYSTORE_ATTR.parseAndSetParameter(value, node, reader);
                    break;
                case SOCKJS_KEYSTORE_PASSWORD:
                    ServerDefinition.SOCKJS_KEYSTORE_PASSWORD_ATTR.parseAndSetParameter(value, node, reader);
                    break;
                case SOCKJS_ENABLE_WEBSOCKET:
                    ServerDefinition.SOCKJS_ENABLE_WEBSOCKET_ATTR.parseAndSetParameter(value, node, reader);
                    break;
                case SOCKJS_WEBSOCKET_HEARTBEAT_INTERVAL:
                    ServerDefinition.SOCKJS_WEBSOCKET_HEARTBEAT_INTERVAL_ATTR.parseAndSetParameter(value, node, reader);
                    break;
                case SOCKJS_WEBSOCKET_PROTOCOLS:
                    ServerDefinition.SOCKJS_WEBSOCKET_PROTOCOLS.parseAndSetParameter(value, node, reader);
                    break;
                default:
                    throw unexpectedAttribute(reader, i);
            }
            node.get(OP_ADDR).set(parentAddress).add(ServerDefinition.SERVER, serverName);
        }
    }

    private void readServerElements(final XMLExtendedStreamReader reader, final ModelNode node, final List<ModelNode> modelNodes) throws XMLStreamException {
        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            final String tagName = reader.getLocalName();
            if (tagName.equals(DataStoreDefinition.DATASTORE)) {
                while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
                    switch (DataStoreDefinition.Element.of(reader.getLocalName())) {
                        case JPA: {
                            final ModelNode jpa = readJpaElement(reader, node.get(OP_ADDR));
                            modelNodes.add(jpa);
                            break;
                        }
                        case REDIS: {
                            final ModelNode redis = readRedisElement(reader, node.get(OP_ADDR));
                            modelNodes.add(redis);
                            break;
                        }
                        case COUCHDB: {
                            final ModelNode couchdb = readCouchDBElement(reader, node.get(OP_ADDR));
                            modelNodes.add(couchdb);
                            break;
                        }
                        case IN_MEMORY: {
                            final ModelNode inmem = readInMemoryDBElement(reader, node.get(OP_ADDR));
                            modelNodes.add(inmem);
                            break;
                        }
                        default: {
                            throw unexpectedElement(reader);
                        }
                    }
                }
            }
        }
    }

    private ModelNode readJpaElement(XMLExtendedStreamReader reader, ModelNode parentAddress) throws XMLStreamException {
        final ModelNode node = new ModelNode();
        node.get(OP).set(ADD);
        node.get(OP_ADDR).set(parentAddress).add(DataStoreDefinition.DATASTORE, DataStoreDefinition.Element.JPA.localName());
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            final String name = reader.getAttributeLocalName(i);
            final String value = reader.getAttributeValue(i);
            switch (DataStoreDefinition.Element.of(name)) {
                case DATASOURCE:
                    DataStoreDefinition.DATASOURCE_ATTR.parseAndSetParameter(value, node, reader);
                    break;
                case PERSISTENCE_UNIT:
                    DataStoreDefinition.PERSISTENCE_UNIT_ATTR.parseAndSetParameter(value, node, reader);
                    break;
                default:
                    throw unexpectedAttribute(reader, i);
            }
        }
        return node;
    }

    private ModelNode readRedisElement(XMLExtendedStreamReader reader, ModelNode parentAddress) throws XMLStreamException {
        final ModelNode node = new ModelNode();
        node.get(OP).set(ADD);
        node.get(OP_ADDR).set(parentAddress).add(DataStoreDefinition.DATASTORE, DataStoreDefinition.Element.REDIS.localName());
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            final String name = reader.getAttributeLocalName(i);
            final String value = reader.getAttributeValue(i);
            switch (DataStoreDefinition.Element.of(name)) {
                case HOST:
                    DataStoreDefinition.HOST_ATTR.parseAndSetParameter(value, node, reader);
                    break;
                case PORT:
                    DataStoreDefinition.PORT_ATTR.parseAndSetParameter(value, node, reader);
                    break;
                default:
                    throw unexpectedAttribute(reader, i);
            }
        }
        return node;
    }

    private ModelNode readCouchDBElement(XMLExtendedStreamReader reader, ModelNode parentAddress) throws XMLStreamException {
        final ModelNode node = new ModelNode();
        node.get(OP).set(ADD);
        node.get(OP_ADDR).set(parentAddress).add(DataStoreDefinition.DATASTORE, DataStoreDefinition.Element.COUCHDB.localName());
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            final String name = reader.getAttributeLocalName(i);
            final String value = reader.getAttributeValue(i);
            switch (DataStoreDefinition.Element.of(name)) {
                case URL:
                    DataStoreDefinition.URL_ATTR.parseAndSetParameter(value, node, reader);
                    break;
                case DB_NAME:
                    DataStoreDefinition.DB_NAME_ATTR.parseAndSetParameter(value, node, reader);
                    break;
                default:
                    throw unexpectedAttribute(reader, i);
            }
        }
        return node;
    }

    private ModelNode readInMemoryDBElement(XMLExtendedStreamReader reader, ModelNode parentAddress) throws XMLStreamException {
        final ModelNode node = new ModelNode();
        node.get(OP).set(ADD);
        node.get(OP_ADDR).set(parentAddress).add(DataStoreDefinition.DATASTORE, DataStoreDefinition.Element.IN_MEMORY.localName());
        return node;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeContent(final XMLExtendedStreamWriter writer, final SubsystemMarshallingContext context) throws XMLStreamException {
        context.startSubsystemElement(SimplePushExtension.NAMESPACE, false);
        final ModelNode node = context.getModelNode();
        final ModelNode server = node.get(ServerDefinition.SERVER);

        for (Property property : server.asPropertyList()) {
            writer.writeStartElement(ServerDefinition.SERVER);
            final ModelNode entry = property.getValue();
            ServerDefinition.SOCKET_BINDING_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.TOKEN_KEY_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.REAPER_TIMEOUT_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.NOTIFICATION_TLS_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.NOTIFICATION_PREFIX_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.NOTIFICATION_ACK_INTERVAL_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.NOTIFICATION_SOCKET_BINDING_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.SOCKJS_PREFIX_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.SOCKJS_COOKIES_NEEDED_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.SOCKJS_URL_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.SOCKJS_SESSION_TIMEOUT_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.SOCKJS_HEARTBEAT_INTERVAL_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.SOCKJS_MAX_STREAMING_BYTES_SIZE_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.SOCKJS_TLS_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.SOCKJS_KEYSTORE_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.SOCKJS_KEYSTORE_PASSWORD_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.SOCKJS_ENABLE_WEBSOCKET_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.SOCKJS_WEBSOCKET_HEARTBEAT_INTERVAL_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.SOCKJS_WEBSOCKET_PROTOCOLS.marshallAsAttribute(entry, true, writer);
            final ModelNode datastore = entry.get(DataStoreDefinition.DATASTORE);
            if (datastore.isDefined()) {
                writer.writeStartElement(DataStoreDefinition.DATASTORE);
                switch (DataStoreDefinition.Element.of(datastore.keys().iterator().next())) {
                    case JPA:
                        writer.writeStartElement(DataStoreDefinition.Element.JPA.localName());
                        final ModelNode jpa = datastore.get(DataStoreDefinition.Element.JPA.localName());
                        DataStoreDefinition.DATASOURCE_ATTR.marshallAsAttribute(jpa, true, writer);
                        DataStoreDefinition.PERSISTENCE_UNIT_ATTR.marshallAsAttribute(jpa, true, writer);
                        writer.writeEndElement();
                        break;
                    case REDIS:
                        writer.writeStartElement(DataStoreDefinition.Element.REDIS.localName());
                        final ModelNode redis = datastore.get(DataStoreDefinition.Element.REDIS.localName());
                        DataStoreDefinition.HOST_ATTR.marshallAsAttribute(redis, true, writer);
                        DataStoreDefinition.PORT_ATTR.marshallAsAttribute(redis, true, writer);
                        writer.writeEndElement();
                        break;
                    case COUCHDB:
                        writer.writeStartElement(DataStoreDefinition.Element.COUCHDB.localName());
                        final ModelNode couchdb = datastore.get(DataStoreDefinition.Element.COUCHDB.localName());
                        DataStoreDefinition.URL_ATTR.marshallAsAttribute(couchdb, true, writer);
                        DataStoreDefinition.DB_NAME_ATTR.marshallAsAttribute(couchdb, true, writer);
                        writer.writeEndElement();
                        break;
                    case IN_MEMORY:
                        writer.writeStartElement(DataStoreDefinition.Element.IN_MEMORY.localName());
                        writer.writeEndElement();
                        break;
                    default:
                        throw new IllegalStateException("Non supported datastore type");
                }
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }

        writer.writeEndElement();
    }
}
