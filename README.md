# JTest Utils

[![Maven Central](https://img.shields.io/maven-central/v/io.github.fslev/jtest-utils.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.fslev%22%20AND%20a:%22jtest-utils%22)
![Build status](https://github.com/fslev/jtest-utils/actions/workflows/maven.yml/badge.svg?branch=main)
[![Coverage Status](https://coveralls.io/repos/github/fslev/jtest-utils/badge.svg?branch=main)](https://coveralls.io/github/fslev/jtest-utils?branch=main)

> Match JSON, XML, plain text, and HTTP responses in Java tests — and capture values out of the actual data on a successful match.

`jtest-utils` is a small library for Java tests. It compares expected against actual values via a single
facade (`ObjectMatcher`), supports regex on every scalar by default, lets you tighten or invert the
comparison with `MatchCondition` flags, and returns a map of values captured from the actual side via
`~[name]` placeholders embedded in the expected side.  

## Features

- One facade for JSON, XML, plain-text, and HTTP-response matching: `ObjectMatcher`.
- `~[name]` capture placeholders inside the expected value — values are returned in a `Map<String, Object>`.
- Java regex on every scalar by default; quote literals with `\Q…\E` or pass `REGEX_DISABLED` to opt out.
- Compose flags (`MatchCondition`) to require non-extensible objects/arrays, strict array order, equal XML child counts/order, equal attribute counts, or to invert the assertion entirely.
- Soft-assertion failures (every difference is reported, not just the first) via `org.opentest4j.AssertionFailedError`.
- Read fixture files from the classpath or an absolute filesystem path: `ResourceUtils.read`, `readYaml`, `readDirectory`.

## Requirements

- Java 17 or later

## Installation

Maven:

```xml
<dependency>
  <groupId>io.github.fslev</groupId>
  <artifactId>jtest-utils</artifactId>
  <version>${latest.version}</version>
</dependency>
```

Gradle:

```groovy
testImplementation 'io.github.fslev:jtest-utils:${latest.version}'
```

## Quickstart

```java
import io.jtest.utils.matcher.ObjectMatcher;

import java.util.Map;

String expected = """
        {
          "copper": [
            {
              "beneath": "~[someValueForBeneath]"
            }
          ],
          "speak": "~[speakValue]"
        }""";
String actual = """
        {
          "copper": [
            {
              "beneath": "heard",
              "men": -1365455482
            }
          ],
          "speak": -263355062.750,
          "nr2": 60.750
        }""";

Map<String, Object> captured = ObjectMatcher.matchJson(null, expected, actual);
captured.get("someValueForBeneath"); // "heard"
captured.get("speakValue");          // "-263355062.750"
```

The match is lenient by default — `actual` may have extra fields and arrays may be out of order. Tighten with
[`MatchCondition`](#match-conditions) flags.

## Matching

All entrypoints live on `io.jtest.utils.matcher.ObjectMatcher`. Each returns a `Map<String, Object>` of
captured placeholder values (empty if none were defined) and throws `AssertionError` on mismatch.

Under the hood: JSON matching is delegated to [json-compare](https://github.com/fslev/json-compare),
XML matching to [XMLUnit](https://github.com/xmlunit/xmlunit), and string matching uses Java's built-in
`java.util.regex`. `matchHttpResponse` routes each response component to one of the above (statuses and
reasons → string, headers → JSON, body → auto-detect), and `match` itself simply tries JSON, then XML,
then string.

| Method | Purpose |
|---|---|
| [`matchJson`](#matchjson) | Match as JSON. Either side may be a JSON string, `JsonNode`, `Map`, `List`, or POJO. |
| [`matchXml`](#matchxml) | Match as XML. Either side may be an XML string or `org.w3c.dom.Node`. |
| [`matchString`](#matchstring) | Match as text — regex by default; pass `REGEX_DISABLED` for literal equality. |
| [`match`](#match) | Auto-detect: try JSON, then XML, then text. |
| [`matchHttpResponse`](#matchhttpresponse) | Match HTTP responses by status / reason / headers / body. |

### `matchJson`

```java
String expected = """
        {
          "copper": [
            { "beneath": "heard", "jack": false, "men": -1365455482 },
            "equipment",
            false
          ],
          "speak": -263355062.75097084,
          "basis": 1670107599
        }""";
String actual = """
        {
          "copper": [
            { "beneath": "heard", "men": -1365455482 },
            "equipment",
            false
          ],
          "speak": -263355062.750,
          "nr1": 62.750,
          "nr2": 60.750
        }""";

ObjectMatcher.matchJson(
        "Seems that JSONs do not match",
        expected, actual,
        MatchCondition.JSON_NON_EXTENSIBLE_OBJECT,
        MatchCondition.JSON_STRICT_ORDER_ARRAY); // throws AssertionError
```

Either side may be a JSON string, a Jackson `JsonNode`, a `Map`, a `List`, or any POJO.

### `matchXml`

```java
String expected = "<a id=\"1\"> <lorem>ipsum</lorem> </a>";
String actual   = "<a id=\"2\"> <lorem>ipsum</lorem> </a>";

ObjectMatcher.matchXml(
        "Seems that XMLs do not match",
        expected, actual,
        MatchCondition.XML_CHILD_NODELIST_LENGTH); // throws AssertionError
```

Either side may be an XML string or an `org.w3c.dom.Node`. Whitespace inside element content is ignored.

### `matchString`

```java
String expected = "lo.*sum \\Q(test)\\E";
String actual   = "lorem \n ipsum (test)";

ObjectMatcher.matchString("Texts do not match", expected, actual);
// passes — `expected` is a regex with DOTALL semantics; \Q…\E quotes the literal "(test)"
```

Pass `MatchCondition.REGEX_DISABLED` to compare literally instead.

### `match`

Auto-detects the content type — tries JSON, then XML, then plain text.

```java
ObjectMatcher.match(null, "{\"a\":1}", "{\"a\":1}");           // matched as JSON
ObjectMatcher.match(null, "<a>1</a>", "<a>1</a>");             // matched as XML
ObjectMatcher.match(null, "{\"a\":i am not a json}",
                          "{\"a\":i am not a json}");          // matched as text (JSON parse failed)
```

Use this when the content type is not known up front; otherwise prefer the typed methods above for
clearer failure messages.

### `matchHttpResponse`

```java
String expected = "{\"status\": 200, \"headers\":[{\"Content-Length\":\"157\"}], \"body\":{\"employee\":\"John Johnson\"}}";
String actual   = "{\"status\": 200, \"headers\":[{\"Content-Length\":\"157\"}], \"body\":{\"employee\":\"John Johnny\"}}";

ObjectMatcher.matchHttpResponse("Matching failure", from(expected), from(actual));
```

The match fails and the assertion error spells out which component differed and why:

```
FOUND 1 DIFFERENCE(S):

_________________________DIFF__________________________
$.employee
Expected value: "John Johnson" But got: "John Johnny"

HTTP Response bodies do not match!
Matching failure

JSONs do not match
```

Only the components set on `expected` are asserted on — leave a component unset to skip it. The body is
matched via the same auto-detect logic as `match` above, so JSON, XML, and plain-text bodies all work
transparently.

`PlainHttpResponse` is a record (Java 17). Accessors are `status()`, `reasonPhrase()`, `entity()`, and
`headers()`. Build one with `PlainHttpResponse.Builder.create()…build()` for hand-crafted expected values,
or deserialize one from a JSON document with the shape
`{ "status": …, "reason": …, "headers": [{…: …}, …], "body": … }`. The `from(...)` helper above is a
project-local Jackson convenience:

```java
public static PlainHttpResponse from(String content) {
    try {
        return new ObjectMapper().readValue(content, PlainHttpResponse.class);
    } catch (JsonProcessingException e) {
        throw new PlainHttpResponse.ParseException("Cannot parse content", e);
    }
}
```

## Capture placeholders

A placeholder of the form `~[name]` inside the expected value matches any text in the actual value at that
position; the matched substring is returned in the result map under `name`.

```java
Map<String, Object> captured = ObjectMatcher.matchString(
        null, "Hello, ~[who]!", "Hello, world!");
captured.get("who"); // "world"
```

A placeholder that occupies an entire scalar (e.g. an entire JSON value, an entire attribute, an entire
string) captures the raw value, not its string form. Otherwise the captured value is the matched substring.

## Match conditions

Pass any combination as the trailing varargs of an `ObjectMatcher.matchXxx` call.

| Flag | Effect |
|---|---|
| `JSON_NON_EXTENSIBLE_OBJECT` | Actual JSON object must have no extra fields. |
| `JSON_NON_EXTENSIBLE_ARRAY`  | Actual JSON array must have no extra elements. |
| `JSON_STRICT_ORDER_ARRAY`    | Actual JSON array elements must be in the same order as expected. |
| `XML_CHILD_NODELIST_LENGTH`     | Equal child element counts at every level. |
| `XML_CHILD_NODELIST_SEQUENCE`   | Child elements must be in the same order. |
| `XML_ELEMENT_NUM_ATTRIBUTES`    | Each element must have the same number of attributes. |
| `DO_NOT_MATCH` | Invert the assertion: pass on mismatch, fail on match. |
| `DO_NOT_MATCH_HTTP_RESPONSE_BY_STATUS` / `_BY_REASON` / `_BY_HEADERS` / `_BY_BODY` | Per-component negation for HTTP responses (the plain `DO_NOT_MATCH` is ambiguous there). |
| `REGEX_DISABLED` | Compare scalars by literal equality instead of as regex. |

## Resource reader

`io.jtest.utils.common.ResourceUtils` reads files from either the classpath (relative path) or the filesystem
(absolute path); the same call works for both.

```java
import io.jtest.utils.common.ResourceUtils;

// Plain text from classpath
ResourceUtils.read("foobar/file1.txt"); // "some content 1\nsome content 2\n"

// YAML to a tree of Maps and Lists
Map<String, Object> cfg = ResourceUtils.readYaml("yaml/config.yaml");

// Recursive directory read with extension filter
Map<String, String> files = ResourceUtils.readDirectory("foobar/dir1", ".properties");
files.get("foobar/dir1/test2.properties"); // "pass"
```

`readProps(String)` returns `java.util.Properties`; `getFilesFromDir(String, String...)` returns the relative
paths only; `getFileName(String)` returns the last path segment. Missing files surface as `IOException`.

## Other utilities

The library also exposes a few small helpers (see Javadoc for full method lists):

- `io.jtest.utils.common.StringFormat` — substitute `#[name]` placeholders in a string from a `Map`.
- `io.jtest.utils.common.StringParser` — extract values from regex capture groups; substitute placeholders with custom delimiters.
- `io.jtest.utils.common.RegexUtils` — check whether a string is a syntactically valid regex; list the metacharacters it contains.
- `io.jtest.utils.common.XmlUtils` — parse a string to a DOM `Node`, serialize a `Node`, walk an XML tree applying a function to each element / attribute / text node.

## Real-world examples

For end-to-end usage in a test framework, see [cucumber-jutils-tutorial](https://github.com/fslev/cucumber-jutils-tutorial).

## License

[Apache License 2.0](LICENSE)
