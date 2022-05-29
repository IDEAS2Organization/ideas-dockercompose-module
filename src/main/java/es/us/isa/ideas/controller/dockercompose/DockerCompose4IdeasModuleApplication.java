package es.us.isa.ideas.controller.dockercompose;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class DockerCompose4IdeasModuleApplication 
    extends SpringBootServletInitializer {
    public static void main(String[] args){
        SpringApplication.run(DockerCompose4IdeasModuleApplication.class,args);
    }
    
    @Override
    protected SpringApplicationBuilder configure(
        SpringApplicationBuilder builder
    ) {
        return builder.sources(DockerCompose4IdeasModuleApplication.class);
    }
}