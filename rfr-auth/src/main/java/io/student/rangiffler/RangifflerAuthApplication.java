package io.student.rangiffler;

import java.util.Locale;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RangifflerAuthApplication {

  public static void main(String[] args) {
    // Security filter messages use Locale.getDefault(), not MVC locale-resolver.
    Locale.setDefault(Locale.US);
    SpringApplication.run(RangifflerAuthApplication.class);
  }
}
