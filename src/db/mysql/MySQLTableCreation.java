package db.mysql;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Connection;

public class MySQLTableCreation {
	// Run this as Java application to reset db schema.
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			// Step 1, Create connection
			Connection conn = null;
			
			// reflection: register for this driver
			// Driver d = new Driver();
			Class.forName("com.mysql.jdbc.Driver").newInstance();

			/*
			 * * Attempts to establish a connection to the given database URL. The
			 * <code>DriverManager</code> attempts to select an appropriate driver from the
			 * set of registered JDBC drivers.
			 */
			conn = DriverManager.getConnection(MySQLDBUtil.URL);

			if (conn == null) return;
			
			
			// Step 2, Drop table
			/*
			 * Syntax for DROP:
			 * 
			 * DROP TABLE IF EXISTS table_name;
			 * 
			 */
			Statement stmt = conn.createStatement();
			//String sql = "DROP TABLE IF EXISTS history";
			stmt.executeUpdate("DROP TABLE IF EXISTS history");

			//sql = "DROP TABLE IF EXISTS categories";
			stmt.executeUpdate("DROP TABLE IF EXISTS categories");

			//sql = "DROP TABLE IF EXISTS users";
			stmt.executeUpdate("DROP TABLE IF EXISTS users");

			//sql = "DROP TABLE IF EXISTS items";
			stmt.executeUpdate("DROP TABLE IF EXISTS items");

			
			
			
			// Step 3. Create new tables.
			/*
			 * 
			 * CREATE TABLE table_name ( column1 datatype, column2 datatype, column3
			 * datatype, .... );
			 * 
			 */
			String sql = null;
			sql = "CREATE TABLE items " + "(item_id VARCHAR(255) NOT NULL, " + " name VARCHAR(255), " + "rating FLOAT,"
					+ "address VARCHAR(255), " + "image_url VARCHAR(255), " + "url VARCHAR(255), " + "distance FLOAT, "
					+ " PRIMARY KEY ( item_id ))";
			stmt.executeUpdate(sql);

			sql = "CREATE TABLE categories " + "(item_id VARCHAR(255) NOT NULL, " + " category VARCHAR(255) NOT NULL, "
					+ " PRIMARY KEY ( item_id, category), " + "FOREIGN KEY (item_id) REFERENCES items(item_id))";
			stmt.executeUpdate(sql);

			sql = "CREATE TABLE users " + "(user_id VARCHAR(255) NOT NULL, " + " password VARCHAR(255) NOT NULL, "
					+ " first_name VARCHAR(255), last_name VARCHAR(255), " + " PRIMARY KEY ( user_id ))";
			stmt.executeUpdate(sql);

			sql = "CREATE TABLE history " + "(user_id VARCHAR(255) NOT NULL , " + " item_id VARCHAR(255) NOT NULL, "
					+ "last_favor_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, "
					+ " PRIMARY KEY (user_id, item_id)," + "FOREIGN KEY (item_id) REFERENCES items(item_id),"
					+ "FOREIGN KEY (user_id) REFERENCES users(user_id))";
			stmt.executeUpdate(sql);

			
			
			
			
			// Step 4: insert data
			// Create a fake user
			/*
			 * Syntax for INSERT. INSERT INTO table_name (column1, column2, column3, ...)
			 * VALUES (value1, value2, value3, ...);
			 * 
			 */

			sql = "INSERT INTO users " + "VALUES (\"1111\", \"3229c1097c00d497a0fd282d586be050\", \"John\", \"Smith\")";

			System.out.println("Executing query:\n" + sql);
			stmt.executeUpdate(sql);

			System.out.println("Import is done successfully.");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

}
