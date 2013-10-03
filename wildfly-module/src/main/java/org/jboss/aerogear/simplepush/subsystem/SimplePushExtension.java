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

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.StandardResourceDescriptionResolver;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.registry.ManagementResourceRegistration;


/**
 * SimplePush Server subsystem for AS 7.x/WildFly
 */
public class SimplePushExtension implements Extension {

    /**
     * The Subystem namespace
     */
    public static final String NAMESPACE = "urn:org.jboss.aerogear.simplepush:1.0";

    /**
     * The name of the subsystem
     */
    public static final String SUBSYSTEM_NAME = "simplepush";

    protected static final String SERVER = "server";
    protected static final PathElement SUBSYSTEM_PATH = PathElement.pathElement(SUBSYSTEM, SUBSYSTEM_NAME);
    protected static final PathElement SERVER_PATH = PathElement.pathElement(SERVER);
    private static final String RESOURCE_NAME = SimplePushExtension.class.getPackage().getName() + ".LocalDescriptions";

    private final SimplePushSubsystemParser parser = new SimplePushSubsystemParser();

    @Override
    public void initializeParsers(final ExtensionParsingContext context) {
        context.setSubsystemXmlMapping(SUBSYSTEM_NAME, NAMESPACE, parser);
    }

    @Override
    public void initialize(final ExtensionContext context) {
        final SubsystemRegistration subsystem = context.registerSubsystem(SUBSYSTEM_NAME, 1, 0);
        final ManagementResourceRegistration registration = subsystem.registerSubsystemModel(SimplePushSubsystemDefinition.INSTANCE);
        registration.registerSubModel(ServerDefinition.INSTANCE);
        subsystem.registerXMLElementWriter(parser);
    }

    static StandardResourceDescriptionResolver getResourceDescriptionResolver(final String keyPrefix) {
        String prefix = SUBSYSTEM_NAME + (keyPrefix == null ? "" : "." + keyPrefix);
        return new StandardResourceDescriptionResolver(prefix, RESOURCE_NAME, SimplePushExtension.class.getClassLoader(), true, false);
    }

}
