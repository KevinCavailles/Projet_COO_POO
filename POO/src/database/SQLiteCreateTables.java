package database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

class SQLiteCreateTables {

	/**
	 * Create the user table if it does not exist in the database.
	 * An user is characterized by :
	 * - an unique username, 
	 * - a password salt,
	 * - a database_key salt,
	 * - a password hash encrypted by the database_key,
	 * - the encrypted database_key,
	 * - the initialization vector used to encrypt the database key.
	 * 
	 * @param connec 	The opened connection to the database.
	 * @throws SQLException
	 */
	protected static void createTableUser(Connection connec) throws SQLException {
		String createTableUser = "CREATE TABLE IF NOT EXISTS user (\r\n"
				+ "    id                     INTEGER       PRIMARY KEY AUTOINCREMENT,\r\n"
				+ "    username               VARCHAR (50) NOT NULL\r\n"
				+ "                                      UNIQUE ON CONFLICT ROLLBACK,\r\n"
				+ "    pwd_salt               BLOB,\r\n" 
				+ "    db_datakey_salt        BLOB,\r\n"
				+ "    encrypted_pwd_hashsalt BLOB,\r\n" 
				+ "    encrypted_db_datakey   BLOB,\r\n"
				+ "    iv_datakey             BLOB\r\n" + ");";

		Statement stmt = connec.createStatement();
		stmt.execute(createTableUser);

	}

	/**
	 * Create the conversation table if it does not exist in the database.
	 * A conversation is characterized by :
	 * - the id of the user who sends the messages,
	 * - the id of the user who receives the messages,
	 * - an initialization vector used to encrypt the conversation's messages.
	 * 
	 * During an session between two users, two conversations are created.
	 * 
	 * @param connec 	The opened connection to the database.
	 * @throws SQLException
	 */
	protected static void createTableConversation(Connection connec) throws SQLException {
		String createTableConversation = "CREATE TABLE IF NOT EXISTS conversation (\r\n"
				+ "    id_conversation INTEGER PRIMARY KEY AUTOINCREMENT,\r\n"
				+ "    id_sender     INTEGER REFERENCES user (id) \r\n" + "                            NOT NULL,\r\n"
				+ "    id_receiver    INTEGER REFERENCES user (id) \r\n" + "                            NOT NULL,\r\n"
				+ "    iv_conversation BLOB NOT NULL" + ");";

		Statement stmt = connec.createStatement();
		stmt.execute(createTableConversation);

	}

	/**
	 * Create the message table if it does not exist in the database.
	 * A message is characterized by :
	 * - the id of the conversation it belongs,
	 * - the id of its type,
	 * - its content,
	 * - the date when it was emitted,
	 * - its extension if it is a file (text or image).
	 * 
	 * @param connec 	The opened connection to the database.
	 * @throws SQLException
	 */
	protected static void createTableMessage(Connection connec) throws SQLException {
		String createTableMessage = "CREATE TABLE IF NOT EXISTS message (\r\n"
				+ "    id_message      INTEGER      PRIMARY KEY AUTOINCREMENT,\r\n"
				+ "    id_conversation INTEGER      REFERENCES conversation (id_conversation) \r\n"
				+ "                                 NOT NULL,\r\n"
				+ "    id_type         INTEGER      REFERENCES type (id_type) \r\n"
				+ "                                 NOT NULL,\r\n" 
				+ "    content         BLOB,\r\n"
				+ "    date            INTEGER      NOT NULL,\r\n" 
				+ "    extension       VARCHAR (20) \r\n" + ");\r\n";

		Statement stmt = connec.createStatement();
		stmt.execute(createTableMessage);
	}

	/**
	 * Create the (message) type table if it does not exist and insert the different
	 * types of message in the database.
	 * A type is characterized by :
	 * - a label.
	 * 
	 * This table only exists because the type "enumeration" does not exist in SQLite.
	 * It is a static table that contains the different types of message stored in the database.
	 * 
	 * @param connec 	The opened connection to the database.
	 * @throws SQLException
	 */
	protected static void createTableType(Connection connec) throws SQLException {
		String createTableType = "CREATE TABLE IF NOT EXISTS type (\r\n" 
				+ "    id_type INTEGER      PRIMARY KEY,\r\n"
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
