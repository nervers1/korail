package or.kr.formulate.korail.dummy.timeout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class RunnableTimeout implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RunnableTimeout.class);
    private final int timeout = 1;
    @Override
    public void run() {

        logger.debug("RunnableTimeout .............. Waiting for {} seconds", timeout);
        /*try {
            TimeUnit.SECONDS.sleep(timeout);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            //
        }*/
    }
}
