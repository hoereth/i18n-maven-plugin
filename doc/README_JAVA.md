# Getting Started with a JAVA project

## pom.xml

Add this to your pom - and the plugin will translate all **i18n.xml** files into **message_{locale}.properties**-files within your project during the _generate-resources_ phase.

```xml
<build>
	<plugins>
		<plugin>
			<groupId>de.pentabyte.tools</groupId>
			<artifactId>i18n-maven-plugin</artifactId>
			<version>1.1.0</version>
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