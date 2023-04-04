# [Bld](https://github.com/rife2/rife2/wiki/What-Is-Bld) Extension to Create or Modify Properties Files

[![License (3-Clause BSD)](https://img.shields.io/badge/license-BSD%203--Clause-blue.svg?style=flat-square)](http://opensource.org/licenses/BSD-3-Clause)
[![Java](https://img.shields.io/badge/java-17%2B-blue)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![GitHub CI](https://github.com/rife2/bld-property-file/actions/workflows/bld.yml/badge.svg)](https://github.com/rife2/bld-property-file/actions/workflows/bld.yml)

A `bld` extension for creating or modifying [property files](https://docs.oracle.com/javase/tutorial/essential/environment/properties.html) with [bld](https://github.com/rife2/rife2/wiki/What-Is-Bld). It is inspired by the [ant PropertyFile task](https://ant.apache.org/manual/Tasks/propertyfile.html).

```java
@BuildCommand
public void updateMajor() throws Exception {
    new PropertyFileOperation(this)
            .file("version.properties")
            .entry(new Entry("version.major", Types.INT).defaultValue(0).calc(ADD))
            .entry(new Entry("version.minor").set(0))
            .entry(new Entry("version.patch").set(0))
            .entry(new Entry("build.date", Types.DATE).set("now").pattern("yyyy-MM-dd"))
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

The `PropertyFileOperation` class is used to configure the [properties file](https://docs.oracle.com/javase/tutorial/essential/environment/properties.html) location, etc.

| Function          | Description                                                     | Required |
|:------------------|:----------------------------------------------------------------|:---------|
| `file()`          | The location of the properties files to modify.                 | Yes      |
| `comment()`       | Comment to be inserted at the top of the properties file.       | No       |       
| `failOnWarning()` | If set to `true`, will cause execution to fail on any warnings. | No       |

## Entry

The `Entry` class is used to specify modifications to be made to the [properties file](https://docs.oracle.com/javase/tutorial/essential/environment/properties.html).

| Function         | Description                                                                                                                                                                                                                                                                                 |
|:-----------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `key()`          | The name of the property name/value pair.                                                                                                                                                                                                                                                   |                                                                                                                                                                                                                                                   
| `set()`          | The value to set the property to, regardless of its previous value.                                                                                                                                                                                                                         |                                                                                                                                                                                                                                                                  
| `defaultValue()` | The initial value to set for the property to, if not already defined.                                                                                                                                                                                                                       |                                                                                                                                                                           
| `type()`         | The value datatype, either `Types.INT`, `Types.DATE`, or `Types.STRING`. If none specified, `Types.STRING` is assumed.                                                                                                                                                                      |                                                                                                                                                                              
| `pattern()`      | For `Types.INT` and `Types.DATE` only. If present, will parse the value as [DecimalFormat](https://docs.oracle.com/javase/7/docs/api/java/text/DecimalFormat.html) or [SimpleDateFormat](https://docs.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html) patterns, respectively. |
| `unit()`         | The unit value to be used with `Types.DATE` calculations. See [Units](#units).                                                                                                                                                                                                              |                                                                                                                                                                          

- For convenience the `key` (and optional `type`) is first set in the constructor.
- `key` is required. `value` or `defaultValue` are required except when deleting.
-  For `Type.DATE`, the `now` keyword can be used as the property value.

## Functions

The following function are available:

| Function   | Example                                                                                                 | Description                                |
|:-----------|:--------------------------------------------------------------------------------------------------------|:-------------------------------------------|
| `calc()`   | `calc(ADD)`<br/>`calc(v -> v + 1)`<br/>`calc(SUB)`<br/>`calc(v -> v - 1)`                               | Perform a calculation with an entry value. |
| `modify()` | `modify("-foo", String::concat)`<br/>`modify("-foo", (v, s) -> v + s)`<br/>`modify((v, s) -> v.trim())` | Modify an entry value.                     |
| `delete()` | `delete()`                                                                                              | Delete an entry.                           |
## Units

The following units are available for `Types.DATE`:

* `Units.MILLISECOND`
* `Units.SECOND`
* `Units.MINUTE`
* `Units.HOUR`
* `Units.DAY`
* `Units.WEEK`
* `Units.MONTH`
* `Units.YEAR`

## Differences with the [ant PropertyFile task](https://ant.apache.org/manual/Tasks/propertyfile.html)

* The comments and layout of the original property file will not be preserved.
* The `jdkproperties` parameter is not implemented.
* The default `Types.DATE` pattern is `yyyy-MM-dd HH:mm` and not `yyyy/MM/dd HH:mm`.