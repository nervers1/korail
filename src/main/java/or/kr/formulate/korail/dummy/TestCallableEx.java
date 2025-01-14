package or.kr.formulate.korail.dummy;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/*
10개의 가용가능한 쓰레드를 쓰레드 풀에 생성하고,
총 10개의 테스크를 동시에 모두 실행하도록 테스트를 정의했습니다.
--> 총 가용가능한 10개의 쓰레드가 각각의 작업을 할당하여 수행함을 확인
 */
public class TestCallableEx {

    @Test
    void callable_void() throws InterruptedException, ExecutionException {

        int taskNum = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(taskNum);

        List<CallableEx> callableExList = new ArrayList<>();
        for (int i = 0; i < taskNum; i++) {
            callableExList.add(new CallableEx(i + 1));
        }

        System.out.println("-----작업 실행-----");
        List<Future<String>> futures = executorService.invokeAll(callableExList);
        System.out.println("-----작업 종료-----");
        System.out.println("-----결과 출력-----");
        for (Future<String> future : futures) {
            System.out.println(future.get());
        }
        executorService.shutdown();

    }
}

