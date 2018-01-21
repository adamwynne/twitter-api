# twitter-api

This is a Clojure library for accessing the Twitter API using [`http.async.client`](https://github.com/cch1/http.async.client).

It endeavors to implement all Twitter APIs:
* [Streaming](https://dev.twitter.com/streaming/public)
* [Search](https://dev.twitter.com/rest/public/search)
* [REST](https://dev.twitter.com/rest/reference)

It is tested by interacting with the live Twitter API.

## Why did I make this library?

* I felt the current offerings were a bit out of date
* I wanted the efficiency of the async comms libraries
* I needed some stuff from the headers returned by twitter (i.e. the rate-limiting stuff and etag)
* I wanted full API coverage (restful, streaming and search)

## Giants upon whose shoulders I have stood

* [`http.async.client`](https://github.com/cch1/http.async.client) by Hubert Iwaniuk
* [`clj-oauth`](https://github.com/mattrepl/clj-oauth) by Matt Revelle

## Leiningen

`twitter-api` is published on [Clojars](https://clojars.org/twitter-api).
Add the following to your `project.clj`'s `:dependencies`:

    [twitter-api "1.8.0"]


## Usage

All of the functions follow Twitter's naming conventions; we convert a resource's path into the function name.
For example:

* `https://api.twitter.com/1.1/account/settings` is available as `account-settings`
* `https://api.twitter.com/1.1/statuses/update_with_media` is available as `statuses-update-with-media`

Parameters are uniform across the functions. All calls accept:

* `:oauth-creds` is the result of the `make-oauth-creds` function.
* `:params` is a map of parameters to pass, e.g., `?list_id=123` would be `{:list-id 123}`
* `:headers` adds or overrides any of the request headers sent to Twitter.
* `:verb` overrides the HTTP verb used to make the request, for resources that support it (e.g., `account-settings`)
* `:callbacks` attaches a custom callback to the request.

All of the API calls return the full HTTP response, including headers, so in most cases you will want to get the response's `:body` value.

## Examples

### RESTful calls

```clojure
(ns mynamespace
  (:use [twitter.oauth]
        [twitter.callbacks]
        [twitter.callbacks.handlers]
        [twitter.api.restful])
  (:import [twitter.callbacks.protocols SyncSingleCallback]))

(def my-creds (make-oauth-creds *app-consumer-key*
                                *app-consumer-secret*
                                *user-access-token*
                                *user-access-token-secret*))

; simply retrieves the user, authenticating with the above credentials
; note that anything in the :params map gets the -'s converted to _'s
(users-show :oauth-creds my-creds :params {:screen-name "AdamJWynne"})

; supplying a custom header
(users-show :oauth-creds my-creds :params {:screen-name "AdamJWynne"} :headers {:x-blah-blah "value"})

; shows the users friends
(friendships-show :oauth-creds my-creds
                  :params {:target-screen-name "AdamJWynne"})

; use a custom callback function that only returns the body of the response
(friendships-show :oauth-creds my-creds
                  :callbacks (SyncSingleCallback. response-return-body
                                                  response-throw-error
                                                  exception-rethrow)
                  :params {:target-screen-name "AdamJWynne"})

; post a text status, using the default sync-single callback
(statuses-update :oauth-creds my-creds
                 :params {:status "hello world"})

; upload a picture tweet with a text status attached, using the default sync-single callback
; (this method has been deprecated by twitter.)
(statuses-update-with-media :oauth-creds my-creds
                            :body [(file-body-part "/pics/test.jpg")
                                   (status-body-part "testing")])

;; upload a picture tweet.
(let [media-id (-> (media-upload-chunked :oauth-creds my-creds
                                         :media "/pics/test.jpg"
                                         :media-type "image/jpeg")
                   :body
                   :media_id)]
  (statuses-update :oauth-creds my-creds :params {:status "hi!" :media-ids [media-id]}))

;; upload a video tweet.
(let [media-id (-> (media-upload-chunked :oauth-creds my-creds
                                         :media "/vids/test.mp4"
                                         :media-type "video/mp4")
                   :body
                   :media_id)]
  (statuses-update :oauth-creds my-creds :params {:status "hi!" :media-ids [media-id]}))
```

### Streaming calls

```clojure
(ns mynamespace
  (:use [twitter.oauth]
        [twitter.callbacks]
        [twitter.callbacks.handlers]
        [twitter.api.streaming])
  (:require [clojure.data.json :as json]
            [http.async.client :as ac])
  (:import [twitter.callbacks.protocols AsyncStreamingCallback]))

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

## Notes on making API calls

* Unlike other APIs, the parameters for each call are not hard-coded into their Clojure wrappers.
  I just figured that you could look them up on the [dev.twitter.com/docs](https://dev.twitter.com/docs) and supply them in the `:params` map.
* You can authenticate or not, by including or omitting the `:oauth-creds` keyword and value.
  The value should be a `twitter.oauth.OauthCredentials` structure (usually the result of the `twitter.oauth/make-oauth-creds` function)
* The callbacks decide how the call will be carried out - be it a single or streaming call, or an async or sync call.
  Read [`twitter.callbacks.protocols`](src/twitter/callbacks/protocols.clj) to see how it works
* You can declare new methods that use different callbacks by either supplying them to the `def-twitter-method` macro,
  or inline at run time (via the `:callbacks` key/value), or both!
* Unless you supply a `:client my-client` pair, the library will use a memoized client.
  This normally won't be a problem if you don't need to exit gracefully (i.e., without `(System/exit 0)` or Ctrl-C),
  but otherwise [may cause your program to hang unexpectedly](https://github.com/adamwynne/twitter-api/issues/74).
  To avoid this, you must do a bit more bookkeeping, via either one of these workarounds:

  1. Create and close your own client:

     ```clojure
     (with-open [client (http.async.client/create-client)]
       (users-show :client client :oauth-creds my-creds :params {:screen-name "AdamJWynne"}))
     ```
  2. When you're done making API calls, close the memoized client:

     ```clojure
     (http.async.client/close (twitter.core/default-client))
     ```

## Building

Use leiningen to build the library into a jar with:

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

Which produces a jar file at `target/twitter-api-*.jar`.

## Testing

The tests require that credentials be provided via environment variables with the following names:

```sh
export CONSUMER_KEY=l4VAFAKEFAKEFAKEpy7R7
export CONSUMER_SECRET=dVnTimJtFAKEFAKEFAKEFAKEFAKEFAKEBVYnO91BR1G
export SCREEN_NAME=twitterapibot
export ACCESS_TOKEN=195648015-OIHb87zuFAKEFAKEFAKEFAKEFAKEFAKEb5aLUMYo
export ACCESS_TOKEN_SECRET=jsVg1HFAKEFAKEFAKEFAKEFAKEFAKE4yfOLC5cXA9fcXr
```

Then simply run `lein test`, which takes about a minute since many of the tests involve calling the Twitter API and waiting for an appropriate response.

If all tests completed successfully, the test output will end with a message like:

    Ran 47 tests containing 123 assertions.
    0 failures, 0 errors.

## License

This library made open-source by [StreamScience](http://streamscience.co)

Follow [@AdamJWynne](https://twitter.com/adamjwynne) and [@StreamScience](https://twitter.com/streamscience) to save kittens and make rainbows.

Copyright (C) 2011 StreamScience

Distributed under the Eclipse Public License, the same as Clojure.
