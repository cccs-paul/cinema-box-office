package com.boxoffice.config;

import com.boxoffice.service.ResponsibilityCentreService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ResponsibilityCentreService rcService;

    @Override
    public void run(String... args) throws Exception {
        rcService.initDemoRC();
    }
}
