package or.kr.formulate.korail.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

public class CmsUtil {

    private static final Logger logger = LoggerFactory.getLogger(CmsUtil.class);

    private Map<String, Object> meta;

    /**
     * 필드정보가 들어있는 맵을 입력받아 각 필드값을 문자셋에 맞는 byte[]로 치환하여 리스트로 반환
     * @param dataList 필드정보 리스트
     * @param charset 문자셋("UTF-8", "UTF-8", "EUC-KR", "ISO8859-1"
     * @return
     */
    public static List<byte[]> getBytesList(List<Map<String, Object>> dataList, String charset) {
        List<byte[]> result = new ArrayList<>();
        for (Map<String, Object> map : dataList) {
            map.forEach((key, value) -> {
                String dataValue = (String) value;
                if ("UTF8".equalsIgnoreCase(charset)  || "UTF-8".equalsIgnoreCase(charset)) {
                    byte[] dataByte = dataValue.getBytes(StandardCharsets.UTF_8);
                    result.add(dataByte);
                } else {
                    try {
                        byte[] dataByte = dataValue.getBytes(charset);
                        result.add(dataByte);
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        return result;
    }

    /**
     * 바이트 리스트를 하나의 바이트로 합쳐서 반환
     * @param bytesList 바이트배열의 리스트
     * @return 리스트의 각 요소의 바이트를 합친 하나의 바이트
     */
    public static byte[] getTotalBytes(List<byte[]> bytesList) {
        byte[] result = new byte[0];
        BiFunction<byte[], byte[], byte[]> add = (b1, b2) -> {
            byte[] sum = new byte[b1.length + b2.length];
            System.arraycopy(b1, 0, sum, 0, b1.length);
            System.arraycopy(b2, 0, sum, b1.length, b2.length);
            return sum;
        };

        int size = bytesList.size();
        byte[] temp = new byte[0];
        for (int i = 0; i < size; i++) {
            if (i == 0) {
                temp = add.apply(new byte[0], bytesList.get(i));
            } else {
                temp = add.apply(temp, bytesList.get(i));
            }
        }
        return temp;
    }

    public static byte[] make0600(Map<String, Object> map, String type) {
        Map<String, Object> msgMap = test0600();
        Map<String, Object> layout = PropertyUtil.getMetaProp("cms");
        List<Map<String, Object>> fields = PropertyUtil.getFieldList(layout, "IF0600");
        fields.forEach(f -> {
           Map<String, Object> field = f;
           logger.debug("{}", field);
           int idx = (int)field.get("idx");
        });

        return null;
    }

    public static Map<String, Object> test0600() {
        Map<String, Object> info = new ConcurrentHashMap<>();
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
