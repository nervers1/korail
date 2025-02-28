package or.kr.formulate.korail.util.cms.nonblock;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncServer {
    private static final Logger logger = LoggerFactory.getLogger(AsyncServer.class);
    private static final int PORT = 8080;
    private static final int TIMEOUT_SECONDS = 30;

    private AsynchronousServerSocketChannel serverChannel;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void start() {
        try {
            serverChannel = AsynchronousServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(PORT));
            logger.info("Asynchronous server started on port: {}", PORT);

            acceptClient();

            // 서버 유지
            Thread.currentThread().join();

        } catch (IOException | InterruptedException e) {
            logger.error("Server error: ", e);
        } finally {
            stop();
        }
    }

    private void acceptClient() {
        serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel clientChannel, Void attachment) {
                // 다음 연결 수락
                acceptClient();
                handleClient(clientChannel);
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                logger.error("Failed to accept a connection: ", exc);
            }
        });
    }

    private void handleClient(AsynchronousSocketChannel clientChannel) {
        ByteBuffer lengthBuffer = ByteBuffer.allocate(4); // 메시지 길이 버퍼

        // 타임아웃 설정
        ScheduledFuture<?> timeoutFuture = scheduler.schedule(() -> {
            logger.warn("Read operation timed out. Closing connection.");
            closeClient(clientChannel);
        }, TIMEOUT_SECONDS, TimeUnit.SECONDS);

        readLength(clientChannel, lengthBuffer, timeoutFuture);
    }

    private void readLength(AsynchronousSocketChannel clientChannel, ByteBuffer lengthBuffer, ScheduledFuture<?> timeoutFuture) {
        clientChannel.read(lengthBuffer, null, new CompletionHandler<Integer, Void>() {
            @Override
            public void completed(Integer result, Void attachment) {
                if (timeoutFuture != null && !timeoutFuture.isDone()) {
                    timeoutFuture.cancel(true);
                }

                if (result == -1) {
                    logger.warn("Client disconnected during read length.");
                    closeClient(clientChannel);
                    return;
                }

                if (lengthBuffer.hasRemaining()) {
                    // 타임아웃 재설정
                    ScheduledFuture<?> newTimeout = scheduler.schedule(() -> {
                        logger.warn("Read operation timed out. Closing connection.");
                        closeClient(clientChannel);
                    }, TIMEOUT_SECONDS, TimeUnit.SECONDS);

                    // 아직 데이터가 부족하므로 계속 읽기
                    readLength(clientChannel, lengthBuffer, newTimeout);
                } else {
                    // 메시지 길이 읽기 완료
                    lengthBuffer.flip();
                    int length = lengthBuffer.getInt();

                    // 메시지 길이 검증
                    int MAX_MESSAGE_SIZE = 1024 * 1024; // 최대 1MB
                    if (length <= 0 || length > MAX_MESSAGE_SIZE) {
                        logger.error("Invalid message length: {}", length);
                        closeClient(clientChannel);
                        return;
                    }

                    ByteBuffer dataBuffer = ByteBuffer.allocate(length);

                    // 타임아웃 재설정
                    ScheduledFuture<?> newTimeout = scheduler.schedule(() -> {
                        logger.warn("Read operation timed out. Closing connection.");
                        closeClient(clientChannel);
                    }, TIMEOUT_SECONDS, TimeUnit.SECONDS);

                    readData(clientChannel, dataBuffer, newTimeout);
                }
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                if (timeoutFuture != null && !timeoutFuture.isDone()) {
                    timeoutFuture.cancel(true);
                }
                logger.error("Failed to read data length from client: ", exc);
                closeClient(clientChannel);
            }
        });
    }

    private void readData(AsynchronousSocketChannel clientChannel, ByteBuffer dataBuffer, ScheduledFuture<?> timeoutFuture) {
        clientChannel.read(dataBuffer, null, new CompletionHandler<Integer, Void>() {
            @Override
            public void completed(Integer result, Void attachment) {
                if (timeoutFuture != null && !timeoutFuture.isDone()) {
                    timeoutFuture.cancel(true);
                }

                if (result == -1) {
                    logger.warn("Client disconnected during data read.");
                    closeClient(clientChannel);
                    return;
                }

                if (dataBuffer.hasRemaining()) {
                    // 타임아웃 재설정
                    ScheduledFuture<?> newTimeout = scheduler.schedule(() -> {
                        logger.warn("Read operation timed out. Closing connection.");
                        closeClient(clientChannel);
                    }, TIMEOUT_SECONDS, TimeUnit.SECONDS);

                    // 아직 데이터가 부족하므로 계속 읽기
                    readData(clientChannel, dataBuffer, newTimeout);
                } else {
                    // 데이터 읽기 완료
                    dataBuffer.flip();
                    byte[] requestData = new byte[dataBuffer.remaining()];
                    dataBuffer.get(requestData);
                    String message;
                    try {
                        message = new String(requestData, "EUC-KR");
                    } catch (UnsupportedEncodingException e) {
                        logger.error("Unsupported Encoding: ", e);
                        closeClient(clientChannel);
                        return;
                    }

                    logger.info("Received data from client: {}", message);

                    // 응답 생성
                    String responseMessage = "Processed: " + message;

                    sendResponse(clientChannel, responseMessage);
                }
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                if (timeoutFuture != null && !timeoutFuture.isDone()) {
                    timeoutFuture.cancel(true);
                }
                logger.error("Failed to read data from client: ", exc);
                closeClient(clientChannel);
            }
        });
    }

    private void sendResponse(AsynchronousSocketChannel clientChannel, String responseMessage) {
        byte[] responseData;
        try {
            responseData = responseMessage.getBytes("EUC-KR");
        } catch (UnsupportedEncodingException e) {
            logger.error("Unsupported Encoding: ", e);
            closeClient(clientChannel);
            return;
        }

        ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
        lengthBuffer.putInt(responseData.length);
        lengthBuffer.flip();

        ByteBuffer dataBuffer = ByteBuffer.wrap(responseData);

        writeData(clientChannel, lengthBuffer, dataBuffer);
    }

    private void writeData(AsynchronousSocketChannel clientChannel, ByteBuffer lengthBuffer, ByteBuffer dataBuffer) {
        clientChannel.write(lengthBuffer, null, new CompletionHandler<Integer, Void>() {
            @Override
            public void completed(Integer result, Void attachment) {
                if (lengthBuffer.hasRemaining()) {
                    writeData(clientChannel, lengthBuffer, dataBuffer);
                } else {
                    // 길이 정보 전송 완료, 데이터 전송 시작
                    clientChannel.write(dataBuffer, null, new CompletionHandler<Integer, Void>() {
                        @Override
                        public void completed(Integer result, Void attachment) {
                            if (dataBuffer.hasRemaining()) {
                                writeData(clientChannel, lengthBuffer, dataBuffer);
                            } else {
                                // 응답 전송 완료
                                logger.info("Response sent to client.");
                                closeClient(clientChannel);
                            }
                        }

                        @Override
                        public void failed(Throwable exc, Void attachment) {
                            logger.error("Failed to send response data to client: ", exc);
                            closeClient(clientChannel);
                        }
                    });
                }
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                logger.error("Failed to send response length to client: ", exc);
                closeClient(clientChannel);
            }
        });
    }

    private void closeClient(AsynchronousSocketChannel clientChannel) {
        try {
            if (clientChannel.isOpen()) {
                clientChannel.close();
                logger.info("Client disconnected.");
            }
        } catch (IOException e) {
            logger.error("Error closing client channel: ", e);
        }
    }

    public void stop() {
        try {
            if (serverChannel != null && serverChannel.isOpen()) {
                serverChannel.close();
                logger.info("Server stopped.");
            }
            scheduler.shutdownNow();
        } catch (IOException e) {
            logger.error("Error closing server channel: ", e);
        }
    }

    public static void main(String[] args) {
        AsyncServer server = new AsyncServer();
        server.start();
    }
}
