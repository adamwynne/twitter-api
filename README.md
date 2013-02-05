# twitter-api

This is an up-to-date twitter API wrapper that is based on the clojure http.async.client library. It offers the full taxonomy of twitter API's (streaming, search and restful) and has been tested to be working. The test coverage is reasonably complete, but I suppose more could be added.

## Why did I make this library?
* I felt the current offerings were a bit out of date
* I wanted the efficiency of the async comms libraries
* I needed some stuff from the headers returned by twitter (i.e. the rate-limiting stuff and etag)
* I wanted full API coverage (restful, streaming and search)

## Giants upon whose shoulders I have stood

* [http.async.client](https://github.com/neotyk/http.async.client) by Hubert Iwaniuk
* [clj-oauth](https://github.com/mattrepl/clj-oauth) by Matt Revelle

##Leiningen

###NOTE: this library is fully tested under Clojure 1.4

Just add the following to your project.clj file in the _dependencies_ section:

```
[twitter-api "0.7.2"]
```

## Usage

All of the functions follow Twitter's naming conventions; we convert a resource's path into the function name. For example:

* `https://api.twitter.com/1.1/account/settings` is available as `account-settings`
* `https://api.twitter.com/1.1/statuses/update_with_media` is available as `statuses-update-with-media`

Parameters are uniform across the functions. All calls can accept:

* `:oauth-creds` is the result of the `make-oauth-creds` function.
* `:params` is a map of parameters to pass, eg, `list_id=123` would be `{:list-id 123}`
* `:headers` adds or overrides any of the request headers sent to Twitter.
* `:verb` overrides the HTTP verb used to make the request, for resources that support it (eg, `account-settings`)
* `:callbacks` attaches a custom callback to the request.

All of the API calls will return the full HTTP response of the request, including headers.

## Examples

### RESTful calls

```clojure
(ns mynamespace
  (:use
   [twitter.oauth]
   [twitter.callbacks]
   [twitter.callbacks.handlers]
   [twitter.api.restful])
  (:import
   (twitter.callbacks.protocols SyncSingleCallback)))

(def my-creds (make-oauth-creds *app-consumer-key*
     			       		    *app-consumer-secret*
			       		        *user-access-token*
			       		        *user-access-token-secret*))

; simply retrieves the user, authenticating with the above credentials
; note that anything in the :params map gets the -'s converted to _'s
(users-show :oauth-creds my-creds :params {:screen-name "AdamJWynne"})

; supplying a custom header
(users-show :oauth-creds my-creds :params {:screen-name "AdamJWynne"} :headers {:x-blah-blah "value"})

; shows the users friends, without using authentication
(friendships-show :params {:screen-name "AdamJWynne"})

; use a custom callback function that only returns the body of the response
(friendships-show :callbacks (SyncSingleCallback. response-return-body 
	      		 		      response-throw-error
					      exception-rethrow)
	      :params {:screen-name "AdamJWynne"})

; upload a picture tweet with a text status attached, using the default sync-single callback
(statuses-update-with-media :oauth-creds *creds*
                   :body [(file-body-part "/pics/test.jpg")
                          (status-body-part "testing")])

```

### Streaming calls

```clojure
(ns mynamespace
  (:use
   [twitter.oauth]
   [twitter.callbacks]
   [twitter.callbacks.handlers]
   [twitter.api.streaming])
  (:require
   [clojure.data.json :as json]
   [http.async.client :as ac])
  (:import
   (twitter.callbacks.protocols AsyncStreamingCallback)))

(def my-creds (make-oauth-creds *app-consumer-key*
			       		        *app-consumer-secret*
			       		        *user-access-token*
			       		        *user-access-token-secret*))

; retrieves the user stream, waits 1 minute and then cancels the async call
(def ^:dynamic *response* (user-stream :oauth-creds my-creds))
(Thread/sleep 60000)
((:cancel (meta *response*)))

; supply a callback that only prints the text of the status
(def ^:dynamic 
     *custom-streaming-callback* 
     (AsyncStreamingCallback. (comp println #(:text %) json/read-json #(str %2)) 
     		 	      (comp println response-return-everything)
			      exception-print))

(statuses-filter :params {:track "Borat"}
		 :oauth-creds my-creds
		 :callbacks *custom-streaming-callback*)

```

## Notes

Unlike other API's, the parameters for each call are not hard-coded into their Clojure wrappers. I just figured that you could look them up on the dev.twitter.com and supply them in the :params map.

###Some points about making the calls:

* You can authenticate or not, by including or omitting the _:oauth-creds_ keyword and value. The value should be a _twitter.oauth.OauthCredentials_ structure (usually the result of the _twitter.oauth/make-oauth-creds_ function)
* The callbacks decide how the call will be carried out - be it a single or streaming call, or an async or sync call. See the twitter.callbacks.protocols to see how it works
* You can declare new methods that use different callbacks by either supplying them to the def-twitter-method macro, or inline at run time (via the _:callbacks_ key/vaue), or both!

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

Testing twitter.api.test.restful

Testing twitter.api.test.search

Testing twitter.api.test.streaming

Testing twitter.test-utils.core

Testing twitter.test.callbacks

Testing twitter.test.core

Testing twitter.test.creds

Testing twitter.test.request

Testing twitter.test.upload

Testing twitter.test.utils

Ran 48 tests containing 112 assertions.
0 failures, 0 errors.
```

Please note that the testing will take some time (about a minute or so) as its actually doing the calls to the twitter API's

## License

This library made open-source by [StreamScience](http://streamscience.co)

Follow [@AdamJWynne](http://twitter.com/#!/adamjwynne) and [@StreamScience](http://twitter.com/#!/streamscience) to save kittens and make rainbows.

Copyright (C) 2011 StreamScience

Distributed under the Eclipse Public License, the same as Clojure.
