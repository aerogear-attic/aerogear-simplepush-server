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

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

/**
 * Utility class for encrypting/decrypting
 */
public final class CryptoUtil {

    private static final Charset ASCII = Charset.forName("US-ASCII");
    private static final String ALGORITHM = "AES";
    private static final String TRANSOFRMATION = ALGORITHM + "/CBC/PKCS5Padding";
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
        final Cipher cipher = Cipher.getInstance(TRANSOFRMATION);
        final IvParameterSpec iv = getIV();
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), iv);
        final byte[] encrypted = cipher.doFinal(content.getBytes(ASCII));
        final String base64 = Base64.encodeBase64URLSafeString(prependIV(encrypted, iv.getIV()));
        return URLEncoder.encode(base64, ASCII.displayName());
    }

    private static byte[] prependIV(final byte[] target, final byte[] iv) {
        final byte[] withIV = new byte[target.length + IV_SIZE];
        System.arraycopy(iv, 0, withIV, 0, IV_SIZE);
        System.arraycopy(target, 0, withIV, IV_SIZE, target.length);
        return withIV;
    }


    private static IvParameterSpec getIV(){
        byte[] iv = new byte[IV_SIZE];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        return new IvParameterSpec(iv);
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
        final Cipher cipher = Cipher.getInstance(TRANSOFRMATION);
        final byte[] decodedContent = Base64.decodeBase64(content);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(extractIV(decodedContent)));
        final byte[] decrypted = cipher.doFinal(extractContent(decodedContent));
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


    public static byte[] secretKey(final String seed) {
        try {
            final SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            final PBEKeySpec keySpec = new PBEKeySpec(seed.toCharArray(), salt(8), 65536, 128);
            final SecretKey key = factory.generateSecret(keySpec);
            return key.getEncoded();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] salt(final int size) throws NoSuchAlgorithmException {
        final SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        byte[] buffer = new byte[size];
        secureRandom.nextBytes(buffer);
        return buffer;
    }

    public static EndpointParam decryptEndpoint(final byte[] key, final String encrypted) throws Exception {
        final String decrypt = CryptoUtil.decrypt(key, encrypted);
        final String[] uaidChannelIdPair = decrypt.split("\\.");
        return new EndpointParam(uaidChannelIdPair[0], uaidChannelIdPair[1]);
    }

    public static byte[] randomKey(final int size) {
        try {
            final KeyGenerator gen = KeyGenerator.getInstance(ALGORITHM);
            final SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.generateSeed(128);
            gen.init(128, sr);
            return gen.generateKey().getEncoded();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class EndpointParam {

        private final String uaid;
        private final String channelId;

        public EndpointParam(final String uaid, final String channelId) {
            this.uaid = uaid;
            this.channelId = channelId;
        }

        public String uaid() {
            return uaid;
        }

        public String channelId() {
            return channelId;
        }

    }

}
