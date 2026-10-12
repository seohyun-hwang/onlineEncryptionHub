package com.example.encryptMsg;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"grpc.client.classifier.negotiation-type=PLAINTEXT",
		"spring.autoconfigure.exclude=net.devh.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration"
})
class EncryptMsgApplicationTests {

	@Test
	void contextLoads() {
	}

}
