# twitter-api

This is an up-to-date twitter API wrapper that is based on the clojure http.async.client library. It offers the full taxonomy of twitter API's (streaming, search and restful) and has been tested to be working. The test coverage is reasonably complete, but I suppose more could be added.

## Why did I make this library?
* I felt the current offerings were a bit out of date
* I wanted the efficiency of the async comms libraries
* I needed some stuff from the headers returned by twitter (i.e. the rate-limiting stuff and etag)
* I wanted full API coverage (restful, streaming and search)

## Giant's upon whose shoulders I stood

* [http.async.client](https://github.com/neotyk/http.async.client) by Hubert Iwaniuk
* [clj-oauth](https://github.com/mattrepl/clj-oauth) by Matt Revelle

##Leiningen

Just add the following to your project.clj file in the _dependencies_ section:

```
[twitter-api "0.1.0"]
```

## Examples

### RESTful calls

```clojure
(ns mynamespace
  (:use
   [twitter.oauth]
   [twitter.callbacks]
   [twitter.api.restful])
  (:import
   (twitter.callbacks Callbacks)))

(def *creds* (make-oauth-creds *app-consumer-key*
			       *app-consumer-secret*
			       *user-access-token*
			       *user-access-token-secret*)

; simply retrieves the user, authenticating with the above credentials
; note that anything in the :params map gets the -'s converted to _'s
(show-user :oauth-creds *creds* :params {:screen-name "AdamJWynne"})

; supplying a custom header
(show-user :oauth-creds *creds* :params {:screen-name "AdamJWynne"} :headers {:x-blah-blah "value"})

; shows the users friends, without using authentication
(show-friends :params {:screen-name "AdamJWynne"})

; use a custom callback function that only returns the body of the response
(show-friends :callbacks (Callbacks. sync-return-body sync-error-thrower)
	      :params {:screen-name "AdamJWynne"})

```

### Streaming calls

```clojure
(ns mynamespace
  (:use
   [twitter.oauth]
   [twitter.callbacks]
   [twitter.api.streaming])
  (:require
   [clojure.contrib.json :as json]
   [http.async.client :as ac])
  (:import
   (twitter.callbacks Callbacks)))

(def *creds* (make-oauth-creds *app-consumer-key*
			       *app-consumer-secret*
			       *user-access-token*
			       *user-access-token-secret*)

; retrieves the user stream and println's each status as it comes in
(user-stream :oauth-creds *creds*)

; supply a callback that only prints the text of the status
(def *custom-streaming-callback* 
     (Callbacks. (call-on-stream #(println (:text (json/read-json %)))) 
     		 #(ac/status %))

(statuses-filter :params {:track "Borat"}
		 :oauth-creds *creds*
		 :callbacks *custom-streaming-callback*)

```

## Usage

The calls are declared with numerous macros that allow all sorts of fanciness. Note that unlike other API's, the parameters for each call are not hard-coded into their Clojure wrappers. I just figured that you could look them up on the dev.twitter.com and supply them in the :params map.

###Some points about making the calls:

* You can authenticate or not, by including or omitting the _:oauth-creds_ keyword and value. The value should be a _twitter.oauth.OauthCredentials_ structure (usually the result of the _twitter.oauth/make-oauth-creds_ function)
* The macros are designed so that you can define new functions, including new default params if you wish by composing functionality from the sub-macros/functions

## Building

Simply use leiningen to build the library into a jar with:

```
$ git clone git://github.com/adamwynne/twitter-api.git
Cloning into twitter-api...
remote: Counting objects: 167, done.
remote: Compressing objects: 100% (115/115), done.
remote: Total 167 (delta 68), reused 125 (delta 26)
Receiving objects: 100% (167/167), 33.60 KiB, done.
Resolving deltas: 100% (68/68), done.
$ cd twitter-api/
$ lein jar
```

## Testing

###NOTE:
You must populate the properties file *resources/test.config* before the tests will work. 

* To get the app consumer keys, simply use the https://dev.twitter.com/apps/<app-id> link and select your app
* To get the user keys, go to https://dev.twitter.com/apps/<app-id>/my_token

You can use leiningen to test the library using the following snippet

```
$ lein test
Testing twitter.test.api.restful
Testing twitter.test.api.search
Testing twitter.test.api.streaming
Testing twitter.test.core
Testing twitter.test.creds
Testing twitter.test.utils
Ran 25 tests containing 70 assertions.
0 failures, 0 errors.
```

Please note that the testing will take some time (about a minute or so) as its actually doing the calls to the twitter API's

## License

Copyright (C) 2011 Adam Wynne

Distributed under the Eclipse Public License, the same as Clojure.
