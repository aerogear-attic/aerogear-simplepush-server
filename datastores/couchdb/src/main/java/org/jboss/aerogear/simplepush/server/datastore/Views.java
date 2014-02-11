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
package org.jboss.aerogear.simplepush.server.datastore;

/**
 * Contains CouchDB <a href="http://wiki.apache.org/couchdb/HTTP_view_API">views</a>
 */
public enum Views {

    CHANNEL("function(doc) { if (doc.type == \"channel\") { emit(doc.chid, {\"doc\": doc, \"rev\": doc._rev});}}"),
    UAID("function(doc) { if (doc.type == \"channel\") { emit(doc.uaid, {\"doc\": doc, \"rev\": doc._rev});}}"),
    TOKEN("function(doc) { if (doc.type == \"channel\") { emit(doc.token, {\"doc\": doc});}}"),
    UNACKS("function(doc) { if (doc.type == \"ack\") { emit(doc.uaid, {\"doc\": doc});}}"),
    SERVER("function(doc) { if (doc.type == \"server\") { emit({\"salt\": doc.salt});}}");

    private final String mapFunction;
    private final String viewName;

    private Views(final String mapFunction) {
        this.mapFunction = mapFunction;
        viewName = toString().toLowerCase();
    }

    public String mapFunction() {
        return mapFunction;
    }

    public String viewName() {
        return viewName;
    }

}
