package rniesler.gphotoshare.security.impl;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

public class GoogleApiResponseErrorHandlerTest {

    private MockRestServiceServer server;
    private RestTemplate restTemplate;

    public GoogleApiResponseErrorHandlerTest() {
        restTemplate = new RestTemplateBuilder()
                .errorHandler(new GoogleApiResponseErrorHandler())
                .build();
        server = MockRestServiceServer.bindTo(restTemplate).build();
    }

    @Test
    public void test404Error() {
        String testUrl = "http://testurl";

        server
                .expect(ExpectedCount.once(), requestTo(testUrl))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThrows(GoogleApiException.class, () -> restTemplate.getForObject(testUrl, String.class));
        server.verify();
    }
}
