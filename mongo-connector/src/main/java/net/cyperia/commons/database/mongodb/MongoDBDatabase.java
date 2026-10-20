package net.cyperia.commons.database.mongodb;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.cyperia.commons.database.DatabaseConfig;
import net.cyperia.commons.database.SQLColumn;
import net.cyperia.commons.database.abstraction.AbstractDatabase;
import net.cyperia.commons.database.action.*;
import net.cyperia.commons.database.exceptions.DatabaseException;
import net.cyperia.commons.database.mongodb.count.MongoDBCountEntries;
import net.cyperia.commons.database.mongodb.deletion.MongoDBDeletion;
import net.cyperia.commons.database.mongodb.insertion.MongoDBInsertion;
import net.cyperia.commons.database.mongodb.insertorupdate.MongoDBInsertionOrUpdate;
import net.cyperia.commons.database.mongodb.list.MongoDBListTables;
import net.cyperia.commons.database.mongodb.query.MongoDBQuery;
import net.cyperia.commons.database.mongodb.update.MongoDBUpdate;
import net.cyperia.commons.database.mongodb.where.MongoDBWhere;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MongoDBDatabase extends AbstractDatabase {

  static {
    Logger.getLogger("org.mongodb").setLevel(Level.SEVERE);
  }

  protected MongoClient client;
  protected MongoDatabase database;

  public MongoDBDatabase(@NotNull DatabaseConfig config) {
    super(config);
  }

  @Override
  public void connect0() throws Exception {
    client = MongoClients.create(MongoClientSettingsFactory.create(config));
    database = client.getDatabase(config.getDatabase());
  }

  @Override
  public void disconnect0() throws Exception {
    client.close();
    client = null;
  }

  @Override
  public void createTable(@NotNull String name, @NotNull SQLColumn... columns) throws DatabaseException {
    checkConnection();

    boolean collectionExists = listTables().execute().contains(name);
    if (collectionExists) return;

    try {
      database.createCollection(name);
    } catch (Exception ex) {
      throw new DatabaseException(ex);
    }
  }

  @NotNull
  @Override
  public DatabaseListTables listTables() {
    return new MongoDBListTables(this);
  }

  @NotNull
  @Override
  public DatabaseCountEntries countEntries(@NotNull String table) {
    return new MongoDBCountEntries(this, table);
  }

  @NotNull
  @Override
  public DatabaseQuery query(@NotNull String table) {
    return new MongoDBQuery(this, table);
  }

  @NotNull
  public DatabaseQuery query(@NotNull String table, @NotNull Map<String, MongoDBWhere> where) {
    return new MongoDBQuery(this, table, where);
  }

  @NotNull
  @Override
  public DatabaseUpdate update(@NotNull String table) {
    return new MongoDBUpdate(this, table);
  }

  @NotNull
  @Override
  public DatabaseInsertion insert(@NotNull String table) {
    return new MongoDBInsertion(this, table);
  }

  @NotNull
  public DatabaseInsertion insert(@NotNull String table, @NotNull Map<String, Object> values) {
    return new MongoDBInsertion(this, table, new Document(values));
  }

  @NotNull
  public DatabaseInsertion insert(@NotNull String table, @NotNull Document document) {
    return new MongoDBInsertion(this, table, document);
  }

  @NotNull
  @Override
  public DatabaseInsertionOrUpdate insertOrUpdate(@NotNull String table) {
    return new MongoDBInsertionOrUpdate(this, table);
  }

  @NotNull
  @Override
  public DatabaseDeletion delete(@NotNull String table) {
    return new MongoDBDeletion(this, table);
  }

  @NotNull
  public MongoCollection<Document> getCollection(@NotNull String collection) {
    return database.getCollection(collection);
  }

  @NotNull
  public MongoDatabase getDatabase() {
    return database;
  }

  @Override
  public boolean isConnected() {
    return client != null && database != null;
  }

}
