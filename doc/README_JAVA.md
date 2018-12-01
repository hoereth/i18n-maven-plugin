# Getting Started with a JAVA project with Properties Files

## pom.xml

Add this to your pom - and the plugin will translate all **i18n.xml** files into **message_{locale}.properties**-files within your project during the _generate-resources_ phase.

```xml
<build>
  <plugins>
    <plugin>
      <groupId>de.pentabyte.tools</groupId>
      <artifactId>i18n-maven-plugin</artifactId>
      <version>1.2.1</version>
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

Note: It is common to have a default file "messages.properties". It will be created if you add an locale with empty value="" to the locale-config element of your [i18n.xml](../src/test/resources/i18n.xml) file.

## Java Accessor

If you specify the optional _javaAccessor_ element (here: packageName="test" className="Messages") within the _output_ element of the [i18n.xml](../src/test/resources/i18n.xml#L19) file, a Java Accessor class _Messages.java_ will be generated ([see example](../src/main/java/test/Messages.java)). It provides you with all translation keys + additional translation methods. It allows you get your translations checked by the compiler. Additional requirement: your translation keys must be valid Java class names. Usage:

### The traditional way

```java
String value = ResourceBundle.getBundle("messages", Locale.ENGLISH).getString("dialog.confirm_update");
```

### With Accessor

```java
// retrieve translation key constant ...
String key = Messages.dialog.confirm_update$;
// ... or use the translation method
String text = Messages.dialog.confirm_update(Locale.ENGLISH);
```

## Re-Engineer the i18n.xml file

You might already have an existing project with properties files. Execute the reengineer goal just once to create the XML language table from your language files like this:

```
mvn de.pentabyte.tools:i18n-maven-plugin:reengineer -DoutputFormat=JAVA -DoutputBasename=messages
```

## Related Links

* The [Java Message Format](https://docs.oracle.com/javase/8/docs/api/java/text/MessageFormat.html) provides a solution to writing translations which may contain localized
dates, times, numbers and singular/plural variations.