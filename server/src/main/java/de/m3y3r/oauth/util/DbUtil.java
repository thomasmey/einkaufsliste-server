package de.m3y3r.oauth.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.sql.DataSource;

@ApplicationScoped
public class DbUtil {

	@Resource(lookup="jdbc/eklDS")
	private DataSource ds;

	public static String toJsonName(String columnName) {
		StringBuilder sb = new StringBuilder(columnName);
		for(int i = 0; i < sb.length(); i++) {
			if(sb.charAt(i) == '_') {
				sb.deleteCharAt(i);
				sb.setCharAt(i, Character.toUpperCase(sb.charAt(i)));
			}
		}
		return sb.toString();
	}

	private static List<JsonObject> toJson(ResultSet rs, int maxResults) throws SQLException {
		int cc = rs.getMetaData().getColumnCount();
		String[] cn = new String[cc];

		for(int i = 0, n = cc; i < n; i++) {
			cn[i] = toJsonName(rs.getMetaData().getColumnName(i + 1));
		}

		List<JsonObject> objs = new ArrayList<>(maxResults);
		while(rs.next() && objs.size() < maxResults) {
			JsonObjectBuilder b = Json.createObjectBuilder();
			for(int i = 0, n = cc; i < n; i++) {
				int idb = i+1;
				int ct = rs.getMetaData().getColumnType(idb);
				switch(ct) {
					case Types.VARCHAR:
					case Types.CHAR:
						b.add(cn[i], rs.getString(idb)); break;
					case Types.INTEGER:
						b.add(cn[i], rs.getInt(idb)); break;
					case Types.BINARY:
						b.add(cn[i], Base64.getEncoder().encodeToString(rs.getBytes(idb))); break;
					case Types.BOOLEAN:
					case Types.BIT:
						b.add(cn[i], rs.getBoolean(idb)); break;
					default:
						throw new IllegalArgumentException("Unsupported SQL Type:"+ ct);
				}
			}
			objs.add(b.build());
		}
		return objs;
	}

	public List<JsonObject> getAsJsons(String sql, int maxResults, Object... parameterMarkers) {
		try {
			Connection c = ds.getConnection();
			PreparedStatement ps = c.prepareStatement(sql);
			ps.setMaxRows(maxResults);
			for(int i = 0, n = parameterMarkers.length; i < n; i++) {
				ps.setObject(i+1, parameterMarkers[i]);
			}
			ResultSet rs = ps.executeQuery();
			List<JsonObject> objs = DbUtil.toJson(rs, maxResults);
			rs.close();
			ps.close();
			c.close();

			return objs;
		} catch (SQLException e) {
			Logger.getLogger(DbUtil.class.getName()).log(Level.SEVERE, "DB failed!", e);
		}
		return Collections.emptyList();
	}

	public JsonObject getAsJson(String sql, Object... parameterMarkers) {
		try {
			Connection c = ds.getConnection();
			PreparedStatement ps = c.prepareStatement(sql);
			for(int i = 0, n = parameterMarkers.length; i < n; i++) {
				ps.setObject(i+1, parameterMarkers[i]);
			}
			ResultSet rs = ps.executeQuery();
			List<JsonObject> obj = DbUtil.toJson(rs, 1);
			rs.close();
			ps.close();
			c.close();

			return obj.get(0);
		} catch (SQLException e) {
			Logger.getLogger(DbUtil.class.getName()).log(Level.SEVERE, "DB failed!", e);
		}
		return null;
	}

}
