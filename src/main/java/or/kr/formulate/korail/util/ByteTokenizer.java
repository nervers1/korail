package or.kr.formulate.korail.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * byte tokenizer는 byte buffer를 토큰으로 쪼갠다.
 * java.util.StringTokenizer 의 구현체를 수정하여 제작하였다.
 * 딜리미터로 사용된 문자는 0 - 255 범위의 캐릭터 문자로 간주한다.
 * 딜리미터와 바이트 배열을 매칭시키기 위해 ISO8859-1 encoding이 사용되었다.
 */
public class ByteTokenizer implements Iterator<String> {
    private int currentPosition;
    private int newPosition;
    private final int maxPosition;
    private final byte[] data;
    private String delimiters;
    private final boolean retDelims;
    private boolean delimsChanged;
    private String encoding;

    private static final Logger logger = LoggerFactory.getLogger(ByteTokenizer.class);

    /**
     * maxDelimChar 딜리미터 문자의 최대 값을 저장한다(delimiter character with the highest value). 이는 딜리미터 문자를 최적화하는데 사용한다..
     */
    private char maxDelimChar;

    /**
     * delimiter set에 maxDelimChar를 세팅한다..
     */
    private void setMaxDelimChar() {
        if (delimiters == null) {
            maxDelimChar = 0;
            return;
        }

        char m = 0;
        for (int i = 0; i < delimiters.length(); i++) {
            char c = delimiters.charAt(i);
            if (m < c) {
                m = c;
            }
        }
        maxDelimChar = m;
    }

    /**
     * ByteTokenizer의 생성자 : byte array를 <code>delim</code> argument로 입력받은 딜리미터로 토큰을 나눈다.
     * 딜리미터에 사용되는 문자는 0 - 255 사이의 문자코드로 이루어 진다..
     * <p>
     * 만일 <code>returnDelims</code> 플래그를 <code>true</code>로 설정하면 delimiter character도 토큰으로 반환된다.
     * 각 딜리미터는 byte[] 나 해당 길이의 String으로 되어있다.
     * <code>false</code>로 지정하면 delimiter로 사용된 character들을 제외한 토큰들만 반환된다.
     *
     * @param bytes        파싱하는 byte array
     * @param start        배열의 첫 byte(offset)
     * @param len          파싱할 바이트 수(length)
     * @param delim        the delimiters.
     * @param returnDelims 반환되는 토큰에 delimiters가 포함되는지 여부.
     */
    public ByteTokenizer(byte[] bytes, int start, int len, String delim, boolean returnDelims) {
        currentPosition = start;
        newPosition = -1;
        delimsChanged = false;
        data = bytes;
        maxPosition = start + len;
        delimiters = delim;
        retDelims = returnDelims;
        setMaxDelimChar();
    }

    /**
     * byte tokenizer 생성자 : 특정 인코딩을 사용하는 경우
     *
     * @param bytes        a byte array to be parsed.
     * @param start        the first byte in the array
     * @param len          the number of bytes to parse
     * @param delim        the delimiters.
     * @param returnDelims flag indicating whether to return the delimiters as tokens.
     * @param encoding     the encoding for which to return the bytes.
     * @throws UnsupportedEncodingException thrown if the supplied encoding is unsupported.
     */
    public ByteTokenizer(byte[] bytes, int start, int len, String delim, boolean returnDelims, String encoding) throws UnsupportedEncodingException {
        this(bytes, start, len, delim, returnDelims);
        try {
            Charset c = Charset.forName(encoding);
            logger.debug("Loaded charset {}", c);
        } catch (IllegalArgumentException ex) {
            throw new UnsupportedEncodingException(ex.toString());
        }
        this.encoding = encoding;
    }

    /**
     * byte tokenizer 생성자 : Delimiter가 token에서 제외되는 경우.
     *
     * @param bytes a byte array to be parsed.
     * @param start the first byte in the array
     * @param len   the number of bytes to parse
     * @param delim the delimiters.
     */
    public ByteTokenizer(byte[] bytes, int start, int len, String delim) {
        this(bytes, start, len, delim, false);
    }

