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
import java.util.concurrent.ThreadFactory;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.naming.deployment.ContextNames.BindInfo;
import org.jboss.as.network.SocketBinding;
import org.jboss.as.threads.ThreadsServices;
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
        ServerDefinition.SOCKET_BINDING_ATTR.validateAndSet(operation, model);
        ServerDefinition.FACTORY_CLASS_ATTR.validateAndSet(operation, model);
        ServerDefinition.THREAD_FACTORY_ATTR.validateAndSet(operation, model);
        ServerDefinition.DATASOURCE_ATTR.validateAndSet(operation, model);
        ServerDefinition.TOKEN_KEY_ATTR.validateAndSet(operation, model);
    }

    @Override
    protected void performRuntime(final OperationContext context,
            final ModelNode operation,
            final ModelNode model,
            final ServiceVerificationHandler verificationHandler,
            final List<ServiceController<?>> newControllers) throws OperationFailedException {
        final String factoryClass = ServerDefinition.FACTORY_CLASS_ATTR.resolveModelAttribute(context, model).asString();
        final String socketBinding = ServerDefinition.SOCKET_BINDING_ATTR.resolveModelAttribute(context, model).asString();
        final ModelNode threadFactoryNode = ServerDefinition.THREAD_FACTORY_ATTR.resolveModelAttribute(context, model);
        final ModelNode datasourceNode = ServerDefinition.DATASOURCE_ATTR.resolveModelAttribute(context, model);

        final String serverName = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();
        final String tokenKey = ServerDefinition.TOKEN_KEY_ATTR.resolveModelAttribute(context, model).asString();
        final NettyService nettyService = new NettyService(serverName, factoryClass, tokenKey);

        final ServiceName name = NettyService.createServiceName(serverName);
        final ServiceBuilder<NettyService> sb = context.getServiceTarget().addService(name, nettyService);
        sb.addDependency(SocketBinding.JBOSS_BINDING_NAME.append(socketBinding), SocketBinding.class, nettyService.getInjectedSocketBinding());

        if (threadFactoryNode.isDefined()) {
            sb.addDependency(ThreadsServices.threadFactoryName(threadFactoryNode.asString()), ThreadFactory.class, nettyService.getInjectedThreadFactory());
        }

        if (datasourceNode.isDefined()) {
            final BindInfo bindinfo = ContextNames.bindInfoFor(datasourceNode.asString());
            logger.info("Adding dependency to [" + bindinfo.getAbsoluteJndiName() + "]");
            sb.addDependencies(bindinfo.getBinderServiceName());
        }

        sb.addListener(verificationHandler);
        sb.setInitialMode(Mode.ACTIVE);
        newControllers.add(sb.install());
    }

}
