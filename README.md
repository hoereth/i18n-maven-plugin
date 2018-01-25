# i18n-maven-plugin

Generator for language files. Supported target formats: [JAVA](src/test/resources/i18n_en.properties), [STRINGS (XCode)](src/test/resources/en.lproj/i18n.strings), [JAVASCRIPT](src/test/resources/i18n_en.js).

## Goals

* de.pentabyte.tools:i18n-maven-plugin:**translate** (i18n.xml => language files)
* de.pentabyte.tools:i18n-maven-plugin:**reengineer** (language files => i18n.xml)

See "Getting Started" for configuring your pom for automatically executing the **translate** goal during the _generate-resources_ phase.

## Introduction

Language files are designed for performance NOT for easy maintenance. Technical translation mistakes usually remain undetected. This plugin suggests that you rather maintain your translations in a structured and bullet-proof **i18n.xml** file. The plugin will create the language files of your choice. This is one more build step, but the following issues will be taken care of:

### Format

The plugin will create the language files for you with all their pecularities. Examples:

* JAVA properties: They must be **Latin1** encoded. UTF-8 characters can be escaped like this: **ä = \u00E4**. You have to deal with edge cases like the **!** character - it needs to be escaped like this: **\!**. Logical line breaks need to be terminated with a backslash.
* C strings: They must be UTF-16 encoded.

### Consistency

No more typos in the keys. No more forgotten translations. The plugin even offers a language fallback mechanism, which allows you to temporarily fill missing translations with a secondary language.

### Redundancy

The XML design gets rid off quite a few redundancies. On top off that, the plugin provides a placeholder mechanism to avoid redundant translations.

### Clarity

No need for "fake" grouping via composite key structures. XML naturally supports nested structures. The translations of all languages will be next to each other, which helps the translator. Also, you can add a description for each translation entry to further explain the intent to the translator.

## Getting started

### i18n.xml Table

This will be the source of your translations for any kind of programming language. You can have more than one per project. You can put it anywhere in your project, because we will configure the plugin to pick up all files named **i18n.xml**:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<table xmlns="http://pentabyte.de/maven/i18n"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://pentabyte.de/maven/i18n http://pentabyte.de/maven/i18n/table-1.1.xsd">
	<locale-config>
		<exported-locale value="de" fallback="en"/>
		<exported-locale value="en"/>
		<exported-locale value="fr" fallback="de"/>
	</locale-config>
	
	<entry key="question">
		<text locale="en">Question</text>
		<text locale="de">Frage</text>
		<text locale="fr">Question</text>
	</entry>

	<entry key="dialog">
		<entry key="confirm_update">
			<description>Will be asked when online update is available.</description>
			<text locale="en">${question}: Do you really want to update?</text>
			<text locale="de">${question}: Möchten Sie wirklich updaten?</text>
		</entry>
		
		<entry key="next_week">
			<description>If the user chooses this action, then there should be a reminder next week.</description>
			<text locale="de">Erinnere mich nächste Woche!</text>
			<text locale="en">Remind me next week!</text>
		</entry>
	</entry>
</table>
```

### Language Files

- [JAVA properties files](README_JAVA.md)