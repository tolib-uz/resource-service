package uz.mservice.resource.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class ResourceConfig {

    /*@Value("$SONG_SERVICE_HOST")
    private String baseURL; */
    @Bean
    public RestTemplate restTemplate(){

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory("http://localhost:8088"));
        return restTemplate;
    }
}
