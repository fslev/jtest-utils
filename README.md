# JTest Utils <sup>[![Stand With Ukraine](https://raw.githubusercontent.com/vshymanskyy/StandWithUkraine/main/badges/StandWithUkraine.svg)](https://vshymanskyy.github.io/StandWithUkraine)</sup>

[![Maven Central](https://img.shields.io/maven-central/v/io.github.fslev/jtest-utils.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.fslev%22%20AND%20a:%22jtest-utils%22)
![Build status](https://github.com/fslev/jtest-utils/actions/workflows/maven.yml/badge.svg?branch=main)
[![Coverage Status](https://coveralls.io/repos/github/fslev/jtest-utils/badge.svg?branch=main)](https://coveralls.io/github/fslev/jtest-utils?branch=main)

A set of testing utilities for Java

## Brief

[JTest-Utils](https://github.com/fslev/jtest-utils) contains a set of utility classes which bump up your test framework
by adding some powerful features:

- **[Matching](#match)**
- **[Polling](#polling)** (Deprecated)
- **[Resource reader](#resource-reader)**

**[Real world examples](#real-world)**

#### Maven Central

```
<dependency>
  <groupId>io.github.fslev</groupId>
  <artifactId>jtest-utils</artifactId>
  <version>${latest.version}</version>
</dependency>

Gradle: compile("io.github.fslev:jtest-utils:${latest.version}")
```  

# <a name="match"></a> 1. Matching

- Match JSONs
- Match XMLs
- Match texts
- Match Objects
- Match HTTP responses

_... with specific matching conditions, regular expression data capture and polling support_

## <a name="match-jsons"></a> 1.1 Match JSONs

Based on [json-compare](https://github.com/fslev/json-compare)

_Example:_

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
ObjectMatcher.matchJson("Seems that JSONs do not match", expected, actual,
        MatchCondition.JSON_NON_EXTENSIBLE_OBJECT, MatchCondition.JSON_STRICT_ORDER_ARRAY); // matching fails
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

Seems that JSONs do not match
JSONs do not match
```

## <a name="match-xmls"></a> 1.2 Match XMLs

Based on [xmlunit](https://github.com/xmlunit/xmlunit)

_Example:_

```javascript
String expected = "<a id=\"1\"> <lorem>ipsum</lorem> </a>";
String actual = "<a id=\"2\"> <lorem>ipsum</lorem> </a>";
ObjectMatcher.matchXml("Seems that XMLs do not match",
        expected, actual, MatchCondition.XML_CHILD_NODELIST_LENGTH); // matching fails
==> 

java.lang.AssertionError: Seems that XMLs do not match
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

## <a name="match-texts"></a> 1.3 Match texts

Match texts with regex support:

```javascript
String expected = "lo.*sum \\Q(test)\\E";
String actual = "lorem \n ipsum (test)";
ObjectMatcher.matchString("Texts do not match", expected, actual); // successful matching
ObjectMatcher.matchString("Texts do match, actually", expected, actual, MatchCondition.DO_NOT_MATCH); // matching fails

-->
org.opentest4j.AssertionFailedError: Texts do match, actually

Strings match!
```

## 1.4 Match Objects

While matching any two Objects using `ObjectMatcher.match()`, one of the matching mechanisms from [above](#match) will be applied in this order:  

> if Objects can be converted to JSON, then match as JSONs
>> else, if Objects are XML strings, then match as XMLs
>>> else, match objects as texts

_Example:_

```javascript
String expected = "{\"a\":1}";
String actual = "{\"a\":1}";
ObjectMatcher.match("Objects were converted and matched as JSONs", expected, actual); // successful matching

expected = "<a>1</a>";
actual = "<a>1</a>";
ObjectMatcher.match("Objects were converted and matched as XMLs", expected, actual); // successful matching

expected = "{\"a\":i am not a json}";
actual = "{\"a\":i am not a json}";
ObjectMatcher.match("Objects were matched as texts", expected, actual); // successful matching
```

## 1.5 Match HTTP responses
If your test framework is querying REST services and makes assertions on the response data, then you might find this type of matching very useful.  
_Example:_
```javascript
String expected = "{\"status\": 200, \"headers\":[{\"Content-Length\":\"157\"}], \"body\":{\"employee\":\"John Johnson\"}}";
String actual = "{\"status\": 200, \"headers\":[{\"Content-Length\":\"157\"}], \"body\":{\"employee\":\"John Johnny\"}}";
ObjectMatcher.matchHttpResponse("Matching failure", from(expected), from(actual)); // matching fails

-->
org.opentest4j.AssertionFailedError: FOUND 1 DIFFERENCE(S):


_________________________DIFF__________________________
employee -> 
Expected value: "John Johnson" But got: "John Johnny"

HTTP Response bodies do not match!
Matching failure

JSONs do not match
```
First off, this is a show purpose example, where the actual HTTP response is represented as text, converted to `io.jtest.utils.matcher.http.PlainHttpResponse` and passed to the _ObjectMatcher.matchHttpResponse()_ method.  
Normally, the __actual__ object is a real HTTP response received from server and has many implementations depending on the HTTP client you are using. For this, you have to build the `PlainHttpResponse` object yourself, by using its builder: `PlainHttpResponse.Builder.create()`.  
The __expected__ object is usually represented as text and can be simply converted to `io.jtest.utils.matcher.http.PlainHttpResponse` by using any library with object deserialization support:  
  
_Example:_
```javascript
import io.jtest.utils.matcher.http.PlainHttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

public static PlainHttpResponse from(String content) {
    try {
        return new ObjectMapper().readValue(content, PlainHttpResponse.class);
    } catch (JsonProcessingException e) {
        throw new PlainHttpResponse.ParseException("Cannot parse content", e);
    }
}
```
The beautiful part while comparing HTTP responses is the fact that depending on the type of the response body, one of the matching mechanisms from above will be automatically applied.  
In other words, the HTTP response bodies / entities might be matched as [JSONs](#match-jsons), [XMLs](#match-xmls) or [texts](#match-texts).  
HTTP statuses and reasons are compared as [texts](#match-texts) while HTTP headers as [JSONs](#match-jsons).  

## 1.6 Match with Polling support (Deprecated)
All the matching mechanisms from above support polling.  
The most used case is when we need to compare HTTP responses from a service which delivers the desired data asynchronously.  
In this case we need to retry the execution of client request until the actual response is matched.  
```javascript
String expected = "{\"status\": 200, \"body\":{\"employee\":\"John Johnson\"}}";
ObjectMatcher.matchHttpResponse("Result not found", from(expected),
        () -> from(client.execute()),
        Duration.ofSeconds(30), 1000L);
```
  
  

## 1.7 Match and Capture
Only comparing objects is not enough in the real world of testing. Often, we need to parse the results that we've just matched, extract specific data and use it inside the next test step. Writing code for this, repeatedly, can be cumbersome.  
So, in case of successful matching, we can extract the data we need by using custom placeholders delimited by  
`~[` and `]` inside the __expected__ object: 
  
```javascript
String expected = "{\n" +
        "  \"copper\": [\n" +
        "    {\n" +
        "      \"beneath\": \"~[someValueForBeneath]\"\n" +
        "    }\n" +
        "  ],\n" +
        "  \"speak\": \"~[speakValue]\"\n" +
        "}";
String actual = "{\n" +
        "  \"copper\": [\n" +
        "    {\n" +
        "      \"beneath\": \"heard\",\n" +
        "      \"men\": -1365455482\n" +
        "    }\n" +
        "  ],\n" +
        "  \"speak\": -263355062.750,\n" +
        "  \"nr2\": 60.750\n" +
        "}";
        
Map<String, Object> capturedData = ObjectMatcher.matchJson(null, expected, actual);

assertEquals("heard", capturedData.get("someValueForBeneath"));
assertEquals("-263355062.750", capturedData.get("speakValue"));
```  
This feature is available for any flavour of object matching (String, Json or XML).    
  
# <a name="polling"></a> 2. Polling (Deprecated)
Retry an operation until desired result or timeout is reached:  
```javascript
Integer result = new Polling<Integer>()
        .duration(Duration.ofSeconds(30), 5L)
        .exponentialBackOff(1.0)
        .supplier(() -> generateRandomFromInterval(4, 7))
        .until(number -> number == 6).get();
        
assertEquals(6, result);
```

# <a name="resource-reader"></a> 3. Resource reader

Read resource as String from either classpath or absolute path:
```javascript
ResourceUtils.read("features/file.txt") // relative to classpath
ResourceUtils.read("/opt/project/hello/src/test/resources/features/file.txt") // absolute path
```
Recursively read all files from a directory, using extension filter: 
```javascript
Map<String, String> data = ResourceUtils.readDirectory("features/data", ".properties", ".txt", ".html", ".json");

assertEquals("lorem content", data.get("features/data/foo.txt"));
assertEquals("<html>text</html>", data.get("features/data/bar.html"));
assertEquals("{\"elephants\" : 1}", data.get("features/data/zoo/animals.json"));
```

# <a name="real-world"></a> 4. Real world examples
Explore [cucumber-jutils-tutorial](https://github.com/fslev/cucumber-jutils-tutorial) to discover how most of these features are used in real life test scenarios.

# Website
https://fslev.github.io/jtest-utils/
