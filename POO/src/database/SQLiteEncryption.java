package database;

import java.util.Base64;
import java.util.Random;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.security.spec.*;

class SQLiteEncryption {

	private static final Random RANDOM = new SecureRandom();
	private static final int ITERATIONS = 10000;
	private static final int KEY_LENGTH = 256;
	protected static final String encryptAlgorithm = "AES/CBC/PKCS5Padding";

	/**
	 * Return a 24 bytes salt.
	 * 
	 * @return The salt in a byte array.
	 */
	protected static byte[] getNextSalt() {
		byte[] salt = new byte[24];
		RANDOM.nextBytes(salt);
		return salt;
	}

	/**
	 * Return the hash of the given password with the given salt.
	 * 
	 * @param password
	 * @param salt
	 * @return The hash in a byte array.
	 */
	protected static byte[] hash(char[] password, byte[] salt) {
		return SQLiteEncryption.getKey(password, salt).getEncoded();
	}

	/**
	 * Return a secret key generated with the given password and salt.
	 * 
	 * @param password
	 * @param salt
	 * @return The secret key.
	 */
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

	/**
	 * Return a 16 bytes Initialization vector.
	 * 
	 * @return The Initialization vector.
	 */
	protected static IvParameterSpec generateIv() {
		byte[] iv = new byte[16];
		RANDOM.nextBytes(iv);
		return new IvParameterSpec(iv);
	}

	/**
	 * Encrypt the given input (byte array) with the given algorithm, secretKey and
	 * initialization vector.
	 * 
	 * 
	 * @param algorithm
	 * @param input
	 * @param key
	 * @param iv
	 * @return The encrypted input in a byte array.
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	protected static byte[] encrypt(String algorithm, byte[] input, SecretKey key, IvParameterSpec iv)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.ENCRYPT_MODE, key, iv);
		byte[] cipherText = cipher.doFinal(input);
		return Base64.getEncoder().encode(cipherText);
	}

	/**
	 * Decrypt the given input (byte array) with the given algorithm, secretKey and
	 * initialization vector.
	 * 
	 * @param algorithm
	 * @param input
	 * @param key
	 * @param iv
	 * @return The decrypted input in a byte array.
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	protected static byte[] decryptByte(String algorithm, byte[] input, SecretKey key, IvParameterSpec iv)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.DECRYPT_MODE, key, iv);
		byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(input));
		return plainText;
	}

	/**
	 * Decrypt the given input (byte array) with the given algorithm, secretKey and
	 * initialization vector.
	 * 
	 * @param algorithm
	 * @param input
	 * @param key
	 * @param iv
	 * @return The decrypted input as a String.
	 * 
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	protected static String decryptString(String algorithm, byte[] input, SecretKey key, IvParameterSpec iv)
			throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
		return new String(SQLiteEncryption.decryptByte(algorithm, input, key, iv));
	}

	protected static byte[] keyToByte(SecretKey key) {
		return Base64.getEncoder().encode(key.getEncoded());
	}

	protected static SecretKey byteToKey(byte[] encodedKey) {
		byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
		return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
	}
}
