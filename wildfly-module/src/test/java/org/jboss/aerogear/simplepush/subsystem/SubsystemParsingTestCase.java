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
import static org.jboss.aerogear.simplepush.subsystem.DataStoreDefinition.Element.DATASOURCE;
import static org.jboss.aerogear.simplepush.subsystem.DataStoreDefinition.Element.PERSISTENCE_UNIT;
import static org.jboss.aerogear.simplepush.subsystem.ServerDefinition.Element.NOTIFICATION_ACK_INTERVAL;
import static org.jboss.aerogear.simplepush.subsystem.ServerDefinition.Element.NOTIFICATION_PREFIX;
import static org.jboss.aerogear.simplepush.subsystem.ServerDefinition.Element.NOTIFICATION_SOCKET_BINDING;
import static org.jboss.aerogear.simplepush.subsystem.ServerDefinition.Element.NOTIFICATION_TLS;
import static org.jboss.aerogear.simplepush.subsystem.ServerDefinition.Element.REAPER_TIMEOUT;
import static org.jboss.aerogear.simplepush.subsystem.ServerDefinition.Element.SOCKET_BINDING;
import static org.jboss.aerogear.simplepush.subsystem.ServerDefinition.Element.SOCKJS_COOKIES_NEEDED;
import static org.jboss.aerogear.simplepush.subsystem.ServerDefinition.Element.SOCKJS_ENABLE_WEBSOCKET;
import static org.jboss.aerogear.simplepush.subsystem.ServerDefinition.Element.SOCKJS_HEARTBEAT_INTERVAL;
import static org.jboss.aerogear.simplepush.subsystem.ServerDefinition.Element.SOCKJS_KEYSTORE;
import static org.jboss.aerogear.simplepush.subsystem.ServerDefinition.Element.SOCKJS_KEYSTORE_PASSWORD;
import static org.jboss.aerogear.simplepush.subsystem.ServerDefinition.Element.SOCKJS_MAX_STREAMING_BYTES_SIZE;
import static org.jboss.aerogear.simplepush.subsystem.ServerDefinition.Element.SOCKJS_PREFIX;
import static org.jboss.aerogear.simplepush.subsystem.ServerDefinition.Element.SOCKJS_SESSION_TIMEOUT;
import static org.jboss.aerogear.simplepush.subsystem.ServerDefinition.Element.SOCKJS_TLS;
import static org.jboss.aerogear.simplepush.subsystem.ServerDefinition.Element.SOCKJS_URL;
import static org.jboss.aerogear.simplepush.subsystem.ServerDefinition.Element.SOCKJS_WEBSOCKET_HEARTBEAT_INTERVAL;
import static org.jboss.aerogear.simplepush.subsystem.ServerDefinition.Element.SOCKJS_WEBSOCKET_PROTOCOLS;
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

    static {
        System.setProperty("org.jboss.logging.provider", "slf4j");
    }

    private final String subsystemXml =
        "<subsystem xmlns=\"" + NAMESPACE + "\">" +
            "<server socket-binding=\"simplepush\" " +
                "token-key=\"testing\" " +
                "useragent-reaper-timeout=\"16000\" " +
                "notification-prefix=\"/update\" " +
                "notification-tls=\"false\" " +
                "notification-ack-interval=\"120000\" " +
                "notification-socket-binding=\"simplepush-notify\" " +
                "sockjs-prefix=\"/someServiceName\" " +
                "sockjs-cookies-needed=\"false\" " +
                "sockjs-url=\"http://somehost.com/sockjs.js\" " +
                "sockjs-session-timeout=\"2000\" " +
                "sockjs-heartbeat-interval=\"8000\" " +
                "sockjs-max-streaming-bytes-size=\"65356\" " +
                "sockjs-tls=\"true\" " +
                "sockjs-keystore=\"/simplepush-sample.keystore\" " +
                "sockjs-keystore-password=\"simplepush\" " +
                "sockjs-enable-websocket=\"false\" " +
                "sockjs-websocket-heartbeat-interval=\"600000\" " +
                "sockjs-websocket-protocols=\"push-notification, myproto\">" +
                "<datastore>" +
                    "<jpa datasource-jndi-name=\"java:jboss/datasources/TestDS\" persistence-unit=\"SimplePushPU\"/>" +
                "</datastore>" +
            "</server>" +
        "</subsystem>";

    public SubsystemParsingTestCase() {
        super(SUBSYSTEM_NAME, new SimplePushExtension());
    }

    @Test
    public void parseSubsystem() throws Exception {
        final List<ModelNode> operations = parse(subsystemXml);
        assertThat(operations.size(), is(3));
        final ModelNode subsystem = operations.get(0);
        assertThat(subsystem.get(OP).asString(), equalTo(ADD));
        final PathAddress address = pathAddress(subsystem.get(OP_ADDR));
        assertThat(address.size(), is(1));
        assertThat(address.getElement(0).getKey(), equalTo(SUBSYSTEM));
        assertThat(address.getElement(0).getValue(), equalTo(SUBSYSTEM_NAME));
    }

    @Test
    public void parseServerAttributes() throws Exception {
        final List<ModelNode> operations = parse(subsystemXml);
        final ModelNode options = operations.get(1);
        assertThat(options.get(OP).asString(), equalTo(ADD));
        assertOptions(options);
        final ModelNode datastore = operations.get(2);
        assertThat(datastore.get(DATASOURCE.localName()).asString(), equalTo("java:jboss/datasources/TestDS"));
        assertThat(datastore.get(PERSISTENCE_UNIT.localName()).asString(), equalTo("SimplePushPU"));
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
        services.getContainer().getRequiredService(SimplePushService.createServiceName("simplepush"));
        super.assertRemoveSubsystemResources(services);
        services.getContainer().getRequiredService(SimplePushService.createServiceName("simplepush"));
    }

    @Test
    public void addSecondServer() throws Exception {
        final KernelServices services = installInController(new AdditionalServices(), subsystemXml);
        final PathAddress serverAddress = pathAddress(pathElement(SUBSYSTEM, SUBSYSTEM_NAME), pathElement("server", "foo"));
        final ModelNode operation = new ModelNode();
        operation.get(OP).set("composite");
        operation.get(ADD).setEmptyList();
        ModelNode steps = operation.get("steps");

        final ModelNode serverTwo = new ModelNode();
        serverTwo.get(OP).set(ADD);
        serverTwo.get(OP_ADDR).set(serverAddress.toModelNode());
        serverTwo.get(SOCKET_BINDING.localName()).set("mysocket");
        serverTwo.get(TOKEN_KEY.localName()).set("123456");
        serverTwo.get(NOTIFICATION_TLS.localName()).set("true");
        serverTwo.get(REAPER_TIMEOUT.localName()).set(20000);
        serverTwo.get(NOTIFICATION_PREFIX.localName()).set("/endpoints");
        serverTwo.get(NOTIFICATION_TLS.localName()).set(false);
        serverTwo.get(NOTIFICATION_ACK_INTERVAL.localName()).set(10000);
        serverTwo.get(NOTIFICATION_SOCKET_BINDING.localName()).set("simplepush-notify");
        serverTwo.get(SOCKJS_PREFIX.localName()).set("/foo");
        serverTwo.get(SOCKJS_COOKIES_NEEDED.localName()).set("false");
        serverTwo.get(SOCKJS_URL.localName()).set("http://foo.com/sockjs.js");
        serverTwo.get(SOCKJS_SESSION_TIMEOUT.localName()).set(3000);
        serverTwo.get(SOCKJS_HEARTBEAT_INTERVAL.localName()).set(9000);
        serverTwo.get(SOCKJS_MAX_STREAMING_BYTES_SIZE.localName()).set(23333);
        serverTwo.get(SOCKJS_TLS.localName()).set(false);
        serverTwo.get(SOCKJS_ENABLE_WEBSOCKET.localName()).set(true);
        serverTwo.get(SOCKJS_WEBSOCKET_HEARTBEAT_INTERVAL.localName()).set(300000L);
        steps.add(serverTwo);


        final ModelNode serverTwoDatastore = new ModelNode();
        serverTwoDatastore.get(OP).set(ADD);
        serverTwoDatastore.get(OP_ADDR).set(serverAddress.toModelNode().add(DataStoreDefinition.DATASTORE, DataStoreDefinition.Element.JPA.localName()));
        serverTwoDatastore.get(DATASOURCE.localName()).set("java:jboss/datasources/NettyDS");
        serverTwoDatastore.get(PERSISTENCE_UNIT.localName()).set("SimplePushPU");
        steps.add(serverTwoDatastore);
        assertThat(services.executeOperation(operation).get(OUTCOME).asString(), equalTo(SUCCESS));

        final ModelNode model = services.readWholeModel();
        assertThat(model.get(SUBSYSTEM, SUBSYSTEM_NAME, "server").hasDefined("foo"), is(true));
        final ModelNode fooOptions = model.get(SUBSYSTEM, SUBSYSTEM_NAME, "server", "foo");
        assertThat(fooOptions.get(SOCKET_BINDING.localName()).asString(), equalTo("mysocket"));
        assertThat(fooOptions.get(TOKEN_KEY.localName()).asString(), equalTo("123456"));
        assertThat(fooOptions.get(REAPER_TIMEOUT.localName()).asLong(), is(20000L));
        assertThat(fooOptions.get(NOTIFICATION_TLS.localName()).asBoolean(), is(false));
        assertThat(fooOptions.get(NOTIFICATION_PREFIX.localName()).asString(), equalTo("/endpoints"));
        assertThat(fooOptions.get(NOTIFICATION_ACK_INTERVAL.localName()).asLong(), is(10000L));
        assertThat(fooOptions.get(NOTIFICATION_SOCKET_BINDING.localName()).asString(), equalTo("simplepush-notify"));
        assertThat(fooOptions.get(SOCKJS_PREFIX.localName()).asString(), equalTo("/foo"));
        assertThat(fooOptions.get(SOCKJS_COOKIES_NEEDED.localName()).asBoolean(), is(false));
        assertThat(fooOptions.get(SOCKJS_URL.localName()).asString(), equalTo("http://foo.com/sockjs.js"));
        assertThat(fooOptions.get(SOCKJS_SESSION_TIMEOUT.localName()).asLong(), is(3000L));
        assertThat(fooOptions.get(SOCKJS_HEARTBEAT_INTERVAL.localName()).asLong(), is(9000L));
        assertThat(fooOptions.get(SOCKJS_MAX_STREAMING_BYTES_SIZE.localName()).asLong(), is(23333L));
        assertThat(fooOptions.get(SOCKJS_TLS.localName()).asBoolean(), is(false));
        assertThat(fooOptions.get(SOCKJS_ENABLE_WEBSOCKET.localName()).asBoolean(), is(true));
        assertThat(fooOptions.get(SOCKJS_WEBSOCKET_HEARTBEAT_INTERVAL.localName()).asLong(), is(300000L));
        assertThat(fooOptions.get(SOCKJS_WEBSOCKET_PROTOCOLS.localName()).asString(), equalTo("push-notification"));

        final ModelNode fooJpa = fooOptions.get(DataStoreDefinition.DATASTORE, DataStoreDefinition.Element.JPA.localName());
        assertThat(fooJpa.get(DATASOURCE.localName()).asString(), equalTo("java:jboss/datasources/NettyDS"));
    }

    private void assertOptions(final ModelNode options) {
        assertThat(options.get(SOCKET_BINDING.localName()).asString(), equalTo("simplepush"));
        assertThat(options.get(TOKEN_KEY.localName()).asString(), equalTo("testing"));
        assertThat(options.get(REAPER_TIMEOUT.localName()).asLong(), is(16000L));
        assertThat(options.get(NOTIFICATION_PREFIX.localName()).asString(), equalTo("/update"));
        assertThat(options.get(NOTIFICATION_TLS.localName()).asBoolean(), is(false));
        assertThat(options.get(NOTIFICATION_ACK_INTERVAL.localName()).asLong(), equalTo(120000L));
        assertThat(options.get(NOTIFICATION_SOCKET_BINDING.localName()).asString(), equalTo("simplepush-notify"));
        assertThat(options.get(SOCKJS_PREFIX.localName()).asString(), equalTo("/someServiceName"));
        assertThat(options.get(SOCKJS_COOKIES_NEEDED.localName()).asBoolean(), is(false));
        assertThat(options.get(SOCKJS_URL.localName()).asString(), equalTo("http://somehost.com/sockjs.js"));
        assertThat(options.get(SOCKJS_SESSION_TIMEOUT.localName()).asLong(), is(2000L));
        assertThat(options.get(SOCKJS_HEARTBEAT_INTERVAL.localName()).asLong(), is(8000L));
        assertThat(options.get(SOCKJS_MAX_STREAMING_BYTES_SIZE.localName()).asLong(), is(65356L));
        assertThat(options.get(SOCKJS_TLS.localName()).asBoolean(), is(true));
        assertThat(options.get(SOCKJS_KEYSTORE.localName()).asString(), equalTo("/simplepush-sample.keystore"));
        assertThat(options.get(SOCKJS_KEYSTORE_PASSWORD.localName()).asString(), equalTo("simplepush"));
        assertThat(options.get(SOCKJS_ENABLE_WEBSOCKET.localName()).asBoolean(), is(false));
        assertThat(options.get(SOCKJS_WEBSOCKET_HEARTBEAT_INTERVAL.localName()).asLong(), is(600000L));
        assertThat(options.get(SOCKJS_WEBSOCKET_PROTOCOLS.localName()).asString(), equalTo("push-notification, myproto"));
    }

    private static class AdditionalServices extends AdditionalInitialization {

        @Override
        protected void setupController(final ControllerInitializer controllerInitializer) {
            controllerInitializer.setBindAddress("127.0.0.1");
            controllerInitializer.addSocketBinding("mysocket", 18888);
            controllerInitializer.addSocketBinding("simplepush", 17777);
            controllerInitializer.addSocketBinding("simplepush-notify", 8000);
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
