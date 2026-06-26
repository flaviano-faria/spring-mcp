package com.mcp.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.mcp")
public class SpringMcpApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringMcpApplication.class, args);
    }

}
