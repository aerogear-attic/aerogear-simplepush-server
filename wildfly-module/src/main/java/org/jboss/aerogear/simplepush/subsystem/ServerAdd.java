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


import java.util.List;

import org.jboss.aerogear.io.netty.handler.codec.sockjs.SockJsConfig;
import org.jboss.aerogear.simplepush.server.DefaultSimplePushConfig;
import org.jboss.aerogear.simplepush.server.DefaultSimplePushConfig.Builder;
import org.jboss.aerogear.simplepush.server.SimplePushServerConfig;
import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.naming.deployment.ContextNames.BindInfo;
import org.jboss.as.network.SocketBinding;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;

class ServerAdd extends AbstractAddStepHandler {

    public static final ServerAdd INSTANCE = new ServerAdd();
    private final Logger logger = Logger.getLogger(ServerAdd.class);

    private ServerAdd() {
    }

    @Override
    protected void populateModel(final ModelNode operation, final ModelNode model) throws OperationFailedException {
        ServerDefinition.SERVER_NAME_ATTR.validateAndSet(operation, model);
        ServerDefinition.SOCKET_BINDING_ATTR.validateAndSet(operation, model);
        ServerDefinition.DATASOURCE_ATTR.validateAndSet(operation, model);
        ServerDefinition.TOKEN_KEY_ATTR.validateAndSet(operation, model);
        ServerDefinition.REAPER_TIMEOUT_ATTR.validateAndSet(operation, model);
        ServerDefinition.NOTIFICATION_TLS_ATTR.validateAndSet(operation, model);
        ServerDefinition.NOTIFICATION_PREFIX_ATTR.validateAndSet(operation, model);
        ServerDefinition.NOTIFICATION_ACK_INTERVAL_ATTR.validateAndSet(operation, model);
        ServerDefinition.NOTIFICATION_HOST_ATTR.validateAndSet(operation, model);
        ServerDefinition.NOTIFICATION_PORT_ATTR.validateAndSet(operation, model);
        ServerDefinition.SOCKJS_PREFIX_ATTR.validateAndSet(operation, model);
        ServerDefinition.SOCKJS_COOKIES_NEEDED_ATTR.validateAndSet(operation, model);
        ServerDefinition.SOCKJS_URL_ATTR.validateAndSet(operation, model);
        ServerDefinition.SOCKJS_SESSION_TIMEOUT_ATTR.validateAndSet(operation, model);
        ServerDefinition.SOCKJS_HEARTBEAT_INTERVAL_ATTR.validateAndSet(operation, model);
        ServerDefinition.SOCKJS_MAX_STREAMING_BYTES_SIZE_ATTR.validateAndSet(operation, model);
        ServerDefinition.SOCKJS_TLS_ATTR.validateAndSet(operation, model);
        ServerDefinition.SOCKJS_ENABLE_WEBSOCKET_ATTR.validateAndSet(operation, model);
        ServerDefinition.SOCKJS_WEBSOCKET_HEARTBEAT_INTERVAL_ATTR.validateAndSet(operation, model);
        ServerDefinition.SOCKJS_WEBSOCKET_PROTOCOLS.validateAndSet(operation, model);
    }

    @Override
    protected void performRuntime(final OperationContext context,
            final ModelNode operation,
            final ModelNode model,
            final ServiceVerificationHandler verificationHandler,
            final List<ServiceController<?>> newControllers) throws OperationFailedException {
        final SimplePushServerConfig simplePushConfig = parseSimplePushOptions(context, model);
        final SockJsConfig sockJsConfig = parseSockJsOptions(context, model);
        final SimplePushService nettyService = new SimplePushService(simplePushConfig, sockJsConfig);

        final String serverName = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();
        final ServiceName serviceName = SimplePushService.createServiceName(serverName);
        final ServiceBuilder<SimplePushService> sb = context.getServiceTarget().addService(serviceName, nettyService);

        final String socketBinding = ServerDefinition.SOCKET_BINDING_ATTR.resolveModelAttribute(context, model).asString();
        sb.addDependency(SocketBinding.JBOSS_BINDING_NAME.append(socketBinding), SocketBinding.class, nettyService.getInjectedSocketBinding());

        final ModelNode datasourceNode = ServerDefinition.DATASOURCE_ATTR.resolveModelAttribute(context, model);
        if (datasourceNode.isDefined()) {
            final BindInfo bindinfo = ContextNames.bindInfoFor(datasourceNode.asString());
            logger.debug("Adding dependency to [" + bindinfo.getAbsoluteJndiName() + "]");
            sb.addDependencies(bindinfo.getBinderServiceName());
        }

        sb.addListener(verificationHandler);
        sb.setInitialMode(Mode.ACTIVE);
        newControllers.add(sb.install());
    }

