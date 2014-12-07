package io.highlandcows.inoutboard;

import javax.security.auth.Subject;
import java.security.Principal;

/**
 * Simple implementation of {@link java.security.Principal} for testing STOMP/WebSocket code.
 * Based on @rstoyanchev code on GitHub for same.
 *
* @author highlandcows
* @since 14/11/14
*/
class DummyPrincipal implements Principal {
    private final String name;

    DummyPrincipal(String name) { this.name = name; }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean implies(Subject subject) {
        return false;
    }
}
