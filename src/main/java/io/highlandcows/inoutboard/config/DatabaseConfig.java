package io.highlandcows.inoutboard.config;

import io.highlandcows.inoutboard.model.InOutBoardUserDatabase;
import io.highlandcows.inoutboard.model.JdbcInOutBoardUserDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Beans related to the user database.
 *
 * @author highlandcows
 * @since 11/11/14
 */
@Configuration
public class DatabaseConfig {

    /**
     * Very simple as we just need the SpringBoot-provided {@link DataSource} to
     * create our database.
     */
    @Bean
    InOutBoardUserDatabase inOutBoardUserStore(DataSource dataSource) {
        return new JdbcInOutBoardUserDatabase(dataSource);
    }
}
