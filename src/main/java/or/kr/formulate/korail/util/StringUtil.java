package or.kr.formulate.korail.util;

import or.kr.formulate.korail.exception.StringUtilException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

public class StringUtil {

    private static final Logger logger = LoggerFactory.getLogger(StringUtil.class);

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String byteArrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b & 0xff));
        }
        return sb.toString();
    }

    // Hex 인코딩(Byte To String)
    public static String encodeHexString(byte[] bytes) {
        return Hex.encodeHexString(bytes);
    }

    // Hex 인코딩(Byte To Char)
    public static char[] encodeHex(byte[] bytes) {
        return Hex.encodeHex(bytes);
    }

    // Hex 디코딩
    public static byte[] decodeHex(String str) {
        byte[] decodeHexByte;
        try {
            decodeHexByte = Hex.decodeHex(str.toCharArray());
        } catch (DecoderException e) {
            throw new RuntimeException(e);
        }
        return decodeHexByte;
    }

    /**
     * byteArrayToHexaString
     * @param bytes 데이터
     * @return 핵사코드
     */
    public static String byteArrayToHexaString(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte data : bytes) {
            builder.append(String.format("%02X", data));
        }
        return builder.toString().trim();
    }

    private static String getHexDump(byte[] bytes) {
        final StringBuilder sb = new StringBuilder();
        for(byte b : bytes){
            sb.append(Integer.toHexString(((int)b & 0xff))).append(" ");
        }
        return sb.toString();
    }

    public static String substringByBytes(String str, int beginBytes, int endBytes, String characterSet) {
        if (str == null || str.isEmpty()) return "";
        if (beginBytes < 0) beginBytes = 0;
        if (endBytes < 1) return "";

        int len = str.length();

        int beginIndex = -1;
        int endIndex = 0;

        int curBytes = 0;
        String ch;
        for (int i = 0; i < len; i++) {
            ch = str.substring(i, i + 1);
            try {
                curBytes += ch.getBytes(characterSet).length;
            } catch (UnsupportedEncodingException e) {
                logger.error("StringUtilException!! - substringByBytes");
                throw new StringUtilException("StringUtilException!! - substringByBytes", e);
            }

            if (beginIndex == -1 && curBytes >= beginBytes) {
                beginIndex = i;
            }

            if (curBytes > endBytes) {
                break;
            } else {
                endIndex = i + 1;
            }
        }

        return str.substring(beginIndex, endIndex);
    }
}
