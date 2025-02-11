package or.kr.formulate.korail.exception;

import or.kr.formulate.korail.code.ResponseCode;

public class EAIException extends RuntimeException {
    private ResponseCode code;
    public ResponseCode getCode() {return code;}
    public EAIException(String message) {super(message);}
    public EAIException(String message, Throwable cause) { super(message, cause); }
    public EAIException(String message, ResponseCode code) { super(message); this.code = code; }
    public EAIException(String message, ResponseCode code, Throwable cause) { super(message, cause); this.code = code; }
}
