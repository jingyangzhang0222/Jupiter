package db.mongodb;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;

public class MongoDBTableCreation {
	// Run as Java application to create MongoDB collections with index.
	public static void main(String[] args) {
		// Step 1: Connection to MongoDB
		MongoClient client = new MongoClient();
		MongoDatabase db = client.getDatabase(MongoDBUtil.DB_NAME);

		// Step 2: remove old collections.
		db.getCollection("users").drop();
		db.getCollection("items").drop();

		// Step 3: create new collections.
		IndexOptions op = new IndexOptions().unique(true);
		// bson: format in mongodb
		db.getCollection("users").createIndex(new Document("user_id", 1), op);// 1: ascending order
		db.getCollection("items").createIndex(new Document("item_id", 1), op);

		// Step 4: insert fake user data
		/*
		  MongoDB insert syntax(in shell):
		  db.users.insertOne( 
		  	{ 
		  		“user_id”: “1111”, 
		  		“password”: “3229c1097c00d497a0fd282d586be050”, 
		  		“first_name”: John, 
		  		“last_name”: Smith, 
		    }
		  ) 
		 */
		db.getCollection("users").insertOne(new Document().append("first_name", "John")
														  .append("last_Name", "Smith")
														  .append("user_id", "1111")
														  .append("password", "3229c1097c00d497a0fd282d586be050"));
		client.close();
		System.out.println("Import is done successfully.");
	}
}