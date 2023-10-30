package ru.itmo.se.soa.lab2.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableWebMvc
@Configuration
@ComponentScan(basePackages = { "ru.itmo.se.soa.lab2.controller", "ru.itmo.se.soa.lab2.service" })
public class ClientWebConfig implements WebMvcConfigurer {
	
}
