package kodanect;

import kodanect.common.config.GlobalsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

//@ConfigurationPropertiesScan
@SpringBootApplication
@EnableConfigurationProperties(GlobalsProperties.class)
public class KodanectBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(KodanectBootApplication.class, args);
	}

}
