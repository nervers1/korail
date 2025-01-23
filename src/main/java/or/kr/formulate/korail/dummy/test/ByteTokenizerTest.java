package or.kr.formulate.korail.dummy.test;

import or.kr.formulate.korail.util.token.ByteTokenizer;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;

public class ByteTokenizerTest {
    @Test
    public void test() {

        // bytes array to parse
        final byte[] bytes = new byte[] {
                (byte) 0x31, (byte) 0xC0, (byte) 0x80,
                (byte) 0x64, (byte) 0x75, (byte) 0x79, (byte) 0x64, (byte) 0xC0, (byte) 0x80,
                (byte) 0x39
        };
        // delimiters
        final byte[] delimiters = new byte[]{(byte) 0xC0, (byte) 0x80};

        System.out.println("Byte array to parse: " + getHexDump(bytes));

        // Create an instance of ByteTokenizer
        final ByteTokenizer tokenizer = new ByteTokenizer(bytes, delimiters);

        // Count tokens
        final int tonkenNums = tokenizer.countTokens();
        System.out.println("Token numbers: " + tonkenNums);

        // Print all tokens
        byte[] token;
        while(tokenizer.hasMoreTokens()) {
            token = (byte[]) tokenizer.nexToken();
            System.out.println("Byte token: " + getHexDump(token));
        }


        System.out.println("-----------------------------------------------");
        final String testString = "테스트문자@열입니다11212afsga@sdfad1123aㅁㄴㅇ@ㄻㄴㅇㄹ";
        byte[] testBytes = testString.getBytes();
        int testlen = testBytes.length;
        final or.kr.formulate.korail.util.ByteTokenizer tokenizer2;
        try {
            tokenizer2 = new or.kr.formulate.korail.util.ByteTokenizer(testBytes, 0, testlen, "@", false,"UTF-8");
            while(tokenizer2.hasNext()) {
                String token2 = tokenizer2.next();
                System.out.println(token2);
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        System.out.println("-----------------------------------------------");
    }


    private static String getHexDump(byte[] bytes) {
        final StringBuffer sb = new StringBuffer();
        for(byte b : bytes){
            sb.append(Integer.toHexString(((int)b & 0xff))).append(" ");
        }
        return sb.toString();
    }
}
