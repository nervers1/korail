package or.kr.formulate.korail.util.cms;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import static or.kr.formulate.korail.util.PropertyUtil.getMetaProp;

public class CmsServer {
    private ExecutorService executorService;
    private String test = "test";
    Map<String, Object> info = getMetaProp(test);


}
