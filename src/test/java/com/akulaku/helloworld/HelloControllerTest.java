package com.akulaku.helloworld;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest(HelloController.class)
class HelloControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @Test
    void helloEndpointReturnsOkStatus() {
        assertThat(mvc.get().uri("/hello"))
            .hasStatusOk();
    }

    @Test
    void helloEndpointReturnsExpectedMessage() {
        assertThat(mvc.get().uri("/hello"))
            .hasStatusOk()
            .hasBodyTextEqualTo("Hello, World! Welcome to my first Spring Boot application for Akulaku SRE");
    }
}
