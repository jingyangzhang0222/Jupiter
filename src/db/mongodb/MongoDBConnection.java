package db.mongodb;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import db.DBConnection;
import entity.Item;
import entity.Item.ItemBuilder;
import external.TicketMasterAPI;

import static com.mongodb.client.model.Filters.eq;

public class MongoDBConnection implements DBConnection {
	private MongoClient mongoClient;
	private MongoDatabase db;

	public MongoDBConnection() {
		mongoClient = new MongoClient();
		db = mongoClient.getDatabase(MongoDBUtil.DB_NAME);
	}

	@Override
	public void close() {
		if (mongoClient != null) {
			mongoClient.close();
		}
	}

	@Override
	public void setFavoriteItems(String userId, List<String> itemIds) {
		/*
		db.users.updateOne
		(
		   {
		      “user_id”: “1111” 
		   },
		   { 
		     $push: 
		     {
		       “favorite”: 
		       {
		         $each: [“abcd”, “efgh”]
		       }
		     }
		   }
		)
		*/
		/*
		before: {“user_id”:1111, “first_name”:john, “favorite”: [“1234”]}
		After: {“user_id”:1111, “first_name”:john, “favorite”: [“1234”, “abcd”, “efgh”]}
		*/
		db.getCollection("users").updateOne(
			new Document("user_id", userId), 
			new Document(
				"$push",   // not "$pushAll", so it is different from the unset function
				 new Document(
			         "favorite", 
					  new Document(
						  "$each", 
						  itemIds
					  )
			     )
			)
	    );
	}

	@Override
	public void unsetFavoriteItems(String userId, List<String> itemIds) {
		/*
		db.users.updateOne
		(
		   {
		      “user_id”: “1111” 
		   },
		   { 
		     $pullAll: 
		     {
		       “favorite”: [“abcd”, “efgh”]
		     }
		   }
		)
		*/
		/*
		Before: {“user_id”:1111, “first_name”:john, “favorite”: [“1234”, “abcd”, “efgh”]} 
		After: {“user_id”:1111, “first_name”:john, “favorite”: [“1234”]}
		 */
		db.getCollection("users").updateOne(
		    new Document("user_id", userId),
		    new Document(
		    	"$pullAll", 
		    	new Document(
		    		"favorite",
		    		itemIds
		    	)
		    )
	    );
	}

	@Override
	public Set<String> getFavoriteItemIds(String userId) {
		Set<String> favoriteItemIds = new HashSet<String>();
		FindIterable<Document> iterable = db.getCollection("users").find(eq("user_id", userId));
		
		if (iterable.first().containsKey("favorite")) {
			@SuppressWarnings("unchecked") // for IDEs
			List<String> list = (List<String>) iterable.first().get("favorite");
			favoriteItemIds.addAll(list);
		}

		return favoriteItemIds;
	}

	@Override
	public Set<Item> getFavoriteItems(String userId) {
		Set<String> favoriteItemIds = getFavoriteItemIds(userId);
		Set<Item> favoriteItems = new HashSet<>();
		for (String itemId : favoriteItemIds) {
			FindIterable<Document> iterable = db.getCollection("items").find(eq("item_id",itemId));
			
			// why not iteratively find Document? because userId is unique
			if (iterable.first() != null) {
				Document doc = iterable.first();
				
				ItemBuilder builder = new ItemBuilder();

				builder.setItemId(doc.getString("item_id"));
				builder.setName(doc.getString("name"));
				builder.setAddress(doc.getString("address"));
				builder.setImageUrl(doc.getString("image_url"));
				builder.setUrl(doc.getString("url"));
				builder.setCategories(getCategories(itemId));
				builder.setDistance(doc.getDouble("distance"));
				builder.setRating(doc.getDouble("rating"));
				
				favoriteItems.add(builder.build());
			}
		}
		return favoriteItems;
	}

	@Override
	public Set<String> getCategories(String itemId) {
		Set<String> categories = new HashSet<>();
		FindIterable<Document> iterable = db.getCollection("items").find(eq("item_id", itemId));
		
		if (iterable.first().containsKey("categories")) {
			@SuppressWarnings("unchecked") // for IDEs
			List<String> list = (List<String>) iterable.first().get("categories");
			categories.addAll(list);
		}
		
		return categories; 
	}

	@Override
	public List<Item> searchItems(double lat, double lon, String term) {
		// search and save
		TicketMasterAPI tmAPI = new TicketMasterAPI();
		List<Item> items = tmAPI.search(lat, lon, term);
		for (Item item : items) {
			saveItem(item);
		}
		return items;
	}

	@Override
	public void saveItem(Item item) {
		if (db == null) {
			return;
		}
		/*
		  db.items.find( 
		      { 
		          “item_id”: “abcd” 
		      } 
		  ) 
		  
		  db.items.insertOne( 
		      { 
		          “Item_id”: “1234”, 
		          “Name”: “abcd”, 
		          “Rating”: 4,
		          ...
		      } 
		  )
		 */
		FindIterable<Document> iterable = db.getCollection("items").find(eq("item_id", item.getItemId()));
		if (iterable.first() == null) {
			db.getCollection("items").insertOne(new Document().append("item_id", item.getItemId()).append("name", item.getName())
							.append("rating", item.getRating()).append("address", item.getAddress())
							.append("image_url", item.getImageUrl()).append("url", item.getUrl())
							.append("categories", item.getCategories()).append("distance", item.getDistance()));
		}
	}

	@Override
	public String getFullname(String userId) {
		String fullName = "";
		FindIterable<Document> iterable = db.getCollection("users").find(eq("user_id", userId));
		if (iterable.first().containsKey("first_name")) {
			fullName += iterable.first().getString("first_name");
		}
		if (iterable.first().containsKey("last_name")) {
			fullName += " " + iterable.first().getString("last_name");
		}
		return fullName;
	}

	@Override
	public boolean verifyLogin(String userId, String password) {
		FindIterable<Document> iterable = db.getCollection("users").find(eq("user_id", userId));
		return (iterable.first()!= null) && 
			   iterable.first().getString("password").equals(password);
	}

}
