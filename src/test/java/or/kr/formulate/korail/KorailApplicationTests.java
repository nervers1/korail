package or.kr.formulate.korail;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class KorailApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void createNewThread() {
        Runnable runnable = new NewState();
        Thread t = new Thread(runnable);
        System.out.println(t.getState());
    }

}
