package or.kr.formulate.korail.service;

public class ThreadTest  implements Runnable {


    public static void main(String[] args) {
        Runnable runnable = new NewState();
        Thread t = new Thread(runnable);
        System.out.println(t.getState());
    }
    public void testThreadCreations() {


    }

    @Override
    public void run() {
        Runnable runnable = new NewState();
        Thread t = new Thread(runnable);
        System.out.println(t.getState());
    }
}