    /**
     * byte tokenizer 생성자 : 특정 인코딩을 사용하는 경우
     *
     * @param bytes    a byte array to be parsed.
     * @param start    the first byte in the array
     * @param len      the number of bytes to parse
     * @param delim    the delimiters.
     * @param encoding the encoding for which to return the bytes.
     * @throws UnsupportedEncodingException thrown if the supplied encoding is unsupported.
     */
    public ByteTokenizer(byte[] bytes, int start, int len, String delim, String encoding) throws UnsupportedEncodingException {
        this(bytes, start, len, delim, false, encoding);
    }

    /**
     * byte tokenizer 생성자 : 특정 문자 <code>"&nbsp;&#92;t&#92;n&#92;r&#92;f"</code>: 공백문자, 탭, 개행문자,
     * 캐리지리턴문자, 폼피드문자 가 딜리미터로 사용되는 경우
     *
     * @param bytes a byte array to be parsed.
     * @param start the first byte in the array
     * @param len   the number of bytes to parse
     */
    public ByteTokenizer(byte[] bytes, int start, int len) {
        this(bytes, start, len, " \t\n\r\f", false);
    }

    /**
     * byte tokenizer 생성자 : 딜리미터 아규먼트가 0 - 255 사이의 문자코드를 가지는 경우.
     * <p>
     *
     * @param bytes        a byte array to be parsed.
     * @param delim        the delimiters.
     * @param returnDelims flag indicating whether to return the delimiters as tokens.
     */
    public ByteTokenizer(byte[] bytes, String delim, boolean returnDelims) {
        this(bytes, 0, bytes.length, delim, returnDelims);
    }

    /**
     * byte tokenizer 생성자 : 특정 인코딩을 사용하는 경우
     *
     * @param bytes        a byte array to be parsed.
     * @param delim        the delimiters.
     * @param returnDelims flag indicating whether to return the delimiters as tokens.
     * @param encoding     the encoding for which to return the bytes.
     * @throws UnsupportedEncodingException thrown if the supplied encoding is unsupported.
     */
    public ByteTokenizer(byte[] bytes, String delim, boolean returnDelims, String encoding) throws UnsupportedEncodingException {
        this(bytes, 0, bytes.length, delim, returnDelims, encoding);
    }

    /**
     * byte tokenizer 생성자 : 특정 딜리미터로 구성된 바이트 배열 전체를 토큰으로 끊어내는 경우
     *
     * @param bytes a byte array to be parsed.
     * @param delim the delimiters.
     */
    public ByteTokenizer(byte[] bytes, String delim) {
        this(bytes, 0, bytes.length, delim);
    }

    /**
     * byte tokenizer 생성자 : 특정 딜리미터로 구성된 특정 인코딩으로 구성된 바이트 배열 전체를 토큰으로 끊어내는 경우
     *
     * @param bytes    a byte array to be parsed.
     * @param delim    the delimiters.
     * @param encoding the encoding for which to return the bytes.
     * @throws UnsupportedEncodingException thrown if the supplied encoding is unsupported.
     */
    public ByteTokenizer(byte[] bytes, String delim, String encoding) throws UnsupportedEncodingException {
        this(bytes, 0, bytes.length, delim, encoding);
    }

    /**
     * byte tokenizer 생성자 : 딜리미터로 특정 문자 <code>"&nbsp;&#92;t&#92;n&#92;r&#92;f"</code>: 공백문자, 탭, 개행문자,
     * 캐리지리턴문자, 폼피드문자 가 딜리미터로 사용되는 경우
     *
     * @param bytes a byte array to be parsed.
     */
    public ByteTokenizer(byte[] bytes) {
        this(bytes, 0, bytes.length);
    }

