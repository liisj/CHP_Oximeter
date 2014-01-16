package org.chp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hsqldb.types.Types;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.postgresql.ds.PGSimpleDataSource;

public class DataBaseFunctions {

	static String URL = "localhost";
	static String PORT = "5433";
	static String DATABASE = "ht13";
	static String USER = "postgres";
	static String PASSWORD = "postgres";
	static Connection connection = null;
	
	static JSONParser jsonParser = new JSONParser();

	static PreparedStatement lastReadingsStatement = null;

	private static PGSimpleDataSource pgSimpleDataSourceWeb = null;

	/**
	 * 
	 * @return A connection to the database, currently having all rights.
	 * @throws SQLException
	 */
	public static Connection getWebConnection() throws SQLException {
		if (pgSimpleDataSourceWeb == null) {
			pgSimpleDataSourceWeb = new PGSimpleDataSource();
			pgSimpleDataSourceWeb.setServerName(URL);
			pgSimpleDataSourceWeb.setPortNumber(Integer.valueOf(PORT));
			pgSimpleDataSourceWeb.setDatabaseName(DATABASE);
			pgSimpleDataSourceWeb.setUser(USER);
			pgSimpleDataSourceWeb.setPassword(PASSWORD);

		}
		if (connection == null || connection.isClosed()) {
			try {
				connection = pgSimpleDataSourceWeb.getConnection();
				connection.setAutoCommit(true);
			} catch (SQLException e) {
				throw new SQLException(String.format(
						"Could not properly build a connection to Database.\n"
								+ "Function: getWebConnection()\n"
								+ "Details: %s\n"
								+ "pgSimpleDataSourceWeb == null: %B\n"
								+ "con == null: %B", e.getMessage(),
						pgSimpleDataSourceWeb == null, connection == null));
			}
			try {
				lastReadingsStatement = connection
						.prepareStatement("SELECT * FROM reading " +
								"WHERE patid = COALESCE(?,patid) " +
								"AND COALESCE((extract('epoch' from (NOW()-\"time\")))::integer < ?,true)");
			} catch (SQLException e) {
				throw new SQLException(String.format(
						"Could not prepare the statements.\n"
								+ "Function: getWebConnection()\n"
								+ "Details: %s", e.getMessage()));
			}
		}
		return connection;
	}

	public static JSONArray getLastReadings(Connection con,
			JSONObject parameters) {
		Object patientIDO = parameters.get("patient_id");
		Object interval = parameters.get("interval_sec");

		int p = 1;
		try {
			if (patientIDO == null) {
				lastReadingsStatement.setNull(p++, Types.INTEGER);
			} else {
				lastReadingsStatement.setInt(p++,
						Integer.valueOf(patientIDO.toString()));
			}
			if (interval == null) {
				lastReadingsStatement.setNull(p++, Types.INTEGER);
			} else {
				lastReadingsStatement.setInt(p++,
						Integer.valueOf(interval.toString()));
			}
			System.out.println(lastReadingsStatement.toString());
		} catch (SQLException e) {

		}
		
		ResultSet rs;
		JSONArray arr = new JSONArray();
		try {
			rs = lastReadingsStatement.executeQuery();
//			while (rs.next()) {
//				String jsonS = rs.getString(1);
//				arr.add((JSONObject)jsonParser.parse(jsonS));
//			}
			arr = ResultSetHelper.resultSetToJSONArray(rs);
		} catch (SQLException e) {
			e.printStackTrace();
//		} catch (ParseException e) {
//			e.printStackTrace();
		}
		
		return arr;
	}
	
	public static void main(String[] args) {
		try {
			Connection con = getWebConnection();
			JSONObject o = new JSONObject();
			o.put("patid", 1);
			o.put("interval", 3600000);
			
			JSONArray arr = getLastReadings(con, o);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	
}
