package or.kr.formulate.korail.util.cms;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AsyncNio {

    AsynchronousChannelGroup channelGroup;
    AsynchronousServerSocketChannel asynchronousServerSocketChannel;


    // 서버 소켓 초기화
    private void init() throws IOException {
        try {
            // 채널그룹
            channelGroup = AsynchronousChannelGroup.withFixedThreadPool(Runtime.getRuntime().availableProcessors(), Executors.defaultThreadFactory());
            // 채널 생성 (비동기 서버소켓)
            asynchronousServerSocketChannel = AsynchronousServerSocketChannel.open(channelGroup);
            // Inet Address
            InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 80);
            // 채널 바인딩
            asynchronousServerSocketChannel.bind(inetSocketAddress);
        } catch (IOException e) {
            // exceptions!
        } finally {
            System.out.println("초기화 성공");
        }
    }

    // 서버 기동
    public void startServer() throws IOException {
        init();
        start();
    }

    // 수신 가능 상태를 만들기 위해 accept() 호출
    // 수신 데이터 읽기 작업을 핸들러에 위임
    private void start() {
        try {
            asynchronousServerSocketChannel.accept(null, acceptCompletionHandler(null, asynchronousServerSocketChannel));
        } catch (Exception e) {
            //
        }
    }

    // 수신 데이터 읽기
    private CompletionHandler<AsynchronousSocketChannel, Void> acceptCompletionHandler(String att, AsynchronousServerSocketChannel asynchronousServerSocketChannel) {
        return new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel channel, Void attachment) {
                // 버퍼용량
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);

                // 채널로부터 버퍼사이즈만큼 데이터를 읽는다.
                channel.read(readBuffer, 60, TimeUnit.SECONDS, attachment, channelReadHandler(readBuffer, channel));
                // flip() 과 유사한 버퍼 클리어
                readBuffer.rewind();
                // 다음 버퍼를 처리하기 위해 재귀호출
                asynchronousServerSocketChannel.accept(null, acceptCompletionHandler(null, asynchronousServerSocketChannel));
            }

            // 읽기 작업 실패 처리
            @Override
            public void failed(Throwable exc, Void attachment) {
            }
        };
    }

    // 채널 정보 읽기
    private CompletionHandler<Integer, Void> channelReadHandler(ByteBuffer readBuffer, AsynchronousSocketChannel asynchronousSocketChannel) {
        return new CompletionHandler<Integer, Void>() {
            @Override
            public void completed(Integer result, Void attachment) {
                // 버퍼용량
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

                try {
                    // biz ....
                } catch (Exception e) {
                    //HTTP 500 error
                } finally {
                    // 데이터 읽기 작업이 끝난 후 쓰기 작업 수행
                    asynchronousSocketChannel.write(byteBuffer, 60, TimeUnit.SECONDS, attachment, channelWriteHandler(byteBuffer, asynchronousSocketChannel));
                    byteBuffer.rewind();
                }
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                //HTTP 503 error
            }
        };
    }

    // 채널 정보 쓰기
    private CompletionHandler<Integer, Void> channelWriteHandler(ByteBuffer writeBuffer, AsynchronousSocketChannel asynchronousSocketChannel) {
        return new CompletionHandler<Integer, Void>() {

            // 쓰기작업 완료 처리
            @Override
            public void completed(Integer result, Void attachment) {

                try {
                    // biz ....
                    // 오류가 발생하면
                    boolean condition = false;
                    if (false) {
                        System.out.println("error!");
                        throw new IOException("썅!");
                    }
                } catch (IOException e) {
                    //
                }
            }

            // 쓰기 작업 실패 처리
            @Override
            public void failed(Throwable exc, Void attachment) {
                try {
                    close();
                } catch (IOException e) {
                }
            }
        };
    }

    // graceful shutdown
    private void close() throws IOException {
        asynchronousServerSocketChannel.close();
        if (channelGroup != null) channelGroup.shutdown();
        channelGroup = null;
    }
}