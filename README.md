# twitter-client

This is an up-to-date twitter API wrapper that is based on the clojure async.http.client library. It offers the full taxonomy of twitter API's (streaming, search and restful) and has been tested to be working. The tests coverage is reasonably complete, but I suppose more could be added.

## Usage



The calls are declared with numerous macros that allow all sorts of fanciness. Note that unlike other API's, the parameters for each call are not hard-coded into their Clojure wrappers. I just figured that you could look them up on the dev.twitter.com and supply them in the :params map.

```clojure
(def *creds* 
(show-user :oauth-creds *creds* :params {:screen-name "AdamJWynne"})
```

## Building

Simply use leiningen to build the library into a jar with:
```
$ lein jar
```

## Testing

*NOTE* You must populate the properties file resources/test.config before the tests will work. 

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
