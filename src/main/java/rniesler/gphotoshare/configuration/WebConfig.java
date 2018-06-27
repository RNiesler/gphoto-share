package rniesler.gphotoshare.configuration;

import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.Formatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.Base64;
import java.util.Locale;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("/webjars/");
        registry.addResourceHandler("/service-worker.js")
                .addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
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

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver lr = new AcceptHeaderLocaleResolver();
        lr.setDefaultLocale(Locale.US);
        return lr;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    @Bean
    public PushService pushService(@Value("${webpush.keys.public}") String vapidPublicKey,
                                   @Value("${webpush.keys.private}") String vapidPrivateKey,
                                   @Value("${app.admin.email") String adminEmail) throws GeneralSecurityException {
        Security.addProvider(new BouncyCastleProvider());
        return new PushService(vapidPublicKey, vapidPrivateKey, "mailto:" + adminEmail);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}