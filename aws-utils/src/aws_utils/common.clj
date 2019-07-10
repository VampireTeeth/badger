(ns aws-utils.common
  (:require [cognitect.aws.client.api :as aws]
            [cognitect.aws.credentials :as creds]))

(defn client-with-assumed-role
  [api-key]
  (let [access-key-id (System/getenv "AWS_ACCESS_KEY_ID")
        secret-access-key (System/getenv "AWS_SECRET_ACCESS_KEY")
        session-token (System/getenv "AWS_SESSION_TOKEN")]
    (aws/client {:api api-key
                 :credentials-provider (reify creds/CredentialsProvider
                                         (fetch [_]
                                           {:aws/access-key-id access-key-id
                                            :aws/secret-access-key secret-access-key
                                            :aws/session-token session-token}))})))
