# アプリサーバーの冗長構成でのbatch実行時に競合を避ける目的でmysqlのgetlockが使えるか？

## 結論

- 使えない

## 試したこと

### getlockを使うことで、別インスタンスでのbatch実行がskipされることの確認

1. サーバーを2つのport 8081, 8082 で動かす
2. 毎分ゼロ秒にScheduledTaskが開始されるので、待機する
3. 片方のサーバーでは `connectionId: 97 . lockを取得できた` のようなログが出力される
4. もう片方のサーバーでは `connectionId: 98 . 他のclientが取得済みなので、lockが取得できなかった` のようなログが出力される

### HicariCPのconnectionがbatch終了前にresetされた場合にlockが取得され続けるかの確認

1. `e298a328c86e262fbd77b944e050c29439bdd22f` にチェックアウト
2. サーバーを動かす(8081, 8082 どちらでもいい)
3. 実装内容
    - HicariCPのmax-lifetimeを30秒に設定している
    - mysqlのwait_timeoutはデフォルトの8時間のまま
    - getLockでlockを取得した後に、40秒間sleepする
4. 毎分ゼロ秒にScheduledTaskが開始されるので、待機する
5. `connectionId: 104 . lockを取得できた` のようなログが出力される
6. `取得したはずの名前付きlockが存在しなかった` というログが出力される

- 結果
  - 取得したlockが解放されてしまっている
  - batch中にHicariCPのconnectionがresetされてしまった場合、かつ、仮にもう片方のサーバーの同じScheduledTaskが開始されてしまった場合、batch処理が競合してしまう

### mysqlのsessionがbatch終了前にresetされた場合にlockが取得され続けるかの確認

1. `42fe946bcbbc9a9da6c548ef510199ac29d9949b` にチェックアウト
2. mysqlのwait_timeoutをデフォルトの8時間から10秒に変更する

    ```sql
    mysql> set global wait_timeout=10;
    Query OK, 0 rows affected (0.00 sec)

    mysql> show global variables like 'wait_timeout';
    +---------------+-------+
    | Variable_name | Value |
    +---------------+-------+
    | wait_timeout  | 10    |
    +---------------+-------+
    1 row in set (0.02 sec)

    mysql>
    ```

3. サーバーを動かす(8081, 8082 どちらでもいい)
4. 実装内容
    - HicariCPのmax-lifetimeをデフォルトの30分に設定している
    - mysqlのwait_timeoutはデフォルトの10秒に設定している
    - getLockでlockを取得した後に、20秒間sleepする
5. 毎分ゼロ秒にScheduledTaskが開始されるので、待機する
6. `connectionId: 104 . lockを取得できた` のようなログが出力される
7. `取得したはずの名前付きlockが存在しなかった` というログが出力される

- 結果
  - 取得したlockが解放されてしまっている
  - batch中にmysqlのsessionがresetされてしまった場合、かつ、仮にもう片方のサーバーの同じScheduledTaskが開始されてしまった場合、batch処理が競合してしまう
