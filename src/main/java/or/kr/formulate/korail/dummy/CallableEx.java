package or.kr.formulate.korail.dummy;

import java.time.Duration;
import java.time.LocalTime;
import java.util.concurrent.Callable;

public class CallableEx implements Callable<String> {
    private int idx;

    CallableEx(int idx) {
        this.idx = idx;
    }

    @Override
    public String call() {
        /*
        현재 쓰레드명과 idx와 작업 수행동안 총 걸린 시간값 정보를 더해 문자열로 반환
         */
        LocalTime startTime = LocalTime.now();
        System.out.println("Thread: " + Thread.currentThread().getName() + ", call idx: " + idx + ", startTime: " + startTime);
        LocalTime endTime = LocalTime.now();
        return "idx: " + idx + ", duration: " + Duration.between(startTime, endTime).toMillis();
    }
}
