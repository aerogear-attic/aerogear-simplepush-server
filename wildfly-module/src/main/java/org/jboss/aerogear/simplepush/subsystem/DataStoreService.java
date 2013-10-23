/**
 * JBoss, Home of Professional Open Source Copyright Red Hat, Inc., and individual contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.jboss.aerogear.simplepush.subsystem;

import org.jboss.aerogear.simplepush.server.datastore.DataStore;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StopContext;

/**
 * A service to inject a {@link DataStore} implementation into the SimplePush service.
 */
public abstract class DataStoreService implements Service<DataStore>{

    public static final ServiceName SERVICE_NAME = ServiceName.JBOSS.append("aerogear", "simplepush", "datastore");

    @Override
    public void stop(final StopContext context) {
    }

}
