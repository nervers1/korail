package or.kr.formulate.korail.dummy.timeout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TimeoutExam {
    private static final Logger logger = LoggerFactory.getLogger(TimeoutExam.class);

    private static final int maxCore = Runtime.getRuntime().availableProcessors();
    private static final int timeout = 30_00;
    private TimeUnit unit = TimeUnit.MILLISECONDS;
    private ExecutorService executor;
    private final AtomicInteger retryCount = new AtomicInteger(0);
    private final List<Future<Map<String, Object>>> response = new ArrayList<>();


    public void invoke() {
        logger.info("Max cores : {}", maxCore);

        CallableTimeout task = new CallableTimeout();

        TimeoutExam ex = new TimeoutExam();

        ex.createServer();

        ex.executeTaskG(task);

//        ex.executeTask();

        ex.checkResponse();

        ex.shutdownServer();
    }




    private void createServer() {

        executor = Executors.newFixedThreadPool(5);
        // Callable Task로 지정된 클래스(: 리턴값 있는 경우) 수행
        System.out.println("Server submit service started");
    }

    private <T extends Callable<Map<String, Object>>> void executeTaskG(T task) {
        Future<Map<String, Object>> result = executor.submit( task);
        response.add(result);

        logger.info("Done ? : {}", result.isDone());
    }

    private void executeTask() {
        CallableTimeout task = new CallableTimeout();
        Future<Map<String, Object>> result = executor.submit(task);
        response.add(result);

        logger.info("Done ? : {}", result.isDone());
    }

    private void executeTask(TimeoutExam ex) {
        ex.executeTask();
    }

    private <T> void checkResponse() {
        if (!response.isEmpty()) {
            int respSize = response.size();
            for (int i = 0; i < respSize; i++) {
                Future<Map<String, Object>> future = response.get(i);
                try {
                    Map<String, Object> res = future.get(timeout, unit);
                    res.forEach((k, v) -> {
                        logger.info("{} --> {}", k, v.toString());
                    });
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (TimeoutException e) {
                    if (getRetryCount() < 3) {
                        increaseRetryCount();
                        logger.info("Retry count : {}", getRetryCount());
                        executeTask(this);
                    } else {
                        retryCount.set(0);
                    }
    //                throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }
//        response.clear();
    }

    private void shutdownServer() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        } finally {
            System.out.println("Server submit service shutdown");
        }
    }

    private int getRetryCount() {
        return retryCount.get();
    }

    private void increaseRetryCount() {
        while (true) {
            int existingRetryCount = retryCount.get();
            int newValue = existingRetryCount + 1;
            if (retryCount.compareAndSet(existingRetryCount, newValue)) {
                return;
            }
        }
    }

}
