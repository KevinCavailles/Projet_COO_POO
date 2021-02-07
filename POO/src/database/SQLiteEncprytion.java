package database;


import java.util.Base64;
import java.util.Random;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.security.spec.*;

class SQLiteEncprytion {
	
	private static final Random RANDOM = new SecureRandom();
	private static final int ITERATIONS = 10000;
	private static final int KEY_LENGTH = 256;
	protected static final String encryptAlgorithm = "AES/CBC/PKCS5Padding";
	
	protected static byte[] getNextSalt() {
		byte[] salt = new byte[24];
		RANDOM.nextBytes(salt); 
		return salt;
	}
	
	
	protected static byte[] hash(char[] password, byte[] salt) {
		return SQLiteEncprytion.getKey(password, salt).getEncoded();
	}
	
	
	protected static SecretKey getKey(char[] password, byte[] salt) {
		PBEKeySpec saltpwd = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
		
		try {
			SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			SecretKey tmp = skf.generateSecret(saltpwd);
			SecretKey key = new SecretKeySpec(tmp.getEncoded(), "AES");
			return key;
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		} finally {
			saltpwd.clearPassword();
		}
		return null;
	}
	
	
	public static IvParameterSpec generateIv() {
	    byte[] iv = new byte[16];
	    new SecureRandom().nextBytes(iv);
	    return new IvParameterSpec(iv);
	}
	
	
	protected static byte[] encrypt(String algorithm, byte[] input, SecretKey key, IvParameterSpec iv) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException{
		Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.ENCRYPT_MODE, key, iv);
		byte[] cipherText = cipher.doFinal(input);
		return Base64.getEncoder().encode(cipherText);
	}
	
	
	protected static byte[] decryptByte(String algorithm, byte[] cipherText, SecretKey key, IvParameterSpec iv) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException{
		Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.DECRYPT_MODE, key, iv);
		byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText));
		return plainText;
	}
	
	protected static String decryptString(String algorithm, byte[] cipherText, SecretKey key, IvParameterSpec iv) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		return new String(SQLiteEncprytion.decryptByte(algorithm, cipherText, key, iv) );
	}
	
	
	public static byte[] keyToByte(SecretKey key) {
		return Base64.getEncoder().encode(key.getEncoded());
	}
	
	
	public static SecretKey byteToKey(byte[] encodedKey) {
		byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
		return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
	}
}
