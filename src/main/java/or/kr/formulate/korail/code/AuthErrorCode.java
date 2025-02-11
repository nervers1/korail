package or.kr.formulate.korail.code;

public enum AuthErrorCode {
    ERR_0000("0000", "SUCCESS"),
    ERR_0011("0011", "인증 식별 코드 오류"),
    ERR_0012("0012", "전문타입오류"),
    ERR_0013("0013", "전문길이오류"),
    ERR_0014("0014", "데이터 오류"),
    ERR_0015("0015", "STX / ETX 오류"),
    ERR_1001("1001", "HSM 접속 실패"),
    ERR_1002("1002", "HSM Key 처리 중 오류"),
    ERR_1003("1003", "키 버전 오류"),
    ERR_1004("1004", "MAC가일치하지않음"),
    ERR_1005("1005", "내부 암호화 모듈 오류"),
    ERR_1101("1101", "지원하지않는암호화모드 "),
    ERR_1401("1401", "내부네트워크오류 "),
    ERR_2011("2011", "Sign1 불일치"),
    ERR_2012("2012", "Sign2 불일치"),
    ERR_2013("2013", "Sign3 불일치"),
    ERR_2014("2014", "SignIND 불일치"),
    ERR_2015("2015", "SignIND2 불일치"),
    ERR_4001("4001", "네트워크 오류"),
    ERR_5001("5001", "시스템 파일 핸들링 도중에 에러 발생"),
    ERR_5011("5011", "시스템이 기동되고 있지 않습니다"),
    ERR_5101("5101", "인증서버 기동중 - 잠시후 재시도"),
    ERR_8000("8000", "제공되지 않는 기능입니다.");

    private final String code;
    private final String desc;

    private AuthErrorCode(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
