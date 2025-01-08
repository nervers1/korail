package or.kr.formulate.korail.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TcpServerMessageService {

    Logger logger = LoggerFactory.getLogger(TcpServerMessageService.class);

    public byte[] processMessage(byte[] message) {

        logger.info("Message: {}", message);
        return message;
    }
}
