package or.kr.formulate.korail.config;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;

//@MessagingGateway
public interface TcpGateway {


    @Gateway(requestChannel = "clientChannel")
    byte[] send(byte[] data);
}
