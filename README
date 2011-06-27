# twitter-client

This is an up-to-date twitter API wrapper that is based on the clojure async.http.client library. It offers the full taxonomy of twitter API's (streaming, search and restful) and has been tested to be working. There are some tests included, but it is far from complete and needs to be made exhaustive.

## Usage

The calls are declared with numerous macros that allow all sorts of fanciness. Note that unlike other API's, the parameters for each call are note hard-coding into their Clojure wrappers. I just figured that you could look them up on the dev.twitter.com and supply them in the :params map that is supplied.

``` Clojure
(def *creds* 
(show-user :oauth-creds *creds* :params {:screen-name "AdamJWynne"})
```

## License

Copyright (C) 2011 Adam Wynne

Distributed under the Eclipse Public License, the same as Clojure.
