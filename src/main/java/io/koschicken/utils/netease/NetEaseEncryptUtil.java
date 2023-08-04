package io.koschicken.utils.netease;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Random;

/**
 * params 参数计算 两次AES/CBC/Padding IV(0102030405060708) 两次加密结果均再次进行Base64编码
 * encSecKey 参数计算 RSA加密NoPadding
 * <p>
 * RSA的算法涉及三个参数，n、e1、e2。其中，n是两个大质数p、q的积，n的二进制表示时所占用的位数，就是所谓的密钥长度。
 * <p>
 * e1和e2是一对相关的值，e1可以任意取，
 * <p>
 * 要求e1与(p-1）*(q-1）互质；
 * <p>
 * 再选择e2，要求（e2*e1）mod((p-1）*(q-1））=1。
 * <p>
 * （n，e1）,(n，e2）就是密钥对。其中(n，e1）为公钥，(n，e2）为私钥。
 * <p>
 * RSA加解密的算法完全相同，设A为明文，B为密文，则：
 * <p>
 * A=B^e2 mod n；
 * <p>
 * B=A^e1 mod n；
 * 
 * @author Guan Yue
 *
 */
@Slf4j
public class NetEaseEncryptUtil {

	private NetEaseEncryptUtil() {
	}

	private static final String IV = "0102030405060708";
	private static String randomStr = null;
	private static final Random RANDOM = new Random();

	@SuppressWarnings("deprecation")
	public static String generateToken(String data) {
        String param2 = "010001";
		String param3 = "00e0b509f6259df8642dbc35662901477df22677ec152b5ff68ace615bb7b725152b3ab17a876aea8a5aa76d2e417629ec4ee341f56135fccf695280104e0312ecbda92557c93870114af6c9d05c4f7f0c3685b7a46bee255932575cce10b424d813cfe4875d3e82047b97ddef52741d546b8e289dc6935b3ece0462db0a22b8e7";
		String param4 = "0CoJUm6Qyw8W8jud";
		StringBuilder sb = new StringBuilder();
		String params = URLEncoder.encode(getParams(data, param4));
		String encSecKey = URLEncoder.encode(getEncSecKey(param2, param3));
		sb.append("params=").append(params).append("&encSecKey=").append(encSecKey);
		return sb.toString();
	}

	/**
	 * OK!
	 * <p>
	 * 010001
	 * 00e0b509f6259df8642dbc35662901477df22677ec152b5ff68ace615bb7b725152b3ab17a876aea8a5aa76d2e417629ec4ee341f56135fccf695280104e0312ecbda92557c93870114af6c9d05c4f7f0c3685b7a46bee255932575cce10b424d813cfe4875d3e82047b97ddef52741d546b8e289dc6935b3ece0462db0a22b8e7
	 */
	public static String getEncSecKey(String param2, String param3) {
		try {
			final int MAX_ENCRYPT_BLOCK = 1024;
			BigInteger pubkey = new BigInteger(param2, 16);
			BigInteger modulus = new BigInteger(param3, 16);
			RSAPublicKeySpec keySpec = new RSAPublicKeySpec(modulus, pubkey);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PublicKey publicKey = keyFactory.generatePublic(keySpec);
			Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");// PKCS1Padding
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			randomStr = new StringBuilder(randomStr).reverse().toString();
			byte[] data = randomStr.getBytes(StandardCharsets.UTF_8);
			int inputLen = data.length;
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int offSet = 0;
			byte[] cache;
			int i = 0;
			// 对数据分段加密
			while (inputLen - offSet > 0) {
				if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
					cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
				} else {
					cache = cipher.doFinal(data, offSet, inputLen - offSet);
				}
				out.write(cache, 0, cache.length);
				i++;
				offSet = i * MAX_ENCRYPT_BLOCK;
			}
			byte[] encryptedData = out.toByteArray();
			out.close();
			String result = byte2HexString(encryptedData);
			if (result.length() >= 256) {
				return result.substring(result.length() - 256);
			} else {
                StringBuilder resultBuilder = new StringBuilder(result);
                while (resultBuilder.length() < 256) {
					resultBuilder.insert(0, 0);
				}
                result = resultBuilder.toString();
                return result;
			}
		} catch (NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException | IOException
				| InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException e) {
			log.error("error", e);
		}
        return null;
	}

	private static String byte2HexString(byte[] encryptedData) {
		StringBuilder sb = new StringBuilder(encryptedData.length);
        for (byte encryptedDatum : encryptedData) {
            String temp = Integer.toHexString(encryptedDatum & 0xff);
            if (temp.length() < 2)
                sb.append("0");
            sb.append(temp.toLowerCase());
        }
		return sb.toString();
	}

	public static String getParams(String param1, String param4) {
		try {
			SecretKey key = getKey(param4.getBytes(StandardCharsets.UTF_8));
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");// PKCS5Padding
			cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8)));
			byte[] res = cipher.doFinal(param1.getBytes(StandardCharsets.UTF_8));
			res = Base64.getEncoder().encode(res);

			randomStr = getRandom(16);
			key = getKey(randomStr.getBytes(StandardCharsets.UTF_8));
			cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8)));
			byte[] result = cipher.doFinal(res);
			return new String(Base64.getEncoder().encode(result), StandardCharsets.UTF_8);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
                 | InvalidAlgorithmParameterException | IllegalBlockSizeException
                 | BadPaddingException e) {
			log.error("error", e);
		}
        return null;
	}

	private static String getRandom(int i) {// 随机16字符即可
		StringBuilder sb = new StringBuilder(i);
		String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		for (int j = 0; j < i; j++) {
			int m = RANDOM.nextInt() * alphabet.length();
			sb.append(alphabet.charAt(m));
		}
		return sb.toString();
	}

	private static SecretKey getKey(byte[] bytes) {
        System.arraycopy(bytes, 0, new byte[16], 0, 16);
		return new SecretKeySpec(bytes, "AES");
	}

}
