package io.hpb.pay;

import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import io.hpb.pay.common.SpringBootContext;

@SpringBootApplication
public class HpbPayComponentApplication {

	public static void main(String[] args) {
		System.setProperty("spring.devtools.restart.enabled", "false");
		SpringApplication springApplication = new SpringApplication(HpbPayComponentApplication.class);
		springApplication.setAddCommandLineProperties(false);
		springApplication.setBannerMode(Banner.Mode.OFF);
		ApplicationContext aplicationContext = springApplication.run(args);
		SpringBootContext.setAplicationContext(aplicationContext);
	}
	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {
			if (SpringBootContext.getAplicationContext() == null) {
				SpringBootContext.setAplicationContext(ctx);
			}
		};
	}
}
