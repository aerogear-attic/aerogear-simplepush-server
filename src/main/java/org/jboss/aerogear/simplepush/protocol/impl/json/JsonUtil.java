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
package org.jboss.aerogear.simplepush.protocol.impl.json;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.module.SimpleModule;
import org.jboss.aerogear.simplepush.protocol.AckMessage;
import org.jboss.aerogear.simplepush.protocol.HandshakeMessage;
import org.jboss.aerogear.simplepush.protocol.HandshakeResponse;
import org.jboss.aerogear.simplepush.protocol.MessageType;
import org.jboss.aerogear.simplepush.protocol.NotificationMessage;
import org.jboss.aerogear.simplepush.protocol.RegisterMessage;
import org.jboss.aerogear.simplepush.protocol.RegisterResponse;
import org.jboss.aerogear.simplepush.protocol.UnregisterMessage;
import org.jboss.aerogear.simplepush.protocol.UnregisterResponse;
import org.jboss.aerogear.simplepush.protocol.Update;
import org.jboss.aerogear.simplepush.protocol.impl.AckMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.HandshakeMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.HandshakeResponseImpl;
import org.jboss.aerogear.simplepush.protocol.impl.NotificationMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.RegisterImpl;
import org.jboss.aerogear.simplepush.protocol.impl.RegisterResponseImpl;
import org.jboss.aerogear.simplepush.protocol.impl.StatusImpl;
import org.jboss.aerogear.simplepush.protocol.impl.UnregisterMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.UnregisterResponseImpl;
import org.jboss.aerogear.simplepush.protocol.impl.UpdateImpl;

public class JsonUtil {

    private static ObjectMapper om = createObjectMapper();

    private static ObjectMapper createObjectMapper() {
        om = new ObjectMapper();
        final SimpleModule module = new SimpleModule("MyModule", new Version(1, 0, 0, null));
        module.addDeserializer(MessageType.class, new MessageTypeDeserializer());
        
        module.addDeserializer(RegisterImpl.class, new RegisterDeserializer());
        module.addSerializer(RegisterImpl.class, new RegisterSerializer());
        module.addDeserializer(RegisterResponseImpl.class, new RegisterResponseDeserializer());
        module.addSerializer(RegisterResponseImpl.class, new RegisterResponseSerializer());
        
        module.addDeserializer(HandshakeMessageImpl.class, new HandshakeDeserializer());
        module.addSerializer(HandshakeMessageImpl.class, new HandshakeSerializer());
        module.addDeserializer(HandshakeResponseImpl.class, new HandshakeResponseDeserializer());
        module.addSerializer(HandshakeResponseImpl.class, new HandshakeResponseSerializer());
        
        module.addDeserializer(AckMessageImpl.class, new AckDeserializer());
        module.addSerializer(AckMessageImpl.class, new AckSerializer());
        
        module.addDeserializer(NotificationMessageImpl.class, new NotificationDeserializer());
        module.addSerializer(NotificationMessageImpl.class, new NotificationSerializer());
        
        module.addDeserializer(UnregisterMessageImpl.class, new UnregisterDeserializer());
        module.addSerializer(UnregisterMessageImpl.class, new UnregisterMessageSerializer());
        module.addDeserializer(UnregisterResponseImpl.class, new UnregisterResponseDeserializer());
        module.addSerializer(UnregisterResponseImpl.class, new UnregisterResponseSerializer());
        
        om.registerModule(module);
        return om;
    }
    
    private JsonUtil() {
    }

    public static <T> T fromJson(final String json, final Class<T> type) {
        try {
            return om.readValue(json, type);
        } catch (final Exception e) {
            throw new RuntimeException("error trying to parse json [" + json + "]", e);
        }
    }

    public static String toJson(final Object obj) {
        final StringWriter stringWriter = new StringWriter();
        try {
            om.writeValue(stringWriter, obj);
            return stringWriter.toString();
        } catch (final Exception e) {
            throw new RuntimeException("error trying to parse json [" + obj + "]", e);
        }
    }
    
