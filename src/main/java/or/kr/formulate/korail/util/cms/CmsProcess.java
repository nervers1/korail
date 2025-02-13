package or.kr.formulate.korail.util.cms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CmsProcess<T> {
    private static final Logger logger = LoggerFactory.getLogger(CmsProcess.class);
    private ExecutorService executor;
    private final T task;
    private static final int timeout = 30;
    private static TimeUnit unit = TimeUnit.SECONDS;
    private final AtomicInteger retryCount = new AtomicInteger(0);

    public CmsProcess(T t) {
        this.task = t;
    }

    private void accept() {
        try (ServerSocket serverSocket = new ServerSocket(9577)) {
            System.out.println("Proxy server listening on port 9577...");

            while (true) {
                Socket clientSocket = serverSocket.accept(); // 클라이언트 연결 대기
                //executor.submit(() -> handleClient(clientSocket)); // 요청 처리
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void createServer(int threadCount) {

        executor = Executors.newFixedThreadPool(threadCount);
        // Callable Task로 지정된 클래스(: 리턴값 있는 경우) 수행
        logger.info("ExecutorService started... ");
        logger.info("Thread count... [{}]", threadCount);
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
            logger.info("Server submit service shutdown");
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
        logger.info("String Data");
        String str = (String)future.get(timeout, unit);
        logger.info("str {}", str);
    }

    private <R> void extractMapData(Future<R> future) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("Map Data");
        Map<String, Object> res = (Map<String, Object>)future.get(timeout, unit);
        res.forEach((k, v) -> {
            logger.info("{} --> {}", k, v.toString());
        });
    }

    private <R> void extractListData(Future<R> future) throws InterruptedException, ExecutionException, TimeoutException {
        logger.info("List Data");
        if (future.get() instanceof List) {
            List<?> list = (List<?>)future.get(timeout, unit);
            list.forEach(item -> {
                if (item instanceof Map) {
                    ((Map) item).forEach((k, v) -> {
                        logger.info("{} --> {}", k, v.toString());
                    });
                } else if (item instanceof String) {
                    logger.info("{}", item);
                }
            });
        }
    }
}
