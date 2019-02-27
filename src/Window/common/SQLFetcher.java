package Window.common;


import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import javax.swing.JOptionPane;
import javax.swing.JTextField;



public class SQLFetcher {
	private static String username;
	private static String password;
	private static String _url;

	static {
		_url = "jdbc:mysql://localhost/vude?autoReconnect=true&useSSL=false";
		List<String> values = new ArrayList<>();
		try {
			Scanner scan = new Scanner(new File("login.txt"));
			while(scan.hasNextLine()) {
				values.add(scan.nextLine()); // your file should have two lines. 1st line is username, second line is password. Nothing else in the file.
			}
		}
		catch (Throwable t) {
			t.printStackTrace();
			String message = "The login file couldn't be found. Please create the login file then run the program again.";
			System.err.println(message);
			JOptionPane.showMessageDialog(null, message);
			System.exit(1);
		}
		
		if(values.size() == 2) {
			username = values.get(0);
			password = values.get(1);			
		}
	}
	
	public static StringBuilder query1(JTextField textField) {
		StringBuilder sb2=new StringBuilder();
		ResultSet rs = null;
		Statement stmt = null;
		Connection conn = null;
		String txt = textField.getText();

		try {

			// Step 1: Load the JDBC driver
//			Class.forName("com.mysql.jdbc.Driver");

			// Step 2: make a connection
			conn = DriverManager.getConnection(_url, username, password);

			// Step 3: Create a statement
			stmt = conn.createStatement();

			// Step 4: Make a query
			rs = stmt.executeQuery(txt);

			// Step 5: Use ResultSetMetaData to discover the size of the returned relation
			ResultSetMetaData metaData = rs.getMetaData();

			// Step 5.1: Get the column header info for report writing
			int numColumns = metaData.getColumnCount();
			
			for(int i = 1; i <= numColumns; i++)
			{
			   sb2.append(String.format(" %-10s",metaData.getColumnLabel(i)));
			   System.out.print(String.format(" %-10s",metaData.getColumnLabel(i)));
			}
			System.out.println("");
			sb2.append("\n");
			// Step 6: Print out the results
			 
			while (rs.next()) {
				for (int i = 1; i <= numColumns; i++) {
					Object obj = rs.getObject(i);
					if (obj != null) {
						sb2.append(String.format(" %-10s",rs.getObject(i).toString()));
					 System.out.print(String.format(" %-10s",rs.getObject(i).toString()));
					}else {
						
						sb2.append(String.format(" %-10s","NULL"));
						 System.out.print(String.format(" %-10s","NULL"));
					}
				}
				System.out.println("");
				sb2.append("\n");
			}
     
		} catch (Exception exc) {
			exc.printStackTrace();
		} finally { // ALWAYS clean up your DB resources
			try {
				if (rs != null)
					rs.close();
				if (stmt != null)
					stmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
		return sb2;
		
	}
	
	/**
	 * Gets a list of game names from the database and returns them to the GUI
	 * 
	 * @return A List of game names
	 */
	public static List<String> getAllGames() {
		List<String> gameNames = new ArrayList<>();
		ResultSet rs = null;
		Statement stmt = null;
		Connection conn = null;
		
		try {
			// Step 2: make a connection
			conn = DriverManager.getConnection(_url, username, password);

			// Step 3: Create a statement
			stmt = conn.createStatement();

			// Step 4: Make a query
			String statement = "SELECT * FROM VIDEO_GAMES";
			rs = stmt.executeQuery(statement);

			// Step 5: Use ResultSetMetaData to discover the size of the returned relation
			ResultSetMetaData metaData = rs.getMetaData();

			// Step 5.1: Get the column header info for report writing
//			int numColumns = metaData.getColumnCount();
//			for(int i = 1; i <= numColumns; i++)
//			{
//			   System.out.print(String.format(" %-10s",metaData.getColumnLabel(i)));
//			}
//			System.out.println("");
			 
			// Add the game names to the list
			while (rs.next()) {
				String gameName = rs.getString("Gname");
				System.out.println(gameName);
				gameNames.add(gameName);
			}
			return gameNames;
		}
		catch (Throwable t) {
			t.printStackTrace();
			JOptionPane.showMessageDialog(null, t.getMessage());
		}
		finally { // ALWAYS clean up your DB resources
			try {
				if (rs != null)
					rs.close();
				if (stmt != null)
					stmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
		
		return new ArrayList<>();
	}
	
	public static void addGameToLibrary(String gameName, int libraryID) {
		PreparedStatement preparedStatement = null;
		Connection conn = null;
		
		try {
			// make a connection
			conn = DriverManager.getConnection(_url, username, password);
			conn.setAutoCommit(false);
			preparedStatement = conn.prepareStatement("INSERT INTO USER_LIBRARY VALUES (?,?,?)"); //TODO: this

			// Create the prepared statement
			preparedStatement.setInt(1, libraryID);
			preparedStatement.setString(2, gameName);
			
			// Generate the date the game was purchased
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String date = sdf.format(new Date());
			preparedStatement.setString(3, date);
			
			// Execute the statement
			if (preparedStatement.executeUpdate() > 0) {
				JOptionPane.showMessageDialog(null, "Congrats! You just bought: " + gameName);
			}
			conn.commit();
		}
		catch (Throwable t) {
			JOptionPane.showMessageDialog(null, t.getMessage());
			t.printStackTrace();
		}
		finally { // ALWAYS clean up your DB resources
			try {
				if (preparedStatement != null) preparedStatement.close(); // close the preparedStatement resource
			}
			catch (Throwable t1) {
                System.err.println("An error occurred when closing the database resources.");
                t1.printStackTrace();
            }
			
			try {
				if (conn != null) {
					conn.rollback();
					conn.close();
				}
			}
			catch (Throwable t2) {
                System.err.println("A serious issue has occurred. The connection couldn't be closed to the database so " +
                        "the connection is now leaking.");
                t2.printStackTrace();
            }
		}
	}
	
	public static void addGameToWishlist(String gameName, String gUsername) {
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		Statement stmt = null;
		Connection conn = null;
		
		try {
			// to gather the highest index of Wish_ID
			// Step 2: make a connection
			conn = DriverManager.getConnection(_url, username, password);

			// Step 3: Create a statement
			stmt = conn.createStatement();

			// Step 4: Make a query
			String statement = "SELECT max(Wish_ID) as Wish_ID FROM WISHLIST";
			rs = stmt.executeQuery(statement);
			
			int maxId = 0;
			while (rs.next()) {
				maxId = rs.getInt("Wish_ID");
			}
			
			maxId++;
			rs.close();
			stmt.close();
			
			// To insert the value --------------------------->
			// Step 2: make a connection
//			conn = DriverManager.getConnection(_url, username, password);
			conn.setAutoCommit(false);
			preparedStatement = conn.prepareStatement("INSERT INTO WISHLIST VALUES (?,?,?,?)");

			// Step 3: Create a statement
			preparedStatement.setInt(1, maxId);
			preparedStatement.setString(2, gameName);
			
			// Generate the date the game was added to wishlist
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String date = sdf.format(new Date());
			preparedStatement.setString(3, date);
			
			// Throw error if the username is blank
			if (gUsername == null || gUsername.equalsIgnoreCase("")) {
				throw new Exception("Please enter your Username into the box before adding the game to your Wishlist");
			}
			
			preparedStatement.setString(4, gUsername);
			
			// Execute the statement
			if (preparedStatement.executeUpdate() > 0) {
				JOptionPane.showMessageDialog(null, "You just added \'" + gameName + "\' to your wishlist");
			}
			conn.commit();
		}
		catch (Throwable t) {
			JOptionPane.showMessageDialog(null, t.getMessage());
			t.printStackTrace();
		}
		finally { // ALWAYS clean up your DB resources
			try {
				if (preparedStatement != null) preparedStatement.close(); // close the preparedStatement resource
			}
			catch (Throwable t1) {
                System.err.println("An error occurred when closing the database resources.");
                t1.printStackTrace();
            }
			
			try {
				if (conn != null) {
					conn.rollback();
					conn.close();
				}
			}
			catch (Throwable t2) {
                System.err.println("A serious issue has occurred. The connection couldn't be closed to the database so " +
                        "the connection is now leaking.");
                t2.printStackTrace();
            }
		}
	}
	
	public static List<String> getWishlist(int wishID) {
		List<String> wishedGames = new ArrayList<>();
		ResultSet rs = null;
		Statement stmt = null;
		Connection conn = null;
		
		try {
			// Step 2: make a connection
			conn = DriverManager.getConnection(_url, username, password);

			// Step 3: Create a statement
			stmt = conn.createStatement();

			// Step 4: Make a query
			String statement = "SELECT GName FROM WISHLIST WHERE Wish_ID IS " + Integer.toString(wishID);
			rs = stmt.executeQuery(statement);

			// Step 5: Use ResultSetMetaData to discover the size of the returned relation
			ResultSetMetaData metaData = rs.getMetaData();

			// Step 5.1: Get the column header info for report writing
//			int numColumns = metaData.getColumnCount();
//			for(int i = 1; i <= numColumns; i++)
//			{
//			   System.out.print(String.format(" %-10s",metaData.getColumnLabel(i)));
//			}
//			System.out.println("");
			 
			// Add the game names to the list
			while (rs.next()) {
				String gameName = rs.getString("Gname");
				System.out.println(gameName);
				wishedGames.add(gameName);
			}
			return wishedGames;
		}
		catch (Throwable t) {
			t.printStackTrace();
			JOptionPane.showMessageDialog(null, t.getMessage());
		}
		finally { // ALWAYS clean up your DB resources
			try {
				if (rs != null)
					rs.close();
				if (stmt != null)
					stmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
		
		return new ArrayList<>();
	}
	
	public static List<String> getLibrary(int libID) {
		List<String> libGames = new ArrayList<>();
		ResultSet rs = null;
		Statement stmt = null;
		Connection conn = null;
		
		try {
			// Step 2: make a connection
			conn = DriverManager.getConnection(_url, username, password);

			// Step 3: Create a statement
			stmt = conn.createStatement();

			// Step 4: Make a query
			String statement = "SELECT GName FROM USER_LIBRARY WHERE Lib_ID IS " + Integer.toString(libID);
			rs = stmt.executeQuery(statement);

			// Step 5: Use ResultSetMetaData to discover the size of the returned relation
			ResultSetMetaData metaData = rs.getMetaData();

			// Step 5.1: Get the column header info for report writing
//			int numColumns = metaData.getColumnCount();
//			for(int i = 1; i <= numColumns; i++)
//			{
//			   System.out.print(String.format(" %-10s",metaData.getColumnLabel(i)));
//			}
//			System.out.println("");
			 
			// Add the game names to the list
			while (rs.next()) {
				String gameName = rs.getString("Gname");
				System.out.println(gameName);
				libGames.add(gameName);
			}
			return libGames;
		}
		catch (Throwable t) {
			t.printStackTrace();
			JOptionPane.showMessageDialog(null, t.getMessage());
		}
		finally { // ALWAYS clean up your DB resources
			try {
				if (rs != null)
					rs.close();
				if (stmt != null)
					stmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
		
		return new ArrayList<>();
	}
}
