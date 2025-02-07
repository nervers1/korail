package or.kr.formulate.korail.util;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static or.kr.formulate.korail.util.PropertyUtil.getFieldList;
import static or.kr.formulate.korail.util.PropertyUtil.getMetaProp;

public class PropertyUtilTest {

    private static final Logger logger = LoggerFactory.getLogger(PropertyUtilTest.class);

    @Test
    public void PropertyUtilTest() {

        // 메타정보위치에서 프로퍼티 파일을 읽어온다 ( resources/meta/test.properties 파일을 읽는 경우 파일명 "test"를 입력한다.
        String path = "test";
        Map<String, Object> info = getMetaProp(path);
        logger.debug("info ---> {}", info);

        // 전문정보를 추출한다.
        logger.debug("============================[전문정보추출]================================================");
        info.forEach((k, v) -> {
            logger.debug("ID: {} --> : {}", k, v);

        });

        // 메타정보를 맵 형태로 읽어온 경우 해당 필드 목록을 맵에서 추출한다.
        List<Map<String, Object>> if0310 = getFieldList(info, "IF0310");

        // 추출된 맵의 리스트를 출력한다.
        if0310.forEach(field -> {
            field.forEach((k, v) -> {
                logger.debug("ID: {} --> : {}", k, v);
            });
        });

    }
}
