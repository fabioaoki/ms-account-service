package br.com.mechanic.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class MsAccountServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsAccountServiceApplication.class, args);
    }
}
