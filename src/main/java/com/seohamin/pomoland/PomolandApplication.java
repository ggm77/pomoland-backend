package com.seohamin.pomoland;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing // updatedAt컬럼을 위해서 추가
public class PomolandApplication {

	public static void main(String[] args) {
		SpringApplication.run(PomolandApplication.class, args);
	}

}
