package or.kr.formulate.korail.dummy;

public class JoinTest {

    public static void main(String[] args) {

        System.out.println("메인쓰레드 시작!");
        Thread1 thread1 = new Thread1();
        thread1.start();
        long startTime = System.currentTimeMillis(); // 시작시간

        try {
            thread1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // thread1의 작업이 끝난 뒤 출력
        System.out.println("\n소요시간: " + (System.currentTimeMillis() - startTime));
    }
}

class Thread1 extends Thread {
    @Override
    public void run() {

        System.out.println("Thread1 시작!");
        for (int i = 0; i < 200; i++) {
            System.out.print("-");
            try {
                Thread.sleep(50); // 50 milli seconds 일시정지
            } catch (InterruptedException e) {
                interrupt();
            }
        }
    }
}