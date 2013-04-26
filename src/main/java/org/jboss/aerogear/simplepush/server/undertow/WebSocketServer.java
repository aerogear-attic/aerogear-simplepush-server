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
package org.jboss.aerogear.simplepush.server.undertow;

import io.undertow.Undertow;
import io.undertow.predicate.Predicates;
import io.undertow.server.handlers.PredicateHandler;
import io.undertow.server.handlers.RedirectHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.websockets.api.WebSocketSession;
import io.undertow.websockets.api.WebSocketSessionHandler;
import io.undertow.websockets.spi.WebSocketHttpExchange;

public class WebSocketServer {

    public static void main(final String[] args) {
        //System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");
        try {
        Undertow server = Undertow
                .builder()
                .addListener(8080, "localhost")
                .addWebSocketHandler("/simplepush", new WebSocketSessionHandler() {
                    @Override
                    public void onSession(final WebSocketSession session, WebSocketHttpExchange exchange) {
                        System.out.println("Sub protocols: " + session.getSubProtocols());
                        //if (session.getSubProtocols().contains("push-notification")) {
                            session.setFrameHandler(new PushNotificationFrameHandler());
                        //}
                    }
                })
                .setDefaultHandler(
                // we use a predicate handler here. If the path is index.html we
                // serve the page
                // otherwise we redirect to index.html
                        new PredicateHandler(Predicates.paths("netty/websocket.html", "/netty/css/socket.css", "netty/js/socket.js"), 
                                new ResourceHandler().setResourceManager(new ClassPathResourceManager(
                                        WebSocketServer.class.getClassLoader(), "")),
                                new RedirectHandler("http://localhost:8080/netty/websocket.html"))).build();
        server.start();
        System.out.println("WebSocketServer...started");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
