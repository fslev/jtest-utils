# JTest Utils <sup>[![Stand With Ukraine](https://raw.githubusercontent.com/vshymanskyy/StandWithUkraine/main/badges/StandWithUkraine.svg)](https://vshymanskyy.github.io/StandWithUkraine)</sup>

[![Maven Central](https://img.shields.io/maven-central/v/io.github.fslev/jtest-utils.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.fslev%22%20AND%20a:%22jtest-utils%22)
![Build status](https://github.com/fslev/jtest-utils/workflows/Java%20CI%20with%20Maven/badge.svg?branch=main)
[![Coverage Status](https://coveralls.io/repos/github/fslev/jtest-utils/badge.svg?branch=main)](https://coveralls.io/github/fslev/jtest-utils?branch=main)


A set of testing utilities for Java  

## Brief
[JTest-Utils](https://github.com/fslev/jtest-utils) contains a set of utility classes which bump up your test framework by adding some extra basic features:  
- **[Matching objects](#match)**
- **[Polling](#polling)**
- **[SpEL parser](#spel)**
- **[Resource reader](#resource)**
- and others...

#### Maven Central
```
<dependency>
  <groupId>io.github.fslev</groupId>
  <artifactId>jtest-utils</artifactId>
  <version>${latest.version}</version>
</dependency>

Gradle: compile("io.github.fslev:jtest-utils:${latest.version}")
```  

# <a name="match"></a> Matching objects
- Match JSONs
- Match XMLs
- Match texts  

_... with regular expression support and specific matching conditions_

## Match JSONs
based on [json-compare](https://github.com/fslev/json-compare)

Example:  
```javascript
String expected = "{\n" +
        "  \"copper\": [\n" +
        "    {\n" +
        "      \"beneath\": \"heard\",\n" +
        "      \"jack\": false,\n" +
        "      \"men\": -1365455482\n" +
        "    },\n" +
        "    \"equipment\",\n" +
        "    false\n" +
        "  ],\n" +
        "  \"speak\": -263355062.75097084,\n" +
        "  \"basis\": 1670107599\n" +
        "}";
String actual = "{\n" +
        "  \"copper\": [\n" +
        "    {\n" +
        "      \"beneath\": \"heard\",\n" +
        "      \"men\": -1365455482\n" +
        "    },\n" +
        "    \"equipment\",\n" +
        "    false\n" +
        "  ],\n" +
        "  \"basis\": 1670107599\n" +
        "}";
ObjectMatcher.match("Seems that objects do not match", expected, actual); // assertion fails

==>

org.opentest4j.AssertionFailedError: FOUND 2 DIFFERENCE(S):


_________________________DIFF__________________________
copper -> 
Expected element from position 1 was NOT FOUND:
{
  "beneath" : "heard",
  "jack" : false,
  "men" : -1365455482
}

_________________________DIFF__________________________
Field 'speak' was NOT FOUND

Seems that objects do not match
JSONs do not match
```