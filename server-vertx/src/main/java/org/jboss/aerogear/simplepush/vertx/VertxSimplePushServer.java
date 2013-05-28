package org.jboss.aerogear.simplepush.vertx;

import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.deploy.Verticle;


public class VertxSimplePushServer extends Verticle {
        
    @Override
    public void start() throws Exception {
        vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
            @Override
            public void handle(final HttpServerRequest request) {
                System.out.println(request);
                request.response.end("bajja");
            }
      }).listen(9999);
    }

}
