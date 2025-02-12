package or.kr.formulate.korail.util;

import org.junit.jupiter.api.Test;

import java.util.Map;

public class CmsTest {
    @Test
    public void test() {
        Map<String, String> dataMap = CmsUtil.test0600();
        CmsUtil.make0600(dataMap, "0600");
    }
}
