package database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

class SQLiteCreateTables {

	
	protected static void createTableUser(Connection connec) throws SQLException {
		String createTableUser = "CREATE TABLE IF NOT EXISTS user (\r\n"
				+ "    id                     INTEGER       PRIMARY KEY AUTOINCREMENT,\r\n"
				+ "    username               VARCHAR (50) NOT NULL\r\n"
				+ "                                      UNIQUE ON CONFLICT ROLLBACK,\r\n"
				+ "    pwd_salt               BLOB,\r\n"
				+ "    db_datakey_salt        BLOB,\r\n"
				+ "    encrypted_pwd_hashsalt BLOB,\r\n"
				+ "    encrypted_db_datakey   BLOB,\r\n"
				+ "    iv_datakey             BLOB\r\n"
				+ ");";

		Statement stmt = connec.createStatement();
		stmt.execute(createTableUser);

	}

	protected static void createTableConversation(Connection connec) throws SQLException {
		String createTableConversation = "CREATE TABLE IF NOT EXISTS conversation (\r\n"
				+ "    id_conversation INTEGER PRIMARY KEY AUTOINCREMENT,\r\n"
				+ "    id_emetteur     INTEGER REFERENCES user (id) \r\n" 
				+ "                            NOT NULL,\r\n"
				+ "    id_recepteur    INTEGER REFERENCES user (id) \r\n" 
				+ "                            NOT NULL,\r\n"
				+"     iv_conversation BLOB NOT NULL"
				+ ");";

		Statement stmt = connec.createStatement();
		stmt.execute(createTableConversation);

	}

	protected static void createTableMessage(Connection connec) throws SQLException {
		String createTableMessage = "CREATE TABLE IF NOT EXISTS message (\r\n"
				+ "    id_message      INTEGER      PRIMARY KEY AUTOINCREMENT,\r\n"
				+ "    id_conversation INTEGER      REFERENCES conversation (id_conversation) \r\n"
				+ "                                 NOT NULL,\r\n"
				+ "    id_type         INTEGER      REFERENCES type (id_type) \r\n"
				+ "                                 NOT NULL,\r\n"
				+ "    content         BLOB,\r\n"
				+ "    date            INTEGER      NOT NULL,\r\n"
				+ "    extension       VARCHAR (20) \r\n"
				+ ");\r\n";

		Statement stmt = connec.createStatement();
		stmt.execute(createTableMessage);
	}

	protected static void createTableType(Connection connec) throws SQLException {
		String createTableType = "CREATE TABLE IF NOT EXISTS type (\r\n" + "    id_type INTEGER      PRIMARY KEY,\r\n"
				+ "    label VARCHAR (20) NOT NULL\r\n" + ");";

		Statement stmt = connec.createStatement();
		stmt.execute(createTableType);

		String typeText = "INSERT OR IGNORE INTO type (id_type, label) " + "VALUES (0, 'text');";
		String typeFile = "INSERT OR IGNORE INTO type (id_type, label) " + "VALUES (1, 'file');";
		String typeImage = "INSERT OR IGNORE INTO type (id_type, label) " + "VALUES (2, 'image');";

		stmt.execute(typeText);
		stmt.execute(typeFile);
		stmt.execute(typeImage);
	}
}
