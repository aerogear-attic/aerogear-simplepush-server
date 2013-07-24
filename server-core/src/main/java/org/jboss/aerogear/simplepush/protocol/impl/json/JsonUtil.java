/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
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
import org.jboss.aerogear.simplepush.protocol.HelloMessage;
import org.jboss.aerogear.simplepush.protocol.HelloResponse;
import org.jboss.aerogear.simplepush.protocol.MessageType;
import org.jboss.aerogear.simplepush.protocol.NotificationMessage;
import org.jboss.aerogear.simplepush.protocol.PingMessage;
import org.jboss.aerogear.simplepush.protocol.RegisterMessage;
import org.jboss.aerogear.simplepush.protocol.RegisterResponse;
import org.jboss.aerogear.simplepush.protocol.UnregisterMessage;
import org.jboss.aerogear.simplepush.protocol.UnregisterResponse;
import org.jboss.aerogear.simplepush.protocol.Update;
import org.jboss.aerogear.simplepush.protocol.impl.AckMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.HelloMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.HelloResponseImpl;
import org.jboss.aerogear.simplepush.protocol.impl.NotificationMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.PingMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.RegisterMessageImpl;
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

        module.addDeserializer(RegisterMessageImpl.class, new RegisterDeserializer());
        module.addSerializer(RegisterMessageImpl.class, new RegisterSerializer());
        module.addDeserializer(RegisterResponseImpl.class, new RegisterResponseDeserializer());
        module.addSerializer(RegisterResponseImpl.class, new RegisterResponseSerializer());

        module.addDeserializer(HelloMessageImpl.class, new HelloDeserializer());
        module.addSerializer(HelloMessageImpl.class, new HelloSerializer());
        module.addDeserializer(HelloResponseImpl.class, new HelloResponseDeserializer());
        module.addSerializer(HelloResponseImpl.class, new HelloResponseSerializer());

        module.addDeserializer(AckMessageImpl.class, new AckDeserializer());
        module.addSerializer(AckMessageImpl.class, new AckSerializer());

        module.addDeserializer(PingMessageImpl.class, new PingDeserializer());
        module.addSerializer(PingMessageImpl.class, new PingSerializer());

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

    private static class RegisterDeserializer extends JsonDeserializer<RegisterMessageImpl> {

        @Override
        public RegisterMessageImpl deserialize(final JsonParser jp, final DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            final ObjectCodec oc = jp.getCodec();
            final JsonNode node = oc.readTree(jp);
            return new RegisterMessageImpl(node.get(RegisterMessage.CHANNEL_ID_FIELD).asText());
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

    private static class HelloDeserializer extends JsonDeserializer<HelloMessageImpl> {

        @Override
        public HelloMessageImpl deserialize(final JsonParser jp, final DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            final ObjectCodec oc = jp.getCodec();
            final JsonNode node = oc.readTree(jp);
            final JsonNode channelIdsNode = node.get(HelloMessage.CHANNEL_IDS_FIELD);
            final Set<String> channelIds = new HashSet<String>();
            if (channelIdsNode != null && channelIdsNode.isArray()) {
                for (JsonNode channelIdNode : channelIdsNode) {
                    channelIds.add(channelIdNode.asText());
                }
            }
            final JsonNode uaid = node.get(HelloMessage.UAID_FIELD);
            if (uaid != null) {
                return new HelloMessageImpl(node.get(HelloMessage.UAID_FIELD).asText(), channelIds);
            } else {
                return new HelloMessageImpl();
            }
        }
    }

    private static class HelloSerializer extends JsonSerializer<HelloMessage> {

        @Override
        public void serialize(final HelloMessage hello, final JsonGenerator jgen,
                final SerializerProvider provider) throws IOException, JsonProcessingException {
            jgen.writeStartObject();
            jgen.writeFieldName(HelloMessage.MESSSAGE_TYPE_FIELD);
            jgen.writeString(hello.getMessageType().toString().toLowerCase());
            jgen.writeFieldName(HelloMessage.UAID_FIELD);
            jgen.writeString(hello.getUAID());
            jgen.writeArrayFieldStart(HelloMessage.CHANNEL_IDS_FIELD);
            for (String channelId : hello.getChannelIds()) {
                jgen.writeString(channelId);
            }
            jgen.writeEndArray();
            jgen.writeEndObject();
        }
    }

    private static class HelloResponseDeserializer extends JsonDeserializer<HelloResponseImpl> {

        @Override
        public HelloResponseImpl deserialize(final JsonParser jp, final DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            final ObjectCodec oc = jp.getCodec();
            final JsonNode node = oc.readTree(jp);
            final JsonNode uaid = node.get(HelloMessage.UAID_FIELD);
            return new HelloResponseImpl(UUID.fromString(uaid.asText()).toString());
        }
    }

    private static class HelloResponseSerializer extends JsonSerializer<HelloResponse> {

        @Override
        public void serialize(final HelloResponse helloResponse, final JsonGenerator jgen,
                final SerializerProvider provider) throws IOException, JsonProcessingException {
            jgen.writeStartObject();
            jgen.writeFieldName(HelloMessage.MESSSAGE_TYPE_FIELD);
            jgen.writeString(helloResponse.getMessageType().toString().toLowerCase());
            jgen.writeFieldName(HelloMessage.UAID_FIELD);
            jgen.writeString(helloResponse.getUAID().toString());
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
            final Set<Update> updates = new HashSet<Update>();
            if (updatesNode.isArray()) {
                for (JsonNode updateNode : updatesNode) {
                    updates.add(new UpdateImpl(updateNode.get("channelID").asText(), updateNode.get("version").asLong()));
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
            for (Update update : ack.getUpdates()) {
                jgen.writeStartObject();
                jgen.writeFieldName("channelID");
                jgen.writeString(update.getChannelId());
                jgen.writeFieldName(AckMessage.VERSION_FIELD);
                jgen.writeNumber(update.getVersion());
                jgen.writeEndObject();
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

    private static class PingDeserializer extends JsonDeserializer<PingMessageImpl> {

        @Override
        public PingMessageImpl deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException,
                JsonProcessingException {
            final ObjectCodec oc = jp.getCodec();
            final JsonNode node = oc.readTree(jp);
            if (node.isObject() && node.size() == 0) {
                return new PingMessageImpl(node.toString());
            }
            throw new RuntimeException("Invalid Ping message format : [" + node.toString() + "]");
        }
    }

    private static class PingSerializer extends JsonSerializer<PingMessage> {

        @Override
        public void serialize(final PingMessage ping, final JsonGenerator jgen,
                final SerializerProvider provider) throws IOException, JsonProcessingException {
            jgen.writeStartObject();
            jgen.writeEndObject();
        }
    }

}
