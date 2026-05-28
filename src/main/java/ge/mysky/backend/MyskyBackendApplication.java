package ge.mysky.backend;

import ge.mysky.backend.config.MyskyProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackageClasses = MyskyProperties.class)
public class MyskyBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyskyBackendApplication.class, args);
    }
}
