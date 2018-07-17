# Getting Started with a JAVA project with Properties Files

## pom.xml

Add this to your pom - and the plugin will translate all **i18n.xml** files into **message_{locale}.properties**-files within your project during the _generate-resources_ phase.

```xml
<build>
	<plugins>
		<plugin>
			<groupId>de.pentabyte.tools</groupId>
			<artifactId>i18n-maven-plugin</artifactId>
			<version>1.2</version>
			<executions>
				<execution>
					<phase>generate-resources</phase>
					<goals>
						<goal>translate</goal>
					</goals>
				</execution>
			</executions>
			<configuration>
				<tableDirectory>${basedir}</tableDirectory>
				<outputBasename>messages</outputBasename>
				<outputFormat>JAVA_PROPERTIES</outputFormat>
			</configuration>
		</plugin>
	</plugins>
</build>
```

## Results

- [messages_en.properties](../src/test/resources/messages_en.properties)
- [messages_de.properties](../src/test/resources/messages_de.properties)
- [messages_fr.properties](../src/test/resources/messages_fr.properties)

Note: It is common to have a default file "messages.properties". It will be created if you add an empty locale to the locale-config element of your [i18n.xml](../src/test/resources/i18n.xml) file.

## Java Accessor

If you specify the optional _javaAccessor_ element within the _output_ element of the i18n.xml file, a [Java Accessor class](../src/main/java/test/Messages.java) will be generated. It provides you with all translation keys + translation methods. It allows you get your translations checked by the compiler. Usage:

### The traditional way

```
String value = ResourceBundle.getBundle("messages", Locale.ENGLISH).getString("dialog.confirm_update");
```

### With Accessor

```
String value = Messages.dialog.confirm_update(Locale.ENGLISH); 
```