# [Bld](https://github.com/rife2/rife2/wiki/What-Is-Bld) Extension to Create or Modify Properties Files

[![License (3-Clause BSD)](https://img.shields.io/badge/license-BSD%203--Clause-blue.svg?style=flat-square)](http://opensource.org/licenses/BSD-3-Clause)
[![GitHub CI](https://github.com/rife2/bld-property-file/actions/workflows/bld.yml/badge.svg)](https://github.com/rife2/bld-property-file/actions/workflows/bld.yml)

An extension for creating or modifying [property files](https://docs.oracle.com/javase/tutorial/essential/environment/properties.html) with [bld](https://github.com/rife2/rife2/wiki/What-Is-Bld). It is inspired by the [ant PropertyFile task](https://ant.apache.org/manual/Tasks/propertyfile.html).

```java
@BuildCommand
public void updateMajor() throws Exception {
    new PropertyFileOperation(this)
            .file("version.properties")
            .entry(new Entry("version.major").defaultValue(0).type(Types.INT).operation(Operations.ADD))
            .entry(new Entry("version.minor").value(0))
            .entry(new Entry("version.patch").value(0))
            .entry(new Entry("build.date").value("now").pattern("yyyy-MM-dd").type(Types.DATE))
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

| Attribute       | Description                                                      | Required |
|:----------------|:-----------------------------------------------------------------|:---------|
| `file`          | The location of the properties files to modify.                  | Yes      |
| `comment`       | Comment to be inserted at the top of the properties file.        | No       |       
| `failOnWarning` | If set to `true`, will cause executiion to fail on any warnings. | No       |        

## Entry

The `Entry` class is used to specify modifications to be made to the [properties file](https://docs.oracle.com/javase/tutorial/essential/environment/properties.html).

| Attribute      | Description                                                                                                                                                                                                                                                                                 |
|:---------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `key`          | The name of the property name/value pair.                                                                                                                                                                                                                                                   |                                                                                                                                                                                                                                                   
| `value`        | The value of the property.                                                                                                                                                                                                                                                                  |                                                                                                                                                                                                                                                                  
| `defaultVlaue` | The initial value to set for the property if not already defined. For `Type.DATE`, the `now` keyword can be used.                                                                                                                                                                           |                                                                                                                                                                           
| `type`         | Tread the value as `Types.INT`, `Types.DATE`, or `Types.STRING`. If none specified, `Types.STRING` is assumed.                                                                                                                                                                              |                                                                                                                                                                              
| `operation`    | See [operations](#operations).                                                                                                                                                                                                                                                              |                                                                                                                                                                                                                                                              
| `pattern`      | For `Types.INT` and `Types.DATE` only. If present, will parse the value as [DecimalFormat](https://docs.oracle.com/javase/7/docs/api/java/text/DecimalFormat.html) or [SimpleDateFormat](https://docs.oracle.com/javase/6/docs/api/java/text/SimpleDateFormat.html) patterns, respectively. |
|  `unit`        | The unit value to be applied to `Operations.ADD` and `Operations.SUBTRACT` for `Types.DATE`. See [Units](#units).                                                                                                                                                                           |                                                                                                                                                                          

`key` is required. `value` or `defaultValue` are required unless the `operation` is `Operations.DELETE`.

## Operations

The following operations are available:

| Operation              | Description                                                               |
|:-----------------------|:--------------------------------------------------------------------------|
| `Operations.ADD`       | Adds a value to an entry.                                                 |
| `Operations.DELETE`    | Deletes an entry.                                                         |
| `Operations.SET`       | Sets the entry value. This is the default operation.                      |
|  `Operations.SUBTRACT` | Subtracts a value from the entry. For `Types.INT` and `Types.DATE` only.  |

## Units

The following units are available for `Types.DATE` with `Operations.ADD` and `Operations.SUBTRACT`:

* `Units.MILLISECOND`
* `Units.SECOND`
* `Units.MINUTE`
* `Units.HOUR`
* `Units.DAY`
* `Units.WEEK`
* `Units.MONTH`
* `Units.YEAR`

## Rules

The rules used when setting a property value are:

* If only `value` is specified, the property is set to it regardless of its previous value.
* If only `defaultValue` is specified and the property previously existed, it is unchanged.
* If only `defaultValue` is specified and the property did not exist, the property is set to `defaultValue`.
* If `value` and `defaultValue` are both specified and the property previously existed, the property is set to `value`.
* If `value` and `defaultValue` are both specified and the property did not exist, the property is set to `defaultValue`.

Operations occur after the rules are evaluated.

## Differences with the [ant PropertyFile task](https://ant.apache.org/manual/Tasks/propertyfile.html)

* The comments and layout of the original property file will not be preserved.
* The `jdkproperties` parameter is not implemented.
* The default `Types.DATE` pattern is `yyyy-MM-dd HH:mm` and not `yyyy/MM/dd HH:mm`.