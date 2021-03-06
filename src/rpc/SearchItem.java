package rpc;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;
import external.TicketMasterAPI;

/**
 * Servlet implementation class SearchItem
 */
@WebServlet("/search")
public class SearchItem extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SearchItem() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		/*
		 * 1 print username + "hello" response.addHeader("Access-Control-Allow-Origin",
		 * "*"); // Create a PrintWriter from response such that we can add data to
		 * response. PrintWriter out = response.getWriter();
		 * 
		 * if (request.getParameter("username") != null) { // Get the username sent from
		 * the client String username = request.getParameter("username"); // In the
		 * output stream, add something to response body. out.print("Hello " +
		 * username); }
		 * 
		 * // Send response back to client out.close();
		 */

		/*
		 * 2 HTML demo response.setContentType("text/html");
		 * response.addHeader("Access-Control-Allow-Origin", "*"); PrintWriter out =
		 * response.getWriter(); out.println("<html><body>");
		 * out.println("<h1>This is a HTML page</h1>"); out.println("</body></html>");
		 * out.close();
		 */

		/*
		 * 3 json response.setContentType("application/json");
		 * response.addHeader("Access-Control-Allow-Origin", "*");
		 * 
		 * String username = ""; if (request.getParameter("username") != null) {
		 * username = request.getParameter("username"); } JSONObject obj = new
		 * JSONObject(); try { obj.put("username", username); } catch (JSONException e)
		 * { e.printStackTrace(); } PrintWriter out = response.getWriter();
		 * out.print(obj); out.close();
		 */

		/*
		 * 4 a list of fake user names response.setContentType("application/json");
		 * response.addHeader("Access-Control-Allow-Origin", "*"); PrintWriter out =
		 * response.getWriter();
		 * 
		 * JSONArray array = new JSONArray(); try { array.put(new
		 * JSONObject().put("username", "abcd")); array.put(new
		 * JSONObject().put("username", "1234")); } catch (JSONException e) {
		 * e.printStackTrace(); } out.print(array); out.close();
		 */

		/*
		 * 5 use RpcHelper to write JSONArray JSONArray array = new JSONArray(); try {
		 * array.put(new JSONObject().put("username", "abcd")); array.put(new
		 * JSONObject().put("username", "1234")); } catch (JSONException e) {
		 * e.printStackTrace(); } RpcHelper.writeJsonArray(response, array);
		 */

		/*
		 * 6.0 use the TicketMasterAPI use RpcHelper to write JSONArray
		 * 
		 * 6.1 Make search result aware of favorite history
		 * 
		 * 6.2 Verify Login
		 */
		
		// 6.2 allow access only if session exists
		HttpSession session = request.getSession(false);
		if (session == null) {
			response.setStatus(403);
			return;
		}

		// optional
		String userId = session.getAttribute("user_id").toString(); 
		// 6.1
		//String userId = request.getParameter("user_id");

		double lat = Double.parseDouble(request.getParameter("lat"));
		double lon = Double.parseDouble(request.getParameter("lon"));

		// term can be empty or null.
		String term = request.getParameter("term");

		// get a DataBase Connection
		DBConnection connection = DBConnectionFactory.getDBConnection();
		List<Item> items = connection.searchItems(lat, lon, term);
		// 6.1
		Set<String> favoriteItemIds = connection.getFavoriteItemIds(userId);

		JSONArray array = new JSONArray();
		// sort by distance
		// Collections.sort(items, new MyComparator());
		try {
			for (Item item : items) {
				// Add a thin version of item object
				JSONObject obj = item.toJSONObject();
				// Check if this is a favorite one.
				// This field is required by frontend to correctly display favorite items.
				boolean isFavorite = favoriteItemIds == null ? false : favoriteItemIds.contains(item.getItemId());
				obj.put("favorite", isFavorite);
				array.put(obj);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		RpcHelper.writeJsonArray(response, array);

		connection.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// allow access only if session exists
		HttpSession session = request.getSession(false);
		if (session == null) {
			response.setStatus(403);
			return;
		}
		doGet(request, response);
	}

	/*
	 * static class MyComparator implements Comparator<Item>{
	 * 
	 * @Override public int compare(Item i1, Item i2) { if (i1.getDistance() <
	 * i2.getDistance()) { return -1; } else if (i1.getDistance() >
	 * i2.getDistance()) { return 1; } return 0; } }
	 */
}
