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
package org.jboss.aerogear.simplepush.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.UUID;

import org.junit.Test;

public class CryptoUtilTest {

    @Test
    public void encrypt() throws Exception {
        final byte[] salt = "some salt for the server private".getBytes();
        final byte[] key = CryptoUtil.secretKey("key", salt);
        final String encrypted = CryptoUtil.encrypt(key, "some string to endrypt");
        assertThat(encrypted, is(notNullValue()));
    }

    @Test
    public void decrypt() throws Exception {
        final byte[] salt = "some salt for the server private".getBytes();
        final byte[] encryptKey = CryptoUtil.secretKey("key", salt);
        final String expected = UUID.randomUUID().toString() + "." + UUID.randomUUID().toString();
        final String encrypted = CryptoUtil.encrypt(encryptKey, expected);
        final byte[] decryptKey = CryptoUtil.secretKey("key", salt);
        assertThat(CryptoUtil.decrypt(decryptKey, encrypted), is(equalTo(expected)));
    }

}
