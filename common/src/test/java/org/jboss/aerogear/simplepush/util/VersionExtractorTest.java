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
package org.jboss.aerogear.simplepush.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

import org.junit.Test;

public class VersionExtractorTest {

    @Test
    public void extractVersion() {
        assertThat(VersionExtractor.extractVersion("version=1"), equalTo("1"));
        assertThat(VersionExtractor.extractVersion("version= 2"), equalTo("2"));
        assertThat(VersionExtractor.extractVersion("version =3"), equalTo("3"));
        assertThat(VersionExtractor.extractVersion(" version=10"), equalTo("10"));
        assertThat(VersionExtractor.extractVersion(" version= 11"), equalTo("11"));
        assertThat(VersionExtractor.extractVersion(" version = 12 "), equalTo("12"));
    }

    @Test
    public void noVersion() {
        assertThat(VersionExtractor.extractVersion(""), is(notNullValue()));
        assertThat(VersionExtractor.extractVersion(null), is(notNullValue()));
    }

    @Test (expected = RuntimeException.class)
    public void invalidVersionPropertyName() {
        VersionExtractor.extractVersion("vorsion=12");
    }

    @Test (expected = RuntimeException.class)
    public void invalidVersion() {
        VersionExtractor.extractVersion("version=");
    }

}
