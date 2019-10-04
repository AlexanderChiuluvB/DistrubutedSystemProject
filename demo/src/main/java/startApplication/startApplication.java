package startApplication;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@EnableKafka
@EnableTransactionManagement
@SpringBootApplication
public class startApplication {
    public static void main(String[] args) {
        SpringApplication.run(startApplication.class, args);
    }
}
