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

import org.jboss.as.controller.AbstractWriteAttributeHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.dmr.ModelNode;

class SimplePushSocketBindingHandler extends AbstractWriteAttributeHandler<Void> {

    public static final SimplePushSocketBindingHandler INSTANCE = new SimplePushSocketBindingHandler();

    private SimplePushSocketBindingHandler() {
        super(ServerDefinition.SOCKET_BINDING_ATTR);
    }

    protected boolean applyUpdateToRuntime(final OperationContext context, 
            final ModelNode operation, 
            final String attributeName, 
            final ModelNode resolvedValue, 
            final ModelNode currentValue, 
            final HandbackHolder<Void> handbackHolder) throws OperationFailedException {
        
        if (attributeName.equals(ServerDefinition.Element.SOCKET_BINDING.localName())) {
            final String serverName = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();
            final SimplePushService service = (SimplePushService) context.getServiceRegistry(true).getRequiredService(SimplePushService.createServiceName(serverName)).getValue();
            //TODO: support changing the socket-binding?
            context.completeStep();
        }
        return false;
    }

    protected void revertUpdateToRuntime(final OperationContext context, 
            final ModelNode operation, 
            final String attributeName, 
            final ModelNode valueToRestore, 
            final ModelNode valueToRevert, 
            final Void handback) {
        // no-op
    }
}
