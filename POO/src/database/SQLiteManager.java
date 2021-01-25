package database;

import java.io.File;
import java.lang.reflect.Array;
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
	
	private Connection connec;
	private int numDatabase;
	private SecretKey dbDataKey;
	
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
			File db = new File("../database"+this.numDatabase+".db");
			if(db.delete()) {
				System.out.println("supp");
			}else {
				System.out.println("no supp");
			}
			e.printStackTrace();
		}

		this.closeConnection();
	}

	private void openConnection() {
		String url = "jdbc:sqlite:"+ DATABASE_RELATIVE_PATH + this.numDatabase + ".db";
		try {
			this.connec = DriverManager.getConnection(url);
//			System.out.println("Connection to bdd established");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	private void closeConnection() {
		try {
			if (this.connec != null) {
				this.connec.close();
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

	}
	
	public int insertAllMessages(ArrayList<Message> messages, String usernameSender, String usernameReceiver) throws SQLException{
		int nbRows = 0;
		this.openConnection();
	
		int idSender = this.getIDUser(usernameSender);
		int idReceiver = this.getIDUser(usernameReceiver);
		
		if(idSender == -1) {
			this.insertUser(usernameSender);
			idSender = this.getIDUser(usernameSender);
		}
		
		if(idReceiver == -1) {
			this.insertUser(usernameReceiver);
			idReceiver = this.getIDUser(usernameReceiver);		
		}
		
		int idConversation = getIDConversation(idSender, idReceiver);
		
		if(idConversation == -1) {
			this.insertConversation(idSender, idReceiver);
			idConversation = getIDConversation(idSender, idReceiver);
		}
		
		IvParameterSpec ivConversation = this.getIvConversation(idConversation);
		
		this.connec.setAutoCommit(false);
		
		for(Message m : messages) {
			try {
				nbRows += this.insertMessage(idConversation, m, ivConversation);
			} catch (SQLException e) {
				e.printStackTrace();
				this.connec.rollback();
			}
		}
		
		this.connec.commit();
		
		this.closeConnection();
		
		//System.out.println("Nombre de message(s) insérée(s) : " + nbRows);
		
		return nbRows;
	}


	public ArrayList<Message> getHistoriquesMessages(String usernameOther, String pseudoOther) throws SQLException {
		
		this.openConnection();
		
		
		ArrayList<Message> messages = new ArrayList<Message>();
		
		String usernameSelf = Utilisateur.getSelf().getId();
		
		int idSelf = this.getIDUser(usernameSelf);
		int idOther = this.getIDUser(usernameOther);
		
		int idConversationSelf = this.getIDConversation(idSelf, idOther);
		int idConversationOther = this.getIDConversation(idOther, idSelf);
		IvParameterSpec ivConversation = this.getIvConversation(idConversationSelf);
//		String str = "datetime(d1,'unixepoch','localtime')";
		
		
		String getHistoriqueRequest = "SELECT id_conversation, id_type, content, date, extension "
									+ "FROM message "
									+ "WHERE id_conversation IN (?,?) "
									+ "ORDER by date";
		
		PreparedStatement prepStmt = this.connec.prepareStatement(getHistoriqueRequest);
		prepStmt.setInt(1, idConversationSelf);
		prepStmt.setInt(2, idConversationOther);
		ResultSet res = prepStmt.executeQuery();
		
		//Retrieve the messages one by one
		//Create the appropriate message object depending on the type and sender/receiver
		//and add the message in the list
		while(res.next()) {
			int idType = res.getInt("id_type");
			String type = this.getType(idType);
			
			String content = null;
			try {
				content = this.bytesToStringContent(res.getBytes("content"), ivConversation);
			} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
					| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException
					| SQLException e1) {
				//System.out.println("erreur déchiffrement");
			}
			
			Message message = null;
			String extension;
			
			if(!type.equals("")) {
				try {
					switch(type) {
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
			
			if(res.getInt("id_conversation") == idConversationSelf) {
				message.setSender("Moi");
			}else{
				message.setSender(pseudoOther);
			}
			message.setDateMessage(res.getString("date"));	
			if(content != null) {
				messages.add(message);
			}
			
		}
		
		
		this.closeConnection();
		
		return messages;
	}


	private void insertUser(String username) throws SQLException {
		String insertUserRequest = "INSERT INTO user (username) " + "VALUES (?);";

		PreparedStatement prepStmt = this.connec.prepareStatement(insertUserRequest);
		prepStmt.setString(1, username);
		prepStmt.executeUpdate();
	}
	
	
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


	private void insertConversation(int idSender, int idReceiver) throws SQLException {
		String insertConversationRequest = "INSERT INTO conversation (id_emetteur, id_recepteur, iv_conversation) " + "VALUES "
				+ "(?, ?, ?),"
				+ "(?, ?, ?);";

		byte[] ivConversation = SQLiteEncprytion.generateIv().getIV();
		
		PreparedStatement prepStmt = this.connec.prepareStatement(insertConversationRequest);
		prepStmt.setInt(1, idSender);
		prepStmt.setInt(2, idReceiver);
		prepStmt.setBytes(3, ivConversation);
		prepStmt.setInt(4, idReceiver);
		prepStmt.setInt(5, idSender);
		prepStmt.setBytes(6, ivConversation);
		
		prepStmt.executeUpdate();
	}
	
	
	private int getIDConversation(int idSender, int idReceiver) throws SQLException {
		String getIDRequest = "SELECT id_conversation " + "FROM conversation " + "WHERE id_emetteur = ? "
				+ "AND id_recepteur = ? ;";

		PreparedStatement prepStmt = this.connec.prepareStatement(getIDRequest);
		prepStmt.setInt(1, idSender);
		prepStmt.setInt(2, idReceiver);
		ResultSet res = prepStmt.executeQuery();

		if (res.next()) {
			return res.getInt("id_conversation");
		}
		return -1;
	}
	
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
	
	
	private String getType(int idType) throws SQLException {
		String getTypeRequest = "SELECT label FROM type WHERE id_type = ?;";
		
		PreparedStatement prepStmt = this.connec.prepareStatement(getTypeRequest);
		prepStmt.setInt(1, idType);
		
		ResultSet res = prepStmt.executeQuery();
		
		if(res.next()) {
			return res.getString("label");
		}
		
		return "";

	}
	
	
	private byte[] stringToBytesContent(Message m, IvParameterSpec iv) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		String content;
		if (m.getTypeMessage() == TypeMessage.TEXTE) {
			MessageTexte messageTxt = (MessageTexte) m;
			content = messageTxt.getContenu();
		}else {
			MessageFichier messageFichier = (MessageFichier) m;
			content = messageFichier.getContenu();
		}
		byte[] encryptedContent = SQLiteEncprytion.encrypt(SQLiteEncprytion.encryptAlgorithm, content.getBytes(), this.dbDataKey, iv);
		return encryptedContent;
		
	}
	
	private String bytesToStringContent(byte[] encryptedContent, IvParameterSpec iv) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		return SQLiteEncprytion.decryptString(SQLiteEncprytion.encryptAlgorithm, encryptedContent, this.dbDataKey, iv);
	}

	
	private String processMessageType(Message m) {
		switch (m.getTypeMessage()) {
		case TEXTE: return "text";
		case FICHIER: return "file";			
		case IMAGE: return "image";
		default: return "";
		}
	}
	
	private String processExtension(Message m) {
		if(m.getTypeMessage() == TypeMessage.TEXTE) {
			return null;
		}else {
			MessageFichier mFile = (MessageFichier) m;
			return mFile.getExtension();
		}
	}

	
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
	
	public void createNewUserEncrypt(String username, String password) {
		
		
		String algo = SQLiteEncprytion.encryptAlgorithm;
		
		KeyGenerator keyGen = null;
		try {
			keyGen = KeyGenerator.getInstance("AES");
			keyGen.init(256);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		byte[] passwordSalt = SQLiteEncprytion.getNextSalt();
		byte[] dbDataKeySalt = SQLiteEncprytion.getNextSalt();
		
		SecretKey dbDataKey = keyGen.generateKey(); 
		
		SecretKey dbDataEncryptKey = SQLiteEncprytion.getKey(password.toCharArray(), dbDataKeySalt);
		IvParameterSpec ivDbDataKey = SQLiteEncprytion.generateIv();
		
		byte[] passwordHash = SQLiteEncprytion.hash(password.toCharArray(), passwordSalt);
		
		byte[] dbDataKeyEncrypted = null;
		byte[] encryptedPasswordHash = null;
		
		try {
			dbDataKeyEncrypted = SQLiteEncprytion.encrypt(
					algo, SQLiteEncprytion.keyToByte(dbDataKey), dbDataEncryptKey, ivDbDataKey);
			encryptedPasswordHash = SQLiteEncprytion.encrypt(
					algo, passwordHash , dbDataKey, ivDbDataKey);
			
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
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
	 * 
	 * @param username
	 * @param password
	 * @return -1 if user do not exists,
	 * 			0 if password incorrect,
	 * 			1 if password correct
	 * @throws SQLException
	 */
	public int checkPwd(String username, char[] password) throws SQLException {
		
		this.openConnection();
		
		String selectUserDataRequest = "SELECT pwd_salt, db_datakey_salt, encrypted_pwd_hashsalt, encrypted_db_datakey, iv_datakey "
									+ "FROM user "
									+ "WHERE username = ?;";
		PreparedStatement prepStmt;
		prepStmt = this.connec.prepareStatement(selectUserDataRequest);
		prepStmt.setString(1, username);
		
		ResultSet res = prepStmt.executeQuery();
		if(!res.next()) {
			return -1;
		}
		
		byte[] passwordSalt = res.getBytes("pwd_salt");
		byte[] dbDataKeySalt = res.getBytes("db_datakey_salt");
		
		SecretKey dbDataEncryptKey = SQLiteEncprytion.getKey(password, dbDataKeySalt);
		IvParameterSpec iv = new IvParameterSpec(res.getBytes("iv_datakey"));
		
		byte[] encryptedDbDataKey = res.getBytes("encrypted_db_datakey");
		
		
		SecretKey dbDataKey = null;
		try {
			 dbDataKey = SQLiteEncprytion.byteToKey(
					SQLiteEncprytion.decryptByte(SQLiteEncprytion.encryptAlgorithm, encryptedDbDataKey, dbDataEncryptKey, iv)
					);
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
			//System.out.println("Problème déchiffrement clé db");
		}
		
		this.dbDataKey = dbDataKey;
		
		byte[] encryptedPasswordHash = res.getBytes("encrypted_pwd_hashsalt");
		
		
		byte[] passwordHash = SQLiteEncprytion.hash(password, passwordSalt);
		
		this.closeConnection();
		
		boolean checkHash = this.checkHashPwd(passwordHash ,encryptedPasswordHash, dbDataKey, iv);
		if(checkHash) {
			return 1;
		}
		return 0;	
	}
	
	
	private boolean checkHashPwd(byte[] passwordHash, byte[] encryptedPasswordHash, SecretKey dbDataKey, IvParameterSpec iv) {
		
		byte[] expectedHash = "".getBytes();
		try {
			expectedHash = SQLiteEncprytion.decryptByte(SQLiteEncprytion.encryptAlgorithm, encryptedPasswordHash, dbDataKey, iv);
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
		}
		
		
		
		if (passwordHash.length != expectedHash.length) return false;
	    for (int i = 0; i < passwordHash.length; i++) {
	      if (passwordHash[i] != expectedHash[i]) return false;
	    }
	    return true;
	}
	
	
	public static void main(String[] args) {
		String[] hardcodedNames = {"Olivia","Liam","Benjamin","Sophia","Charlotte","Noah","Elijah","Isabella",
				"Oliver","Emma","William","Amelia","Evelyn","James","Mia","Ava","Lucas","Mason","Ethan","Harper"};
		
		String pwdPrefix = "aze1$";
		
		SQLiteManager sqlManager = new SQLiteManager(0);
		
		for(int i=0; i<hardcodedNames.length; i++) {
			sqlManager.createNewUserEncrypt(hardcodedNames[i]+i, pwdPrefix+hardcodedNames[i].charAt(0)+i);
		}
		
	}

}