    public static MessageType parseFrame(final String json) {
        return fromJson(json, MessageType.class);
    }

    private static class RegisterDeserializer extends JsonDeserializer<RegisterImpl> {

        @Override
        public RegisterImpl deserialize(final JsonParser jp, final DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            final ObjectCodec oc = jp.getCodec();
            final JsonNode node = oc.readTree(jp);
            return new RegisterImpl(node.get(RegisterMessage.CHANNEL_ID_FIELD).asText());
        }
    }

    private static class RegisterSerializer extends JsonSerializer<RegisterMessage> {

        @Override
        public void serialize(final RegisterMessage register, final JsonGenerator jgen,
                final SerializerProvider provider) throws IOException, JsonProcessingException {
            jgen.writeStartObject();
            jgen.writeFieldName(RegisterMessage.MESSSAGE_TYPE_FIELD);
            jgen.writeString(register.getMessageType().toString().toLowerCase());
            jgen.writeFieldName(RegisterMessage.CHANNEL_ID_FIELD);
            jgen.writeString(register.getChannelId());
            jgen.writeEndObject();
        }
    }
    
    private static class RegisterResponseDeserializer extends JsonDeserializer<RegisterResponseImpl> { 
        
        @Override
        public RegisterResponseImpl deserialize(final JsonParser jp, final DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            final ObjectCodec oc = jp.getCodec();
            final JsonNode node = oc.readTree(jp);
            return new RegisterResponseImpl(node.get(RegisterMessage.CHANNEL_ID_FIELD).asText(),
                    new StatusImpl(node.get(RegisterResponseImpl.STATUS_FIELD).asInt(), "N/A"), 
                    node.get(RegisterResponseImpl.PUSH_ENDPOINT__FIELD).asText());
        }
    }
    
    private static class RegisterResponseSerializer extends JsonSerializer<RegisterResponse> {

        @Override
        public void serialize(final RegisterResponse registerResponse, final JsonGenerator jgen,
                final SerializerProvider provider) throws IOException, JsonProcessingException {
            jgen.writeStartObject();
            jgen.writeFieldName(RegisterResponse.MESSSAGE_TYPE_FIELD);
            jgen.writeString(registerResponse.getMessageType().toString().toLowerCase());
            jgen.writeFieldName(RegisterResponse.CHANNEL_ID_FIELD);
            jgen.writeString(registerResponse.getChannelId());
            jgen.writeFieldName(RegisterResponse.STATUS_FIELD);
            jgen.writeNumber(registerResponse.getStatus().getCode());
            jgen.writeFieldName(RegisterResponse.PUSH_ENDPOINT__FIELD);
            jgen.writeString(registerResponse.getPushEndpoint());
            jgen.writeEndObject();
        }
    }

    private static class HandshakeDeserializer extends JsonDeserializer<HandshakeMessageImpl> {

        @Override
        public HandshakeMessageImpl deserialize(final JsonParser jp, final DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            final ObjectCodec oc = jp.getCodec();
            final JsonNode node = oc.readTree(jp);
            final JsonNode channelIdsNode = node.get(HandshakeMessage.CHANNEL_IDS_FIELD);
            final Set<String> channelIds = new HashSet<String>();
            if (channelIdsNode != null && channelIdsNode.isArray()) {
                for (JsonNode channelIdNode : channelIdsNode) {
                    channelIds.add(channelIdNode.asText());
                }
            }
            final JsonNode uaid = node.get(HandshakeMessage.UAID_FIELD);
            if (uaid !=null) {
                return new HandshakeMessageImpl(node.get(HandshakeMessage.UAID_FIELD).asText(), channelIds);
            } else {
                return new HandshakeMessageImpl();
            }
        }
    }

    private static class HandshakeSerializer extends JsonSerializer<HandshakeMessage> {

