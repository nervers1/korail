package or.kr.formulate.korail.dummy;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TestTCPClient {

    public static void main(String[] args) {

        int length = 20;
        String str = "Hello World";
        String strNum = "123456789";
        String enStr = null;
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
