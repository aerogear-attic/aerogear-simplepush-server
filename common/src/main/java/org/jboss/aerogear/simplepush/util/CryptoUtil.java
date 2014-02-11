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

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import org.jboss.aerogear.AeroGearCrypto;
import org.jboss.aerogear.crypto.BlockCipher;
import org.jboss.aerogear.crypto.CryptoBox;
import org.jboss.aerogear.crypto.encoders.Encoder;

/**
 * Utility class for encrypting/decrypting
 */
public final class CryptoUtil {

    private static final Charset ASCII = Charset.forName("US-ASCII");
    private static final int IV_SIZE = 16;

    private CryptoUtil() {
    }

    /**
     * Encrypts the content passed-in.
     *
     * @param key the key used for the underlying cypher.
     * @param content the content to encrypted
     * @return {@code String} the encoded content as base64 encoded string.
     *
     * @throws Exception
     */
    public static String encrypt(final byte[] key, final String content) throws Exception {
        final byte[] iv = BlockCipher.getIV();
        final byte[] encrypted = new CryptoBox(key).encrypt(iv, content.getBytes(ASCII));
        final String base64 = Encoder.BASE64.encode(prependIV(encrypted, iv));
        return URLEncoder.encode(base64, ASCII.displayName());
    }

    private static byte[] prependIV(final byte[] target, final byte[] iv) {
        final byte[] withIV = new byte[target.length + IV_SIZE];
        System.arraycopy(iv, 0, withIV, 0, IV_SIZE);
        System.arraycopy(target, 0, withIV, IV_SIZE, target.length);
        return withIV;
    }

    /**
     * Decrypts the content passed-in.
     *
     * @param key the key used for the underlying cypher.
     * @param content the content to decrypted.
     * @return {@code String} the descrypted content as a String.
     * @throws InvalidKeyException
     *
     * @throws Exception
     */
    public static String decrypt(final byte[] key, final String content) throws Exception {
        final byte[] decodedContent = Encoder.BASE64.decode(URLDecoder.decode(content, ASCII.displayName()));
        final byte[] iv = extractIV(decodedContent);
        final byte[] decrypted = new CryptoBox(key).decrypt(iv, extractContent(decodedContent));
        return new String(decrypted, ASCII);
    }

    private static byte[] extractIV(final byte[] from) {
        final byte[] iv = new byte[IV_SIZE];
        System.arraycopy(from, 0, iv, 0, iv.length);
        return iv;
    }

    private static byte[] extractContent(final byte[] from) {
        final byte[] decodedContent = new byte[from.length - IV_SIZE];
        System.arraycopy(from, IV_SIZE, decodedContent, 0, from.length - IV_SIZE);
        return decodedContent;
    }


    public static byte[] secretKey(final String password, final byte[] salt) {
        try {
            return AeroGearCrypto.pbkdf2().generateSecretKey(password, salt, 100000).getEncoded();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String endpointToken(final String uaid, final String channelId, final byte[] key) {
        try {
            final String path = uaid + "." + channelId;
            return encrypt(key, path);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

}