        @Override
        public void serialize(final HandshakeMessage handshake, final JsonGenerator jgen,
                final SerializerProvider provider) throws IOException, JsonProcessingException {
            jgen.writeStartObject();
            jgen.writeFieldName(HandshakeMessage.MESSSAGE_TYPE_FIELD);
            jgen.writeString(handshake.getMessageType().toString().toLowerCase());
            jgen.writeFieldName(HandshakeMessage.UAID_FIELD);
            jgen.writeString(handshake.getUAID().toString());
            jgen.writeArrayFieldStart(HandshakeMessage.CHANNEL_IDS_FIELD);
            for (String channelId : handshake.getChannelIds()) {
                jgen.writeString(channelId);
            }
            jgen.writeEndArray();
            jgen.writeEndObject();
        }
    }
    
    private static class HandshakeResponseDeserializer extends JsonDeserializer<HandshakeResponseImpl> {

        @Override
        public HandshakeResponseImpl deserialize(final JsonParser jp, final DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            final ObjectCodec oc = jp.getCodec();
            final JsonNode node = oc.readTree(jp);
            final JsonNode uaid = node.get(HandshakeMessage.UAID_FIELD);
            return new HandshakeResponseImpl(UUID.fromString(uaid.asText()));
        }
    }
    
    private static class HandshakeResponseSerializer extends JsonSerializer<HandshakeResponse> {

        @Override
        public void serialize(final HandshakeResponse handshakeResponse, final JsonGenerator jgen,
                final SerializerProvider provider) throws IOException, JsonProcessingException {
            jgen.writeStartObject();
            jgen.writeFieldName(HandshakeMessage.MESSSAGE_TYPE_FIELD);
            jgen.writeString(handshakeResponse.getMessageType().toString().toLowerCase());
            jgen.writeFieldName(HandshakeMessage.UAID_FIELD);
            jgen.writeString(handshakeResponse.getUAID().toString());
            jgen.writeEndObject();
        }
    }

    private static class AckDeserializer extends JsonDeserializer<AckMessageImpl> {

        @Override
        public AckMessageImpl deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException,
                JsonProcessingException {
            final ObjectCodec oc = jp.getCodec();
            final JsonNode node = oc.readTree(jp);
            final JsonNode updatesNode = node.get(AckMessage.UPDATES_FIELD);
            final Set<String> updates = new HashSet<String>();
            if (updatesNode.isArray()) {
                for (JsonNode channelIdNode : updatesNode) {
                    updates.add(channelIdNode.asText());
                }
            }
            return new AckMessageImpl(updates);
        }
    }

    private static class AckSerializer extends JsonSerializer<AckMessage> {

        @Override
        public void serialize(final AckMessage ack, final JsonGenerator jgen,
                final SerializerProvider provider) throws IOException, JsonProcessingException {
            jgen.writeStartObject();
            jgen.writeFieldName(AckMessage.MESSSAGE_TYPE_FIELD);
            jgen.writeString(ack.getMessageType().toString().toLowerCase());
            jgen.writeArrayFieldStart(AckMessage.UPDATES_FIELD);
            for (String channelId : ack.getUpdates()) {
                jgen.writeString(channelId);
            }
            jgen.writeEndArray();
            jgen.writeEndObject();
        }
    }
    
    private static class NotificationDeserializer extends JsonDeserializer<NotificationMessageImpl> {

        @Override
        public NotificationMessageImpl deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException,
                JsonProcessingException {
            final ObjectCodec oc = jp.getCodec();
            final JsonNode node = oc.readTree(jp);
            final JsonNode updatesNode = node.get(NotificationMessage.UPDATES_FIELD);
            final Set<Update> updates = new HashSet<Update>();
            if (updatesNode.isArray()) {
                for (JsonNode channelNode : updatesNode) {
                    final JsonNode versionNode = channelNode.get(NotificationMessage.VERSION_FIELD);
                    final JsonNode channelIdNode = channelNode.get(RegisterMessage.CHANNEL_ID_FIELD);
                    updates.add(new UpdateImpl(channelIdNode.asText(), versionNode.asLong()));
                }
            }
            return new NotificationMessageImpl(updates);
        }
    }
    
    private static class NotificationSerializer extends JsonSerializer<NotificationMessage> {

