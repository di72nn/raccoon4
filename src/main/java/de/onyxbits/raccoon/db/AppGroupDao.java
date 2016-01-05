/*
 * Copyright 2015 Patrick Ahlbrecht
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.onyxbits.raccoon.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

public class AppGroupDao extends DataAccessObject {

	/**
	 * Table version
	 */
	protected static final int VERSION = 1;

	@Override
	protected void upgradeFrom(int oldVersion, Connection c) throws SQLException {
		switch (oldVersion + 1) {
			case 1: {
				v1(c);
			}
		}
	}
	
	@Override
	protected int getVersion() {
		return 1;
	}

	private static void v1(Connection c) throws SQLException {
		PreparedStatement st = c
				.prepareStatement("CREATE TABLE appgroups (gid BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY, name VARCHAR(255) NOT NULL UNIQUE)");
		st.execute();
		st.close();

		// NOTE: this is the junction table between apps and groups. The apps table
		// must already exist!
		st = c
				.prepareStatement("CREATE TABLE androidapps_appgroups (aid BIGINT FOREIGN KEY REFERENCES androidapps ON DELETE CASCADE, gid BIGINT FOREIGN KEY REFERENCES appgroups ON DELETE CASCADE )");
		st.execute();
		st.close();
	}

	/**
	 * Insert
	 * 
	 * @param group
	 * @return
	 * @throws SQLException
	 */
	public AppGroup insert(AppGroup group) throws SQLException {
		Connection c = manager.connect();
		PreparedStatement st = null;
		ResultSet res = null;
		try {
			st = c.prepareStatement(
					"INSERT INTO appgroups (gid, name) VALUES (DEFAULT, ?)",
					Statement.RETURN_GENERATED_KEYS);
			st.setString(1, group.getName());
			st.executeUpdate();
			res = st.getGeneratedKeys();
			res.next();
			group.setGroupId(res.getLong(1));
		}
		finally {
			manager.disconnect(c);
			if (st != null) {
				st.close();
			}
			if (res != null) {
				res.close();
			}
		}
		return group;
	}

	public synchronized void update(AppGroup group) throws SQLException {
		Connection c = manager.connect();
		PreparedStatement st = null;
		try {
			st = c.prepareStatement("UPDATE appgroups SET name = ? WHERE gid = ?");
			st.setString(1, group.getName());
			st.setLong(2, group.getGroupId());
			st.executeUpdate();
		}
		finally {
			manager.disconnect(c);
			if (st != null) {
				st.close();
			}
		}
	}

	public void delete(AppGroup group) throws SQLException {
		Connection c = manager.connect();
		PreparedStatement st = null;
		try {
			st = c.prepareStatement("DELETE FROM appgroups WHERE gid=?");
			st.setLong(1, group.getGroupId());
			st.executeUpdate();
		}
		finally {
			manager.disconnect(c);
			if (st != null) {
				st.close();
			}
		}
	}

	public Vector<AppGroup> list() throws SQLException {
		Connection c = manager.connect();
		PreparedStatement st = null;
		Vector<AppGroup> ret = new Vector<AppGroup>();
		ResultSet res = null;

		try {
			st = c
					.prepareStatement("SELECT gid, name FROM appgroups ORDER by name ASC");
			st.execute();
			res = st.getResultSet();
			while (res.next()) {
				AppGroup group = new AppGroup();
				group.setGroupId(res.getLong(1));
				group.setName(res.getString(2));
				ret.add(group);
			}
		}
		finally {
			manager.disconnect(c);
			if (st != null) {
				st.close();
			}
			if (res != null) {
				res.close();
			}
		}

		return ret;
	}
}
