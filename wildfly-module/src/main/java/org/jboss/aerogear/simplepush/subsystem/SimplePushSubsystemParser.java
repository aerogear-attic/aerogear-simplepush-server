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

import java.util.Collections;
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
    public void readElement(final XMLExtendedStreamReader reader, final List<ModelNode> list) throws XMLStreamException {
        final ModelNode address = new ModelNode();
        address.add(SUBSYSTEM, SimplePushExtension.SUBSYSTEM_NAME);
        address.protect();

        final ModelNode subsystem = new ModelNode();
        subsystem.get(OP).set(ADD);
        subsystem.get(OP_ADDR).set(address);
        list.add(subsystem);

        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            readServerType(reader, list);
        }
    }

    private void readServerType(final XMLExtendedStreamReader reader, final List<ModelNode> list) throws XMLStreamException {
        final ModelNode addServerOperation = new ModelNode();
        addServerOperation.get(OP).set(ModelDescriptionConstants.ADD);

        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            final String name = reader.getAttributeLocalName(i);
            final String value = reader.getAttributeValue(i);
            switch (ServerDefinition.Element.of(name)) {
            case SOCKET_BINDING:
                ServerDefinition.SOCKET_BINDING_ATTR.parseAndSetParameter(value, addServerOperation, reader);
                break;
            case FACTORY_CLASS:
                ServerDefinition.FACTORY_CLASS_ATTR.parseAndSetParameter(value, addServerOperation, reader);
                break;
            case THREAD_FACTORY:
                ServerDefinition.THREAD_FACTORY_ATTR.parseAndSetParameter(value, addServerOperation, reader);
                break;
            case DATASOURCE:
                ServerDefinition.DATASOURCE_ATTR.parseAndSetParameter(value, addServerOperation, reader);
                break;
            case TOKEN_KEY:
                ServerDefinition.TOKEN_KEY_ATTR.parseAndSetParameter(value, addServerOperation, reader);
                break;
            case ENDPOINT_TLS:
                ServerDefinition.ENDPOINT_TLS_ATTR.parseAndSetParameter(value, addServerOperation, reader);
                break;
            case NAME:
                if (value == null) {
                    throw ParseUtils.missingRequiredElement(reader, Collections.singleton(ServerDefinition.Element.NAME.toString()));
                }
                final PathAddress addr = PathAddress.pathAddress(SimplePushExtension.SUBSYSTEM_PATH, PathElement.pathElement(SimplePushExtension.SERVER, value));
                addServerOperation.get(OP_ADDR).set(addr.toModelNode());
                break;
            default:
                throw unexpectedAttribute(reader, i);
            }
        }
        ParseUtils.requireNoContent(reader);
        list.add(addServerOperation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeContent(final XMLExtendedStreamWriter writer, final SubsystemMarshallingContext context) throws XMLStreamException {
        context.startSubsystemElement(SimplePushExtension.NAMESPACE, false);
        final ModelNode node = context.getModelNode();
        final ModelNode type = node.get(SimplePushExtension.SERVER);
        for (Property property : type.asPropertyList()) {
            writer.writeStartElement(SimplePushExtension.SERVER);
            writer.writeAttribute(ServerDefinition.Element.NAME.localName(), property.getName());
            final ModelNode entry = property.getValue();
            ServerDefinition.SOCKET_BINDING_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.FACTORY_CLASS_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.THREAD_FACTORY_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.DATASOURCE_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.TOKEN_KEY_ATTR.marshallAsAttribute(entry, true, writer);
            ServerDefinition.ENDPOINT_TLS_ATTR.marshallAsAttribute(entry, true, writer);
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }
}
