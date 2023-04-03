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
```
Upon execution, the content of the `verison.properties` file will be displayed, reflecting the modification to the
`version.major`, `version.minor` or `version.patch` properties.

```shell
./bld updatePatch run
```

```shell
+---------------------------+
| version.properties        |
+---------------------------+
#
#Sun Apr 02 17:19:10 PDT 2023
version.major=1
version.minor=0
version.patch=1
```
