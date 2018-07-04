/**
 * 
 */
package de.pentabyte.tools.i18n.core;

/**
 * @author Michael Höreth
 *
 */
public class JavaAccessor {
	String packageName;
	String className;

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "packageName: " + packageName + ", className: " + className;
	}
}