    private SockJsConfig parseSockJsOptions(
            final OperationContext context, final ModelNode model) throws OperationFailedException {
        final ModelNode sockJsPrefix = ServerDefinition.SOCKJS_PREFIX_ATTR.resolveModelAttribute(context, model);
        final ModelNode sockJsCookiesNeeded = ServerDefinition.SOCKJS_COOKIES_NEEDED_ATTR.resolveModelAttribute(context, model);
        final ModelNode sockJsUrl = ServerDefinition.SOCKJS_URL_ATTR.resolveModelAttribute(context, model);
        final ModelNode sockJsSessionTimeout = ServerDefinition.SOCKJS_SESSION_TIMEOUT_ATTR.resolveModelAttribute(context, model);
        final ModelNode sockJsHeartbeatInterval = ServerDefinition.SOCKJS_HEARTBEAT_INTERVAL_ATTR.resolveModelAttribute(context, model);
        final ModelNode sockJsMaxStreamingBytesSize = ServerDefinition.SOCKJS_MAX_STREAMING_BYTES_SIZE_ATTR.resolveModelAttribute(context, model);
        final ModelNode sockJsTls = ServerDefinition.SOCKJS_TLS_ATTR.resolveModelAttribute(context, model);
        final ModelNode sockJsEnableWebSocket = ServerDefinition.SOCKJS_ENABLE_WEBSOCKET_ATTR.resolveModelAttribute(context, model);
        final ModelNode sockJsWebSocketHeartbeatInterval = ServerDefinition.SOCKJS_WEBSOCKET_HEARTBEAT_INTERVAL_ATTR.resolveModelAttribute(context, model);
        final ModelNode sockJsWebSocketProtocols = ServerDefinition.SOCKJS_WEBSOCKET_PROTOCOLS.resolveModelAttribute(context, model);
        org.jboss.aerogear.io.netty.handler.codec.sockjs.SockJsConfig.Builder sockJsConfig = new SockJsConfig.Builder(sockJsPrefix.asString());
        if (sockJsCookiesNeeded.isDefined() && sockJsCookiesNeeded.asBoolean()) {
            sockJsConfig.cookiesNeeded();
        }
        if (sockJsUrl.isDefined()) {
            sockJsConfig.sockJsUrl(sockJsUrl.asString());
        }
        if (sockJsSessionTimeout.isDefined()) {
            sockJsConfig.sessionTimeout(sockJsSessionTimeout.asLong());
        }
        if (sockJsHeartbeatInterval.isDefined()) {
            sockJsConfig.heartbeatInterval(sockJsHeartbeatInterval.asLong());
        }
        if (sockJsMaxStreamingBytesSize.isDefined()) {
            sockJsConfig.maxStreamingBytesSize(sockJsMaxStreamingBytesSize.asInt());
        }
        if (sockJsTls.isDefined()) {
            sockJsConfig.tls(sockJsTls.asBoolean());
        }
        if (sockJsEnableWebSocket.isDefined() && !sockJsEnableWebSocket.asBoolean()) {
            sockJsConfig.disableWebSocket();
        }
        if (sockJsWebSocketHeartbeatInterval.isDefined()) {
            sockJsConfig.webSocketHeartbeatInterval(sockJsWebSocketHeartbeatInterval.asLong());
        }
        if (sockJsWebSocketProtocols.isDefined()) {
            sockJsConfig.webSocketProtocols(sockJsWebSocketProtocols.asString().split(","));
        }
        return sockJsConfig.build();
    }

    private SimplePushServerConfig parseSimplePushOptions(final OperationContext context, final ModelNode model)
            throws OperationFailedException {
        final ModelNode reaperTimeout = ServerDefinition.REAPER_TIMEOUT_ATTR.resolveModelAttribute(context, model);
        final ModelNode notificationPrefix = ServerDefinition.NOTIFICATION_PREFIX_ATTR.resolveModelAttribute(context, model);
        final ModelNode notificationtTls = ServerDefinition.NOTIFICATION_TLS_ATTR.resolveModelAttribute(context, model);
        final ModelNode notificationAckInterval = ServerDefinition.NOTIFICATION_ACK_INTERVAL_ATTR.resolveModelAttribute(context, model);
        final ModelNode notificationHost = ServerDefinition.NOTIFICATION_HOST_ATTR.resolveModelAttribute(context, model);
        final ModelNode notificationPort = ServerDefinition.NOTIFICATION_PORT_ATTR.resolveModelAttribute(context, model);

        final Builder simplePushConfig = DefaultSimplePushConfig.create();
        simplePushConfig.tokenKey(ServerDefinition.TOKEN_KEY_ATTR.resolveModelAttribute(context, model).asString());
        if (notificationtTls.isDefined()) {
            simplePushConfig.useTls(notificationtTls.asBoolean());
        }
        if (reaperTimeout.isDefined()) {
            simplePushConfig.userAgentReaperTimeout(reaperTimeout.asLong());
        }
        if (notificationPrefix.isDefined()) {
            simplePushConfig.endpointUrlPrefix(notificationPrefix.asString());
        }
        if (notificationAckInterval.isDefined()) {
            simplePushConfig.ackInterval(notificationAckInterval.asLong());
        }
        if (notificationHost.isDefined()) {
            simplePushConfig.host(notificationHost.asString());
        }
        if (notificationPort.isDefined()) {
            simplePushConfig.port(notificationPort.asInt());
        }
        return simplePushConfig.build();
    }

}
