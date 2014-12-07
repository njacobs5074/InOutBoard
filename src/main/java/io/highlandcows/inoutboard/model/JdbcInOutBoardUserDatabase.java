package io.highlandcows.inoutboard.model;

import org.apache.commons.lang3.Validate;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Spring JdbcTemplate class that manages {@link io.highlandcows.inoutboard.model.InOutBoardUser} persistence.
 *
 * @author highlandcows
 * @since 11/11/14
 */
public class JdbcInOutBoardUserDatabase implements InOutBoardUserDatabase {

    private final JdbcTemplate jdbcTemplate;

    public JdbcInOutBoardUserDatabase(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void addUser(InOutBoardUser user) {
        Validate.notNull(user, "Req'd argument `user' is null");

        int count = jdbcTemplate.queryForObject("select count(*) from inout_board_user where user_handle = ?",
                                                Integer.class, user.getHandle());
        Validate.isTrue(count == 0, "Error: User %s already exists", user);

        jdbcTemplate.update("insert into inout_board_user (user_handle, user_name, status) values (?, ?, ?)",
                            user.getHandle(), user.getName(), InOutBoardStatus.REGISTERED.toString());
    }

    @Override
    public void deleteUser(InOutBoardUser user) {
        Validate.notNull(user, "Req'd argument `user' is null");
        int count = jdbcTemplate.queryForObject("select count(*) from inout_board_user where user_handle = ?",
                                                Integer.class, user.getHandle());
        Validate.isTrue(count == 1, "Error: User %s does not exist", user);

        jdbcTemplate.update("delete from inout_board_user where user_handle = ?", user.getHandle());
    }

    @Override
    public void updateUser(InOutBoardUser user, InOutBoardStatus inOutBoardStatus, String comment) {
        Validate.notNull(user, "Req'd argument `user' is null");
        int count = jdbcTemplate.queryForObject("select count(*) from inout_board_user where user_handle = ?",
                                                Integer.class, user.getHandle());
        Validate.isTrue(count == 1, "Error: User %s does not exist", user);

        jdbcTemplate.update("update inout_board_user set status = ?, comment = ? where user_handle = ?",
                            inOutBoardStatus.toString(), comment, user.getHandle());
    }

    @Override
    public InOutBoardUser getUser(String userHandle)  {
        Validate.notBlank(userHandle, "Req'd argument `handle' is null/empty");

        try {
            return jdbcTemplate.queryForObject("select user_handle, user_name, status, comment from inout_board_user " +
                                               "where user_handle = ?",
                                               new Object[]{userHandle},
                                               new InOutBoardUserMapper());
        }
        catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public int getNumberOfUsers() {
        return jdbcTemplate.queryForObject("select count(*) from inout_board_user", Integer.class);
    }

    @Override
    public Collection<InOutBoardUser> getAllUsers() {
        return jdbcTemplate.query("select user_handle, user_name, status, comment from inout_board_user",
                                  new InOutBoardUserMapper());
    }

    private static final class InOutBoardUserMapper implements RowMapper<InOutBoardUser> {
        @Override
        public InOutBoardUser mapRow(ResultSet resultSet, int i) throws SQLException {
            return new InOutBoardUser(resultSet.getString("user_handle"),
                                      resultSet.getString("user_name"),
                                      InOutBoardStatus.valueOf(resultSet.getString("status")),
                                      resultSet.getString("comment"));
        }
    }
}
