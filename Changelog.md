# Changelog

## 6.2 (2025-11-10)
- #### Changed
    - Updated dependencies

## 6.1 (2025-08-01)
- #### Changed
  - Updated dependencies  

## 6.0 (2025-03-28)
- #### Changed
  - Updated `json-compare` dependency to the next major version  
    - This changes the way JSON diffs are displayed  

## 5.23 (2025-03-26)
- #### Changed
  - Further enhance error message when file not found while using [ResourceUtils.java](src/main/java/io/jtest/utils/common/ResourceUtils.java)

## 5.22 (2025-03-25)
- #### Changed
  - Enhance error message when file not found while using [ResourceUtils.java](src/main/java/io/jtest/utils/common/ResourceUtils.java)

## 5.21 (2024-09-26)
- #### Changed
  - Update dependencies   
  - Aligned Jackson dependencies via Jackson BOM  

## 5.20 (2024-09-26)
- #### Changed
  - Updated json-compare dependency

## 5.19 (2024-09-20)
- #### Changed
  - Updated json-compare dependency
  
## 5.18 (2024-09-20)
- #### Changed
  - Updated dependencies

## 5.17 (2024-05-22)
- #### Changed
  - Change type of `PlainHttpResponse.headers` to `List<Map.Entry<String, Object>>`
  - Updated dependencies 
  
## 5.16 (2024-04-08)
- #### Changed
  - HttpResponseMatcher now supports `MatchCondition.DO_NOT_MATCH`

## 5.15 (2024-03-22)
- #### Changed
  - Updated dependencies


## 5.14 (2023-12-05)
- #### Changed
  - Deprecate Polling support. Use [Awaitility](https://github.com/awaitility/awaitility) instead. 

## 5.13 (2023-11-26)
- #### Changed
  - Upgrade dependencies (json-compare)  

## 5.12 (2023-11-16)
- #### Removed
  - Removed progress bar while polling. It can be integrated on a higher level via the Polling supplier. 

## 5.11 (2023-11-15)
- #### Changed
  - Added progress bar while polling (https://github.com/ctongfei/progressbar)

## 5.10 (2023-09-15)
- #### Changed
  - Update dependencies (json-compare)

## 5.9 (2023-06-06)
- #### Changed
  - Update dependencies (json-compare)

## 5.8 (2023-04-27)
- #### Changed
  - Use Integer.MAX_VALUE for ObjectMapper max depth, number and String limits

## 5.7 (2023-04-26)
- #### Changed
  - Update dependencies (json-compare:6.8, Jackson:2.15.0, Log4j:2.20.0)

## 5.6 (2023-01-10)
- #### Changed
  - Update dependencies (json-compare:6.7, xmlunit-core:2.9.1)  

## 5.5 (2022-12-31)
- #### Changed
  - Update dependencies (json-compare)  
  - Code refactoring  

## 5.4 (2022-12-22)
- #### Changed
  - Update dependencies (json-compare)    

## 5.3 (2022-12-19) 
- #### Changed
  - Added MatchCondition.REGEX_DISABLED
    - While using this condition, the default regex based matching mechanism will be disabled. All present regexes will be treated as plain text.          
  - Refactoring - optimized imports  
  
## 5.2 (2022-11-27)
- #### Removed  
  - Remove Polling`.getLastResult()` method     

## 5.1 (2022-11-24)
- #### Changed
  - Update dependencies (Jackson 2.14.1 & json-compare 6.3)
  
## 5.0 (2022-11-19)
- #### Removed
  - Removed SpEL support  
