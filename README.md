# JTest Utils <sup>[![Stand With Ukraine](https://raw.githubusercontent.com/vshymanskyy/StandWithUkraine/main/badges/StandWithUkraine.svg)](https://vshymanskyy.github.io/StandWithUkraine)</sup>

[![Maven Central](https://img.shields.io/maven-central/v/io.github.fslev/jtest-utils.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.fslev%22%20AND%20a:%22jtest-utils%22)
![Build status](https://github.com/fslev/jtest-utils/workflows/Java%20CI%20with%20Maven/badge.svg?branch=main)
[![Coverage Status](https://coveralls.io/repos/github/fslev/jtest-utils/badge.svg?branch=main)](https://coveralls.io/github/fslev/jtest-utils?branch=main)


A set of Java testing utilities  

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