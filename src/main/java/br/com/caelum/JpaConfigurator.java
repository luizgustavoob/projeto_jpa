package br.com.caelum;

import java.beans.PropertyVetoException;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.mchange.v2.c3p0.ComboPooledDataSource;

@Configuration
@EnableTransactionManagement
public class JpaConfigurator {

	@Bean(destroyMethod = "close") 
	//Quando o tomcat é desligado, chama o método "close" do pool, matando todas as conexões.
	public DataSource getDataSource() throws PropertyVetoException {
		//Pool de conexões gerenciada pelo c3p0
		ComboPooledDataSource dataSource = new ComboPooledDataSource();

	    dataSource.setDriverClass("org.postgresql.Driver");
	    dataSource.setJdbcUrl("jdbc:postgresql://localhost:5432/projeto_jpa");
	    dataSource.setUser("postgres");
	    dataSource.setPassword("postgres");
	    
	    dataSource.setMinPoolSize(3);
	    dataSource.setMaxPoolSize(5);
	    dataSource.setNumHelperThreads(15);
	    dataSource.setIdleConnectionTestPeriod(2); //A cada 2 segundos, testamos as conexões ociosas.
	  
	    return dataSource;
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean getEntityManagerFactory(DataSource dataSource) {
		LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();

		entityManagerFactory.setPackagesToScan("br.com.caelum");
		entityManagerFactory.setDataSource(dataSource);

		entityManagerFactory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

		Properties props = new Properties();
		props.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
		props.setProperty("hibernate.show_sql", "true");
		props.setProperty("hibernate.hbm2ddl.auto", "update");
		
		//Habilitando cache de segundo nível
		props.setProperty("hibernate.cache.use_second_level_cache", "true");
		//Habilitando cache de queries
		props.setProperty("hibernate.cache.use_query_cache", "true");
		props.setProperty("hibernate.cache.region.factory_class", "org.hibernate.cache.ehcache.EhCacheRegionFactory");
		
		//Habilitando as estatísticas
		props.setProperty("hibernate.generate_statistics", "true");
		
		entityManagerFactory.setJpaProperties(props);

		return entityManagerFactory;
	}
	
	@Bean
	public Statistics statistics(EntityManagerFactory emf) {
		SessionFactory sf = emf.unwrap(SessionFactory.class);
		Statistics s = sf.getStatistics();
		return s;
	}

	@Bean
	public JpaTransactionManager getTransactionManager(EntityManagerFactory emf) {
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(emf);
		return transactionManager;
	}
}
