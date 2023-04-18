/*
 * Copyright 2002-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.transaction.annotation;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.transaction.config.TransactionManagementConfigUtils;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

/**
 * {@code @Configuration} class that registers the Spring infrastructure beans
 * necessary to enable proxy-based annotation-driven transaction management.
 *
 * @author Chris Beams
 * @author Sebastien Deleuze
 * @since 3.1
 * @see EnableTransactionManagement
 * @see TransactionManagementConfigurationSelector
 */
@Configuration(proxyBeanMethods = false)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class ProxyTransactionManagementConfiguration extends AbstractTransactionManagementConfiguration {

	//明显是创建事务切面实例
	@Bean(name = TransactionManagementConfigUtils.TRANSACTION_ADVISOR_BEAN_NAME)
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public BeanFactoryTransactionAttributeSourceAdvisor transactionAdvisor(
			TransactionAttributeSource transactionAttributeSource, TransactionInterceptor transactionInterceptor) {
		//创建BeanFactoryTransactionAttributeSourceAdvisor
		//创建事务切面  Advisor
		BeanFactoryTransactionAttributeSourceAdvisor advisor = new BeanFactoryTransactionAttributeSourceAdvisor();
		//切面里面设置处理事务属性对象  transactionAttributeSource解析Transaction注解的工具类	设置属性源
		advisor.setTransactionAttributeSource(transactionAttributeSource);
		//设置切面的advice 通知类	设置通知
		advisor.setAdvice(transactionInterceptor);
		//设置切面排序  EnableTransactionManagement.order属性	设置@EnableTransactionManagement注解的order属性
		if (this.enableTx != null) {
			advisor.setOrder(this.enableTx.<Integer>getNumber("order"));
		}
		return advisor;
	}

	/**
	 * 注册一个事务属性源bean定义，名为"transactionAttributeSource"
	 * 类型就是AnnotationTransactionAttributeSource
	 */
	//负责事务属性的解析 如@Transactional(propagation = Propagation.REQUIRED,isolation = Isolation.DEFAULT,readOnly = false,rollbackFor = RuntimeException.class)
	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public TransactionAttributeSource transactionAttributeSource() {
		//创建事务属性处理器 transactionAttributeSource注解解析工具类 解析完封装成TransactionAttribute
		return new AnnotationTransactionAttributeSource();
	}

	/**
	 * 注册一个事务拦截器bean定义，名为"transactionAttributeSource"
	 * 类型就是TransactionInterceptor
	 *
	 * @param transactionAttributeSource 事务属性源
	 */
	//创建事务advice
	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public TransactionInterceptor transactionInterceptor(TransactionAttributeSource transactionAttributeSource) {
		//创建事务切面 事务拦截器 此处定义事务拦截器
		TransactionInterceptor interceptor = new TransactionInterceptor();
		//事务属性处理器设置到advice中 属性解析器transactionAttributeSource 事务解析器
		interceptor.setTransactionAttributeSource(transactionAttributeSource);//此处定义事务解析器
		//把事务管理器设置到advice中  此处定义事务源对象
		//设置事务管理器，这个事务管理器是指通过TransactionManagementConfigurer配置的，而不是通过bean定义的，一般为null
		//所以说通过注解配置的TransactionInterceptor的transactionManager一般都为null，但是没关系，在使用时如果发现为null
		//Spring会查找容器中的TransactionManager的实现来作为事务管理器
		if (this.txManager != null) {
			interceptor.setTransactionManager(this.txManager);
		}
		return interceptor;
	}

}
