package ca.mcgill.ecse321.opls;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableWebMvc
public class OplsApplication {
	
	@Autowired
	private OplsStartupService startupService;
	
	@Bean
	public InitializingBean initDatabase() {
		return () -> {
			String isTestMode = System.getenv("opls_test_mode");
			if (isTestMode == null || !Boolean.parseBoolean(isTestMode)) {
				startupService.startupBoot();
			} else {
				startupService.startupTest();
			}
		};
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(OplsApplication.class, args);
	}

}
