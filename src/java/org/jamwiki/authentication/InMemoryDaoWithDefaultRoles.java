/**
 *  Copyright 2006
 *
 *  ABAS Software AG (http://www.abas.de)
 *  All rights reserved.
 */
package org.jamwiki.authentication;

import java.util.Properties;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.acegisecurity.userdetails.memory.UserMap;
import org.acegisecurity.userdetails.memory.UserMapEditor;
import org.jamwiki.model.WikiUser;
import org.springframework.dao.DataAccessException;
import org.springframework.util.Assert;

/**
 * Retrieves user details from an in-memory list created by the bean context. If
 * no user information can be found, a new UserDetails object is created
 * containing the defaultAuthorities.
 *
 * This class is useful with authentication services not providing user
 * information like CAS. Each user authenticated by CAS is assigned the
 * specified in defaultAuthorities. In addition special user mappings can be
 * provided in a userMap, e.g. to grant certain users the administrator role.
 *
 * @author Rainer Schmitz
 * @since 05.12.2006
 * @see org.acegisecurity.userdetails.memory.InMemoryDaoImpl
 *
 */
public class InMemoryDaoWithDefaultRoles implements UserDetailsService {

    private UserMap userMap;
    private GrantedAuthority[] defaultAuthorities;

    public void setUserMap(UserMap userMap) {
        this.userMap = userMap;
    }

    public UserMap getUserMap() {
        return userMap;
    }

    /**
     * Modifies the internal <code>UserMap</code> to reflect the
     * <code>Properties</code> instance passed. This helps externalise user
     * information to another file etc.
     *
     * @param props
     *            the account information in a <code>Properties</code> object
     *            format
     */
    public void setUserProperties(Properties props) {
        this.userMap = UserMapEditor.addUsersFromProperties(new UserMap(), props);
    }

    /**
     * Default authorities provided to all users not mentioned in userMap.
     *
     * @param defaultAuthorities
     *            to set.
     */
    public void setDefaultAuthorities(GrantedAuthority[] defaultAuthorities) {
        Assert.notNull(defaultAuthorities, "Cannot pass a null GrantedAuthority array");
        for (int i = 0; i < defaultAuthorities.length; i++) {
            Assert.notNull(defaultAuthorities[i],
                "Granted authority element " + i + " is null - GrantedAuthority[] cannot contain any null elements");
        }
        this.defaultAuthorities = defaultAuthorities;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.acegisecurity.providers.dao.memory.InMemoryDaoImpl#loadUserByUsername(java.lang.String)
     */
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
        try {
            if (userMap == null) {
                return newUserWithDefaultAuthorities(username);
            }
            UserDetails user = userMap.getUser(username);
            WikiUser wikiUser = new WikiUser(user.getUsername(), user.getPassword(), true, true, true, true, user
                    .getAuthorities());
            return wikiUser;
        } catch (UsernameNotFoundException e) {
            return newUserWithDefaultAuthorities(username);
        }
    }

    private UserDetails newUserWithDefaultAuthorities(String username) {
        return new WikiUser(username, "ignored", true, true, true, true, defaultAuthorities);
    }

}
