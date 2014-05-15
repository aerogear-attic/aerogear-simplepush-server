FROM dbevenius/sps-integration
MAINTAINER Daniel Bevenius <daniel.bevenius@gmail.com>

WORKDIR /home/aerogear-simplepush-server

# Change to the correct repository before committing.
RUN git clone https://github.com/danbev/aerogear-simplepush-server  /home/aerogear-simplepush-server

# Remove this once the pull request has been tested.
RUN git checkout -b docker origin/docker

# Run a Maven install when installing the image to avoid this cost 
# of downloading the internet (all deps).
RUN mvn install -DskipTests=true

EXPOSE 7777

# startup.sh will start CouchDB and Redis 
# and the run the integration/functional tests.
CMD ["/bin/bash", "/startup.sh"]

