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
package org.jboss.aerogear.simplepush.server.netty;

import static org.jboss.aerogear.simplepush.protocol.impl.json.JsonUtil.toJson;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.aerogear.simplepush.protocol.impl.HelloMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.PingMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.RegisterMessageImpl;
import org.jboss.aerogear.simplepush.protocol.impl.UnregisterMessageImpl;

public final class TestUtil {

    private static final Pattern MESSAGE_PATTERN = Pattern.compile("^a\\[\\\"([{\\\"]?\\S.*\\\"?})\\\"\\]");

    private TestUtil() {
    }

    public static String helloFrameAsJson(final String uaid, final String... channelIds) {
        return toJson(new HelloMessageImpl(uaid.toString(), new HashSet<String>(Arrays.asList(channelIds))));
    }

    public static TextWebSocketFrame helloWebSocketFrame(final String uaid, final String... channelIds) {
        return new TextWebSocketFrame(helloFrameAsJson(uaid.toString(), channelIds));
    }

    public static String helloSockJSFrame(final String uaid, final String... channelIds) {
        return asSockjsMessage(helloFrameAsJson(uaid, channelIds));
    }

    public static String registerChannelIdMessageSockJSFrame(final String channelId) {
        return asSockjsMessage(toJson(new RegisterMessageImpl(channelId)));
    }

    public static TextWebSocketFrame registerChannelIdWebSocketFrame(final String channelId) {
        final String json = toJson(new RegisterMessageImpl(channelId));
        return new TextWebSocketFrame(json);
    }

    public static TextWebSocketFrame pingWebSocketFrame() {
        return new TextWebSocketFrame(toJson(new PingMessageImpl()));
    }

    public static TextWebSocketFrame unregisterChannelIdWebSocketFrame(final String channelId) {
        final String json = toJson(new UnregisterMessageImpl(channelId));
        return new TextWebSocketFrame(json);
    }

    public static String unregisterChannelIdMessageSockJSFrame(final String channelId) {
        return asSockjsMessage(toJson(new UnregisterMessageImpl(channelId)));
    }

    public static String pingSockJSFrame() {
        return asSockjsMessage(toJson(new PingMessageImpl()));
    }

    private static String asSockjsMessage(final String content) {
        return "[" + content + "]";
    }

    public static String extractJsonFromSockJSMessage(final String message) {
        final Matcher matcher = MESSAGE_PATTERN.matcher(message);
        if (matcher.find()) {
            return matcher.group(1).replaceAll("\\\\", "");
        } else {
            throw new IllegalArgumentException("The message was not in the correct SockJS message format: " + message);
        }
    }
}
