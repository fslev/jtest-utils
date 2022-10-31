# Changelog

### 3.2-SNAPSHOT

### 3.1 (2022-10-31)
- #### Changed
  - reformat the custom AssertionError message from a failed object matching  
  - update dependencies

### 3.0 (2022-10-16)
- #### Removed
  - Unnecessary loggers were removed  
  - HTTP, SQL, JSCH And Shell client support was entirely removed. Test frameworks should define their own clients  
  
- #### Changed
  - Update dependencies 

### 2.9 (2022-10-05)
- #### Changed
  - Update dependencies

### 2.8 (2022-10-04)
- #### Changed
  - Reformat Assertion error message
  - Update dependencies
  - Upgrade to new major version of json-compare (differences support)

### 2.7
Refactor Assertion error messages (enable assertion error difference support)  
Upgrade dependencies  

### 2.6
Upgrade dependencies (update json-compare)     

### 2.5
Upgrade dependencies 
Moved JsonUtils to json-compare library  


### 2.4
Upgrade dependencies (json-compare)  

### 2.3  
Remove unused duration methods from Polling class  
Refactor message when XML and String matching fails  
Update Json-Compare dependency  

### 2.2  
Code refactoring  
PlainHttpResponse conversion from null should throw exception    

### 2.1
Update dependencies  

### 2.0
Remove logging from ShellClient  
Remove Polling method with duration of type Integer  
Refactor & rename HttpResponseWrapper to PlainHttpResponse  
Update dependencies  

### 1.66
Optimization: Use buffered reader for Java process input / error stream   

### 1.65
Read Java process input / error stream using UTF-8 encoding  (ShellClient)
Update dependencies    

### 1.64
Refactor SpELParser    

### 1.63
Refactor HttpClient    

### 1.62
Refactor how HttpClient sets URI path (do not encode path)       

### 1.61
Fix hint message about unintentional regexes  

### 1.60
Enhance AssertionError message with hint about unintentional regexes    

### 1.59
Update dependencies  

### 1.58
Update dependencies  

### 1.57
Update Jackson dependencies  
Update Log4j to 2.17.0 (Major vulnerability fix)  

### 1.56
Use exact big decimals at JsonNode conversion      

### 1.55
Add option to replace Http client header  
Update dependencies 
- Log4j vulnerability fix

### 1.54
Update dependencies  

### 1.53
Update dependencies
Use Junit Jupiter API

### 1.52
Update dependencies  
Json-Compare - bugfix  

### 1.51
Update dependencies  
Json path support  

### 1.50
Bug fixes    
Update dependencies  

### 1.49
JsonUtils converts Object to JsonNode  
Update dependencies  

### 1.48
Update dependencies  

### 1.47
Update dependencies  

### 1.46
HttpClient execute() throws IOException   

### 1.45
Optimize polling  

### 1.44
Refactor polling  
Remove SupplierUtils  

### 1.43
Refactor Polling  

### 1.42
Update dependencies  

### 1.41
Refactor Http response conversion error message  
Encode Http request and response entity using StandardCharsets.UTF_8  
Upgrade dependencies  

### 1.40
Refactoring  

### 1.39
Enhance HTTP client with context  
Refactor Http response matching mechanism  
Remove StringFormat.toColumns() method  

### 1.38
Update json-compare  

### 1.37
Update json-compare  

### 1.36
XML matcher bug fix  
Update json-compare  

### 1.35
Update json-compare      

### 1.34
Crop long messages  

### 1.33
Handle XML parsing errors  

### 1.32
XMLMatcher - Bug fix  

### 1.31
Refactoring  
Ignore XML namespaces match  

### 1.30
Bug fix  

### 1.29
Ignore error if entity cannot be consumed  

### 1.28
Update dependencies  

### 1.27
Update dependencies  

### 1.26
Update dependencies  

### 1.25
Update dependencies  

### 1.24
Bump up dependency versions  
Refactor StringParser  

### 1.23
Refactor error messages  
Upgrade dependencies  

### 1.22
Small refactoring of SQL client  
Upgrade dependencies  
Enhance error messages while matching HTTP responses    

### 1.21
Enhance documentation    
Upgrade dependencies  

### 1.20
Upgrade dependencies  

### 1.19
Code optimization  

### 1.18
Fix JsonUtils pretty print  

### 1.17
Use Java 8 as base Java version for release  

### 1.16
Refactoring  
Java 8 support  
Upgrade dependencies  

### 1.15
Refactoring  

### 1.14
Refactor polling mechanism  

### 1.13
Refactoring    

### 1.12
Logger is static  
Refactor & enhance ShellClient  

### 1.11
Add SpEL parser support  

### 1.10
Refactoring  

### 1.9
Upgrade json-compare  

### 1.8
Refactor assertion error message  

### 1.7
Update doc  
Upgrade dependencies  

### 1.6
Refactor headers  

### 1.5
Upgrade dependencies  
Refactor HTTP client headers    

### 1.4
Refactor HttpClient - check for nulls    

### 1.3
Refactor HttpClient  

### 1.2
Refactored error handling      
Upgrade dependencies  
Refactor HTTP client  
Add support for nonempty headers and query params    

### 1.1
Upgrade dependencies  

### 1.0
Java Utilities for testing