package org.jmwiki;

/**
 * @author garethc
 * Date: Dec 17, 2002
 */
public class Permission {

	public static final Permission READ = new Permission("READ", "Read contents of topic");
	public static final Permission MODIFY = new Permission("MODIFY", "Read contents of topic");
	public static final Permission ATTACH = new Permission("ATTACH", "Read contents of topic");
	private String name;
	private String description;

	/**
	 *
	 */
	private Permission(String name, String description) {
		this.name = name;
		this.description = description;
	}

	/**
	 *
	 */
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Permission)) return false;
		final Permission permission = (Permission) o;
		if (name != null ? !name.equals(permission.name) : permission.name != null) return false;
		return true;
	}

	/**
	 *
	 */
	public int hashCode() {
		return (name != null ? name.hashCode() : 0);
	}

	/**
	 *
	 */
	public String getName() {
		return name;
	}

	/**
	 *
	 */
	public String getDescription() {
		return description;
	}
}
