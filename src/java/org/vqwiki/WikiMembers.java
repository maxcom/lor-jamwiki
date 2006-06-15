package org.vqwiki;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;


/**
 * Stores a list of usernames and their registered email addresses so
 * that users may set notifications and reminders per topic page. Users
 * must set a canonical username and provide a valid email address.
 * An email will then be sent to the supplied address with a hyperlink
 * containing a validation key. The key is then checked against the
 * list of registered names and confirmed, at which point the user
 * is allowed to set notifications and reminders.
 *
 * @author Robert E Brewer
 * @version 0.1
 */
public interface WikiMembers {

    /**
     *
     */
    public boolean requestMembership(String username, String email, HttpServletRequest request) throws Exception;

    /**
     *
     */
    public boolean createMembershipWithoutRequest(String username, String email) throws Exception;

    /**
     *
     */
    public boolean confirmMembership(String username, String key) throws Exception;

    /**
     *
     */
    public boolean removeMember(String username) throws Exception;

    /**
     *
     */
    public WikiMember findMemberByName(String username) throws Exception;

    /**
     *
     */
    public Collection getAllMembers() throws Exception;

    /**
     *
     */
    public void addMember(String username, String email, String key) throws Exception;
}
