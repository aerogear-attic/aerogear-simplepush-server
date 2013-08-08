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
package org.jboss.aerogear.simplepush.server.netty;

import static org.jboss.aerogear.simplepush.server.netty.NettySockJSServer.Options.value;
import static org.jboss.aerogear.simplepush.server.netty.NettySockJSServer.Options.Args;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.sockjs.Config;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.aerogear.simplepush.server.DefaultSimplePushConfig;
import org.jboss.aerogear.simplepush.server.SimplePushServerConfig;
import org.jboss.aerogear.simplepush.server.datastore.DataStore;
import org.jboss.aerogear.simplepush.server.datastore.InMemoryDataStore;

/**
 * A standalone Netty SockJS SimplePush Server.
 */
public class NettySockJSServer {

    private final SimplePushServerConfig simplePushConfig;
    private final Config sockJSConfig;
    private Map<Options.Args, Option<?>> options;

    public NettySockJSServer(final Map<Args, Option<?>> options, final SimplePushServerConfig simplePushConfig, final Config sockJSConfig) {
        this.options = options;
        this.simplePushConfig = simplePushConfig;
        this.sockJSConfig = sockJSConfig;
    }

    public void run() throws Exception {
        final DataStore datastore = new InMemoryDataStore();
        final EventLoopGroup bossGroup = new NioEventLoopGroup();
        final EventLoopGroup workerGroup = new NioEventLoopGroup();
        final DefaultEventExecutorGroup reaperExcutorGroup = new DefaultEventExecutorGroup(1);
        try {
            final ServerBootstrap sb = new ServerBootstrap();
            sb.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new SockJSChannelInitializer(simplePushConfig, datastore, sockJSConfig, reaperExcutorGroup));
            final Channel ch = sb.bind(simplePushConfig.host(), simplePushConfig.port()).sync().channel();
            System.out.println("SockJS server with options " + options);
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * Starts the server using the passed in configuration options.
     * </p>
     * Options:
     * <pre>
     * -host=localhost -port=8888 -tls=false -ack_interval=10000 -useragent_reaper_timeout=60000 -token_key=testing
     * </pre>
     *
     * @param args the command line arguments passed.
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception {
        final Map<Args, Option<?>> options = Options.options(args);

        final String host = value(Args.HOST, options, "localhost");
        final int port = value(Args.PORT, options, 7777);
        final SimplePushServerConfig simplePushConfig = DefaultSimplePushConfig.create(host, port)
                .userAgentReaperTimeout(Options.<Long> value(Args.USERAGENT_REAPER_TIMEOUT, options))
                .ackInterval(Options.<Long> value(Args.ACK_INTERVAL, options))
                .tokenKey(Options.<String> value(Args.TOKEN_KEY, options))
                .build();

        final Config sockJSConfig = Config.prefix("/simplepush")
                .tls(Options.value(Args.TLS, options, false))
                .websocketProtocols("push-notification")
                .cookiesNeeded()
                .sessionTimeout(60000)
                .build();
        new NettySockJSServer(options, simplePushConfig, sockJSConfig).run();
    }

    public static class Options {

        private Options() {
        }

        public enum Args {
            HOST(String.class),
            PORT(Integer.class),
            USERAGENT_REAPER_TIMEOUT(Long.class),
            ACK_INTERVAL(Long.class),
            TLS(Boolean.class),
            TOKEN_KEY(String.class);

            private Class<?> type;

            private Args(final Class<?> type) {
                this.type = type;
            }

            public Class<?> type() {
                return type;
            }
        };

        public static <T> T value(final Options.Args name, final Map<Options.Args, Option<?>> options, final T defaultValue) {
            final T t = value(name, options);
            return options == null ? defaultValue : t;
        }

        @SuppressWarnings("unchecked")
        public static <T> T value(final Options.Args name, final Map<Options.Args, Option<?>> options) {
            final Option<?> option = options.get(name);
            if (option == null) {
                return null;
            }
            if (option.name().type() == String.class) {
                return (T) option.value();
            }
            if (option.name().type() == Integer.class) {
                return (T) Integer.valueOf((String) option.value());
            }
            if (option.name().type() == Long.class) {
                return (T) Long.valueOf((String) option.value());
            }
            if (option.name().type() == Boolean.class) {
                return (T) Boolean.valueOf((String) option.value());
            }
            throw new IllegalArgumentException("Type is not supported: " + name);
        }

        public static Map<Options.Args, Option<?>> options(final String[] args) {
            final Map<Options.Args, Option<?>> options = new HashMap<Options.Args, Option<?>>();
            for (String arg : args) {
                final Option<String> option = parseOptionName(arg);
                switch (option.name()) {
                case HOST:
                    options.put(option.name(), new Option<String>(option.name(), (String) option.value()));
                    break;
                case PORT:
                    options.put(option.name(), new Option<Integer>(option.name(), Integer.parseInt(option.value())));
                    break;
                case USERAGENT_REAPER_TIMEOUT:
                    options.put(option.name(), new Option<Long>(option.name(), Long.parseLong(option.value())));
                    break;
                case ACK_INTERVAL:
                    options.put(option.name(), new Option<Long>(option.name(), Long.parseLong(option.value())));
                    break;
                case TLS:
                    options.put(option.name(), new Option<Boolean>(option.name(), Boolean.parseBoolean(option.value())));
                    break;
                case TOKEN_KEY:
                    options.put(option.name(), new Option<String>(option.name(), (String) option.value()));
                    break;
                }
                options.put(option.name(), option);
            }
            return options;
        }

        private static final Pattern OPTION_PATTERN = Pattern.compile("^-(\\w+)=([\\d\\w]+)");

        private static Option<String> parseOptionName(final String cmdArg) {
            final Matcher matcher = OPTION_PATTERN.matcher(cmdArg);
            if (matcher.find()) {
                return new Option<String>(Options.Args.valueOf(matcher.group(1).toUpperCase()), matcher.group(2));
            }
            throw new IllegalStateException("Invalid command line argument : " + cmdArg);
        }
    }

    private static class Option<T> {
        private Options.Args name;
        private T value;

        public Option(final Options.Args name, final T value) {
            this.name = name;
            this.value = value;
        }

        public Options.Args name() {
            return name;
        }

        public T value() {
            return value;
        }

        public String toString() {
            return value.toString();
        }

    }

}
