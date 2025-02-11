package or.kr.formulate.korail.util;

import or.kr.formulate.korail.exception.StringUtilException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class StringUtil {

    private static final Logger logger = LoggerFactory.getLogger(StringUtil.class);

    // HEX to Byte[]
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    // Byte[] to HEX
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
    // returns HexDump
    private static String getHexDump(byte[] bytes) {
        final StringBuilder sb = new StringBuilder();
        for(byte b : bytes){
            sb.append(Integer.toHexString(((int)b & 0xff))).append(" ");
        }
        return sb.toString();
    }

    // 스트링을 바이트 단위로 substring 한다
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

    // 입력 스트링의 Null여부, 인코딩에 따른 바이트 길이 체크, 입력된 길이를 넘는 경우 바이트 단위로 substring 하여 반환
    private static String checkLength(String str, int len, String characterSet) {
        if (str == null || str.isEmpty()) return "";
        if ("utf-8".equalsIgnoreCase(characterSet) || "utf8".equalsIgnoreCase(characterSet)) {
            if (str.getBytes(StandardCharsets.UTF_8).length > len) {
                str = substringByBytes(str, 0, len, characterSet);
            }
        } else {
            try {
                if (str.getBytes(characterSet).length > len) {
                    str = substringByBytes(str, 0, len, characterSet);
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return str;
    }

    // 인코딩 형식에 따른 전문 문자열 세팅
    public static String setString(String str, int len, String characterSet) {
        str = checkLength(str, len, characterSet);
        StringBuffer sb = new StringBuffer(str);
        if ("utf-8".equalsIgnoreCase(characterSet) || "utf8".equalsIgnoreCase(characterSet)) {
            while (sb.toString().getBytes(StandardCharsets.UTF_8).length < len) {
                sb.append(' ');
            }
        } else {
            while (true) {
                try {
                    if (!(sb.toString().getBytes(characterSet).length < len)) break;
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                sb.append(' ');
            }
        }
        return sb.toString();
    }

    // 인코딩 형식에 따른 전문 숫자 세팅
    public static String setNumber(String str, int len, String characterSet) {
        str = checkLength(str, len, characterSet);
        StringBuffer sb = new StringBuffer();
        if ("utf-8".equalsIgnoreCase(characterSet) || "utf8".equalsIgnoreCase(characterSet)) {
            while (sb.length() < len - str.getBytes(StandardCharsets.UTF_8).length) {
                sb.append('0');
            }
        } else {
            while (true) {
                try {
                    if (!(sb.length() < len - str.getBytes(characterSet).length)) break;
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                sb.append('0');
            }
        }
        sb.append(str);
        return sb.toString();
    }

    // 입력된 문자열로 입력된 바이트 길이 만큼 채워진 문자열을 반환
    public static String fillString(char c, int len, String characterSet) {
        StringBuffer sb = new StringBuffer();
        sb.setLength(0);
        if ("utf-8".equalsIgnoreCase(characterSet) || "utf8".equalsIgnoreCase(characterSet)) {
            while (sb.toString().getBytes(StandardCharsets.UTF_8).length < len) {
                sb.append(c);
            }
        } else {
            while (true) {
                try {
                    if (!(sb.toString().getBytes(characterSet).length < len)) break;
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                sb.append(c);
            }

        }
        return sb.toString();
    }
}
