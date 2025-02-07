package or.kr.formulate.korail.util;

import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EncodingTest {
    @Test
    public void eucKrToUtf8() {

        String helloString = "안녕하세요. ㄱㄴㄷㄹㅁㅂㅆㅢ 놟쐛씗쀍";
        System.out.println("Source : " + helloString);

        // String 을 euc-kr 로 인코딩.
        byte[] euckrStringBuffer = helloString.getBytes(Charset.forName("euc-kr"));
        System.out.println();

        System.out.println("euc-kr - length : " + euckrStringBuffer.length);
        String decodedFromEucKr;
        try {
            decodedFromEucKr = new String(euckrStringBuffer, "euc-kr");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        System.out.println("String from euc-kr : " + decodedFromEucKr);


        // String 을 utf-8 로 인코딩.
        byte[] utf8StringBuffer;
        utf8StringBuffer = decodedFromEucKr.getBytes(StandardCharsets.UTF_8);

        System.out.println();
        System.out.println("utf-8 - length : " + utf8StringBuffer.length);
        String decodedFromUtf8;
        decodedFromUtf8 = new String(utf8StringBuffer, StandardCharsets.UTF_8);
        System.out.println("String from utf-8 : " + decodedFromUtf8);
        System.out.println();
        byte[] bytes = helloString.getBytes(StandardCharsets.UTF_8);
        String eucString = new String(bytes, StandardCharsets.UTF_8);
        System.out.println("String from utf-8(normal) : " + eucString);
        assertEquals("안녕하세요. ㄱㄴㄷㄹㅁㅂㅆㅢ 놟쐛씗쀍", eucString);

    }
}
