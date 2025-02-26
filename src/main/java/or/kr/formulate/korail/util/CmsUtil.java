package or.kr.formulate.korail.util;

import or.kr.formulate.korail.code.ResponseCode;
import or.kr.formulate.korail.exception.EAIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

public class CmsUtil {

    private static final Logger logger = LoggerFactory.getLogger(CmsUtil.class);

    private Map<String, Object> meta;

    /**
     * 필드정보가 들어있는 맵을 입력받아 각 필드값을 문자셋에 맞는 byte[]로 치환하여 리스트로 반환
     *
     * @param dataList 필드정보 리스트
     * @param charset  문자셋("UTF-8", "UTF-8", "EUC-KR", "ISO8859-1"
     * @return
     */
    public static List<byte[]> getBytesList(List<Map<String, Object>> dataList, String charset) {
        List<byte[]> result = new ArrayList<>();
        for (Map<String, Object> map : dataList) {
            map.forEach((key, value) -> {
                String dataValue = (String) value;
                if ("UTF8".equalsIgnoreCase(charset) || "UTF-8".equalsIgnoreCase(charset)) {
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
     *
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


    // 요청 전문을 생성한다.
    public static String makeMessage(String interfaceId, Map<String, Object> data) {
        final Properties prop = PropertyUtil.getInterfaceProp("cms");
        final String encoding = prop.getProperty("Server.ENCODING");
        Map<String, Object> cms = PropertyUtil.getMetaProp("cms");
        List<Map<String, Object>> fields = PropertyUtil.getFieldList(cms, interfaceId);
        StringBuffer sb = new StringBuffer();

        AtomicInteger totalBytes = new AtomicInteger();
        AtomicReference<Map<String, Object>> lenField = new AtomicReference<>();
        fields.forEach(field -> {
            String type = (String) field.get("type");
            int len = (int) field.get("length");
            String key = (String) field.get("name");
            logger.debug("type: {}, len: {}, key: {}", type, len, key);

            String fieldValue;
            byte[] fieldBytes;
            if (key != null && !key.isEmpty() && "length".equals(key)) {
                logger.debug("길이 필드 정보를 저장한다 {}", field);
                // 길이 필드 정보를 저장한다.
                lenField.set(field);
            } else if (key != null && !key.isEmpty() && !"length".equals(key)) {
                try {
                    if (type.equals("S")) {
                        fieldValue = StringUtil.setString((String) data.get(key), len, encoding);
                        logger.debug("data : [{}] --> value : [{}]", data.get(key), fieldValue);
                        fieldBytes = fieldValue.getBytes(encoding);
                    } else if (type.equals("N")) {
                        fieldValue = StringUtil.setNumber((String) data.get(key), len, encoding);
                        logger.debug("data : [{}] --> value : [{}]", data.get(key), fieldValue);
                        fieldBytes = fieldValue.getBytes(encoding);
                    } else if (type.equals("B")) {
                        fieldBytes = (byte[]) data.get(key);
                        fieldValue = new String(fieldBytes);
                    } else {
                        // Error!
                        throw new EAIException(ResponseCode.RES_800.getDesc(), ResponseCode.RES_800);
                    }
                    totalBytes.addAndGet(fieldBytes.length);
                    sb.append(fieldValue);
                } catch (UnsupportedEncodingException e) {
                    // Error!
                    throw new EAIException(ResponseCode.RES_800.getDesc(), ResponseCode.RES_800);
                }

            }

        });
        // 전체 데이터 바이트 수
        int total = totalBytes.get();
        Map<String, Object> lenFieldMap = lenField.get();
        int len = (int) lenFieldMap.get("length");
        String lengthField = StringUtil.setNumber(String.valueOf(total), len, encoding);
        logger.debug("Total bytes: {}", total);
        sb.insert(0, lengthField);
        return sb.toString();
    }

    /**
     * 전문 공통부 파싱
     *
     * @param data        입력 바이트 byte[]
     * @param interfaceId 인터페이스아이디
     * @return 파싱 공통부 Map
     */
    public static Map<String, String> parseMessageCommon(byte[] data, String interfaceId) {
        Map<String, String> result = new LinkedHashMap<>();
        final Properties prop = PropertyUtil.getInterfaceProp("cms");
        final String encoding = prop.getProperty("Server.ENCODING");
        Map<String, Object> cms = PropertyUtil.getMetaProp("cms");
        List<Map<String, Object>> fields = PropertyUtil.getFieldList(cms, interfaceId);
        logger.debug("Fields Length: {}", fields.size());
        AtomicInteger fieldOffset = ("IFCOMM".equals(interfaceId)) ? new AtomicInteger(4) : new AtomicInteger(0);

        fields.forEach(field -> {
            logger.debug("field: {}", field);
            String type = (String) field.get("type");
            int offset = (int) field.get("offset");
            int length = (int) field.get("length");
            String key = (String) field.get("name");
            logger.debug("type: {}, offset: {}, len: {}, key: {}", type, offset, length, key);

            byte[] temp = new byte[length];
            int offSet = fieldOffset.getAndAdd(length);
            System.arraycopy(data, offSet, temp, 0, length);
            try {
                result.put(key, new String(temp, encoding));
            } catch (UnsupportedEncodingException e) {
                throw new EAIException(ResponseCode.RES_800.getDesc(), ResponseCode.RES_800, e);
            }
        });

        return result;
    }


    public static Map<String, Object> test0600() {
        ZonedDateTime zdateTime = ZonedDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMddHHmmss");
        String txDateTime = zdateTime.format(formatter);

        Map<String, Object> info = new LinkedHashMap<>(); // 입력 순서를 보장하기 위해 LinkedHashMap 사용
//        info.put("transactionCd", "123456789");   // TRANSACTION CODE(9)
        info.put("workDivCd", "FTE");             // 업무구분코드, 기관과 도로공사간("FTE")
        info.put("orgCd", "10100110");            // 기관코드(국민은행)
        info.put("msgKindCd", "0600");            // 전문종별코드 "0600"
        info.put("transactionDivCd", "R");        // 거래구분코드: 도로공사 송신 : S, 도로공사 수신: R
        info.put("txFlag", "E");                  // 송수신Flag: 도로공사 전문 발생: 'C', 기관에서 전문 발생: 'E'
        info.put("fileNm", "   ");                // 파일명: 0600/001, 0600/004 전문은 SPACE 처리한다.
        info.put("responseCd", "000");            // 응답코드: 전문처리결과, 요구전문에는 "000" Set
        info.put("txDateTime", txDateTime);      // 전문전송일시: 10자리 (MMDDhhmmss)
        info.put("mngCd", "001");                 // 업무관리정보: 업무개시(001), 파일송수신완료(002,송신할파일존재), 파일송수신완료(003, 송신할 파일없음), 업무종료(004)
        info.put("senderNm", "김병기");            // 송신자명
        info.put("senderEnc", "1111");            // 송신자암호

        return info;

    }


    public static Map<String, Object> test0610() {
        ZonedDateTime zdateTime = ZonedDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMddHHmmss");
        String txDateTime = zdateTime.format(formatter);

        Map<String, Object> info = new LinkedHashMap<>(); // 입력 순서를 보장하기 위해 LinkedHashMap 사용
//        info.put("transactionCd", "123456789");   // TRANSACTION CODE(9)
        info.put("workDivCd", "FTE");             // 업무구분코드, 기관과 도로공사간("FTE")
        info.put("orgCd", "10100110");            // 기관코드(국민은행)
        info.put("msgKindCd", "0610");            // 전문종별코드 "0610"
        info.put("transactionDivCd", "S");        // 거래구분코드: 도로공사 송신 : S, 도로공사 수신: R
        info.put("txFlag", "C");                  // 송수신Flag: 도로공사 전문 발생: 'C', 기관에서 전문 발생: 'E'
        info.put("fileNm", "   ");                // 파일명: 0600/001, 0600/004 전문은 SPACE 처리한다.
        info.put("responseCd", "000");            // 응답코드: 전문처리결과, 요구전문에는 "000" Set
        info.put("txDateTime", txDateTime);      // 전문전송일시: 10자리 (MMDDhhmmss)
        info.put("mngCd", "001");                 // 업무관리정보: 업무개시(001), 파일송수신완료(002,송신할파일존재), 파일송수신완료(003, 송신할 파일없음), 업무종료(004)
        info.put("senderNm", "김병기");            // 송신자명
        info.put("senderEnc", "1111");            // 송신자암호

        return info;

    }
}
