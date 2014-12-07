package io.highlandcows.inoutboard.model;

import java.util.Collection;

/**
 * Defines high-level CRUD operations for in/out board users
 *
 * @author highlandcows
 * @since 11/11/14
 */
public interface InOutBoardUserDatabase {

    void addUser(InOutBoardUser user);

    void deleteUser(InOutBoardUser user);

    void updateUser(InOutBoardUser user, InOutBoardStatus inOutBoardStatus, String comment);

    InOutBoardUser getUser(String userHandle);

    int getNumberOfUsers();

    Collection<InOutBoardUser> getAllUsers();
}
