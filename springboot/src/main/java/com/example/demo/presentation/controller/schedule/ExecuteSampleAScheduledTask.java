package com.example.demo.presentation.controller.schedule;

import com.example.demo.domain.repository.LockRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
@AllArgsConstructor
public class ExecuteSampleAScheduledTask {
    private final String LOCK_NAME = "sampleA";
    private final int LOCK_WAIT_TIMEOUT_SECOND = 10;
    private LockRepository lockRepository;

    /**
     * 毎分0秒に定期実行 <br>
     * 別のportで2つサーバーを立てる
     */
    @Scheduled(cron = "0 * * * * *")
    public void executeSampleATask() {
        var connectionId = lockRepository.getConnectionId();
        log.info(">>>>> getLock");
        var getLock = lockRepository.getLock(LOCK_NAME, LOCK_WAIT_TIMEOUT_SECOND);
        log.info("<<<<< getLock");

        if (Objects.isNull(getLock)) {
            log.error("connectionId: " + connectionId + " . errorでlockが取得できなかった");
        } else if (Objects.equals(getLock, 0)) {
            log.info("connectionId: " + connectionId + " . 他のclientが取得済みなので、lockが取得できなかった");
        } else if (Objects.equals(getLock, 1)) {
            log.info("connectionId: " + connectionId + " . lockを取得できた");

            // application.ymlでHicariCPのmax-lifetimeを30秒に指定している
            // HicariCPのconnectionがresetされるのを待つため、40秒間sleepする
            log.info(">>>>> sleep");
            try {
                Thread.sleep(40000);
            } catch (InterruptedException e) {
                log.error("InterruptedException が発生. ", e);
            }
            log.info("<<<<< sleep");

            log.info(">>>>> releaseLock");
            var releaseLock = lockRepository.releaseLock(LOCK_NAME);
            log.info("<<<<< releaseLock");

            if (Objects.isNull(releaseLock)) {
                // HicariCPのconnectionがresetされた後はmysqlのsessionがが異なるため、
                // 取得したlockが解放されてしまっている
                log.error("取得したはずの名前付きlockが存在しなかった");
            } else if (Objects.equals(releaseLock, 0)) {
                log.info("lockが取得されているが、他のclientが取得したlockなので、解放できなかった");
            } else if (Objects.equals(releaseLock, 1)) {
                log.info("取得したlockを解放できた");
            } else {
                log.error("releaseLock が null, 0, 1 のいずれでもない");
            }
        } else {
            log.error("getLock が null, 0, 1 のいずれでもない");
        }
    }
}
