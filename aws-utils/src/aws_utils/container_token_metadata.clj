(ns aws-utils.container-token-metadata
  (:require [cognitect.aws.client.api :as aws]
            [aws-utils.core :as c])
  (:gen-class))

(defn- find-container-token-metadata
  [client table-name cloudUrl]
  (aws/invoke client
                {:op :Scan
                 :request {:TableName table-name
                           :ProjectionExpression "SystemAccountId,CloudId,CloudUrl,CreatedTime"
                           :FilterExpression "CloudUrl=:cloudUrl"
                           :ExpressionAttributeValues {":cloudUrl" {:S cloudUrl}}}}))

(defn -main
  "Finding the container token metada from dynamoDB"
  [& args]
  (let [client (c/client-with-assumed-role :dynamodb)
        table-name "rps-stg-east-migration-service-container-token-1527662421507"
        cloudUrl "https://mpt-test-sliu2-mo-kt-1.jira-dev.com"
        res (find-container-token-metadata client table-name cloudUrl)]
        (println (-> res :Items last))))
