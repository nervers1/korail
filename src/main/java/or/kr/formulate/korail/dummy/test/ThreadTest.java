package or.kr.formulate.korail.dummy.test;

import javax.swing.*;

public class ThreadTest extends Thread {


    public static void main(String[] args) {

        ThreadTest newThread = new ThreadTest();
        newThread.start();
        System.out.println(newThread.isInterrupted()); // false
        String input = JOptionPane.showInputDialog("아무 값이나 입력하세요");
        System.out.println("입력 값은 :" +input+"입니다.");
        newThread.interrupt(); // false -> true 변경
        System.out.println(newThread.isInterrupted()); // true

    }

    @Override
    public void run() {
        for (int i=10; i>=0; i--) {
            if (isInterrupted()){ // true면 break
                break;
            }
            System.out.println("카운트 :" +i);
            try {
                Thread.sleep(2000); // 2초간 일시정지
            } catch (InterruptedException e) {
                interrupt();
            }
        }
        System.out.println("카운트가 종료되었습니다.");
    }
}
