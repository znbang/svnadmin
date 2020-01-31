package service;

import models.User;
import models.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Play;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public final class LdapService {
	private final Logger LOG = LoggerFactory.getLogger(LdapService.class);

	protected LdapContext createContext(String userName, String password) throws NamingException {
		if (!userName.contains("@")) {
			userName += Play.configuration.getProperty("ldap.suffix");
		}
		Hashtable<String, String> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.PROVIDER_URL, Play.configuration.getProperty("ldap.url"));
		env.put(Context.SECURITY_PRINCIPAL, userName);
		env.put(Context.SECURITY_CREDENTIALS, password);
		return new InitialLdapContext(env, null);
	}

	public User authenticate(String userName, String password) {
		userName = userName.toLowerCase().trim();
		password = password.trim();

		if (userName.isEmpty() || password.isEmpty()) {
			return null;
		}

		try {
			LdapContext ctx = createContext(userName, password);
			SearchControls ctls = new SearchControls();
			ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			String filter = String.format("(&(objectCategory=Person)(objectClass=user)(sAMAccountName=%s))", userName);
			NamingEnumeration<SearchResult> userEnum = ctx.search("", filter, ctls);
			User user = null;
			if (userEnum.hasMoreElements()) {
				SearchResult userResult = userEnum.nextElement();
				user = new User(
						(String) userResult.getAttributes().get("sAMAccountName").get(),
						(String) userResult.getAttributes().get("name").get(),
						UserRole.user
				);
			}
			ctx.close();
			return user;
		} catch (AuthenticationException e) {
			LOG.debug("User login with incorrect password: {}", userName);
		} catch (NamingException e) {
			LOG.debug("", e);
		}

		return null;
	}

	public List<User> getUsers() {
		List<User> users = new ArrayList<>();

		try {
			LdapContext ctx = createContext(
					Play.configuration.getProperty("ldap.user"),
					Play.configuration.getProperty("ldap.password")
			);
			SearchControls ctls = new SearchControls();
			ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			String filter = "(&(objectCategory=Person)(objectClass=user))";
			NamingEnumeration<SearchResult> userEnum = ctx.search("", filter, ctls);
			while (userEnum.hasMoreElements()) {
				SearchResult userResult = userEnum.nextElement();
				if ((Integer.parseInt((String) userResult.getAttributes().get("userAccountControl").get()) & 0x2) == 0) {
					User user = new User(
							(String) userResult.getAttributes().get("sAMAccountName").get(),
							(String) userResult.getAttributes().get("name").get(),
							UserRole.user
					);
					if (user.displayName.contains("(") && user.displayName.contains(")")) {
						users.add(user);
					}
				}
			}
			ctx.close();
		} catch (NamingException e) {
			LOG.info("", e);
		}

		return users;
	}
}
