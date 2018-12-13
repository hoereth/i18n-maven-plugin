# Getting Started with a Web project with Javascript Files

## pom.xml

In order to execute a maven plugin, you would need to have a pom.xml file. This fragment will be necessary for this plugin:

```
<build>
  <plugins>
    <plugin>
      <groupId>de.pentabyte.tools</groupId>
      <artifactId>i18n-maven-plugin</artifactId>
      <version>1.2.2</version>
      <configuration>
        <tableDirectory>${basedir}</tableDirectory>
      </configuration>
    </plugin>
  </plugins>
</build>
```

This example configuration will make the plugin scan your **basedir** for **i18n.xml** files and will create JSON files as such: **i18n_{locale}.js**.

## Results

- [i18n_de.js](../src/test/resources/i18n_de.js)
- [i18n_en.js](../src/test/resources/i18n_en.js)
- [i18n_fr.js](../src/test/resources/i18n_fr.js)

## Usage

Unlike other target language files, you can instantly access the translations like this:

```javascript
alert(i18n.dialog.confirm_update$);
```

Please note the dollar sign, which acts as 'terminator'. Without it, Javascript would not be able to tell if you are about to access the tree structure or a concrete value. 