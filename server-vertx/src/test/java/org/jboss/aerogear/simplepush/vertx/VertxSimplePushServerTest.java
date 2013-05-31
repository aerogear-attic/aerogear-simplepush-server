/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.simplepush.vertx;


public class VertxSimplePushServerTest {//extends TestVerticle {
    
    /*
     * TODO: Add tests
    public void testSomething() {
        container.deployVerticle(VertxSimplePushServer.class.getName(), new JsonObject(), 1, new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> ar) {
                if (ar.failed()) {
                    ar.cause().printStackTrace();
                }
                assertThat(ar.succeeded(), is(true));
                final HttpClient httpClient = vertx.createHttpClient();
                httpClient.setHost("localhost").setPort(7777);
                httpClient.post("/simplepush/000/232", new Handler<HttpClientResponse>(){
                    @Override
                    public void handle(final HttpClientResponse response) {
                        System.out.println("Response : " + response.statusCode());
                    }
                });
            }
        });
    }
    */
}