        @Override
        public void serialize(final NotificationMessage notification, final JsonGenerator jgen,
                final SerializerProvider provider) throws IOException, JsonProcessingException {
            jgen.writeStartObject();
            jgen.writeFieldName(NotificationMessage.MESSSAGE_TYPE_FIELD);
            jgen.writeString(notification.getMessageType().toString().toLowerCase());
            jgen.writeArrayFieldStart(NotificationMessage.UPDATES_FIELD);
            for (Update update : notification.getUpdates()) {
                jgen.writeStartObject();
                jgen.writeFieldName(RegisterMessage.CHANNEL_ID_FIELD);
                jgen.writeString(update.getChannelId());
                jgen.writeFieldName(NotificationMessage.VERSION_FIELD);
                jgen.writeNumber(update.getVersion());
                jgen.writeEndObject();
            }
            jgen.writeEndArray();
            jgen.writeEndObject();
        }
    }
    
    private static class UnregisterDeserializer extends JsonDeserializer<UnregisterMessageImpl> {

        @Override
        public UnregisterMessageImpl deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException,
                JsonProcessingException {
            final ObjectCodec oc = jp.getCodec();
            final JsonNode node = oc.readTree(jp);
            final JsonNode channelIdNode = node.get(RegisterMessage.CHANNEL_ID_FIELD);
            return new UnregisterMessageImpl(channelIdNode.asText());
        }
    }
    
    private static class UnregisterMessageSerializer extends JsonSerializer<UnregisterMessage> {

        @Override
        public void serialize(final UnregisterMessage unregister, final JsonGenerator jgen,
                final SerializerProvider provider) throws IOException, JsonProcessingException {
            jgen.writeStartObject();
            jgen.writeFieldName(UnregisterMessage.MESSSAGE_TYPE_FIELD);
            jgen.writeString(unregister.getMessageType().toString().toLowerCase());
            jgen.writeFieldName(RegisterMessage.CHANNEL_ID_FIELD);
            jgen.writeString(unregister.getChannelId());
            jgen.writeEndObject();
        }
    }
    
    private static class MessageTypeDeserializer extends JsonDeserializer<MessageType> {

        @Override
        public MessageType deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException,
                JsonProcessingException {
            final ObjectCodec oc = jp.getCodec();
            final JsonNode node = oc.readTree(jp);
            final JsonNode messageTypeNode = node.get(MessageType.MESSSAGE_TYPE_FIELD);
            return new MessageType() {
                @Override
                public Type getMessageType() {
                    return MessageType.Type.valueOf(messageTypeNode.asText().toUpperCase());
                }
            };
        }
    }
    
    private static class UnregisterResponseDeserializer extends JsonDeserializer<UnregisterResponseImpl> {

        @Override
        public UnregisterResponseImpl deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException,
                JsonProcessingException {
            final ObjectCodec oc = jp.getCodec();
            final JsonNode node = oc.readTree(jp);
            final JsonNode channelIdNode = node.get(RegisterResponse.CHANNEL_ID_FIELD);
            return new UnregisterResponseImpl(channelIdNode.asText(), new StatusImpl(node.get(UnregisterResponse.STATUS_FIELD).asInt(), "N/A"));
        }
    }
    
    private static class UnregisterResponseSerializer extends JsonSerializer<UnregisterResponse> {

        @Override
        public void serialize(final UnregisterResponse unregisterResponse, final JsonGenerator jgen,
                final SerializerProvider provider) throws IOException, JsonProcessingException {
            jgen.writeStartObject();
            jgen.writeFieldName(RegisterResponse.MESSSAGE_TYPE_FIELD);
            jgen.writeString(unregisterResponse.getMessageType().toString().toLowerCase());
            jgen.writeFieldName(RegisterResponse.CHANNEL_ID_FIELD);
            jgen.writeString(unregisterResponse.getChannelId());
            jgen.writeFieldName(RegisterResponse.STATUS_FIELD);
            jgen.writeNumber(unregisterResponse.getStatus().getCode());
            jgen.writeEndObject();
        }
    }
    
}
