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


import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jboss.aerogear.simplepush.subsystem.ServerDefinition.Element.DATASOURCE;
import static org.jboss.aerogear.simplepush.subsystem.ServerDefinition.Element.ENDPOINT_TLS;
import static org.jboss.aerogear.simplepush.subsystem.ServerDefinition.Element.SOCKET_BINDING;
import static org.jboss.aerogear.simplepush.subsystem.ServerDefinition.Element.TOKEN_KEY;
import static org.jboss.aerogear.simplepush.subsystem.SimplePushExtension.NAMESPACE;
import static org.jboss.aerogear.simplepush.subsystem.SimplePushExtension.SUBSYSTEM_NAME;
import static org.jboss.as.controller.PathAddress.pathAddress;
import static org.jboss.as.controller.PathElement.pathElement;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIBE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OUTCOME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUCCESS;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.naming.deployment.ContextNames;
import org.jboss.as.naming.deployment.ContextNames.BindInfo;
import org.jboss.as.subsystem.test.AbstractSubsystemTest;
import org.jboss.as.subsystem.test.AdditionalInitialization;
import org.jboss.as.subsystem.test.ControllerInitializer;
import org.jboss.as.subsystem.test.KernelServices;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceNotFoundException;
import org.jboss.msc.service.ServiceTarget;
import org.junit.Test;

public class SubsystemParsingTestCase extends AbstractSubsystemTest {

    private final String subsystemXml =
        "<subsystem xmlns=\"" + NAMESPACE + "\">" +
            "<server socket-binding=\"simplepush\" " +
                "datasource-jndi-name=\"java:jboss/datasources/TestDS\" token-key=\"testing\" endpoint-tls=\"false\"/>" +
        "</subsystem>";

    public SubsystemParsingTestCase() {
        super(SUBSYSTEM_NAME, new SimplePushExtension());
    }

    @Test
    public void parseSubsystem() throws Exception {
        final List<ModelNode> operations = parse(subsystemXml);
        assertThat(operations.size(), is(2));
        final ModelNode subsystem = operations.get(0);
        assertThat(subsystem.get(OP).asString(), equalTo(ADD));
        final PathAddress address = pathAddress(subsystem.get(OP_ADDR));
        assertThat(address.size(), is(1));
        assertThat(address.getElement(0).getKey(), equalTo(SUBSYSTEM));
        assertThat(address.getElement(0).getValue(), equalTo(SUBSYSTEM_NAME));
    }

    @Test
    public void parseOptions() throws Exception {
        final List<ModelNode> operations = parse(subsystemXml);
        final ModelNode options = operations.get(1);
        assertThat(options.get(OP).asString(), equalTo(ADD));
        assertOptions(options);
    }

    @Test
    public void installIntoController() throws Exception {
        final KernelServices services = installInController(new AdditionalServices(), subsystemXml);
        final ModelNode model = services.readWholeModel();
        assertThat(model.get(SUBSYSTEM).hasDefined(SUBSYSTEM_NAME), is(true));
        assertThat(model.get(SUBSYSTEM, SUBSYSTEM_NAME).hasDefined("server"), is(true));
        assertOptions(model.get(SUBSYSTEM, SUBSYSTEM_NAME, "server", "simplepush"));
    }

    @Test
    public void parseAndMarshalModel() throws Exception {
        final KernelServices servicesA = installInController(new AdditionalServices(), subsystemXml);
        final ModelNode modelA = servicesA.readWholeModel();
        servicesA.shutdown();
        final String marshalled = servicesA.getPersistedSubsystemXml();
        final KernelServices servicesB = installInController(new AdditionalServices(), marshalled);
        final ModelNode modelB = servicesB.readWholeModel();
        super.compare(modelA, modelB);
    }

