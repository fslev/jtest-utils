# Changelog

## 5.14-SNAPSHOT

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
