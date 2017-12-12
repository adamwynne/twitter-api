(ns twitter.test.upload
  (:require [clojure.test :refer :all]
            [twitter.api.restful :refer [media-upload-chunked
                                         statuses-destroy-id
                                         statuses-update
                                         statuses-update-with-media]]
            [twitter.request :refer [file-body-part
                                     prepare-request-with-multi
                                     status-body-part]]
            [twitter.test.creds :refer [make-test-creds]]
            [twitter.utils :refer [classpath-file]])
  (:import (com.ning.http.client.multipart FilePart StringPart)))

(def ^:dynamic *test-image-file-name* (classpath-file "testimage.gif"))
(def ^:dynamic *test-animated-gif-file-name* (classpath-file "Asteroide_Eros_vu_par_sonde_Near_a_7000_km.gif"))

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

(deftest test-chunked-media-upload
  (let [status "testing chunked media upload"
        creds (make-test-creds)
        upload-result (media-upload-chunked
                       :oauth-creds creds
                       :media *test-animated-gif-file-name*
                       :media-type "image/gif")
        media-id (get-in upload-result [:body :media_id])]
    (is (= (:code (:status upload-result)) 201))
    (is media-id)
    (let [update-result (statuses-update
                         :oauth-creds creds
                         :params {:status status
                                  :media-ids [(get-in upload-result [:body :media_id])]})]
      (is (= (:code (:status update-result)) 200))
      (statuses-destroy-id :oauth-creds creds :params {:id (:id (:body update-result))}))))
