package de.m3y3r.oauth.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.sql.DataSource;

import static de.m3y3r.util.StructUtil.*;

@ApplicationScoped
public class DbUtil {

	@Resource(lookup="jdbc/eklDS")
	private DataSource ds;

	private Map<String, PreparedStatement> sqls;

	@PostConstruct
	private void setup() {
		sqls = m(e("readEkl",   prepareSql("select * from einkaufsliste t where t.id = ?")),
				e("readItem",  prepareSql("select * from item t where t.id = ?")),
				e("readItem",  prepareSql("select * from einkaufsliste t where t.id = ?"))
				);
	}

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
				case Types.OTHER:
					b.add(cn[i], rs.getObject(idb).toString()); break;
				default:
					throw new IllegalArgumentException("Unsupported SQL Type:"+ ct);
				}
			}
			objs.add(b.build());
		}
		return objs;
	}

	public JsonObject getAsJson(String sql, Object... parameterMarkers) {
		List<JsonObject> o = getAsJsons(sql, 1, parameterMarkers);
		if(o.size() > 0) return o.get(0); else return null;
	}
	public JsonObject getAsJson(PreparedStatement ps, Object... parameterMarkers) {
		List<JsonObject> o = getAsJsons(ps, 1, parameterMarkers);
		if(o.size() > 0) return o.get(0); else return null;
	}
	public List<JsonObject> getAsJsons(String sql, int maxResults, Object... parameterMarkers) {
		return getAsJsons(prepareSql(sql), maxResults, parameterMarkers);
	}

	public List<JsonObject> getAsJsons(PreparedStatement ps, int maxResults, Object... parameterMarkers) {
		try {
			ps.setMaxRows(maxResults);
			for(int i = 0, n = parameterMarkers.length; i < n; i++) {
				ps.setObject(i+1, parameterMarkers[i]);
			}
			ResultSet rs = ps.executeQuery();
			List<JsonObject> objs = DbUtil.toJson(rs, maxResults);
			rs.close();
			ps.close();

			return objs;
		} catch (SQLException e) {
			Logger.getLogger(DbUtil.class.getName()).log(Level.SEVERE, "DB failed!", e);
		}
		return Collections.emptyList();
	}

	public PreparedStatement prepareSql(String sql) {
		try {
			Connection c = ds.getConnection();
			PreparedStatement ps = c.prepareStatement(sql);
			c.close();
			return ps;
		} catch (SQLException e) {
			Logger.getLogger(DbUtil.class.getName()).log(Level.SEVERE, "DB failed!", e);
		}
		return null;
	}

	public int insertFromJson(String table, JsonObject obj) {
		StringBuilder sb = new StringBuilder().append("insert into ").append(table).append(" (");
		for(String k: obj.keySet()) {
			sb.append(toSqlName(k)).append(", ");
		}
		sb.setLength(sb.length() - ", ".length());
		sb.append(" ) values ( ");
		for(int i = 0, n = obj.size(); i < n; i++) {
			sb.append("?, ");
		}
		sb.setLength(sb.length() - ", ".length());
		sb.append(')');

		try {
			Connection c = ds.getConnection();
			DatabaseMetaData dmd = c.getMetaData();
			ResultSet columns = dmd.getColumns(null, null, table, "%");
			HashMap<String, Object[]> ct = new HashMap<String, Object[]>();
			while(columns.next()) {
				ct.put(columns.getString("COLUMN_NAME"), new Object[] {columns.getInt("DATA_TYPE"), columns.getString("TYPE_NAME")} );
			}
			PreparedStatement ps = prepareSql(sb.toString());
			int i = 1;
			for(String k: obj.keySet()) {
				JsonValue v = obj.get(k);
				switch((Integer)ct.get(toSqlName(k))[0]) {
				case Types.CHAR:
				case Types.VARCHAR:
					ps.setString(i, v.toString()); break;
				case Types.NUMERIC:
					ps.setBigDecimal(i, ((JsonNumber)v).bigDecimalValue()); break;
				case Types.INTEGER:
					ps.setInt(i, ((JsonNumber)v).intValue()); break;
				case Types.OTHER:
					switch((String) ct.get(toSqlName(k))[1]) {
					case "uuid":
						ps.setObject(i, UUID.fromString(((JsonString)v).getString())); break;
					}
					break;
				default:
					throw new IllegalArgumentException("attr="+k);
				}
				i++;
			}
			int n = ps.executeUpdate();
			ps.close();
			c.close();

			return n;
		} catch (SQLException e) {
			Logger.getLogger(DbUtil.class.getName()).log(Level.SEVERE, "DB failed!", e);
		}
		return 0;
	}

	private static String toSqlName(String k) {
		StringBuilder sb = new StringBuilder(k);
		for(int i = 0; i < sb.length(); i++) {
			if(Character.isUpperCase(sb.charAt(i))) {
				sb.setCharAt(i, Character.toLowerCase(sb.charAt(i)));
				sb.insert(i, '_');
			}
		}
		return sb.toString();
	}

	public JsonObject getAsJsonFrom(String sqlId, Object... parameterMarkers) {
		PreparedStatement ps = sqls.get(sqlId);
		if(ps == null)
			throw new IllegalArgumentException();
		return getAsJson(ps, 1, parameterMarkers);
	}

	public boolean deleteFromJson(String type, JsonObject obj) {
		PreparedStatement ps = prepareSql("delete from " + type + "t where t.id = ? and t.data_version_no = ?");
		try {
			ps.setObject(1, obj.get("id"));
			ps.setObject(2, obj.get("dataVersionNo"));
			int rc = ps.executeUpdate();
			if(rc > 0) return true;
		} catch (SQLException e) {
			Logger.getLogger(DbUtil.class.getName()).log(Level.SEVERE, "DB delete failed!", e);
		}
		return false;
	}

	public void updateFromJson(String sql, JsonObject obj) {
		// TODO Auto-generated method stub
	}
}
