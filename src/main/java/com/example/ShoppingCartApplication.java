package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class ShoppingCartApplication {

	public static void main(String[] args) {
		  Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
          System.setProperty("DB_USER", dotenv.get("DB_USER"));
          System.setProperty("DB_PASS", dotenv.get("DB_PASS"));
		SpringApplication.run(ShoppingCartApplication.class, args);
	}

}
