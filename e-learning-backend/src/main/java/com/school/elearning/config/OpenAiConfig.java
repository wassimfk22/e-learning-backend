package com.school.elearning.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.OpenAiApi;
import com.theokanning.openai.service.OpenAiService;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class OpenAiConfig {

    @Value("${openai.api.key}")
    private String apiKey;
    
    @Value("${openai.api.url}") // <--- Ajoute cette ligne pour lire l'URL d'Ollama
    private String apiUrl;

//    @Bean
//    public OpenAiService openAiService() {
//        return new OpenAiService(apiKey, Duration.ofSeconds(120));
//    }
    
    @Bean
    public OpenAiService openAiService() {
        ObjectMapper mapper = OpenAiService.defaultObjectMapper();
     // Dans OpenAiConfig.java
        OkHttpClient client = OpenAiService.defaultClient(apiKey, Duration.ofSeconds(600));

        // Ici on remplace l'URL par défaut d'OpenAI par ton URL Ollama
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(apiUrl) // Utilise ton http://localhost:11434/v1
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        OpenAiApi api = retrofit.create(OpenAiApi.class);
        return new OpenAiService(api);
    }
    
    
    
}