package io.highlandcows.inoutboard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

/**
 * @author highlandcows
 * @since 11/11/14
 */
@Configuration
public class InOutBoardServerTestConfig {

    @Bean
    EmbeddedDatabase embeddedDatabase() {
        return new EmbeddedDatabaseBuilder().addScript("schema-hsqldb.sql").build();
    }

}