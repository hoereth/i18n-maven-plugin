package de.pentabyte.maven.i18n.format.java;

import java.util.regex.Matcher;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author michael hoereth
 *
 */
public class JavaAccessorCreatorTest {
	@Test
	public void test_placeholderPattern() {
		Matcher m = JavaAccessorCreator.PLACEHOLDER.matcher("This is {0} test");
		Assert.assertTrue(m.find());
		Assert.assertEquals("0", m.group(1));

		m = JavaAccessorCreator.PLACEHOLDER.matcher("This {1} is {0} test");
		Assert.assertTrue(m.find());
		Assert.assertEquals("1", m.group(1));
		Assert.assertTrue(m.find());
		Assert.assertEquals("0", m.group(1));

		m = JavaAccessorCreator.PLACEHOLDER.matcher("This {0,date} is {1,time,long} test");
		Assert.assertTrue(m.find());
		Assert.assertEquals("0", m.group(1));
		Assert.assertTrue(m.find());
		Assert.assertEquals("1", m.group(1));
	}
}
