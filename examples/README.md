# [Bld Property File](https://github.com/rife2/bld-property-file) Extension Examples

## Compile

First make sure the project up-to-date and compiled:

```shell
./bld download compile
```
## Run

To run the examples, issue one of the following command or combination there off.

```shell
./bld updateMajor run
./bld updateMinor run
./bld updatePatch run
./bld updateRelease run
```
Upon execution, the `version.properties` file will be created and displayed:

```shell
./bld updateMajor run
```

```text
+---------------------------+
| version.properties        |
+---------------------------+
#
#Sun Apr 02 23:51:39 PDT 2023
build.date=2023-04-02
version.major=1
version.minor=0
version.patch=0
```

Subsequent commands will reflect the modifications to the
`version.major`, `version.minor` or `version.patch` properties:

```shell
./bld upatePatch run
```

```text
+---------------------------+
| version.properties        |
+---------------------------+
#
#Sun Apr 02 23:55:09 PDT 2023
build.date=2023-04-02
version.major=1
version.minor=0
version.patch=10
```

[View the Examples Build](https://github.com/rife2/bld-property-file/blob/master/examples/src/bld/java/com/example/PropertyFileExampleBuild.java)