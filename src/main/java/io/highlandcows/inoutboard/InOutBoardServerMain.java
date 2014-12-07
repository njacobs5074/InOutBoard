package io.highlandcows.inoutboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main entry point for InOutBoardServer. Uses {@link org.springframework.boot.SpringApplication}
 * to control bean creation & composition so very little code here.
 *
 * @author highlandcows
 * @since 11/11/14
 */
@ComponentScan
@EnableAutoConfiguration
public class InOutBoardServerMain {
    public static void main(String... args) {
        SpringApplication.run(InOutBoardServerMain.class, args);
    }
}
