package or.kr.formulate.korail.config;

import or.kr.formulate.korail.service.TcpServerMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.ip.tcp.TcpInboundGateway;
import org.springframework.integration.ip.tcp.TcpOutboundGateway;
import org.springframework.integration.ip.tcp.connection.*;
import org.springframework.integration.ip.tcp.serializer.ByteArrayCrLfSerializer;
import org.springframework.integration.ip.tcp.serializer.ByteArrayStxEtxSerializer;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

@Configuration
public class TcpClientConfig implements ApplicationEventPublisherAware {

    @Value("${integration.tcp.server.host}")
    private String host;

    @Value("${integration.tcp.client.port}")
    private int port;

    @Value("${integration.tcp.server.port}")
    private int serverPort;

    @Value(value = "${integration.tcp.client.connection.timeout}")
    private int connectionTimeout;

    @Value("${integration.tcp.client.connection.poolSize}")
    private int connectionPoolSize;

    @Value("${integration.max-message.size}")
    private int maxMessageSize;

    @Autowired
    private TcpServerMessageService tcpServerMessageService;

    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }


    @Bean
    public AbstractClientConnectionFactory integrationClientConnectionFactory() {
        TcpNioClientConnectionFactory tcpNioClientConnectionFactory = new TcpNioClientConnectionFactory(host, port);
        tcpNioClientConnectionFactory.setUsingDirectBuffers(true);
        tcpNioClientConnectionFactory.setSingleUse(true);
        tcpNioClientConnectionFactory.setSerializer(byteArrayCrLfSerializer());
        tcpNioClientConnectionFactory.setDeserializer(byteArrayCrLfSerializer());
        tcpNioClientConnectionFactory.setApplicationEventPublisher(applicationEventPublisher);
        return new CachingClientConnectionFactory(tcpNioClientConnectionFactory, connectionPoolSize);
    }

    @Bean
    public MessageChannel clientChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "outboundChannel")
    public MessageHandler outboundGateway(AbstractClientConnectionFactory integrationClientConnectionFactory) {
        TcpOutboundGateway tcpOutboundGateway = new TcpOutboundGateway();
        tcpOutboundGateway.setConnectionFactory(integrationClientConnectionFactory);
        tcpOutboundGateway.setRemoteTimeout(connectionTimeout);
        return tcpOutboundGateway;
    }

    @ServiceActivator(inputChannel = "inboundChannel", async = "true")
    public byte[] process(byte[] message) throws ParserConfigurationException, IOException, SAXException {
        return tcpServerMessageService.processMessage(message);
    }

    public ByteArrayCrLfSerializer byteArrayCrLfSerializer() {
        ByteArrayCrLfSerializer crLfSerializer = new ByteArrayCrLfSerializer();
        crLfSerializer.setMaxMessageSize(maxMessageSize);
        return crLfSerializer;
    }


    public ByteArrayStxEtxSerializer byteArrayStxEtxSerializer() {
//        ByteArrayCrLfSerializer crLfSerializer = new ByteArrayCrLfSerializer();
        ByteArrayStxEtxSerializer stxEtxSerializer = new ByteArrayStxEtxSerializer();
//        crLfSerializer.setMaxMessageSize(409600000);
//        return crLfSerializer;
        stxEtxSerializer.setMaxMessageSize(409600000);
        return stxEtxSerializer;
    }

    @Bean
    public AbstractServerConnectionFactory serverConnectionFactory() {
        TcpNioServerConnectionFactory serverConnectionFactory = new TcpNioServerConnectionFactory(serverPort);
        serverConnectionFactory.setSerializer(byteArrayStxEtxSerializer());
        serverConnectionFactory.setDeserializer(byteArrayStxEtxSerializer());
        serverConnectionFactory.setSingleUse(true);
        serverConnectionFactory.setUsingDirectBuffers(true);
        return serverConnectionFactory;
    }

    @Bean
    public MessageChannel inboundChannel() {
        return new DirectChannel();
    }

    @Bean
    public TcpInboundGateway inboundGateway(AbstractServerConnectionFactory serverConnectionFactory,
                                            MessageChannel inboundChannel) {
        TcpInboundGateway tcpInboundGateway = new TcpInboundGateway();
        tcpInboundGateway.setConnectionFactory(serverConnectionFactory);
        tcpInboundGateway.setRequestChannel(inboundChannel);
        return tcpInboundGateway;
    }
}
