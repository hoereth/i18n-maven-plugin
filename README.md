# i18n-maven-plugin

Generator for language files. Currently supported target formats: [JAVA](doc/README_JAVA.md), [STRINGS (XCode)](doc/README_STRINGS.md), JAVASCRIPT.

## Goals

* de.pentabyte.tools:i18n-maven-plugin:**translate** (i18n.xml => language files)
* de.pentabyte.tools:i18n-maven-plugin:**reengineer** (language files => i18n.xml)

See "Getting Started" for configuring your pom for automatically executing the **translate** goal during the _generate-resources_ phase.

## Introduction

Language files are designed for performance NOT for easy maintenance. Technical translation mistakes usually remain undetected. This plugin suggests that you rather maintain your translations in a structured and bullet-proof [**i18n.xml**](src/test/resources/i18n.xml) file. The plugin will create the language files of your choice. This is one more build step, but the following issues will be taken care of:

### Format

The plugin will create the language files for you with all their peculiarities. Examples:

* JAVA properties: They must be **Latin1** encoded. UTF-8 characters can be escaped like this: **ä = \u00E4**. You have to deal with edge cases like the **!** character - it needs to be escaped like this: **\!**. Logical line breaks need to be terminated with a backslash.
* C strings: They must be UTF-16 encoded.

### Consistency

No more typos in the keys. No more forgotten translations. The plugin even offers a language fallback mechanism, which allows you to temporarily fill missing translations with a secondary language.

### Redundancy

The XML design gets rid off quite a few redundancies. On top off that, the plugin provides a placeholder mechanism to avoid redundant translations.

### Clarity

No need for "fake" grouping via composite key structures. XML naturally supports nested structures. The translations of all languages will be next to each other, which helps the translator. Also, you can add a description for each translation entry to further explain the intent to the translator.

## Getting started

### Dependency

Available at Maven Central with these coordinates:

```
<dependency>
  <groupId>de.pentabyte.tools</groupId>
  <artifactId>i18n-maven-plugin</artifactId>
  <version>1.2</version>
</dependency>
```

### i18n.xml Table

This will be the source of your translations for any kind of programming language. You can have more than one per project. You can put it anywhere in your project, because we will usually configure the plugin to pick up all files named **i18n.xml**. Please see [this example i18n.xml file](src/test/resources/i18n.xml), which is configured to result in all different kinds of language files. Editing is almost self-explanatory, if your editor supports the provided XSD schema.

### Target Format

Continue reading now depending on your target format needs:

- [JAVA project with properties files](doc/README_JAVA.md)
- [XCode project with strings files](doc/README_STRINGS.md)