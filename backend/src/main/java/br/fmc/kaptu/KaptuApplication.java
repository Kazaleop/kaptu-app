package br.fmc.kaptu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KaptuApplication {

	public static void main(String[] args) {
		SpringApplication.run(KaptuApplication.class, args);
	}

}
