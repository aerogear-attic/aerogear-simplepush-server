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
package org.jboss.aerogear.simplepush.server.netty.standalone;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.jboss.aerogear.simplepush.server.datastore.CouchDBDataStore;
import org.junit.Test;

public class ConfigReaderCouchDBTest {

    @Test
    public void couchDBDataStore() {
        final StandaloneConfig config = ConfigReader.parse(ConfigReaderCouchDBTest.class.getResourceAsStream("/simplepush-couchdb-config.json"));
        assertThat(config.dataStore(), is(instanceOf(CouchDBDataStore.class)));
    }

}
