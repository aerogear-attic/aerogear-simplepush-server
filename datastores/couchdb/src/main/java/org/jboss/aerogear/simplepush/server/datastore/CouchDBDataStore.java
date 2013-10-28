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

import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.ektorp.BulkDeleteDocument;
import org.ektorp.ViewQuery;
import org.ektorp.ViewResult;
import org.ektorp.ViewResult.Row;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;
import org.ektorp.support.DesignDocument;
import org.ektorp.support.DesignDocument.View;
import org.jboss.aerogear.simplepush.protocol.Ack;
import org.jboss.aerogear.simplepush.protocol.impl.AckImpl;
import org.jboss.aerogear.simplepush.server.Channel;
import org.jboss.aerogear.simplepush.server.DefaultChannel;

/**
 * DataStore that uses a CouchDB database for storage.
 */
public class CouchDBDataStore implements DataStore {

    private static final String UAID_FIELD = "uaid";
    private static final String TYPE_FIELD = "type";
    private static final String TOKEN_FIELD = "token";
    private static final String CHID_FIELD = "chid";
    private static final String VERSION_FIELD = "version";
    private static final String DOC_FIELD = "doc";

    private final HttpClient httpClient;
    private final StdCouchDbInstance stdCouchDbInstance;
    private final StdCouchDbConnector db;
    private final DesignDocument designDocument;

    public CouchDBDataStore(final String url, final String dbName) {
        try {
            httpClient = new StdHttpClient.Builder().url(url).build();
        } catch (final MalformedURLException e) {
            throw new IllegalStateException(e);
        }
        stdCouchDbInstance = new StdCouchDbInstance(httpClient);
        db = new StdCouchDbConnector(dbName, stdCouchDbInstance);
        db.createDatabaseIfNotExists();
        designDocument = new DesignDocument("_design/channels");
        addView(designDocument, Views.CHANNEL);
        addView(designDocument, Views.UAID);
        addView(designDocument, Views.TOKEN);
        addView(designDocument, Views.UNACKS);
        if (!db.contains(designDocument.getId())) {
            db.create(designDocument);
        }
    }

    private void addView(final DesignDocument doc, final Views view) {
        if (!doc.containsView(view.viewName())) {
            doc.addView(view.viewName(), new View(view.mapFunction()));
        }
    }

    @Override
    public boolean saveChannel(final Channel channel) {
        db.create(channelAsMap(channel));
        return true;
    }

    private Map<String, String> channelAsMap(final Channel channel) {
        final Map<String, String> map = new HashMap<String, String>(5);
        map.put(UAID_FIELD, channel.getUAID());
        map.put(TYPE_FIELD, Views.CHANNEL.viewName());
        map.put(TOKEN_FIELD, channel.getEndpointToken());
        map.put(CHID_FIELD, channel.getChannelId());
        map.put(VERSION_FIELD, Long.toString(channel.getVersion()));
        return map;
    }

    @Override
    public Channel getChannel(final String channelId) throws ChannelNotFoundException {
        return channelFromJson(getChannelJson(channelId));
    }

    private JsonNode getChannelJson(final String channelId) throws ChannelNotFoundException {
        final ViewResult viewResult = db.queryView(query(Views.CHANNEL.viewName(), channelId));
        final List<Row> rows = viewResult.getRows();
        if (rows.isEmpty()) {
            throw new ChannelNotFoundException("Cound not find channel", channelId);
        }
        if (rows.size() > 1) {
            throw new IllegalStateException("There should not be multiple channelId with the same id");
        }
        return rows.get(0).getValueAsNode();
    }

    private Channel channelFromJson(final JsonNode node) {
        final JsonNode doc = node.get("doc");
        return new DefaultChannel(doc.get(UAID_FIELD).asText(),
                doc.get(CHID_FIELD).asText(),
                doc.get(VERSION_FIELD).asLong(),
                doc.get(TOKEN_FIELD).asText());
    }

    @Override
    public void removeChannels(final String uaid) {
        final ViewResult viewResult = db.queryView(query(Views.UAID.viewName(), uaid));
        final List<Row> rows = viewResult.getRows();
        final Set<String> channelIds = new HashSet<String>(rows.size());
        for (Row row : rows) {
            final JsonNode json = row.getValueAsNode().get(DOC_FIELD);
            channelIds.add(json.get(CHID_FIELD).asText());
        }
        removeChannels(channelIds);
    }

    private ViewQuery query(final String viewName, final String key) {
        return new ViewQuery()
                    .dbPath(db.path())
                    .viewName(viewName)
                    .designDocId(designDocument.getId())
                    .key(key);
    }

