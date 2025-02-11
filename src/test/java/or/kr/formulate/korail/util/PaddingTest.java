package or.kr.formulate.korail.util;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class PaddingTest {

    private static final Logger logger = LoggerFactory.getLogger(PaddingTest.class);

    @Test
    public void testPadding() {
        String test = "인터페이스";
        String testNum = "12312";
        int len = 20;
        logger.debug("Length of UTF_8: {}", test.getBytes(StandardCharsets.UTF_8).length);
        try {
            logger.debug("Length of EUC-KR: {}", test.getBytes("EUC-KR").length);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        String s = StringUtil.setString(test, len, "UTF-8");
        logger.debug("UTF-8,  {}, {} : [{}]", test, s.getBytes(StandardCharsets.UTF_8).length, s);
        String s2 = StringUtil.setString(test, len, "euc-kr");
        try {
            logger.debug("euc-kr, {}, {} : [{}]", test, s2.getBytes("euc-kr").length, s2);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        String n = StringUtil.setNumber(testNum, len, "UTF-8");
        logger.debug("UTF-8,  {}, {} : [{}]", testNum, n.getBytes(StandardCharsets.UTF_8).length, n);
        String n2 = StringUtil.setNumber(testNum, len, "euc-kr");
        try {
            logger.debug("euc-kr, {}, {} : [{}]", testNum, n2.getBytes("euc-kr").length, n2);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        logger.debug("-----------------------------------------------");
        logger.debug("fill String {}: [{}]", ' ', StringUtil.fillString(' ', 5, "UTF-8"));
        logger.debug("fill String {}: [{}]", '0', StringUtil.fillString('0', 5, "UTF-8"));
        logger.debug("fill String {}: [{}]", ' ', StringUtil.fillString(' ', 10, "euc-kr"));
        logger.debug("fill String {}: [{}]", '0', StringUtil.fillString('0', 10, "euc-kr"));
    }
}
