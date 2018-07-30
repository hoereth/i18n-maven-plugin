# i18n-maven-plugin

Generator for language files. Currently supported target formats: [JAVA](doc/README_JAVA.md), [STRINGS (XCode)](doc/README_STRINGS.md), [JAVASCRIPT](doc/README_JAVASCRIPT.md).

## Goals

* de.pentabyte.tools:i18n-maven-plugin:**translate** (i18n.xml => language files)
* de.pentabyte.tools:i18n-maven-plugin:**reengineer** (language files => i18n.xml)

## Introduction

Language files are designed for performance NOT for easy maintenance. Technical translation mistakes usually remain undetected. This plugin suggests that you rather maintain your translations in a structured and bullet-proof [**i18n.xml**](src/test/resources/i18n.xml) file. The plugin will then generate the language files of your choice. This is one more build step, but the following issues will be taken care of:

### Correct Syntax

The plugin will create the language files for you with all their peculiarities. Examples:

* JAVA properties: They must be **Latin1** encoded. UTF-8 characters can be escaped like this: **ä = \u00E4**. You have to deal with edge cases like the **!** character - it needs to be escaped like this: **\!**. Logical line breaks need to be terminated with a backslash.
* C strings: They must be UTF-16 encoded.

### Consistency

No more typos in the keys. No more forgotten translations. The plugin even offers a language fallback mechanism, which allows you to temporarily fill missing translations with a secondary language.

### Redundancy

The XML design gets rid off quite a few redundancies. On top off that, the plugin provides a placeholder mechanism to avoid redundant translations.

### Structure

No need for "fake" grouping via composite key structures. XML naturally supports nested structures. The translations of all languages will be next to each other, which helps the translator. Also, you can add a description for each translation entry to further explain the intent to the translator.

## Getting started

### i18n.xml Table

This will be the source of your translations for any kind of programming language. You can have more than one per project. You can put it anywhere in your project, because we will usually configure the plugin to pick up all files named **i18n.xml**. Please see [this example i18n.xml file](src/test/resources/i18n.xml), which is configured to result in all different kinds of language files. Editing is almost self-explanatory, if your editor supports the provided XSD schema.

### Configuration

The de.pentabyte.tools:i18n-maven-plugin:translate Plugin provides these configuration params:

- **tableDirectory** — Recursivly check for all "i18n.xml"-Tables within that directory. Default: *${basedir}*
- **tableFile** — Path of i18n-Table.
- **inputBasename** — Name of XML-File. Default: *i18n*
- **targetDir** — Base directory of generated source code files. Default: *${basedir}/src/main/java*

Please note that you can specifiy the following parameters within the <output> element of each of your i18n.xml files instead of defining them for the whole project:

- **outputDirectory** — Language Files should be written here. Defaults to directory of table file.
- **outputFormat** — JAVA_PROPERTIES, STRINGS, JAVASCRIPT
- **outputBasename** — Base name of language files. Defaults according to outputFormat.
- **keySeparator** — Keys of nested entries will be separated with this value. Default: *.*

### Target Format

To get more into detail, please continue reading the appropriate README file. You will also find information about the usage of the reengineer plugin, if available:

- [JAVA project with Properties Files](doc/README_JAVA.md)
- [XCode project with Strings Files](doc/README_STRINGS.md)
- [Web project with Javascript Files](doc/README_JAVASCRIPT.md)