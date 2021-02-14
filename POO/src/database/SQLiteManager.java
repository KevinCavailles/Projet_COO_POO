package database;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import main.Utilisateur;
import messages.MauvaisTypeMessageException;
import messages.Message;
import messages.MessageTexte;
import messages.Message.TypeMessage;
import messages.MessageFichier;

public class SQLiteManager {

	private static final String DATABASE_RELATIVE_PATH = "../database";

	public static String[] hardcodedNames = { "Olivia", "Liam", "Benjamin", "Sophia", "Charlotte", "Noah", "Elijah",
			"Isabella", "Oliver", "Emma", "William", "Amelia", "Evelyn", "James", "Mia", "Ava", "Lucas", "Mason",
			"Ethan", "Harper" };

	private Connection connec;
	private int numDatabase;
	private SecretKey dbDataKey;

	
	/**
	 * Create the object that will interact with the database. Each time a
	 * SQLiteManager is created, it creates the tables for the application to work
	 * if they do not exist already. In the context of a local usage of the
	 * application, this constructor takes a number that is used to manage different
	 * database's files (see openConnection() ).
	 * 
	 * @param numDatabase
	 */
	public SQLiteManager(int numDatabase) {
		this.numDatabase = numDatabase;

		this.openConnection();

		try {

			SQLiteCreateTables.createTableUser(this.connec);
			SQLiteCreateTables.createTableConversation(this.connec);
			SQLiteCreateTables.createTableType(this.connec);
			SQLiteCreateTables.createTableMessage(this.connec);

		} catch (SQLException e) {
			this.closeConnection();
			e.printStackTrace();
		}

		this.closeConnection();
	}

	
	// -------------- CONNECTION METHODS -------------- //
	
