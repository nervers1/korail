package or.kr.formulate.korail.config;

import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.stereotype.Component;

@Component
@MessagingGateway(defaultReplyChannel = "outboundChannel")
public interface IntegrationTcpClientGateway {

    byte[] send(byte[] message);
}
