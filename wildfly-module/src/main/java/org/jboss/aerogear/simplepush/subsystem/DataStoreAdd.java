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

import org.jboss.aerogear.simplepush.server.datastore.DataStore;
import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.naming.deployment.ContextNames.BindInfo;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;

class DataStoreAdd extends AbstractAddStepHandler {

    public static final DataStoreAdd INSTANCE = new DataStoreAdd();
    private final Logger logger = Logger.getLogger(DataStoreAdd.class);

    private DataStoreAdd() {
    }

    @Override
    protected void populateModel(final ModelNode operation, final ModelNode model) throws OperationFailedException {
        DataStoreDefinition.DATASOURCE_ATTR.validateAndSet(operation, model);
        DataStoreDefinition.PERSISTENCE_UNIT_ATTR.validateAndSet(operation, model);
        DataStoreDefinition.HOST_ATTR.validateAndSet(operation, model);
        DataStoreDefinition.PORT_ATTR.validateAndSet(operation, model);
    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model,
            ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
            throws OperationFailedException {

        final PathAddress pathAddress = PathAddress.pathAddress(operation.get("address"));
        final String serverName = pathAddress.getElement(1).getValue();
        final String type = pathAddress.getLastElement().getValue();
        switch (DataStoreDefinition.Element.of(type)) {
            case JPA:
                final ModelNode datasourceNode = DataStoreDefinition.DATASOURCE_ATTR.resolveModelAttribute(context, model);
                final ModelNode persistenceUnitNode = DataStoreDefinition.PERSISTENCE_UNIT_ATTR.resolveModelAttribute(context, model);
                final BindInfo bindinfo = ContextNames.bindInfoFor(datasourceNode.asString());
                logger.debug("Adding dependency to [" + bindinfo.getAbsoluteJndiName() + "]");
                final DataStoreService datastoreService = new JpaDataStoreService(persistenceUnitNode.asString());
                final ServiceBuilder<DataStore> sb = context.getServiceTarget().addService(DataStoreService.SERVICE_NAME.append(serverName), datastoreService);
                sb.addDependencies(bindinfo.getBinderServiceName());
                sb.addListener(verificationHandler);
                sb.setInitialMode(Mode.ACTIVE);
                newControllers.add(sb.install());
                break;
            default:
                throw new IllegalStateException("invalid datastore type");
        }
    }



}