    @Override
    public void removeChannels(final Set<String> channelIds) {
        final ViewResult viewResult = db.queryView(channelsQuery(channelIds));
        final List<Row> rows = viewResult.getRows();
        final Collection<BulkDeleteDocument> removals = new LinkedHashSet<BulkDeleteDocument>();
        for (Row row : rows) {
            final JsonNode json = row.getValueAsNode();
            removals.add(BulkDeleteDocument.of(json.get(DOC_FIELD)));
        }
        db.executeBulk(removals);
    }

    private ViewQuery channelsQuery(final Set<String> keys) {
        return new ViewQuery()
                    .dbPath(db.path())
                    .viewName(Views.CHANNEL.viewName())
                    .designDocId(designDocument.getId())
                    .keys(keys);
    }

    @Override
    public Set<String> getChannelIds(final String uaid) {
        final ViewResult viewResult = db.queryView(query(Views.UAID.viewName(), uaid));
        final List<Row> rows = viewResult.getRows();
        if (rows.isEmpty()) {
            return Collections.emptySet();
        }
        final Set<String> channelIds = new HashSet<String> (rows.size());
        for (Row row : rows) {
            channelIds.add(row.getValueAsNode().get(DOC_FIELD).get(CHID_FIELD).asText());
        }
        return channelIds;
    }

    @Override
    public String updateVersion(final String endpointToken, final long version) throws VersionException, ChannelNotFoundException {
        final ViewResult viewResult = db.queryView(query(Views.TOKEN.viewName(), endpointToken));
        final List<Row> rows = viewResult.getRows();
        if (rows.isEmpty()) {
            throw new ChannelNotFoundException("Cound not find channel for endpointToken", endpointToken);
        }
        final ObjectNode node = (ObjectNode) rows.get(0).getValueAsNode().get(DOC_FIELD);
        final long currentVersion = node.get(VERSION_FIELD).asLong();
        if (version <= currentVersion) {
            throw new VersionException("version [" + version + "] must be greater than the current version [" + currentVersion + "]");
        }
        node.put(VERSION_FIELD, String.valueOf(version));
        db.update(node);
        return node.get(CHID_FIELD).asText();
    }

    @Override
    public String saveUnacknowledged(final String channelId, final long version) throws ChannelNotFoundException {
        final JsonNode json = getChannelJson(channelId);
        final Map<String, String> unack = docToAckMap((ObjectNode) json.get(DOC_FIELD), version);
        db.create(unack);
        return unack.get(UAID_FIELD);
    }

    private Map<String, String> docToAckMap(final ObjectNode doc, final long version) {
        final String uaid = doc.get(UAID_FIELD).asText();
        final String chid = doc.get(CHID_FIELD).asText();
        final String token = doc.get(TOKEN_FIELD).asText();
        final Map<String, String> map = new HashMap<String, String>(5);
        map.put(UAID_FIELD, uaid);
        map.put(TYPE_FIELD, "ack");
        map.put(TOKEN_FIELD, token);
        map.put(CHID_FIELD, chid);
        map.put(VERSION_FIELD, Long.toString(version));
        return map;
    }

    @Override
    public Set<Ack> getUnacknowledged(final String uaid) {
        final ViewResult viewResult = db.queryView(query(Views.UNACKS.viewName(), uaid));
        return rowsToAcks(viewResult.getRows());
    }

    @Override
    public Set<Ack> removeAcknowledged(final String uaid, final Set<Ack> acked) {
        final ViewResult viewResult = db.queryView(query(Views.UNACKS.viewName(), uaid));
        final List<Row> rows = viewResult.getRows();
        final Collection<BulkDeleteDocument> removals = new LinkedHashSet<BulkDeleteDocument>();
        for (Iterator<Row> iter = rows.iterator(); iter.hasNext(); ) {
            final Row row = iter.next();
            final JsonNode json = row.getValueAsNode();
            final JsonNode doc = json.get(DOC_FIELD);
            final String channelId = doc.get(CHID_FIELD).asText();
            for (Ack ack : acked) {
                if (ack.getChannelId().equals(channelId)) {
                    removals.add(BulkDeleteDocument.of(doc));
                    iter.remove();
                }
            }
        }
        db.executeBulk(removals);
        return rowsToAcks(rows);
    }

    private Set<Ack> rowsToAcks(final List<Row> rows) {
        if (rows.isEmpty()) {
            return Collections.emptySet();
        }
        final Set<Ack> unacks = new HashSet<Ack>(rows.size());
        for (Row row : rows) {
            final JsonNode json = row.getValueAsNode().get(DOC_FIELD);
            unacks.add(new AckImpl(json.get(CHID_FIELD).asText(), json.get(VERSION_FIELD).asLong()));
        }
        return unacks;
    }

}
