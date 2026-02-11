package com.birdbook.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class ConnectionHandler {
    private static MongoClient client;
    private static MongoDatabase database;

    static {
        if(client == null || database == null) {
            Properties prop = new Properties();
            try(InputStream input = ConnectionHandler.class.getClassLoader().getResourceAsStream("application.properties")) {
                if(input == null) {
                    throw new Exception("Unable to find application.properties");
                }
                prop.load(input);
                client = MongoClients.create(prop.getProperty("db.url"));
                database = client.getDatabase(prop.getProperty("db.database"));
            } catch(IOException | ClassNotFoundException e) {
                throw new RuntimeException("Failed to load config file");
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static MongoClient getClient() {
        return client;
    }

    public static MongoDatabase getDatabase() {
        return database;
    }

    public static void closeClient() {
        client.close();
    }
}
