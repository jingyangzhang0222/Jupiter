package db.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.List;
import java.util.Set;

import db.DBConnection;
import entity.Item;
import external.TicketMasterAPI;

public class MySQLConnection implements DBConnection {
	private Connection conn;

	public MySQLConnection() {
		try {
			// reflection: register for this driver
			// Driver d = new Driver();
			Class.forName("com.mysql.jdbc.Driver").newInstance();

			/*
			 * * Attempts to establish a connection to the given database URL. The
			 * <code>DriverManager</code> attempts to select an appropriate driver from the
			 * set of registered JDBC drivers.
			 */
			conn = DriverManager.getConnection(MySQLDBUtil.URL);

			if (conn == null)
				return;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		if (conn != null) {
			try {
				conn.close();
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}

	@Override
	public void setFavoriteItems(String userId, List<String> itemIds) {
		if (conn == null)
			return;
		try {
			// sql has not finished yet! use "?" instead.
			String sql = "INSERT IGNORE INTO history (user_id, item_id) VALUES (?,?)";
			// "IGNORE": avoid repeat

			PreparedStatement statement = conn.prepareStatement(sql);

			for (String itemId : itemIds) {
				statement.setString(1, userId);
				statement.setString(2, itemId);

				statement.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void unsetFavoriteItems(String userId, List<String> itemIds) {
		if (conn == null) {
			return;
		}

		try {
			String sql = "DELETE FROM history WHERE user_id = ? AND item_id = ?";
			PreparedStatement statement = conn.prepareStatement(sql);

			for (String itemId : itemIds) {
				statement.setString(1, userId);
				statement.setString(2, itemId);

				statement.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Set<String> getFavoriteItemIds(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Item> getFavoriteItems(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getCategories(String itemId) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * Previously we call TicketMasterAPI.search from our SearchItem servlet
	 * directly. But actually our recommendation code also needs to call the same
	 * search function, so we make a designated function here to do the search call.
	 */
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
		/*
		 * Syntax for INSERT. INSERT INTO table_name (column1, column2, column3, ...)
		 * VALUES (value1, value2, value3, ...);
		 * 
		 */
		if (conn == null)
			return;
		try {
			// sql has not finished yet! use "?" instead.
			String sql = "INSERT IGNORE INTO items VALUES (?,?,?,?,?,?,?)";
			// "IGNORE": avoid repeat
			// SQL injection:
			// DELETE FROM users WHERE user_id = "1234; DELETE FROM users WHERE user_id =
			// abcd"

			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, item.getItemId());
			statement.setString(2, item.getName());
			statement.setDouble(3, item.getRating());
			statement.setString(4, item.getAddress());
			statement.setString(5, item.getImageUrl());
			statement.setString(6, item.getUrl());
			statement.setDouble(7, item.getDistance());
			statement.execute();

			sql = "INSERT IGNORE INTO categories VALUES (?,?)";
			for (String category : item.getCategories()) {
				statement = conn.prepareStatement(sql);
				statement.setString(1, item.getItemId());
				statement.setString(2, category);

				statement.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getFullname(String userId) {
		if (conn == null) {
			return null;
		}
		return null;
	}

	@Override
	public boolean verifyLogin(String userId, String password) {
		// TODO Auto-generated method stub
		return false;
	}
}