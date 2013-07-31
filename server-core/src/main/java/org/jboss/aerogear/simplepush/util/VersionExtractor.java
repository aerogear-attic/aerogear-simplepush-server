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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for extracting the version from a notification request.
 */
public class VersionExtractor {

    private final static Pattern VERSION_PATTERN = Pattern.compile("\\s*version\\s*=\\s*(\\d+)");

    private VersionExtractor() {
    }

    public static String extractVersion(final String payload) {
        final Matcher matcher = VERSION_PATTERN.matcher(payload);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new RuntimeException("Could not find a version in payload [" + payload + "]");
    }

}
