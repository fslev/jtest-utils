# JTest Utils <sup>[![Stand With Ukraine](https://raw.githubusercontent.com/vshymanskyy/StandWithUkraine/main/badges/StandWithUkraine.svg)](https://vshymanskyy.github.io/StandWithUkraine)</sup>

[![Maven Central](https://img.shields.io/maven-central/v/io.github.fslev/jtest-utils.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.fslev%22%20AND%20a:%22jtest-utils%22)
![Build status](https://github.com/fslev/jtest-utils/workflows/Java%20CI%20with%20Maven/badge.svg?branch=main)
[![Coverage Status](https://coveralls.io/repos/github/fslev/jtest-utils/badge.svg?branch=main)](https://coveralls.io/github/fslev/jtest-utils?branch=main)


A set of testing utilities for Java  

## Brief
[JTest-Utils](https://github.com/fslev/jtest-utils) contains a set of utility classes which bump up your test framework by adding some extra useful features:  
- **[Matching objects](#match)**
- **[Polling](#polling)**
- **[SpEL parser](#spel)**
- **[Resource reader](#resource)**
...and others

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
Based on [json-compare](https://github.com/fslev/json-compare)

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
        "  \"speak\": -263355062.750,\n" +
        "  \"nr1\": 62.750,\n" +
        "  \"nr2\": 60.750\n" +
        "}";
ObjectMatcher.matchJson("Seems that objects do not match", expected, actual,
        MatchCondition.JSON_NON_EXTENSIBLE_OBJECT, MatchCondition.JSON_STRICT_ORDER_ARRAY); // assertion fails
==>

org.opentest4j.AssertionFailedError: FOUND 4 DIFFERENCE(S):

_________________________DIFF__________________________
copper -> JSON ARRAY elements differ at position 1:
{
  "beneath" : "heard",
  "jack" : false,
  "men" : -1365455482
}
________diffs________
Field 'jack' was NOT FOUND

_________________________DIFF__________________________
speak -> 
Expected value: -263355062.75097084 But got: -263355062.750

_________________________DIFF__________________________
Field 'basis' was NOT FOUND

_________________________DIFF__________________________
Actual JSON OBJECT has extra fields

Seems that objects do not match
JSONs do not match
```
## Match XMLs
Based on [xmlunit](https://github.com/xmlunit/xmlunit)

Example:
```javascript
String expected = "<a id=\"1\"> <lorem>ipsum</lorem> </a>";
String actual = "<a id=\"2\"> <lorem>ipsum</lorem> </a>";
ObjectMatcher.matchXml("Seems that objects do not match",
        expected, actual, MatchCondition.XML_CHILD_NODELIST_LENGTH); // assertion fails
==> 

java.lang.AssertionError: Seems that objects do not match
XMLs do not match

Matching is by default done using regular expressions.
If expected object contains any unintentional regexes, then quote them between \Q and \E delimiters.


Expected: Expected attribute name '/a[1]/@id' - comparing <a...> at /a[1]/@id to <a...> at /a[1]:
<a id="1">
  <lorem>ipsum</lorem>
</a>
     but: result was: 
<a id="2">
  <lorem>ipsum</lorem>
</a>
```
