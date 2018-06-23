package rniesler.gphotoshare.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.Formatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Base64;
import java.util.Locale;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/webjars/**")
                .addResourceLocations("/webjars/");
    }

    @Override
    public void addFormatters(final FormatterRegistry registry) {
        registry.addFormatter(base64BytesFormatter());
    }

    @Bean
    Formatter<byte[]> base64BytesFormatter() {
        return new Formatter<>() {
            @Override
            public byte[] parse(String base64, Locale locale) {
                return Base64.getDecoder().decode(base64);
            }

            @Override
            public String print(byte[] bytes, Locale locale) {
                return Base64.getEncoder().encodeToString(bytes);
            }
        };
    }
}