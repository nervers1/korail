package or.kr.formulate.korail.util.cms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CmsProcess<T extends Runnable> {
    private static final Logger logger = LoggerFactory.getLogger(CmsProcess.class);
    private ExecutorService executor;
    private final T task;
    private static final int timeout = 30;
    private static TimeUnit unit = TimeUnit.SECONDS;
    private final AtomicInteger retryCount = new AtomicInteger(0);

    public CmsProcess(T t) {
        this.task = t;
    }


    private void cmsServer() {

        ServerSocketChannel serverSocketChannel = null;

        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(5001));

            while (true) {

                System.out.println("[연결 기다림]");
                SocketChannel socketChannel = serverSocketChannel.accept();
                InetSocketAddress isa = (InetSocketAddress) socketChannel.getRemoteAddress();
                System.out.println("[연결 수락함] " + isa.getHostName());

                ByteBuffer byteBuffer = null;
//                Charset charset = Charset.forName("UTF-8");
                Charset charset = StandardCharsets.UTF_8;

                byteBuffer = ByteBuffer.allocate(100);
                int byteCount = socketChannel.read(byteBuffer);
                byteBuffer.flip();
                String message = charset.decode(byteBuffer).toString();
                System.out.println("[데이터 받기 성공]: " + message);

                byteBuffer = charset.encode("Hello Client");
                socketChannel.write(byteBuffer);
                System.out.println("[데이터 보내기 성공]");

            }
        } catch (Exception e) {
            //
        }

        if (serverSocketChannel.isOpen()) {
            try {
                serverSocketChannel.close();
            } catch (IOException e1) {
                //
            }
        }

    }

    public void invoke() {

        createServer(5);
        logger.info("Starting process");

        CompletableFuture<Void> future = CompletableFuture.runAsync(this::cmsServer, executor);

        try {
            future.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

//        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
//            logger.info("CompletableFuture started");
//            return "Hello World!";
//        }, executor);

//        future.thenAccept(res  -> {
//            logger.info("result: {}", res);
//        });
//        CmsServer1 svr = new CmsServer1();
//        CmsClient1 client = new CmsClient1();
//        CompletableFuture<Void> future1 = CompletableFuture.runAsync(svr, executor);



//
//
//        createServer(5);
//        Future<String> response = executor.submit(cms);
//        String s = null;
//        try {
//            s = response.get();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        } catch (ExecutionException e) {
//            throw new RuntimeException(e);
//        }
//        logger.info("response: {}", s);
//
//        executor.execute(task);

//        shutdownServer();
    }
    private void accept() {
        try (ServerSocket serverSocket = new ServerSocket(5001)) {
            System.out.println("Proxy server listening on port 5001...");

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
    // <Future>를 <CompletableFuture>로 변환
    static <T> CompletableFuture<T> toCompletableFuture(Future<T> future, ExecutorService executor) {
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        executor.submit(() -> {
            try {
                completableFuture.complete(future.get());
            } catch (Exception e) {
                completableFuture.completeExceptionally(e);
            }
        });
        return completableFuture;
    }
}
