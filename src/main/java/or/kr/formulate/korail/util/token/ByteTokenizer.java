package or.kr.formulate.korail.util.token;

import java.util.Enumeration;
import java.util.NoSuchElementException;

public class ByteTokenizer implements Enumeration<Object> {

    private int currentPosition;
    private int newPosition;
    private int maxPosition;

    private byte[] bytes;
    private byte[] delimiters;

    /**
     * 생성자 constructor - ByteTokenizer
     * @param bytes 데이터
     * @param delimiters 딜리미터
     */
    public ByteTokenizer(byte[] bytes, byte[] delimiters) {
        this.bytes = bytes;
        this.delimiters = delimiters;
        currentPosition = 0;
        newPosition = -1;
        maxPosition = bytes.length;
    }

    /**
     * 생성자 constructor - ByteTokenizer
     * @param bytes 데이터
     * @param delimiter 딜리미터
     */
    public ByteTokenizer(byte[] bytes, byte delimiter) {
        this(bytes, new byte[] { delimiter });
    }

    /**
     * 토큰이 남아있는지 체크
     * @return 남아있는 토큰 존재여부: boolean
     */
    public boolean hasMoreTokens() {
        newPosition = skipDelimiters(currentPosition);
        return (newPosition < maxPosition);
    }

    /**
     * 엘리먼트가 더 남아있는지 체크
     * @return 엘리먼트 존재여부 : boolean
     */
    public boolean hasMoreElements() {
        return hasMoreTokens();
    }

    /**
     * 토큰 이터레이션 : 다음 토큰 반환
     * @return Object 다음 토큰
     */
    public Object nexToken() {
        currentPosition = (newPosition >= 0) ? newPosition
                : skipDelimiters(currentPosition);

        // Reset
        newPosition = -1;

        if (currentPosition >= maxPosition) {
            throw new NoSuchElementException(
                    "current token position is out of bound.");
        }
        final int startPosition = currentPosition;
        currentPosition = scanToken(currentPosition);
        final int length = currentPosition - startPosition;
        final byte[] token = new byte[length];
        System.arraycopy(bytes, startPosition, token, 0, length);
        return token;
    }

    /**
     * 다음 요소 반환
     * @return Object 다음 요소 반환
     */
    public Object nextElement() {
        return nexToken();
    }

    /**
     * 전체 토큰 갯수 반환
     * @return 토큰 수: int
     */
    public int countTokens() {
        int tokenNums = 0;
        int position = currentPosition;
        while (position < maxPosition) {
            position = skipDelimiters(position);
            if (position >= maxPosition) {
                break;
            }
            position = scanToken(position);
            tokenNums++;
        }
        return tokenNums;
    }

    /**
     * 딜리미터 스킵
     * @param startPosition 시작위치 : int
     * @return 시작위치 이후에 존재하는 딜리미터가 아닌 바이트 : int
     */
    private int skipDelimiters(final int startPosition) {
        if (delimiters == null) {
            throw new NullPointerException("delimiters");
        }
        int position = startPosition;
        while (position < maxPosition) {
            if (isDelimiter(position)) {
                position += delimiters.length;
            }
            break;
        }
        return position;
    }

    private int scanToken(final int startPosition) {
        int position = startPosition;
        while (position < maxPosition) {
            if (isDelimiter(position)) {
                break;
            }
            position++;
        }
        return position;
    }

    private boolean isDelimiter(final int startPosition) {
        int position = startPosition;
        boolean isDelimiter = true;
        while ((position < maxPosition) && isDelimiter) {
            int nDelimiter = 0;
            while (nDelimiter < delimiters.length) {
                if (bytes[startPosition + nDelimiter] != delimiters[nDelimiter]) {
                    isDelimiter = false;
                    break;
                }
                nDelimiter++;
            }
            position++;
        }
        return isDelimiter;
    }
}