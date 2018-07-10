# Getting Started with a Web / Javascript project

## pom.xml

In order to execute a maven plugin, you would need to have a pom.xml file. This fragment will be necessary for this plugin:

```
	<build>
		<plugins>
			<plugin>
				<groupId>de.pentabyte.tools</groupId>
				<artifactId>i18n-maven-plugin</artifactId>
				<version>1.2</version>
				<configuration>
						<tableDirectory>${basedir}</tableDirectory>
				</configuration>
			</plugin>
		</plugins>
	</build>
```

## Results

- [i18n_de.js](../src/test/resources/i18n_de.js)
- [i18n_en.js](../src/test/resources/i18n_en.js)
- [i18n_fr.js](../src/test/resources/i18n_fr.js)

## Usage

Unlike other target language files, you can instantly access the translations like this:

```
alert(i18n.dialog.confirm_update$);
```

Please note the dollar sign, which acts as 'terminator'. Otherwise, Javascript would not be able to tell, if you are about to access the tree structure or a concrete value. 