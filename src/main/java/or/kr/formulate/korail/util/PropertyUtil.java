package or.kr.formulate.korail.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PropertyUtil {

    private static final Logger logger = LoggerFactory.getLogger(PropertyUtil.class);

    public static void getMetaProp(String propName) {
        Map<String, List<Map<Integer, Object>>> map = new ConcurrentHashMap<>();
        Map<String, Map<?,?>> result = new ConcurrentHashMap<>();
        String path = "meta/" + propName + ".properties";
        String propPath = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(path)).getPath();
        logger.debug("propPath ---> {}", propPath);

        final Properties prop = new Properties();
        Map<String, Object> ifMap = new TreeMap<>((o1, o2) -> o1.toString().compareTo(o2.toString()));
        Map<Integer, Map<String, Object>> fields;
        try (FileInputStream stream = new FileInputStream(propPath)) {
            prop.load(stream);
            Enumeration<Object> keys = prop.keys();

            logger.debug("============================================================================");
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                String[] splitKeys = key.split("\\.");

                String interfaceId = splitKeys[0];
                String propType = splitKeys[1];
                String desc;
                String fieldName;
                if ("name".equalsIgnoreCase(propType)) {
                    // IF0310.name=[0310]
                    desc = prop.getProperty(key);

                } else if ("field".equalsIgnoreCase(propType)) {
                    // IF0310.field.name1=1|TCP/IP 송신 바이트수|N|4|4
                    fields = new TreeMap<>();
                    Map<String, Object> itemMap = new ConcurrentHashMap<>();

                    fieldName = splitKeys[2];  // name1
                    String[] fieldArr = prop.getProperty(key).split("[|]");

                    // 1|TCP/IP 송신 바이트수|N|4|4
                    // 2|업무구분코드|S|3|7
                    // ...
                    logger.debug("interfaceId: {}",interfaceId);
                    logger.debug("name: {}",splitKeys[2]);
                    logger.debug("idx: {}",fieldArr[0]);
                    logger.debug("type: {}",fieldArr[2]);
                    logger.debug("nameKr: {}",fieldArr[1]);
                    logger.debug("length: {}",fieldArr[3]);
                    logger.debug("offset: {}",fieldArr[4]);

                    logger.debug("----------------------------------------------------------------------------");
                    itemMap.put("interfaceId",interfaceId);
                    itemMap.put("name",splitKeys[2]);
                    itemMap.put("idx",Integer.parseInt(fieldArr[0]));
                    itemMap.put("type",fieldArr[2]);
                    itemMap.put("nameKr",fieldArr[1]);
                    itemMap.put("length",Integer.parseInt(fieldArr[3]));
                    itemMap.put("offset",Integer.parseInt(fieldArr[4]));
                    fields.put(Integer.parseInt(fieldArr[0]), itemMap);

//
//                    fields.add(fieldMap);
                }
//                result.put("id", interfaceId);

            }
            logger.debug("----------------------------------------------------------------------------");
            logger.debug("============================================================================");
            ifMap.forEach((k, v) -> {
                logger.debug("ID: {} --> : {}", k, v);

            });
            logger.debug("============================================================================");
//            fields.forEach(field -> {
//                logger.debug(field.toString());
//            });
            logger.debug("============================================================================");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    public static void main(String[] args) {
        String path = "test";
        getMetaProp(path);

    }
}
