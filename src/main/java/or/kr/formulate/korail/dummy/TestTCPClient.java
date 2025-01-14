package or.kr.formulate.korail.dummy;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;

public class TestTCPClient {

    public static void main(String[] args) {

        String rootPath = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("")).getPath();
        System.out.println("rootPath ---> " + rootPath);

        String resource = "test.properties";
        String ifProperties = "interfaceId.properties";

        String appConfigPath = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(resource)).getPath();
        String ifConfigPath = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(ifProperties)).getPath();

        Properties properties = new Properties();
        Properties ifProp = new Properties();
        Properties appProp = new Properties();

        try (
                InputStream stream = TestTCPClient.class.getClassLoader().getResourceAsStream(resource);
                FileInputStream fis = new FileInputStream(ifConfigPath);
                FileInputStream appFis = new FileInputStream(appConfigPath)
        ) {
            properties.load(stream);
            ifProp.load(fis);
            appProp.load(appFis);

            System.out.println("----------------------[properties]----------------------------------");
            System.out.println("properties.str1 : " + properties.getProperty("str1"));
            System.out.println("properties.key1 : " + properties.getProperty("key1"));
            System.out.println("properties.test.type : " + properties.getProperty("test.type"));

            System.out.println("----------------------[interfaceId.properties]----------------------------------");
            System.out.println("ifProp.str1 : " + ifProp.getProperty("str1"));
            System.out.println("ifProp.key1 : " + ifProp.getProperty("key1"));
            System.out.println("ifProp.test.type : " + ifProp.getProperty("test.type"));
            System.out.println("----------------------[test.properties]----------------------------------");
            System.out.println("appProp.str1 : " + appProp.getProperty("str1"));
            System.out.println("appProp.key1 : " + appProp.getProperty("key1"));
            System.out.println("appProp.test.type : " + appProp.getProperty("test.type"));

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("----------------------[ResourceBundle]----------------------------------");
        ResourceBundle rb = ResourceBundle.getBundle("test");
        System.out.println("ResourceBundle.str1 : " + rb.getString("str1"));
        System.out.println("ResourceBundle.key1 : " + rb.getString("key1"));
        System.out.println("ResourceBundle.test.type : " + rb.getString("test.type"));
        System.out.println("--------------------------------------------------------");


        int length = 20;
        String str = "Hello World";
        String strNum = "123456789";
        String enStr;
        try {
            enStr = new String(str.getBytes(), "ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        System.out.println("enStr[" + enStr + "]");
//        String output = fill(length, str, "R", "EUC-KR", fillZero);
//        String output2 = fill(length, str, "l", "EUC-KR", fillZero);
//        String output3 = fill(length, strNum, "l", "UTF-8", true);
//        String output4 = fill(length, strNum, "r", "UTF-8", true);
//        System.out.println("[" + output + "]");
//        System.out.println("[" + output2 + "]");
//        System.out.println("[" + output3 + "]");
//        System.out.println("[" + output4 + "]");
        String outputStr = fillString(length, str, "UTF-8");
        String outputNumeric = fillNumeric(length, strNum, "UTF-8");
        System.out.println("outputStr    [" + outputStr + "]");
        System.out.println("outputNumeric[" + outputNumeric + "]");
    }


    public static String fill(int length, String str, String align, String charSet, boolean fillZero) {
        StringBuilder format = new StringBuilder();
        format.append('%');
        if ("L".equalsIgnoreCase(align)) {
            format.append('-');
        }
        format.append(length).append('s');
        String out = str;
        try {
            String formatedStr = String.format(format.toString(), new String(str.getBytes(), charSet));
            if (fillZero) {
                out = formatedStr.replace(' ', '0');
            } else {
                out = formatedStr;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return out;
    }

    public static String fillString(int length, String str, String charSet) {
        return fill(length, str, "L", charSet, false);
    }

    public static String fillNumeric(int length, String str, String charSet) {
        return fill(length, str, "R", charSet, true);
    }

    public static String send(String host, int port) {
        Socket socket = null;
        try {
            InetAddress Address = InetAddress.getByName("");
            socket = new Socket(Address, port);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }


    // 좌측 정렬 / 인코딩 UTF-8
    public String fillLengthUTF8Left(int length, String str, boolean fillZero) {
        StringBuilder padded = new StringBuilder(str);
        while (padded.toString().getBytes(StandardCharsets.UTF_8).length < length) {
            if (fillZero) {
                padded.append('0');
            } else {
                padded.append(' ');
            }
        }
        return padded.toString();
    }

    // 좌측 정렬 / 인코딩 EUC-KR
    public String fillLengthEUCKRLeft(int length, String str, boolean fillZero) throws UnsupportedEncodingException {
        StringBuilder padded = new StringBuilder(str);
        while (padded.toString().getBytes("EUC_KR").length < length) {
            if (fillZero) {
                padded.append('0');
            } else {
                padded.append(' ');
            }
        }
        return padded.toString();
    }

    // 우측 정렬 / 인코딩 UTF-8
    public String fillLengthUTF8Right(int length, String str, boolean fillZero) {
        StringBuilder padded = new StringBuilder();
        while (padded.length() < length - str.getBytes(StandardCharsets.UTF_8).length) {
            if (fillZero) {
                padded.append('0');
            } else {
                padded.append(' ');
            }
        }
        padded.append(str);
        return padded.toString();
    }

    // 우측 정렬 / 인코딩 EUC-KR
    public String fillLengthEUCKRRight(int length, String str, boolean fillZero) throws UnsupportedEncodingException {
        StringBuilder padded = new StringBuilder();
        while (padded.length() < length - str.getBytes("EUC_KR").length) {
            if (fillZero) {
                padded.append('0');
            } else {
                padded.append(' ');
            }
        }
        padded.append(str);
        return padded.toString();
    }


    public static void testStr() {
        /*
         * "왼쪽"에서부터 "공백" 문자열을 채워넣습니다.
         * 1. 해당 예시는 왼쪽에서부터 지정한 문자열을 포함하여 총 5개의 문자열 개수가 만들어지는데 지정한 문자열을 제외한 공백으로 이를 채웁니다.
         * 2. 해당 예시에는 공백을 다시 "z"라는 문자열로 바꿉니다.
         */
        String lPadStr = "abc";
        lPadStr = String.format("%5s", lPadStr).replace(" ", "z");  // zzabc

        /*
         * "오른쪽"에서부터 "공백" 문자열을 채워넣습니다.
         * 1. 해당 예시는 오른쪽에서부터 지정한 문자열을 포함하여 총 5개의 문자열 개수가 만들어지는데 지정한 문자열을 제외한 공백으로 이를 채웁니다.
         * 2. 해당 예시에서는 공백을 다시 "z"라는 문자열로 바꿉니다.
         */
        String rPadStr = "abc";
        rPadStr = String.format("%-5s", rPadStr).replace(" ", "z"); // abczz

        int length = 20;
        String testStr = "ABC";
        String testNum = "123";
        String strAlign = "r";
        StringBuilder format = new StringBuilder();
        format.append('%');
        if ("L".equalsIgnoreCase(strAlign)) {
            format.append('-');
        }
        format.append(length).append('s');
        System.out.println("[" + format.toString() + "]");
        System.out.println("[" + String.format(format.toString(), testStr) + "]");
        System.out.println("[" + String.format(format.toString(), testNum).replace(' ', '0') + "]");


    }
}
