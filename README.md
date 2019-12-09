# はじめに
本コードは Postgresql の PVC を RWO でダイナミックプロビジョニングするサンプルです。

# 構成
```
REST API(Quarkus) <--> Postgresql  
                         ↑
                         |
                        PV(Rook-Ceph による Dynamic Provisioning)        
```

# 前提
Rook-Ceph Operator を K8s にインストールしておく必要があります。

# 手順
1. PVC を宣言して、Rook-Ceph による iSCSI ブロックストレージをダイナミックプロビジョニング。
    ```shell script
    $ kubectl apply -f ./customer-postgres-pvc.yml
    ```
    - PV が自動的に作成されたか確認
        ```shell script
        $ kubectl get pv
        NAME                                       CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS   CLAIM                            STORAGECLASS      REASON   AGE
        pvc-be35f6c0-e084-4274-acc9-19de20e3ae3b   3Gi        RWO            Delete           Bound    default/customer-postgress-pvc   rook-ceph-block            2s
        ```
2. Postgresql の yaml を apply
    ```shell script
    $ kubectl apply -f ./customer-postgres.yml
    ```
   - Postgresql の Pod が Running になるまで待機
        ```shell script
        $ kubectl get pods -w 
        ```
3. Postgresql にデータを格納
    ```shell script
    $ kubectl exec -it $(kubectl get pods -o name -l app=customer-postgres) /bin/bash
    ```
   - 格納する SQL 文は[ここ](https://gist.github.com/k-kosugi/c8dc558307be72bc968da4a064f48c40)にあるので、コピー&ペーストしてね
   - Postgresql から control + D で抜ける
4. Quarkus アプリの起動
    - 既に Linux　用のバイナリとしてコンパイルしたものが[ここ](https://hub.docker.com/repository/docker/kkosugiredhat/customer-api)にあります。v3 を使ってください。
    ```shell script
    $ kubectl apply -f ./customer-api.yml
    ```
    - ソースコードは本プロジェクトに格納してあるので src/main/java の中を覗いてみてください。
5. Quarkus が割り当てられたクラスター外部からアクセス可能な Service ポートの取得とアクセス
    ```shell script
    $ kubectl get svc
    NAME                        TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
    customer-api-service        NodePort    10.106.104.60   <none>        8080:32460/TCP   5s
    customer-postgres-service   ClusterIP   10.105.178.87   <none>        5432/TCP         8m58s
    kubernetes                  ClusterIP   10.96.0.1       <none>        443/TCP          65d
    ``` 
   - 上記の場合は 32460 ポートだよ！
   - ブラウザで http://<Master/NodeのIP>:32460/api/v1/customer にアクセス。
   - こんな JSON が取得できたら完了！
    ```shell script
   [{"id":10,"name":"Joseph Smith","ssn":"CST01002                 "},{"id":11,"name":"Nicholas Ferguson","ssn":"CST01003                 "},{"id":12,"name":"Jane Aire","ssn":"CST01004                 "}]
    ```
   
# Postgresql を殺してみる
1. postgreql を殺してオートヒーリングされた Postgresql がデータを引き継いでいるか確認
    ```shell script
    $ kubectl delete $(kubectl get pods -o name -l app=customer-postgres)
    ```
2. postgresql があがってくるまで待機(Running)
    ```shell script
    $ kubectl get pods -w
    ```
3. 再度、Quarkus REST アプリケーションを何回か動作させて JSON が取得できているか確認。

# Quarkus を殺してみる
1. Quarkus を停止
    ```shell script
    $ kubectl delete $(kubectl get pod -o name -l app=customer-api)
    ```
2. オートヒーリングされた Quarkus を確認
    ```shell script
    $ kubectl logs $(kubectl get pods -o name -l app=customer-api)
    2019-12-09 03:02:35,694 INFO  [io.quarkus] (main) customer 1.0.0-SNAPSHOT (running on Quarkus 1.0.0.CR1) started in 0.016s. Listening on: http://0.0.0.0:8080
    2019-12-09 03:02:35,694 INFO  [io.quarkus] (main) Profile prod activated. 
    2019-12-09 03:02:35,694 INFO  [io.quarkus] (main) Installed features: [agroal, cdi, hibernate-orm, jdbc-postgresql, narayana-jta, resteasy, resteasy-jsonb]
    ```
   - 上記は 0.016秒で Pod が復旧
   
# [参考] Quarkus をビルドしたい場合
- 以下のコマンドを投入。
    ```shell script
    $ ./mvnw package -Pnative -Dquarkus.native.container-build=true
    ```
  - すると target/customer-1.0.0-SNAPSHOT-ruuner の Linux 実行用バイナリが生成される。Mac では実行が不可。
- Linux バイナリを使用しない場合、上記の -P と -D オプションをはずす。