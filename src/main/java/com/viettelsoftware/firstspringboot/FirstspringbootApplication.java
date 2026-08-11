package com.viettelsoftware.firstspringboot;

import com.viettelsoftware.firstspringboot.entities.Task;
import com.viettelsoftware.firstspringboot.services.repositories.TaskRepository;
import lombok.val;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class FirstspringbootApplication {

	public static void main(String[] args) {
		SpringApplication.run(FirstspringbootApplication.class, args);
	}

}
