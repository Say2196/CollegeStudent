package com.example.demo.Config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
@Configuration
public class FaultToleranceConfg {


    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry(){
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50.00f)
                .waitDurationInOpenState(Duration.ofMillis(10))
                .slidingWindowSize(20).build();

        return CircuitBreakerRegistry.of(config);

    }



}
