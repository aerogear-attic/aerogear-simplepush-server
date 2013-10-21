/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.simplepush.server;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class DefaultChannelTest {

    @Test(expected = IllegalArgumentException.class)
    public void constructWithNegativeVersion() {
        new DefaultChannel("test", "123abc", -1, "33ddd2elaeee");
    }

    @Test
    public void equalsContractReflexive() {
        final Channel x = new DefaultChannel("test", "123abc", 1, "33ddd2elaeee");
        assertThat(x, equalTo(x));
    }

    @Test
    public void equalsContractSymetric() {
        final Channel x = new DefaultChannel("test", "123abc", 1, "33ddd2elaeee");
        final Channel y = new DefaultChannel("test", "123abc", 1, "33ddd2elaeee");
        assertThat(x, equalTo(y));
        assertThat(y, equalTo(x));
        assertThat(x.hashCode(), equalTo(y.hashCode()));
    }

    @Test
    public void equalsContractTransitive() {
        final Channel x = new DefaultChannel("test", "123abc", 1, "33ddd2elaeee");
        final Channel y = new DefaultChannel("test",  "123abc", 1, "33ddd2elaeee");
        final Channel z = new DefaultChannel("test", "123abc", 1, "33ddd2elaeee");
        assertThat(x, equalTo(y));
        assertThat(y, equalTo(z));
        assertThat(x, equalTo(z));
        assertThat(x.hashCode(), equalTo(z.hashCode()));
    }

    @Test
    public void equalsConsistent() {
        final Channel x = new DefaultChannel("test", "123abc", 1, "33ddd2elaeee");
        final Channel y = new DefaultChannel("test", "xyz987", 1, "33ddd2elaeee");
        assertThat(x.equals(y), is(false));
    }

    @Test
    public void equalsContractNull() {
        final Channel x = new DefaultChannel("test", "123abc", 1, "33ddd2elaeee");
        assertThat(x.equals(null), is(false));
    }

}
