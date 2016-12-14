# twitter-api changelog

## 0.7.9
* Add `statuses-lookup` restful method (thanks Ortuna)
* Fix `user-stream-api` version (thanks dotemacs)
* Restyle several instances of `(:use ...)` as `(:require ...)` (thanks dotemacs)
* Update dependencies:
  - `org.clojure/clojure "1.6.0"` -> `"1.8.0"`
  - `org.clojure/data.json "0.2.5"` -> `"0.2.6"`
  - `clj-oauth "1.5.1"` -> `"1.5.5"`

## 0.7.8
* Format rate-limit errors with the reset time (thanks holguinj)
* Implement app-only authentication (thanks holguinj)
  - Add `org.clojure/data.codec` dependency

## 0.7.7
* Add `site-stream` streaming method (thanks yogsototh)
* Add `statuses-update` example to README (thanks morganastra)
* Update dependencies:
  - `org.clojure/clojure "1.4.0"` -> `"1.6.0"`
  - `org.clojure/data.json "0.2.1"` -> `"0.2.5"`
  - `clj-oauth "1.4.0"` -> `"1.5.1"`

## 0.7.6
* Fix `friendships-show` examples in README (thanks paulbowler)
* Update `http.async.client` dependency to fix problem with uploading (thanks minleychris)
* Add `lists/ownerships` restful method (thanks ryane)

## 0.7.5
* changed all URLs to use https scheme (thanks tjoy)

## 0.7.4
* removed the dependency to adamwynne/clj-oauth and moved to main clj-oauth
* removed the user suggestion tests as the endpoint seems down

## 0.7.3
* removed some extraneous .json's in uris (thanks Takahiro Hozumi)

## 0.7.2
* upgraded the lein dependency to 2 (and removed the plugins - should be .lein/profiles.clj)
* upgraded the async library to 0.5.0

## 0.7.1
* bumped the search api version to 1.1 (thanks Aaron Steele)
* incorporated some recommendations from Kibit (thanks Seymores)

## 0.7.0
* BREAKING CHANGE: changed the REST function naming convention to the one described in the readme
* updated the tests to reflect the new function names
* added this changelog (recursive overload!)
