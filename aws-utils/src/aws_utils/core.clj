(ns aws-utils.core
  (:require [cognitect.aws.client.api :as aws]
            [cognitect.aws.credentials :as creds]
            [clojure.core.async :as async])
  (:gen-class))

(defn- client-with-assumed-role
  []
  (let [access-key-id (System/getenv "AWS_ACCESS_KEY_ID")
        secret-access-key (System/getenv "AWS_SECRET_ACCESS_KEY")
        session-token (System/getenv "AWS_SESSION_TOKEN")]
    (aws/client {:api :dynamodb
                 :credentials-provider (reify creds/CredentialsProvider
                                         (fetch [_]
                                           {:aws/access-key-id access-key-id
                                            :aws/secret-access-key secret-access-key
                                            :aws/session-token session-token}))})))

(defn- find-container-token-metadata
  [client table-name]
  (aws/invoke client
                {:op :Scan
                 :request {:TableName table-name
                           :ProjectionExpression "SystemAccountId,CloudId,CloudUrl,CreatedTime"
                           :FilterExpression "CloudUrl=:cloudUrl"
                           :ExpressionAttributeValues {":cloudUrl" {:S "https://mpt-test-sliu2-mo-kt-1.jira-dev.com"}}}}))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [client (client-with-assumed-role)
        table-name "rps-stg-east-migration-service-container-token-1527662421507"
        res (find-container-token-metadata client table-name)]
        (println res)))
