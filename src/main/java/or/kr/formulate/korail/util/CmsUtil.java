package or.kr.formulate.korail.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CmsUtil {

    private static final Logger logger = LoggerFactory.getLogger(CmsUtil.class);

    private Map<String, Object> meta;

    public static byte[] make0600(Map<String, String> map, String type) {
        Map<String, String> msgMap = test0600();
        Map<String, Object> layout = PropertyUtil.getMetaProp("cms");
        List<Map<String, Object>> fields = PropertyUtil.getFieldList(layout, "IF0600");
        fields.forEach(f -> {
           Map<String, Object> field = f;
           logger.debug("{}", field);
           int idx = (int)field.get("idx");
        });

        return null;
    }

    public static Map<String, String> test0600() {
        int byteLength = 0;
        Map<String, String> info = new ConcurrentHashMap<>();
        info.put("transactionCd", "123456789");   // TRANSACTION CODE(9)
        info.put("workDivCd", "FTE");             // 업무구분코드, 기관과 도로공사간("FTE")
        info.put("orgCd", "10100110");            // 기관코드(국민은행)
        info.put("msgKindCd", "0600");            // 전문종별코드 "0600"
        info.put("transactionDivCd", "R");        // 거래구분코드: 도로공사 송신 : S, 도로공사 수신: R
        info.put("txFlag", "E");                  // 송수신Flag: 도로공사 전문 발생: 'C', 기관에서 전문 발생: 'E'
        info.put("fileNm", "   ");                // 파일명: 0600/001, 0600/004 전문은 SPACE 처리한다.
        info.put("responseCd", "000");            // 응답코드: 전문처리결과, 요구전문에는 "000" Set
        info.put("txDateTime","0211163412");      // 전문전송일시: 10자리 (MMDDhhmmss)
        info.put("mngCd", "001");                 // 업무관리정보: 업무개시(001), 파일송수신완료(002,송신할파일존재), 파일송수신완료(003, 송신할 파일없음), 업무종료(004)
        info.put("senderNm", "김병기");            // 송신자명
        info.put("senderEnc", "1111");            // 송신자암호

        return info;

    }
}
