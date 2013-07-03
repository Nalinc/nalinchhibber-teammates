package teammates.common.util;

import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/** Holds String-related helper functions
 */
public class StringHelper {

	public static String generateStringOfLength(int length) {
		return StringHelper.generateStringOfLength(length, 'a');
	}

	public static String generateStringOfLength(int length, char character) {
		assert (length >= 0);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			sb.append(character);
		}
		return sb.toString();
	}

	public static boolean isWhiteSpace(String string) {
		return string.trim().isEmpty();
	}

	public static String getIndent(int length) {
		return generateStringOfLength(length, ' ');
	}

	public static String encrypt(String value) {
		try {
			SecretKeySpec sks = new SecretKeySpec(
					hexStringToByteArray(Config.ENCRYPTION_KEY), "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, sks, cipher.getParameters());
			byte[] encrypted = cipher.doFinal(value.getBytes());
			return byteArrayToHexString(encrypted);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static String decrypt(String message) {
		try {
			SecretKeySpec sks = new SecretKeySpec(
					hexStringToByteArray(Config.ENCRYPTION_KEY), "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, sks);
			byte[] decrypted = cipher.doFinal(hexStringToByteArray(message));
			return new String(decrypted);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Concatenates a list of strings to a single string, separated by line breaks.
	 * @return Concatenated string.
	 */
	public static String toString(List<String> strings) {
		return toString(strings, Constants.EOL);	
	}

	/**
	 * Concatenates a list of strings to a single string, separated by the given delimiter.
	 * @return Concatenated string.
	 */
	public static String toString(List<String> strings, String delimiter) {
		String returnValue = "";
		
		if(strings.size()==0){
			return returnValue;
		}
		
		for(int i=0; i < strings.size()-1; i++){
			String s = strings.get(i);
			returnValue += s + delimiter;
		}
		//append the last item
		returnValue += strings.get(strings.size()-1);
		
		return returnValue;		
	}

	private static String byteArrayToHexString(byte[] b) {
		StringBuffer sb = new StringBuffer(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			int v = b[i] & 0xff;
			if (v < 16) {
				sb.append('0');
			}
			sb.append(Integer.toHexString(v));
		}
		return sb.toString().toUpperCase();
	}

	private static byte[] hexStringToByteArray(String s) {
		byte[] b = new byte[s.length() / 2];
		for (int i = 0; i < b.length; i++) {
			int index = i * 2;
			int v = Integer.parseInt(s.substring(index, index + 2), 16);
			b[i] = (byte) v;
		}
		return b;
	}

}