    /**
     * 특정 위치에서부터 시작되는 딜리미터를 Skips 한다.
     * */
    private int skipDelimiters(int startPos) {
        if (delimiters == null) {
            throw new NullPointerException();
        }

        int position = startPos;
        while (!retDelims && position < maxPosition) {
            char c = (char) (0xFF & data[position]);
            if ((c > maxDelimChar) || (delimiters.indexOf(c) < 0)) {
                break;
            }
            position++;
        }
        return position;
    }

    /**
     * 시작 위치까지지의 바이트를 스킵하고 이후부터 딜리미터를 탐색하여 발견된 토큰의 시작 index를 반환, 탐색에 시패할 경우 maxPosition를 반환
     */
    private int scanToken(int startPos) {
        int position = startPos;
        while (position < maxPosition) {
            char c = (char) (0xFF & data[position]);
            if ((c <= maxDelimChar) && (delimiters.indexOf(c) >= 0)) {
                break;
            }
            position++;
        }
        if (retDelims && (startPos == position)) {
            char c = (char) (0xFF & data[position]);
            if ((c <= maxDelimChar) && (delimiters.indexOf(c) >= 0)) {
                position++;
            }
        }
        return position;
    }

    /**
     * Tests if there are more tokens available from this tokenizer's string. If this method returns <code>true</code>, then
     * a subsequent call to <code>next</code> with no argument will successfully return a token.
     *
     * @return <code>true</code> if and only if there is at least one token in the string after the current position;
     * <code>false</code> otherwise.
     */
    @Override
    public boolean hasNext() {
        /*
         * Temporary store this position and use it in the following next() method only if the delimiters haven't been changed
         * in that next() invocation.
         */
        newPosition = skipDelimiters(currentPosition);
        return (newPosition < maxPosition);
    }

    /**
     * Returns the next token from this string tokenizer.
     *
     * @return the next token from this string tokenizer.
     * @throws NoSuchElementException if there are no more tokens in this tokenizer's string.
     */
    @Override
    public String next() {
        /*
         * If next position already computed in hasMoreElements() and delimiters have changed between the computation and this
         * invocation, then use the computed value.
         */

        currentPosition = (newPosition >= 0 && !delimsChanged) ? newPosition : skipDelimiters(currentPosition);

        /* Reset these anyway */
        delimsChanged = false;
        newPosition = -1;

        if (currentPosition >= maxPosition) {
            throw new NoSuchElementException();
        }
        int start = currentPosition;
        currentPosition = scanToken(currentPosition);

        String token = null;
        try {
            if (encoding != null) {
                token = new String(data, start, currentPosition - start, encoding);
            } else {
                token = new String(data, start, currentPosition - start);
            }
        } catch (UnsupportedEncodingException uee) {
            // cannot happen...we already verified in constructor
        }
        return token;
    }

    /**
     * Returns the next token in this string tokenizer's string. First, the set of characters considered to be delimiters by
     * this <code>ByteTokenizer</code> object is changed to be the characters in the string <code>delim</code>. Then the
     * next token in the string after the current position is returned. The current position is advanced beyond the
     * recognized token. The new delimiter set remains the default after this call.
     *
     * @param delim the new delimiters.
     * @return the next token, after switching to the new delimiter set.
     * @throws NoSuchElementException if there are no more tokens in this tokenizer's string.
     */
    public String next(String delim) {
        delimiters = delim;

        /* delimiter string specified, so set the appropriate flag. */
        delimsChanged = true;

        setMaxDelimChar();
        return next();
    }

    /**
     * Calculates the number of times that this tokenizer's <code>next</code> method can be called before it generates an
     * exception. The current position is not advanced.
     *
     * @return the number of tokens remaining in the string using the current delimiter set.
     * @see ByteTokenizer#next()
     */
    public int countTokens() {
        int count = 0;
        int currpos = currentPosition;
        while (currpos < maxPosition) {
            currpos = skipDelimiters(currpos);
            if (currpos >= maxPosition) {
                break;
            }
            currpos = scanToken(currpos);
            count++;
        }
        return count;
    }
}