    @Test
    public void describeHandler() throws Exception {
        final String subsystemXml = "<subsystem xmlns=\"" + NAMESPACE + "\">" + "</subsystem>";
        final KernelServices servicesA = installInController(new AdditionalServices(), subsystemXml);
        final ModelNode modelA = servicesA.readWholeModel();
        final ModelNode describeOp = new ModelNode();
        describeOp.get(OP).set(DESCRIBE);
        describeOp.get(OP_ADDR).set(pathAddress(pathElement(SUBSYSTEM, SUBSYSTEM_NAME)).toModelNode());
        final List<ModelNode> operations = checkResultAndGetContents(servicesA.executeOperation(describeOp)).asList();
        final KernelServices servicesB = installInController(new AdditionalServices(), operations);
        final ModelNode modelB = servicesB.readWholeModel();
        super.compare(modelA, modelB);

    }

    @Test (expected = ServiceNotFoundException.class)
    public void subsystemRemoval() throws Exception {
        final KernelServices services = installInController(new AdditionalServices(), subsystemXml);
        services.getContainer().getRequiredService(NettyService.createServiceName("simplepush"));
        super.assertRemoveSubsystemResources(services);
        services.getContainer().getRequiredService(NettyService.createServiceName("simplepush"));
    }

    @Test
    public void addSecondServer() throws Exception {
        final KernelServices services = installInController(new AdditionalServices(), subsystemXml);
        final PathAddress serverAddress = pathAddress(pathElement(SUBSYSTEM, SUBSYSTEM_NAME), pathElement("server", "foo"));
        final ModelNode serverTwo = new ModelNode();
        serverTwo.get(OP).set(ADD);
        serverTwo.get(OP_ADDR).set(serverAddress.toModelNode());
        serverTwo.get(SOCKET_BINDING.localName()).set("mysocket");
        serverTwo.get(DATASOURCE.localName()).set("java:jboss/datasources/NettyDS");
        serverTwo.get(TOKEN_KEY.localName()).set("123456");
        serverTwo.get(ENDPOINT_TLS.localName()).set("true");
        assertThat(services.executeOperation(serverTwo).get(OUTCOME).asString(), equalTo(SUCCESS));

        final ModelNode model = services.readWholeModel();
        assertThat(model.get(SUBSYSTEM, SUBSYSTEM_NAME, "server").hasDefined("foo"), is(true));
        final ModelNode fooOptions = model.get(SUBSYSTEM, SUBSYSTEM_NAME, "server", "foo");
        assertThat(fooOptions.get(SOCKET_BINDING.localName()).asString(), equalTo("mysocket"));
        assertThat(fooOptions.get(DATASOURCE.localName()).asString(), equalTo("java:jboss/datasources/NettyDS"));
        assertThat(fooOptions.get(TOKEN_KEY.localName()).asString(), equalTo("123456"));
        assertThat(fooOptions.get(ENDPOINT_TLS.localName()).asBoolean(), is(true));
    }

    private void assertOptions(final ModelNode options) {
        assertThat(options.get(SOCKET_BINDING.localName()).asString(), equalTo("simplepush"));
        assertThat(options.get(DATASOURCE.localName()).asString(), equalTo("java:jboss/datasources/TestDS"));
        assertThat(options.get(TOKEN_KEY.localName()).asString(), equalTo("testing"));
        assertThat(options.get(ENDPOINT_TLS.localName()).asBoolean(), is(false));
    }

    private static class AdditionalServices extends AdditionalInitialization {

        @Override
        protected void setupController(final ControllerInitializer controllerInitializer) {
            controllerInitializer.setBindAddress("127.0.0.1");
            controllerInitializer.addSocketBinding("mysocket", 18888);
            controllerInitializer.addSocketBinding("simplepush", 17777);
        }

        @Override
        protected void addExtraServices(final ServiceTarget serviceTarget) {
            final Service<?> ds = mock(Service.class);
            final BindInfo testBindInfo = ContextNames.bindInfoFor("java:jboss/datasources/TestDS");
            final ServiceBuilder<?> testDS = serviceTarget.addService(testBindInfo.getBinderServiceName(), ds);
            testDS.install();
            final BindInfo nettyBindInfo = ContextNames.bindInfoFor("java:jboss/datasources/NettyDS");
            final ServiceBuilder<?> nettyDS = serviceTarget.addService(nettyBindInfo.getBinderServiceName(), ds);
            nettyDS.install();
        }
    }
}
