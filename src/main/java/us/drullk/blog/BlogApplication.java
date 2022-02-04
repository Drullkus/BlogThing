package us.drullk.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class BlogApplication {
    @RequestMapping("/result")
    public String defaultDocumentContents() {
        return "Hello from a docker world built by Gradle!";
    }

    public static void main(String... args) {
        SpringApplication.run(BlogApplication.class, args);
    }
}
