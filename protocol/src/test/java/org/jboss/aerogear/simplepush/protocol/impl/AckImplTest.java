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
package org.jboss.aerogear.simplepush.protocol.impl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import org.jboss.aerogear.simplepush.protocol.Ack;
import org.junit.Test;

public class AckImplTest {

    @Test
    public void equalsContractReflexive() {
        final Ack x = new AckImpl("ch1", 10L);
        assertThat(x, equalTo(x));
    }

    @Test
    public void equalsContractSymetric() {
        final Ack x = new AckImpl("ch1", 10L);
        final Ack y = new AckImpl("ch1", 10L);
        assertThat(x, equalTo(y));
        assertThat(y, equalTo(x));
        assertThat(x.hashCode(), equalTo(y.hashCode()));
    }

    @Test
    public void equalsContractTransitive() {
        final Ack x = new AckImpl("ch1", 10L);
        final Ack y = new AckImpl("ch1", 10L);
        final Ack z = new AckImpl("ch1", 10L);
        assertThat(x, equalTo(y));
        assertThat(y, equalTo(z));
        assertThat(x, equalTo(z));
    }

    @Test
    public void equalsContractConsistent() {
        final Ack x = new AckImpl("ch1", 10L);
        final Ack y = new AckImpl("ch2", 10L);
        assertThat(x.equals(y), is(false));
    }

    @Test
    public void equalsContractNull() {
        final Ack x = new AckImpl("ch1", 10L);
        assertThat(x.equals(null), is(false));
    }

    @Test
    public void versionNotPartOfContract() {
        final Ack x = new AckImpl("ch1", 11L);
        final Ack y = new AckImpl("ch1", 12L);
        assertThat(x.equals(y), is(true));
    }

}
