package tk.tcomad.unibot.client.config;

import feign.codec.Encoder;
import feign.form.FormEncoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;

public class KeycloakClientConfig {

    @Bean
    Encoder feignFormEncoder(ObjectFactory<HttpMessageConverters> messageConverters) {
        return new FormEncoder(new SpringEncoder(messageConverters));
    }
}
