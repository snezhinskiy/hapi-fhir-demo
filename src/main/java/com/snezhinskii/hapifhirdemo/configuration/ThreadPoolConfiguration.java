package com.snezhinskii.hapifhirdemo.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class ThreadPoolConfiguration {
    @Value("${hapi-fhir-executor.pool-size:10}")
    private int hfcTaskExecutorPoolSize;

    @Value("${hapi-fhir-executor.max-pool-size:15}")
    private int hfcTaskExecutorMaxPoolSize;

    @Bean(name = "hapiFhirTaskExecutor")
    public Executor productThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(hfcTaskExecutorPoolSize);
        executor.setMaxPoolSize(hfcTaskExecutorMaxPoolSize);
        executor.setThreadNamePrefix("hapiFhirTaskExecutor-");
        executor.initialize();
        return executor;
    }

}
