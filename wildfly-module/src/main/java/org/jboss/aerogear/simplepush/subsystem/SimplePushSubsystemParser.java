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
import static org.jboss.as.controller.parsing.ParseUtils.unexpectedAttribute;

import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
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
    public void readElement(final XMLExtendedStreamReader reader, final List<ModelNode> modelNodes) throws XMLStreamException {
        final ModelNode subsystem = new ModelNode();
        subsystem.get(OP).set(ADD);
        subsystem.get(OP_ADDR).set(PathAddress.pathAddress(SimplePushExtension.SUBSYSTEM_PATH).toModelNode());
        modelNodes.add(subsystem);

        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            if (reader.isStartElement()) {
                readServerType(reader, modelNodes);
            }
        }
    }

    private void readServerType(final XMLExtendedStreamReader reader, final List<ModelNode> modelNodes) throws XMLStreamException {
        final ModelNode addServerOperation = new ModelNode();
        addServerOperation.get(OP).set(ModelDescriptionConstants.ADD);
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
                    ServerDefinition.SOCKET_BINDING_ATTR.parseAndSetParameter(value, addServerOperation, reader);
                    break;
                case DATASOURCE:
                    ServerDefinition.DATASOURCE_ATTR.parseAndSetParameter(value, addServerOperation, reader);
                    break;
                case TOKEN_KEY:
                    ServerDefinition.TOKEN_KEY_ATTR.parseAndSetParameter(value, addServerOperation, reader);
                    break;
                case REAPER_TIMEOUT:
                    ServerDefinition.REAPER_TIMEOUT_ATTR.parseAndSetParameter(value, addServerOperation, reader);
                    break;
                case NOTIFICATION_PREFIX:
                    ServerDefinition.NOTIFICATION_PREFIX_ATTR.parseAndSetParameter(value, addServerOperation, reader);
                    break;
                case NOTIFICATION_TLS:
                    ServerDefinition.NOTIFICATION_TLS_ATTR.parseAndSetParameter(value, addServerOperation, reader);
                    break;
                case NOTIFICATION_ACK_INTERVAL:
                    ServerDefinition.NOTIFICATION_ACK_INTERVAL_ATTR.parseAndSetParameter(value, addServerOperation, reader);
                    break;
                case NOTIFICATION_HOST:
                    ServerDefinition.NOTIFICATION_HOST_ATTR.parseAndSetParameter(value, addServerOperation, reader);
                    break;
                case NOTIFICATION_PORT:
                    ServerDefinition.NOTIFICATION_PORT_ATTR.parseAndSetParameter(value, addServerOperation, reader);
                    break;
                case SOCKJS_PREFIX:
                    ServerDefinition.SOCKJS_PREFIX_ATTR.parseAndSetParameter(value, addServerOperation, reader);
                    break;
                case SOCKJS_COOKIES_NEEDED:
                    ServerDefinition.SOCKJS_COOKIES_NEEDED_ATTR.parseAndSetParameter(value, addServerOperation, reader);
                    break;
                case SOCKJS_URL:
                    ServerDefinition.SOCKJS_URL_ATTR.parseAndSetParameter(value, addServerOperation, reader);
                    break;
                case SOCKJS_SESSION_TIMEOUT:
                    ServerDefinition.SOCKJS_SESSION_TIMEOUT_ATTR.parseAndSetParameter(value, addServerOperation, reader);
                    break;
                case SOCKJS_HEARTBEAT_INTERVAL:
                    ServerDefinition.SOCKJS_HEARTBEAT_INTERVAL_ATTR.parseAndSetParameter(value, addServerOperation, reader);
                    break;
                case SOCKJS_MAX_STREAMING_BYTES_SIZE:
                    ServerDefinition.SOCKJS_MAX_STREAMING_BYTES_SIZE_ATTR.parseAndSetParameter(value, addServerOperation, reader);
                    break;
                case SOCKJS_TLS:
                    ServerDefinition.SOCKJS_TLS_ATTR.parseAndSetParameter(value, addServerOperation, reader);
                    break;
                case SOCKJS_ENABLE_WEBSOCKET:
                    ServerDefinition.SOCKJS_ENABLE_WEBSOCKET_ATTR.parseAndSetParameter(value, addServerOperation, reader);
                    break;
                case SOCKJS_WEBSOCKET_HEARTBEAT_INTERVAL:
                    ServerDefinition.SOCKJS_WEBSOCKET_HEARTBEAT_INTERVAL_ATTR.parseAndSetParameter(value, addServerOperation, reader);
                    break;
                default:
                    throw unexpectedAttribute(reader, i);
                }
        }
        final PathAddress addr = PathAddress.pathAddress(SimplePushExtension.SUBSYSTEM_PATH, PathElement.pathElement(SimplePushExtension.SERVER, serverName));
        addServerOperation.get(OP_ADDR).set(addr.toModelNode());
        modelNodes.add(addServerOperation);
        ParseUtils.requireNoContent(reader);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeContent(final XMLExtendedStreamWriter writer, final SubsystemMarshallingContext context) throws XMLStreamException {
        context.startSubsystemElement(SimplePushExtension.NAMESPACE, false);
        final ModelNode node = context.getModelNode();
        final ModelNode server = node.get(SimplePushExtension.SERVER);

        for (Property property : server.asPropertyList()) {
            writer.writeStartElement(SimplePushExtension.SERVER);
            final ModelNode entry = property.getValue();
            ServerDefinition.SOCKET_BINDING_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.DATASOURCE_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.TOKEN_KEY_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.REAPER_TIMEOUT_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.NOTIFICATION_TLS_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.NOTIFICATION_PREFIX_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.NOTIFICATION_ACK_INTERVAL_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.NOTIFICATION_HOST_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.NOTIFICATION_PORT_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.SOCKJS_PREFIX_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.SOCKJS_COOKIES_NEEDED_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.SOCKJS_URL_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.SOCKJS_SESSION_TIMEOUT_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.SOCKJS_HEARTBEAT_INTERVAL_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.SOCKJS_MAX_STREAMING_BYTES_SIZE_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.SOCKJS_TLS_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.SOCKJS_ENABLE_WEBSOCKET_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.SOCKJS_WEBSOCKET_HEARTBEAT_INTERVAL_ATTR.marshallAsAttribute(entry, true, writer);
            writer.writeEndElement();
        }

        writer.writeEndElement();
    }
}
