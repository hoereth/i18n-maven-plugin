# Getting Started with an XCode project with String Files

## pom.xml

In order to execute a maven plugin, you would need to have a pom.xml file. This fragment will be necessary for this plugin:

```xml
<build>
  <plugins>
    <plugin>
      <!-- https://mvnrepository.com/artifact/de.pentabyte.tools/i18n-maven-plugin -->
      <groupId>de.pentabyte.tools</groupId>
      <artifactId>i18n-maven-plugin</artifactId>
      <version>1.2.3</version>
      <configuration>
        <tableDirectory>${basedir}</tableDirectory>

        <!-- useful defaults for XCode, can be overwritten in i18n.xml: -->
        <outputFormat>STRINGS</outputFormat>
      </configuration>
    </plugin>
  </plugins>
</build>
```

This example configuration will scan your **basedir** for all **i18n.xml** files and create Strings files as such **{Locale}/Localizable.strings**.

## Translation Build Phase

You can take advantage of XCode's "Run Script" - build phase. If you specify the language table file by using XCode's placeholder mechanism, you will increase the performance: The script phase will only be run, if any of the input files has changed OR any of the output files has not been created yet. Please note that your output files will be created within your SRC directory, because that's where the translation files still belong. This is just a "pre-build" step.

![XCode Build Phase](xcode_build_phase.png)

After you set up this build phase, you only need to edit the XML language table files and XCode will automatically run the i18n maven plugin for you.

## Results

- [de.lproj/Localizable.strings](../src/test/resources/de.lproj/Localizable.strings)
- [en.lproj/Localizable.strings](../src/test/resources/en.lproj/Localizable.strings)

- de.lproj/InfoPlist.strings
- en.lproj/InfoPlist.strings

## Re-Engineer the i18n.xml file

You might already have an existing project with Localizable.strings files. Execute the reengineer goal just once to create the XML language table from your language files like this:

```
mvn de.pentabyte.tools:i18n-maven-plugin:reengineer -DoutputFormat=STRINGS -DoutputBasename=Localizable -DinputBasename=Localizable
```

You might already have more than one localized strings file. Our 2nd example will be the "InfoPlist.strings" file.

mvn de.pentabyte.tools:i18n-maven-plugin:reengineer -DoutputFormat=STRINGS -DoutputBasename=InfoPlist -DinputBasename=InfoPlist
