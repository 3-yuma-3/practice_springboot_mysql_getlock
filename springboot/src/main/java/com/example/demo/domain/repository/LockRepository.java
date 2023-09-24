package com.example.demo.domain.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LockRepository {
    /**
     * <b> 同じセッション内では同じ名前のロックを複数取得することも可能なので、</b>
     * <br>
     * timeout 秒のタイムアウトを使用して、文字列 str で指定された名前でロックの取得を試みます。 <br>
     * 負の timeout 値は、無限のタイムアウトを表します。 ロックは排他的です。 <br>
     * あるセッションで保持されている間は、他のセッションは同じ名前のロックを取得できません。<br> <br>
     * ロックの取得に成功した場合は 1 を返し、<br>
     * 試行がタイムアウトになった場合 (たとえば、ほかのクライアントがすでにその名前をロックしている場合) は 0 を返し、<br>
     * エラー (メモリー不足や mysqladmin kill によるスレッドの停止など) が発生した場合は NULL を返します。 <br> <br>
     * GET_LOCK() で取得されたロックは、RELEASE_LOCK() を実行して明示的に解放されるか、セッションの終了時に暗黙的に解放されます (通常または異常)。 <br>
     * GET_LOCK() で取得されたロックは、トランザクションのコミットまたはロールバック時に解放されません。<br> <br>
     * 特定のセッションで同じ名前の複数のロックを取得することもできます。 <br>
     * 他のセッションは、取得しているセッションがその名前のロックをすべて解放するまで、その名前のロックを取得できません。
     * <br>
     *
     * @param lockName
     * @param timeout
     * @return
     * @see https://dev.mysql.com/doc/refman/8.0/ja/locking-functions.html#function_get-lock
     */
    Integer getLock(
        @Param("lockName") String lockName,
        @Param("timeout") int timeout
    );

    String isUsedLock(
        @Param("lockName") String lockName
    );

    Integer releaseLock(
        @Param("lockName") String lockName
    );

    Integer getConnectionId();
}
