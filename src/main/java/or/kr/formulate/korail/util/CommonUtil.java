package or.kr.formulate.korail.util;

import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CommonUtil {

    private final static String LOCAL_MANUAL_PATH = "static/manuals/";

    public static String getMarkdownValueFormLocal(String manualPage) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        ClassPathResource classPathResource = new ClassPathResource(LOCAL_MANUAL_PATH + manualPage);

        BufferedReader br = Files.newBufferedReader(Paths.get(classPathResource.getURI()));
        br.lines().forEach(line -> stringBuilder.append(line).append("\n"));

        return stringBuilder.toString();
    }
}
