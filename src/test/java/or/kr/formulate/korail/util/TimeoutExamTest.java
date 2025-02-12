package or.kr.formulate.korail.util;

import or.kr.formulate.korail.dummy.timeout.TimeoutExam;
import org.junit.jupiter.api.Test;

public class TimeoutExamTest {
    @Test
    public void test() {
        TimeoutExam exam = new TimeoutExam();
        exam.invoke();

    }
}
