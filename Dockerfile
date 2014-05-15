FROM dbevenius/sps-integration
MAINTAINER Daniel Bevenius <daniel.bevenius@gmail.com>

WORKDIR /home/aerogear-simplepush-server

RUN git clone https://github.com/aerogear/aerogear-simplepush-server  /home/aerogear-simplepush-server

# Run a Maven install when installing the image to avoid this cost 
# of downloading the internet (all deps).
RUN mvn install -DskipTests=true

EXPOSE 7777

# startup.sh will start CouchDB and Redis 
# and the run the integration/functional tests.
CMD ["/bin/bash", "/startup.sh"]

