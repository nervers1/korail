package or.kr.formulate.korail.exception;

public class StringUtilException extends RuntimeException {
    public StringUtilException(String message) {
        super(message);
    }
    public StringUtilException(String message, Throwable cause) { super(message, cause); }
}
