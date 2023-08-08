package org.rj.modelgen.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@ComponentScan(basePackages = "org.rj")
@RestController
public class CodegenServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CodegenServiceApplication.class, args);
	}

	@GetMapping("/echo/{value}")
	public String echo(
			@PathVariable(name="value") String value
	) {
		return value;
	}
}
