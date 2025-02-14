package or.kr.formulate.korail.util;

import or.kr.formulate.korail.util.cms.CmsProcess;
import or.kr.formulate.korail.util.cms.CmsServer1;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CmsTest {

    private static final Logger logger = LoggerFactory.getLogger(CmsTest.class);

    @Test
    public void test() {
        Map<String, String> dataMap = CmsUtil.test0600();
        CmsUtil.make0600(dataMap, "0600");
    }


    @Test
    public void connect() {
        CmsServer1 server = new CmsServer1();
        CmsProcess<CmsServer1> exam = new CmsProcess<>(server);
        exam.invoke();
    }

    @Test
    public void complete_future_get_vs_join() throws ExecutionException, InterruptedException, TimeoutException {
        long timeout = 3L;
        TimeUnit timeUnit = TimeUnit.SECONDS;
        CompletableFuture<String> future = new CompletableFuture<>();
        future.complete("Hello World");

        String result = future.join();
        logger.info("result: {}", result);
        String result2 = future.get(timeout, timeUnit); // 타임아웃 적용
        logger.info("result2: {}", result2);



    }

    @Test
    public void byteSummary() {
        String test1 = "Hello World";
        String test2 = "I am Fine";

        Map<String, Object> map = new HashMap<>();
        map.put("test1", test1);
        map.put("test2", test2);
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(map);
        List<byte[]> bytesList = CmsUtil.getBytesList(list, "UTF-8");
        logger.info("bytesList: {}", bytesList);
        byte[] totalBytes = CmsUtil.getTotalBytes(bytesList);
        logger.info("totalBytes: [{}]", new String(totalBytes));
    }
}
