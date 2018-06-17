package rniesler.gphotoshare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.ReactiveOAuth2ClientAutoConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({ReactiveOAuth2ClientAutoConfiguration.class})
public class GphotoShareApplication {

	public static void main(String[] args) {
		SpringApplication.run(GphotoShareApplication.class, args);
	}
}
