package de.pentabyte.maven.i18n.format.java;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;

/**
 * Properties, which keeps the order of keys.
 * 
 * @author Michael Höreth
 */
class LinkedProperties extends NoTimestampProperties {
	private static final long serialVersionUID = -5660063416707777624L;
	private final LinkedHashSet<Object> keys = new LinkedHashSet<Object>();

	@Override
	public Enumeration<Object> keys() {
		return Collections.<Object>enumeration(keys);
	}

	@Override
	public Object put(Object key, Object value) {
		keys.add(key);
		return super.put(key, value);
	}
}