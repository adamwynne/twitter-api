(ns twitter.test.upload
  (:use [clojure.test]
        [twitter.test utils creds]
        [twitter.utils]
        [twitter.request]
        [twitter.api.restful])
  (:import [com.ning.http.multipart StringPart FilePart]))

(def ^:dynamic *test-image-file-name* (classpath-file "testimage.gif"))

(deftest test-status-body-part
  (is (instance? StringPart (status-body-part "test"))))

(deftest test-file-body-part
  (is (instance? FilePart (file-body-part *test-image-file-name*)))
  (is (thrown? Exception (file-body-part "test.unknown"))))

(deftest test-multipart
  (let [r (prepare-request-with-multi
           :post "http://www.cnn.com"
           :headers {:content-type "multipart/form-data"}
           :body (file-body-part *test-image-file-name*))]
    (is r)
    (is (.getParts r))))

(defn test-image-upload []
  (let [status "testing"
        result (statuses-update-with-media
                :oauth-creds (make-test-creds)
                :body [(file-body-part *test-image-file-name*)
                       (status-body-part status)])
        result-text (:text (:body result))]
    (is (= (:code (:status result)) 200))
    (is (= (.substring result-text 0 (count status)) status))
    (statuses-destroy-id :oauth-creds (make-test-creds) :params {:id (:id (:body result))})))