	/**
	 * Open a connection to the file DATABASE_RELATIVE_PATH+this.numDatabase+".db"
	 */
	private void openConnection() {
		String url = "jdbc:sqlite:" + DATABASE_RELATIVE_PATH + this.numDatabase + ".db";
		try {
			this.connec = DriverManager.getConnection(url);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	
	/**
	 * Close this object connection, if it exists
	 */
	private void closeConnection() {
		try {
			if (this.connec != null) {
				this.connec.close();
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

	}

	
	// -------------- INSERT METHODS -------------- //

	/**
	 * Insert a collection of message in the database. They all correspond to the
	 * conversation between the sender and the receiver. The users and conversations
	 * are inserted in the database if they do not exist when this method is called.
	 * 
	 * @param messages
	 * @param usernameSender
	 * @param usernameReceiver
	 * @return The number of messages inserted
	 * @throws SQLException
	 */
	public int insertAllMessages(ArrayList<Message> messages, String usernameSender, String usernameReceiver)
			throws SQLException {
		int nbRows = 0;
		this.openConnection();

		int idSender = this.getIDUser(usernameSender);
		int idReceiver = this.getIDUser(usernameReceiver);

		if (idSender == -1) {
			this.insertUser(usernameSender);
			idSender = this.getIDUser(usernameSender);
		}

		if (idReceiver == -1) {
			this.insertUser(usernameReceiver);
			idReceiver = this.getIDUser(usernameReceiver);
		}

		int idConversation = getIDConversation(idSender, idReceiver);

		if (idConversation == -1) {
			this.insertConversation(idSender, idReceiver);
			idConversation = getIDConversation(idSender, idReceiver);
		}

		IvParameterSpec ivConversation = this.getIvConversation(idConversation);

		// Disable autocommit to efficiently insert all the messages.
		this.connec.setAutoCommit(false);

		for (Message m : messages) {
			try {
				nbRows += this.insertMessage(idConversation, m, ivConversation);
			} catch (SQLException e) {
				e.printStackTrace();
				this.connec.rollback();
			}
		}

		// Commit once all the messages are inserted
		this.connec.commit();

		this.closeConnection();

		return nbRows;
	}

	
	/**
	 * Insert a message corresponding to a given conversation (respresented by its
	 * id). The message content is first encrypted with the database key and the
	 * given iv.
	 * 
	 * @param idConversation
	 * @param m
	 * @param iv
	 * @return The number of rows inserted (it should be 1).
	 * @throws SQLException
	 */
	private int insertMessage(int idConversation, Message m, IvParameterSpec iv) throws SQLException {

		String dateMessage = m.getDateMessage();
		String extension = this.processExtension(m);
		String type = this.processMessageType(m);
		int idType = this.getIDType(type);

		byte[] content = null;

		try {
			content = this.stringToBytesContent(m, iv);
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {

			e.printStackTrace();
		}

		String insertMessageRequest = "INSERT INTO message(id_conversation, id_type, content, date, extension) "
				+ "VALUES (?, ?, ?, ?, ?);";

		PreparedStatement prepStmt = this.connec.prepareStatement(insertMessageRequest);
		prepStmt.setInt(1, idConversation);
		prepStmt.setInt(2, idType);
		prepStmt.setBytes(3, content);
		prepStmt.setString(4, dateMessage);
		prepStmt.setString(5, extension);

		int nbRows = prepStmt.executeUpdate();

		return nbRows;
	}

	
	/**
	 * Insert an user in the database with only its username.
	 * 
	 * @param username
	 * @throws SQLException
	 */
	private void insertUser(String username) throws SQLException {
		String insertUserRequest = "INSERT INTO user (username) " + "VALUES (?);";

		PreparedStatement prepStmt = this.connec.prepareStatement(insertUserRequest);
		prepStmt.setString(1, username);
		prepStmt.executeUpdate();
	}

	
	/**
	 * Insert the two conversations corresponding to a session between two users
	 * (they both can be sender and receiver).
	 * 
	 * @param idUser1
	 * @param idUser2
	 * @throws SQLException
	 */
	private void insertConversation(int idUser1, int idUser2) throws SQLException {
		String insertConversationRequest = "INSERT INTO conversation (id_sender, id_receiver, iv_conversation) "
				+ "VALUES " + "(?, ?, ?)," + "(?, ?, ?);";

		byte[] ivConversation = SQLiteEncryption.generateIv().getIV();

		PreparedStatement prepStmt = this.connec.prepareStatement(insertConversationRequest);
		prepStmt.setInt(1, idUser1);
		prepStmt.setInt(2, idUser2);
		prepStmt.setBytes(3, ivConversation);
		prepStmt.setInt(4, idUser2);
		prepStmt.setInt(5, idUser1);
		prepStmt.setBytes(6, ivConversation);

		prepStmt.executeUpdate();
	}

	
	// -------------- GET METHODS -------------- //

	/**
	 * Get the message record between two users. In context, it only needs the
	 * username and the id of the other user since we can get this application's
	 * user's data with Utilisateur.getSelf().
	 * 
	 * @param usernameOther
	 * @param pseudoOther
	 * @return the messages previously exchanged in chronological order or null if
	 *         there is none.
	 * @throws SQLException
	 */
	public ArrayList<Message> getMessageRecord(String usernameOther, String pseudoOther) throws SQLException {

		this.openConnection();

		ArrayList<Message> messages = new ArrayList<Message>();

		String usernameSelf = Utilisateur.getSelf().getId();

		// Get the ids from the usernames
		int idSelf = this.getIDUser(usernameSelf);
		int idOther = this.getIDUser(usernameOther);

		// Get the two conversations corresponding to the exchanges between the two
		// users
		int idConversationSelf = this.getIDConversation(idSelf, idOther);
		int idConversationOther = this.getIDConversation(idOther, idSelf);
		IvParameterSpec ivConversation = this.getIvConversation(idConversationSelf);

		// Get all the messages
		String getHistoriqueRequest = "SELECT id_conversation, id_type, content, date, extension " + "FROM message "
				+ "WHERE id_conversation IN (?,?) " + "ORDER by date";

		PreparedStatement prepStmt = this.connec.prepareStatement(getHistoriqueRequest);
		prepStmt.setInt(1, idConversationSelf);
		prepStmt.setInt(2, idConversationOther);
		ResultSet res = prepStmt.executeQuery();

		// Process the messages one by one
		// Create the appropriate message object depending on the type and
		// sender/receiver
		// and add the message in the list
		while (res.next()) {
			int idType = res.getInt("id_type");
			String type = this.getType(idType);

			String content = null;
			try {
				// Decrypt the message's content with the database key and the conversation's
				// iv.
				content = this.bytesToStringContent(res.getBytes("content"), ivConversation);
			} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
					| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException
					| SQLException e1) {
			}

			Message message = null;
			String extension;

			if (!type.equals("")) {
				try {
					switch (type) {
					case "text":
						message = new MessageTexte(TypeMessage.TEXTE, content);
						break;
					case "file":
						extension = res.getString("extension");
						message = new MessageFichier(TypeMessage.FICHIER, content, extension);
						break;
					default:
						extension = res.getString("extension");
						message = new MessageFichier(TypeMessage.IMAGE, content, extension);
					}

				} catch (MauvaisTypeMessageException e) {
					e.printStackTrace();
				}
			}

			if (res.getInt("id_conversation") == idConversationSelf) {
				message.setSender("Moi");
			} else {
				message.setSender(pseudoOther);
			}
			message.setDateMessage(res.getString("date"));
			if (content != null) {
				messages.add(message);
			}

		}

		this.closeConnection();

		return messages;
	}

	
	/**
	 * Return the id of the user with the given username if it exists in the
	 * database.
	 * 
	 * @param username
	 * @return The id of the user or -1 if he does not exist in the database.
	 * @throws SQLException
	 */
	private int getIDUser(String username) throws SQLException {
		String getIDRequest = "SELECT id " + " FROM user" + " WHERE username = ? ;";

		PreparedStatement prepStmt = this.connec.prepareStatement(getIDRequest);
		prepStmt.setString(1, username);
		ResultSet res = prepStmt.executeQuery();

		if (res.next()) {
			return res.getInt("id");
		}
		return -1;

	}

	
	/**
	 * Get the id of the conversation between two users (represented by their ids).
	 * 
	 * @param idSender
	 * @param idReceiver
	 * @return The id of the conversation or -1 if the conversation does not exists
	 *         in the database.
	 * @throws SQLException
	 */
	private int getIDConversation(int idSender, int idReceiver) throws SQLException {
		String getIDRequest = "SELECT id_conversation " + "FROM conversation " + "WHERE id_sender = ? "
				+ "AND id_receiver = ? ;";

		PreparedStatement prepStmt = this.connec.prepareStatement(getIDRequest);
		prepStmt.setInt(1, idSender);
		prepStmt.setInt(2, idReceiver);
		ResultSet res = prepStmt.executeQuery();

		if (res.next()) {
			return res.getInt("id_conversation");
		}
		return -1;
	}

	
	/**
	 * Get the initialization vector for a given conversation (represented by its
	 * id).
	 * 
	 * @param idConversation
	 * @return The iv corresponding to the conversation or null if the conversation
	 *         does not exist in the database.
	 * @throws SQLException
	 */
	private IvParameterSpec getIvConversation(int idConversation) throws SQLException {
		String getIvRequest = "SELECT iv_conversation " + "FROM conversation " + "WHERE id_conversation = ?;";

		PreparedStatement prepStmt = this.connec.prepareStatement(getIvRequest);
		prepStmt.setInt(1, idConversation);
		ResultSet res = prepStmt.executeQuery();

		if (res.next()) {
			return new IvParameterSpec(res.getBytes("iv_conversation"));
		}

		return null;
	}

	
	/**
	 * Get the id of the message type corresponding to the given label.
	 * 
	 * @param label
	 * @return The id of the message type or -1 if it does not exist in the
	 *         database.
	 * @throws SQLException
	 */
	private int getIDType(String label) throws SQLException {
		String getIDRequest = "SELECT id_type FROM type WHERE label = ?;";

		PreparedStatement prepStmt = this.connec.prepareStatement(getIDRequest);
		prepStmt.setString(1, label);

		ResultSet res = prepStmt.executeQuery();

		if (res.next()) {
			return res.getInt("id_type");
		}
		return -1;
	}

	
	/**
	 * Get the label of the message type corresponding to the given id.
	 * 
	 * @param idType
	 * @return The label of the message type or "" if it does not exist in the
	 *         database.
	 * @throws SQLException
	 */
	private String getType(int idType) throws SQLException {
		String getTypeRequest = "SELECT label FROM type WHERE id_type = ?;";

		PreparedStatement prepStmt = this.connec.prepareStatement(getTypeRequest);
		prepStmt.setInt(1, idType);

		ResultSet res = prepStmt.executeQuery();

		if (res.next()) {
			return res.getString("label");
		}

		return "";

	}

	
	// -------------- PROCESSING MESSAGE METHODS -------------- //
	
	/**
	 * Convert a message in bytes. First get the content of the message. Then
	 * encrypt it with the database_key and the given iv (initialization vector).
	 * 
	 * @param m
	 * @param iv
	 * @return The bytes of the encrypted message's content.
	 * 
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	private byte[] stringToBytesContent(Message m, IvParameterSpec iv)
			throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		String content;
		if (m.getTypeMessage() == TypeMessage.TEXTE) {
			MessageTexte messageTxt = (MessageTexte) m;
			content = messageTxt.getContenu();
		} else {
			MessageFichier messageFichier = (MessageFichier) m;
			content = messageFichier.getContenu();
		}
		byte[] encryptedContent = SQLiteEncryption.encrypt(SQLiteEncryption.encryptAlgorithm, content.getBytes(),
				this.dbDataKey, iv);
		return encryptedContent;

	}

	
	/**
	 * Convert bytes in a String. In this context, the bytes correspond to the
	 * encrypted content of a message.
	 * 
	 * @param encryptedContent
	 * @param iv
	 * @return The String corresponding to the decrypted bytes.
	 * 
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	private String bytesToStringContent(byte[] encryptedContent, IvParameterSpec iv)
			throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		return SQLiteEncryption.decryptString(SQLiteEncryption.encryptAlgorithm, encryptedContent, this.dbDataKey, iv);
	}

	
	/**
	 * Process the type of a message
	 * 
	 * @param m
	 * @return The type of the message
	 */
	private String processMessageType(Message m) {
		switch (m.getTypeMessage()) {
		case TEXTE:
			return "text";
		case FICHIER:
			return "file";
		case IMAGE:
			return "image";
		default:
			return "";
		}
	}

	
	/**
	 * Process the extension of a message
	 * 
	 * @param m
	 * @return The extension of the message. null if it is a simple text message.
	 */
	private String processExtension(Message m) {
		if (m.getTypeMessage() == TypeMessage.TEXTE) {
			return null;
		} else {
			MessageFichier mFile = (MessageFichier) m;
			return mFile.getExtension();
		}
	}

	// -------------- USER SECURITY RELATED METHODS -------------- //

	
	/**
	 * Creates a new user in the database from a given username and password. The
	 * username is stored in plain text. An encrypted hash of the password is
	 * stored. The key used to encrypt the password's hash is itself encrypted then
	 * stored. Every other useful data to decrypt the key and compare the password's
	 * hash is stored as plaintext.
	 * 
	 * Fail if a user with the given username already exists in the database.
	 * 
	 * @param username
	 * @param password
	 */
	public void createNewUserEncrypt(String username, String password) {

		String algo = SQLiteEncryption.encryptAlgorithm;

		KeyGenerator keyGen = null;
		try {
			keyGen = KeyGenerator.getInstance("AES");
			keyGen.init(256);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		byte[] passwordSalt = SQLiteEncryption.getNextSalt();
		byte[] dbDataKeySalt = SQLiteEncryption.getNextSalt();

		SecretKey dbDataKey = keyGen.generateKey();

		SecretKey dbDataEncryptKey = SQLiteEncryption.getKey(password.toCharArray(), dbDataKeySalt);
		IvParameterSpec ivDbDataKey = SQLiteEncryption.generateIv();

		byte[] passwordHash = SQLiteEncryption.hash(password.toCharArray(), passwordSalt);

		byte[] dbDataKeyEncrypted = null;
		byte[] encryptedPasswordHash = null;

		try {
			dbDataKeyEncrypted = SQLiteEncryption.encrypt(algo, SQLiteEncryption.keyToByte(dbDataKey), dbDataEncryptKey,
					ivDbDataKey);
			encryptedPasswordHash = SQLiteEncryption.encrypt(algo, passwordHash, dbDataKey, ivDbDataKey);

		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}

		this.openConnection();

		String createUserRequest = "INSERT INTO user(username, pwd_salt, db_datakey_salt, encrypted_pwd_hashsalt, encrypted_db_datakey, iv_datakey) "
				+ "VALUES (?, ?, ?, ?, ?, ?); ";

		PreparedStatement prepStmt = null;
		try {
			prepStmt = this.connec.prepareStatement(createUserRequest);
			prepStmt.setString(1, username);
			prepStmt.setBytes(2, passwordSalt);
			prepStmt.setBytes(3, dbDataKeySalt);
			prepStmt.setBytes(4, encryptedPasswordHash);
			prepStmt.setBytes(5, dbDataKeyEncrypted);
			prepStmt.setBytes(6, ivDbDataKey.getIV());

		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
			prepStmt.executeUpdate();
			System.out.println("Utilisateur crée");
		} catch (SQLException e) {
			System.out.println("Nom d'utilisateur déjà pris");
		}

		this.closeConnection();

	}

	
	/**
	 * Check if a given password has the same hash (= they're the same) as the one
	 * stored in the database for a given username.
	 * 
	 * @param username
	 * @param password
	 * @return -1 if user do not exists, 0 if password is incorrect, 1 if password
	 *         is correct
	 * @throws SQLException
	 */
	public int checkPwd(String username, char[] password) throws SQLException {

		this.openConnection();

		String selectUserDataRequest = "SELECT pwd_salt, db_datakey_salt, encrypted_pwd_hashsalt, encrypted_db_datakey, iv_datakey "
				+ "FROM user " + "WHERE username = ?;";
		PreparedStatement prepStmt;
		prepStmt = this.connec.prepareStatement(selectUserDataRequest);
		prepStmt.setString(1, username);

		ResultSet res = prepStmt.executeQuery();
		if (!res.next()) {
			return -1;
		}

		byte[] passwordSalt = res.getBytes("pwd_salt");

		if (passwordSalt == null) {
			return 0;
		}

		byte[] dbDataKeySalt = res.getBytes("db_datakey_salt");

		SecretKey dbDataEncryptKey = SQLiteEncryption.getKey(password, dbDataKeySalt);

		byte[] ivBytes = res.getBytes("iv_datakey");

		if (ivBytes == null) {
			return 0;
		}

		IvParameterSpec iv = new IvParameterSpec(ivBytes);
		byte[] encryptedDbDataKey = res.getBytes("encrypted_db_datakey");

		SecretKey dbDataKey = null;
		try {
			dbDataKey = SQLiteEncryption.byteToKey(SQLiteEncryption.decryptByte(SQLiteEncryption.encryptAlgorithm,
					encryptedDbDataKey, dbDataEncryptKey, iv));
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
		}

		byte[] encryptedPasswordHash = res.getBytes("encrypted_pwd_hashsalt");

		byte[] passwordHash = SQLiteEncryption.hash(password, passwordSalt);

		this.closeConnection();

		boolean checkHash = this.checkHashPwd(passwordHash, encryptedPasswordHash, dbDataKey, iv);
		if (checkHash) {

			// Set the database key to be used in future encryptions if the given password is
			// correct.
			this.dbDataKey = dbDataKey;
			return 1;
		}

		return 0;
	}
	

	/**
	 * Check if two given hash are the same once the second one has been decrypted
	 * with the given key and iv.
	 * 
	 * @param passwordHash
	 * @param encryptedPasswordHash
	 * @param dbDataKey
	 * @param iv
	 * @return true if the first hash is equal to the second one once decrypted,
	 *         false otherwise.
	 * 
	 */
	private boolean checkHashPwd(byte[] passwordHash, byte[] encryptedPasswordHash, SecretKey dbDataKey,
			IvParameterSpec iv) {

		byte[] expectedHash = "".getBytes();
		try {
			expectedHash = SQLiteEncryption.decryptByte(SQLiteEncryption.encryptAlgorithm, encryptedPasswordHash,
					dbDataKey, iv);
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
		}

		if (passwordHash.length != expectedHash.length)
			return false;
		for (int i = 0; i < passwordHash.length; i++) {
			if (passwordHash[i] != expectedHash[i])
				return false;
		}
		return true;
	}
	
	

	// Main to create 20 users in the database with the given number
	public static void main(String[] args) {
		String[] hardcodedNames = { "Olivia", "Liam", "Benjamin", "Sophia", "Charlotte", "Noah", "Elijah", "Isabella",
				"Oliver", "Emma", "William", "Amelia", "Evelyn", "James", "Mia", "Ava", "Lucas", "Mason", "Ethan",
				"Harper" };

		String pwdPrefix = "aze1$";

		SQLiteManager sqlManager = new SQLiteManager(0);

		for (int i = 0; i < hardcodedNames.length; i++) {
			sqlManager.createNewUserEncrypt(hardcodedNames[i] + i, pwdPrefix + hardcodedNames[i].charAt(0) + i);
		}

	}

}
