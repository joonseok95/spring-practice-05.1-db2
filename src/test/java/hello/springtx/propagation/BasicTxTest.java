package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;

@Slf4j
@SpringBootTest
public class BasicTxTest {

    @Autowired
    PlatformTransactionManager tm;

    @TestConfiguration
    static class Config {
        @Bean
        public PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
    }

    @Test
    void commit() {
        log.info("트랜잭션 시작");
        TransactionStatus status = tm.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션 커밋 시작");
        tm.commit(status);
        log.info("트랜잭션 커밋 완료");

    }

    @Test
    void rollback() {
        log.info("트랜잭션 시작");
        TransactionStatus status = tm.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션 롤백 시작");
        tm.rollback(status);
        log.info("트랜잭션 롤백 완료");

    }

    @Test
    void double_commit() {
        log.info("트랜잭션1 시작");
        TransactionStatus tx1 = tm.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션1 커밋");
        tm.commit(tx1);

        log.info("트랜잭션2 시작");
        TransactionStatus tx2 = tm.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션2 커밋");
        tm.commit(tx2);

    }

    @Test
    void double_commit_rollback() {
        log.info("트랜잭션1 시작");
        TransactionStatus tx1 = tm.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션1 커밋");
        tm.commit(tx1);

        log.info("트랜잭션2 시작");
        TransactionStatus tx2 = tm.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션2 롤백");
        tm.rollback(tx2);

    }

    @Test
    void inner_commit() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = tm.getTransaction(new DefaultTransactionAttribute());
        log.info("outer.isNewTransaction() = {}", outer.isNewTransaction());

        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = tm.getTransaction(new DefaultTransactionAttribute());
        log.info("inner.isNewTransaction() = {}", inner.isNewTransaction());
        log.info("내부 트랜잭션 커밋");
        tm.commit(inner);

        log.info("외부 트랜잭션 커밋");
        tm.commit(outer);
    }

    @Test
    void outer_rollback() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = tm.getTransaction(new DefaultTransactionAttribute());

        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = tm.getTransaction(new DefaultTransactionAttribute());
        log.info("내부 트랜잭션 커밋");
        tm.commit(inner);

        log.info("외부 트랜잭션 롤백");
        tm.rollback(outer);
    }
    @Test
    void inner_rollback() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = tm.getTransaction(new DefaultTransactionAttribute());

        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = tm.getTransaction(new DefaultTransactionAttribute());
        log.info("내부 트랜잭션 롤백");
        tm.rollback(inner);

        log.info("외부 트랜잭션 커밋");
        Assertions.assertThatThrownBy(() -> tm.commit(outer)).isInstanceOf(UnexpectedRollbackException.class);
    }

    @Test
    void inner_rollback_requires_new() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = tm.getTransaction(new DefaultTransactionAttribute());
        log.info("outer.isNewTransaction() = {}", outer.isNewTransaction());

        log.info("내부 트랜잭션 시작");
        DefaultTransactionAttribute definition = new DefaultTransactionAttribute();
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus inner = tm.getTransaction(definition);
        log.info("inner.isNewTransaction() = {}", inner.isNewTransaction());

        log.info("내부 트랜잭션 롤백");
        tm.rollback(inner);

        log.info("외부 트랜잭션 커밋");
        tm.commit(outer);
    }
}
