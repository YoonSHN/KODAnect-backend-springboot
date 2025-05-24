package kodanect.common.config;

import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.*;

import javax.sql.DataSource;
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
	 * 트랜잭션 매니저 Bean
	 *
	 * DataSource 기반 트랜잭션 처리기 등록
	 */
	@Bean(name="txManager")
	public DataSourceTransactionManager txManager(@Qualifier("dataSource") DataSource dataSource) {
		DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
		dataSourceTransactionManager.setDataSource(dataSource);
		return dataSourceTransactionManager;
	}

	/**
	 * 트랜잭션 인터셉터 Bean
	 *
	 * 트랜잭션 전파 방식과 롤백 정책 설정
	 */
	@Bean
	public TransactionInterceptor txAdvice(DataSourceTransactionManager txManager) {
		RuleBasedTransactionAttribute txAttribute = new RuleBasedTransactionAttribute();
		txAttribute.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		txAttribute.setRollbackRules(Collections.singletonList(new RollbackRuleAttribute(Exception.class)));

		HashMap<String, TransactionAttribute> txMethods = new HashMap<String, TransactionAttribute>();
		txMethods.put("*", txAttribute);

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
	public Advisor txAdvisor(@Qualifier("txManager") DataSourceTransactionManager txManager) {
		AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
		pointcut.setExpression("execution(* kodanect.domain..service.impl..*(..))");
		return new DefaultPointcutAdvisor(pointcut, txAdvice(txManager));
	}

}
