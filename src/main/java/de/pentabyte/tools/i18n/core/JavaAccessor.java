/**
 * 
 */
package de.pentabyte.tools.i18n.core;

import java.io.File;

/**
 * @author Michael Höreth
 *
 */
public class JavaAccessor {
	File directory;
	String packageName;
	String className;
	String resourceBundleBaseName;

	public File getDirectory() {
		return directory;
	}

	public void setDirectory(File directory) {
		this.directory = directory;
	}

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

	public String getResourceBundleBaseName() {
		return resourceBundleBaseName;
	}

	public void setResourceBundleBaseName(String resourceBundleBaseName) {
		this.resourceBundleBaseName = resourceBundleBaseName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "directory: " + directory + ", packageName: " + packageName + ", className: " + className
				+ ", resourceBundleName: " + resourceBundleBaseName;
	}
}
