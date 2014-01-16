package org.chp;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ResultSetHelper {

	private static JSONParser jsonParser = new JSONParser();

	/**
	 * Transforms the rows, received through the given ResultSet, into
	 * JSONObjects and returns them as a JSONArray
	 * 
	 * @param resultSet
	 *            ResultSet to be transformed
	 * @return JSONArray containing JSONObjects
	 * @throws SQLException
	 */
	@SuppressWarnings("unchecked")
	static JSONArray resultSetToJSONArray(ResultSet resultSet)
			throws SQLException {

		ResultSetMetaData resultMeta = resultSet.getMetaData();

		int columnNumber = resultMeta.getColumnCount();
		String[] columnNames = new String[columnNumber];
		Integer[] columnTypes = new Integer[columnNumber];
		for (int columnIndex = 1; columnIndex <= columnNumber; columnIndex++) {
			columnNames[columnIndex - 1] = resultMeta
					.getColumnLabel(columnIndex);
			columnTypes[columnIndex - 1] = resultMeta
					.getColumnType(columnIndex);

		}

		JSONArray resultArray = new JSONArray();
		while (resultSet.next()) {
			JSONObject jsonRow = resultSetRowToJSONObject(resultSet);
			resultArray.add(jsonRow);
		}
		return resultArray;

	}

	private static JSONObject resultSetRowToJSONObject(ResultSet resultSet)
			throws SQLException {
		ResultSetMetaData resultMeta;
		int columnNumber;
		try {
			resultMeta = resultSet.getMetaData();
			columnNumber = resultMeta.getColumnCount();
		} catch (SQLException e) {
			throw new SQLException(String.format(
					"Meta Data of ResultSet could not be retreived.\n"
							+ "Function: resultSetRowToJSONObject()\n"
							+ "Statement: %s\n", resultSet.getStatement()
							.toString()));
		}

		String[] columnNames = new String[columnNumber];
		Integer[] columnTypes = new Integer[columnNumber];
		for (int columnIndex = 1; columnIndex <= columnNumber; columnIndex++) {
			columnNames[columnIndex - 1] = resultMeta
					.getColumnLabel(columnIndex);
			columnTypes[columnIndex - 1] = resultMeta
					.getColumnType(columnIndex);

		}
		// for (String name : columnNames)
		// System.out.println(name);

		JSONObject jsonRow = new JSONObject();
		for (int columnIndex = 1; columnIndex <= columnNumber; columnIndex++) {
			String columnName = columnNames[columnIndex - 1];

			columnIntoJSONObject(columnName, resultSet,
					columnTypes[columnIndex - 1], jsonRow);

		}
		return jsonRow;
	}

	@SuppressWarnings("unchecked")
	private static void columnIntoJSONObject(String columnName,
			ResultSet resultSet, int columnType, JSONObject jsonObject)
			throws SQLException {
		try {
			switch (columnType) {
			case Types.INTEGER:
				jsonObject.put(columnName, resultSet.getInt(columnName));
				break;
			case Types.TIMESTAMP:
				jsonObject.put(columnName, resultSet.getTimestamp(columnName)
						.toString());
				break;
			case Types.VARCHAR:
			case Types.CHAR:
				String a = resultSet.getString(columnName);
				try {
					Object jsonO = a == null ? null : jsonParser.parse(a);
					if (jsonO == null)
						jsonObject.put(columnName, null);
					else if (jsonO instanceof JSONObject)
						jsonObject.put(columnName, (JSONObject) jsonO);
					else if (jsonO instanceof JSONArray)
						jsonObject.put(columnName, (JSONArray) jsonO);
					else
						jsonObject.put(columnName, a);
				} catch (ParseException e) {
					jsonObject.put(columnName, a);
				}
				break;
			case Types.NUMERIC:
			case Types.DOUBLE:
				jsonObject.put(columnName, resultSet.getDouble(columnName));
				break;
			case Types.BIT:
			case Types.BOOLEAN:
				jsonObject.put(columnName, resultSet.getBoolean(columnName));
				break;
			default:
				System.out.println("Type: " + columnType);
				break;
			}
		} catch (SQLException e1) {
			throw new SQLException(String.format(
					"Column could not be transformed to a json Value.\n"
							+ "Function: columnIntoJSONObject()\n"
							+ "Statement: %s\n" + "Column Name: %s", resultSet
							.getStatement().toString(), columnName));
		}
	}

}
