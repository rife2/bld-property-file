# [Bld](https://github.com/rife2/rife2/wiki/What-Is-Bld) Extension to Create or Modify Properties Files

[![License (3-Clause BSD)](https://img.shields.io/badge/license-BSD%203--Clause-blue.svg?style=flat-square)](http://opensource.org/licenses/BSD-3-Clause)
[![Java](https://img.shields.io/badge/java-17%2B-blue)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Release](https://flat.badgen.net/maven/v/metadata-url/repo.rife2.com/releases/com/uwyn/rife2/bld-property-file/maven-metadata.xml?color=blue)](https://repo.rife2.com/#/releases/com/uwyn/rife2/bld-property-file)
[![Snapshot](https://flat.badgen.net/maven/v/metadata-url/repo.rife2.com/snapshots/com/uwyn/rife2/bld-property-file/maven-metadata.xml?label=snapshot)](https://repo.rife2.com/#/snapshots/com/uwyn/rife2/bld-property-file)
[![GitHub CI](https://github.com/rife2/bld-property-file/actions/workflows/bld.yml/badge.svg)](https://github.com/rife2/bld-property-file/actions/workflows/bld.yml)

An extension for creating or modifying [property files](https://docs.oracle.com/javase/tutorial/essential/environment/properties.html) with [bld](https://github.com/rife2/rife2/wiki/What-Is-Bld). It is inspired by the [ant PropertyFile task](https://ant.apache.org/manual/Tasks/propertyfile.html).

```java
@BuildCommand
public void updateMajor() throws Exception {
    new PropertyFileOperation(this)
            .file("version.properties")
            .entry(new EntryInt("version.major").defaultValue(0).calc(ADD))
            .entry(new EntryInt("version.minor").set(0))
            .entry(new EntryInt("version.patch").set(0))
            .entry(new EntryDate("build.date").now().pattern("yyyy-MM-dd"))
            .execute();
}
```
Invoking the `updateMajor` command, will create the `version.propertees`file:

```sh
./bld updateMajor ...
```

```ini
# version.properties
build.date=2023-04-02
version.major=1
version.minor=0
version.patch=0
```

Invoking the `updateMajor` command again, will increase the `version.major` property:

```sh
./bld updateMajor ...
```

```ini
# version.properties
build.date=2023-04-02
version.major=2
version.minor=0
version.patch=0
```

- [View Examples](https://github.com/rife2/bld-property-file/tree/master/examples)

## Property File

The [PropertyFileOperation](https://rife2.github.io/bld-property-file/rife/bld/extension/propertyfile/PropertyFileOperation.html) class is used to configure the [properties file](https://docs.oracle.com/javase/tutorial/essential/environment/properties.html) location, etc.

| Function          | Description                                                     | Required |
|:------------------|:----------------------------------------------------------------|:---------|
| `file()`          | The location of the properties files to modify.                 | Yes      |
| `comment()`       | Comment to be inserted at the top of the properties file.       | No       |       
| `failOnWarning()` | If set to `true`, will cause execution to fail on any warnings. | No       |

## Entry

The [Entry](https://rife2.github.io/bld-property-file/rife/bld/extension/propertyfile/Entry.html) class is used to specify modifications to a [String property](https://docs.oracle.com/javase/tutorial/essential/environment/properties.html).

| Function         | Description/Example                                                                                     |
|:-----------------|:--------------------------------------------------------------------------------------------------------|
| `defaultValue()` | The value to be used if the property doesn't exist.                                                     |
| `delete()`       | Delete the property.                                                                                    |
| `modify()`       | `modify("-foo", String::concat)`<br/>`modify("-foo", (v, s) -> v + s)`<br/>`modify((v, s) -> v.trim())` | Modify an entry value.                     |
| `set()`          | The value to set the property to, regardless of its previous value.                                     |

## EntryDate

The [EntryDate](https://rife2.github.io/bld-property-file/rife/bld/extension/propertyfile/EntryDate.html) class is used to specify modifications to a [date property](https://docs.oracle.com/javase/tutorial/essential/environment/properties.html).

| Function         | Description/Example                                                                                                                                                                                                                                                                                                                                                         |
|:-----------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `calc()`         | `calc(ADD)`<br/>`calc(v -> v + 1)`<br/>`calc(SUB)`<br/>`calc(v -> v - 1)`                                                                                                                                                                                                                                                                                                   |
| `delete()`       | Delete the property.                                                                                                                                                                                                                                                                                                                                                        |
| `now()`          | Set the entry to the current date/time.                                                                                                                                                                                                                                                                                                                                     |
| `pattern()`      | If present, will parse the value as a [DateTimeFormatter](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/format/DateTimeFormatter.html) pattern.                                                                                                                                                                                                    |
| `set()`          | The [Calendar](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Calendar.html), [Date](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Date.html), or [java.time](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/package-summary.html) value to set the property to, regardless of its previous value. |
| `unit()`         | The unit to be used calculations. See [Units](#units).                                                                                                                                                                                                                                                                                                                      |                                                                                                                                                                          

- `set` or `now` are required.

### Units

The following [Units](https://rife2.github.io/bld-property-file/rife/bld/extension/propertyfile/EntryDate.Units.html) are available:

* `Units.MILLISECOND`
* `Units.SECOND`
* `Units.MINUTE`
* `Units.HOUR`
* `Units.DAY`
* `Units.WEEK`
* `Units.MONTH`
* `Units.YEAR`

## EntryInt

The [EntryInt](https://rife2.github.io/bld-property-file/rife/bld/extension/propertyfile/EntryInt.html) class is used to specify modifications to a [integer property](https://docs.oracle.com/javase/tutorial/essential/environment/properties.html).

| Function         | Description/Example                                                                                                                                                |
|:-----------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `defaultValue()` | The value to be used if the property doesn't exist.                                                                                                                |
| `calc()`         | `calc(ADD)`<br/>`calc(v -> v + 1)`<br/>`calc(SUB)`<br/>`calc(v -> v - 1)`                                                                                          |
| `delete()`       | Delete the property.                                                                                                                                               |
| `pattern()`      | If present, will parse the value as a [DecimalFormat](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/text/DecimalFormat.html) pattern.          |
| `set()`          | The [integer value](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Integer.html) to set the property to, regardless of its previous value. |
