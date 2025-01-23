package or.kr.formulate.korail.dummy.test;

import or.kr.formulate.korail.util.StringUtil;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

public class TestHex {

    @Test
    public void test() {
        String testText = "hex Test Text";
        byte[] testToBytes = null;
        try {
            testToBytes = testText.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        //Hex 인코딩(Byte To String)
        String encodeHexString = StringUtil.encodeHexString(testToBytes);

        //Hex 인코딩(Byte To Char)
        char[] encodeHexChar = StringUtil.encodeHex(testToBytes);

        //Hex 디코딩
        byte[] decodeHexByte = StringUtil.decodeHex(encodeHexString);

        System.out.println("인코딩 전: " + testText);
        System.out.println("인코딩(String): " + encodeHexString);
        System.out.println("인코딩(Char): " + new String(encodeHexChar));
        System.out.println("디코딩: " + new String(decodeHexByte));
        System.out.println("byteArrayToHexaString: ["+ StringUtil.byteArrayToHexaString(decodeHexByte) + "]");
    }

    @Test
    public void sha1Test() {
        String inputData = "Simple Solution";

        MessageDigest sha1MessageDigest = DigestUtils.getSha1Digest();
        byte[] hashedBytes = sha1MessageDigest.digest(inputData.getBytes(StandardCharsets.UTF_8));
        String hashedString = Hex.encodeHexString(hashedBytes);

        System.out.println("Input String:" + inputData);
        System.out.println("SHA-1:" + hashedString);

    }

    @Test
    public void substringByBytesTest() {
        final String inputData = "가나다abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890";
        final String characterset = "UTF-8";
        String outputBytes = StringUtil.substringByBytes(inputData,0,5,characterset);
        System.out.println("[" + outputBytes + "]");
        System.out.println("[" + inputData.substring(0,5) + "]");

    }
}
