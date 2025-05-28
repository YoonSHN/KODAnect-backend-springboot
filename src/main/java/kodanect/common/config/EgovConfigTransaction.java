package kodanect.common.config;

import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.*;

import javax.persistence.EntityManagerFactory;
import java.util.Collections;
import java.util.HashMap;

/**
 * 트랜잭션 AOP 설정
 *
 * 트랜잭션 전파, 롤백 정책, 적용 대상 메서드 패턴을 AOP 방식으로 설정
 *
 * 역할
 * - 서비스 메서드 실행 시 트랜잭션 처리 적용
 * - 예외 발생 시 자동 롤백 수행
 *
 * 특징
 * - TransactionInterceptor 기반 AOP 구성
 * - 특정 메서드 패턴에 트랜잭션 자동 적용
 */
@Configuration
public class EgovConfigTransaction {

	/**
	 * 트랜잭션 매니저 Bean (JPA 전용)
	 *
	 * EntityManagerFactory 기반의 JPA 트랜잭션 처리기 등록
	 */
	@Bean(name = "transactionManager")
	public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
		return new JpaTransactionManager(emf);
	}

	/**
	 * 트랜잭션 인터셉터 Bean
	 *
	 * 트랜잭션 전파 방식과 롤백 정책 설정
	 *
	 * [읽기 계열] (readOnly = true)
	 * - get*, find*, read*, select*, list*, fetch*, load*, search*, query*, check*
	 *
	 * [쓰기 계열] (트랜잭션 전파 + 예외 발생 시 롤백)
	 * - save*, insert*, update*, delete*, create*, remove*, register*,
	 *   edit*, change*, process*, apply*, sync*
	 *
	 * [기본 처리]
	 * - 위 조건에 해당하지 않는 모든 메서드는 쓰기 계열로 처리
	 */
	@Bean
	public TransactionInterceptor txAdvice(@Qualifier("transactionManager") PlatformTransactionManager txManager) {

		RuleBasedTransactionAttribute readOnlyTx = new RuleBasedTransactionAttribute();
		readOnlyTx.setReadOnly(true);
		readOnlyTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_SUPPORTS);

		RuleBasedTransactionAttribute writeTx = new RuleBasedTransactionAttribute();
		writeTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		writeTx.setRollbackRules(Collections.singletonList(new RollbackRuleAttribute(Exception.class)));

		HashMap<String, TransactionAttribute> txMethods = new HashMap<>();

		txMethods.put("get*", readOnlyTx);
		txMethods.put("find*", readOnlyTx);
		txMethods.put("read*", readOnlyTx);
		txMethods.put("select*", readOnlyTx);
		txMethods.put("list*", readOnlyTx);
		txMethods.put("fetch*", readOnlyTx);
		txMethods.put("load*", readOnlyTx);
		txMethods.put("search*", readOnlyTx);
		txMethods.put("query*", readOnlyTx);
		txMethods.put("check*", readOnlyTx);

		txMethods.put("save*", writeTx);
		txMethods.put("insert*", writeTx);
		txMethods.put("update*", writeTx);
		txMethods.put("delete*", writeTx);
		txMethods.put("create*", writeTx);
		txMethods.put("remove*", writeTx);
		txMethods.put("register*", writeTx);
		txMethods.put("edit*", writeTx);
		txMethods.put("change*", writeTx);
		txMethods.put("process*", writeTx);
		txMethods.put("apply*", writeTx);
		txMethods.put("sync*", writeTx);
		txMethods.put("increase*", writeTx);

		txMethods.put("*", writeTx);

		NameMatchTransactionAttributeSource txAttributeSource = new NameMatchTransactionAttributeSource();
		txAttributeSource.setNameMap(txMethods);

		TransactionInterceptor txAdvice = new TransactionInterceptor();
		txAdvice.setTransactionAttributeSource(txAttributeSource);
		txAdvice.setTransactionManager(txManager);

		return txAdvice;
	}

	/**
	 * 트랜잭션 AOP 어드바이저 Bean
	 *
	 * kodanect.domain 하위 service.impl 패키지의 모든 메서드에 트랜잭션 AOP 적용
	 */
	@Bean
	public Advisor txAdvisor(@Qualifier("transactionManager") PlatformTransactionManager txManager) {
		AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
		pointcut.setExpression("execution(* kodanect.domain..service.impl..*(..))");
		return new DefaultPointcutAdvisor(pointcut, txAdvice(txManager));
	}

}