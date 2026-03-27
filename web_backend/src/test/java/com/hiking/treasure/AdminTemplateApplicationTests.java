package com.hiking.treasure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.quartz.scheduler.enabled=false")
class AdminTemplateApplicationTests {

    @Test
    void contextLoads() {
    }

}
