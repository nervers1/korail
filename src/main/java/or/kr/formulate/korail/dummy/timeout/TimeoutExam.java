package or.kr.formulate.korail.dummy.timeout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TimeoutExam<T> {
    private static final Logger logger = LoggerFactory.getLogger(TimeoutExam.class);

    private final T task;
    private static final int maxCore = Runtime.getRuntime().availableProcessors();
    private static final int timeout = 5_000;
    private TimeUnit unit = TimeUnit.MILLISECONDS;
    private ExecutorService executor;
    private final AtomicInteger retryCount = new AtomicInteger(0);
    private final List<Future<?>> response = new ArrayList<>();

    public TimeoutExam(T t) {
        this.task = t;
    }

    public void invoke() {
        logger.debug("Max cores : {}", maxCore);

        createServer();

        executeTask(task);

        getResponse(task);

        shutdownServer();
    }



    private void createServer() {

        executor = Executors.newFixedThreadPool(5);
        // Callable Task로 지정된 클래스(: 리턴값 있는 경우) 수행
        logger.debug("ExecutorService started... ");
    }

    private void executeTask(T task) {
        if (task instanceof Callable) {
            Future<?> result = executor.submit((Callable) task);
            response.add(result);
        } else if (task instanceof Runnable) {
            executor.execute((Runnable) task);
        }
    }

    private void getResponse(T t) {
        logger.debug("checkResponse - received count {}",response.size());
        if (!response.isEmpty()) {
            response.forEach(future -> {
                try {
                    extractData(future);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (TimeoutException e) {
                    logger.warn("TimeoutException!",e);
                    if (getRetryCount() < 3) {
                        increaseRetryCount();
                        logger.debug("Retry count : {}", getRetryCount());
                        executeTask(t);
                        getResponse(t);
                    } else {
                        logger.debug("else Retry count : {}", getRetryCount());
                        shutdownServer();
                    }
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });
        }
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
            logger.debug("Server submit service shutdown");
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

    private <R> void extractData(final Future<R> future) throws InterruptedException, ExecutionException, TimeoutException {
        if (future.get(timeout, unit) instanceof String) extractStringData(future);
        else if (future.get(timeout, unit) instanceof Map) extractMapData(future);
        else if (future.get(timeout, unit) instanceof List) extractListData(future);
    }

    private <R> void extractStringData(Future<R> future) throws InterruptedException, ExecutionException, TimeoutException {
        logger.debug("String Data");
        String str = (String)future.get(timeout, unit);
        logger.debug("str {}", str);
    }

    private <R> void extractMapData(Future<R> future) throws InterruptedException, ExecutionException, TimeoutException {
        logger.debug("Map Data");
        Map<String, Object> res = (Map<String, Object>)future.get(timeout, unit);
        res.forEach((k, v) -> {
            logger.debug("{} --> {}", k, v.toString());
        });
    }

    private <R> void extractListData(Future<R> future) throws InterruptedException, ExecutionException, TimeoutException {
        logger.debug("List Data");
        if (future.get() instanceof List) {
            List<?> list = (List<?>)future.get(timeout, unit);
            list.forEach(item -> {
                if (item instanceof Map) {
                    ((Map) item).keySet().forEach(key -> {
                        logger.debug("{} --> {}", key, item.toString());
                    });
                } else if (item instanceof String) {
                    logger.debug("{}", item);
                }
            });
        }
    }


}
