# RSS-Generator

Automatically generate RSS for static websites.

## Requirements
- maven
- Java 8

## Build
```
mvn compile assembly:single
```

## Run
```
java -cp target/RSS-Generator-1.0-jar-with-dependencies.jar com.mrepol742.rssgenerator.App --domain https://mrepol742.github.io/ --publisher "Melvin Jones Repol"
```

## Options
```
--domain 
--publisher
--projectFolder [optional]
```