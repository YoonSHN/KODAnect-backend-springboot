package kodanect;

import kodanect.common.config.GlobalsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

//@ConfigurationPropertiesScan
@SpringBootApplication
@EnableConfigurationProperties(GlobalsProperties.class)
@EnableScheduling
public class KodanectBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(KodanectBootApplication.class, args);
	}

}
