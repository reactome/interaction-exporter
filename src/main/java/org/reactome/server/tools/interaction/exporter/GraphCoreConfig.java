package org.reactome.server.tools.interaction.exporter;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.SessionFactory;
import org.reactome.server.graph.config.Neo4jConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Config the neo4j graph data base.
 *
 * @author Chuan-Deng dengchuanbio@gmail.com
 */
@org.springframework.context.annotation.Configuration
@ComponentScan(basePackages = {"org.reactome.server.graph"})
@EnableTransactionManagement
@EnableNeo4jRepositories(basePackages = {"org.reactome.server.graph.repository"})
@EnableSpringConfigured
public class GraphCoreConfig extends Neo4jConfig {
	private SessionFactory sessionFactory;
	private Logger logger = LoggerFactory.getLogger(GraphCoreConfig.class);

	@Bean
	public Configuration getConfiguration() {
		Configuration config = new Configuration();
		config.driverConfiguration()
//                .setDriverClassName(System.getProperty("neo4j.driver")) // <neo4j.driver>org.neo4j.ogm.drivers.http.driver.HttpDriver</neo4j.driver>
				.setDriverClassName("org.neo4j.ogm.drivers.http.driver.HttpDriver")
				.setURI("http://".concat(System.getProperty("neo4j.host")).concat(":").concat(System.getProperty("neo4j.port")))
				.setCredentials(System.getProperty("neo4j.user"), System.getProperty("neo4j.password"));
		return config;
	}

	@Override
	@Bean
	public SessionFactory getSessionFactory() {
		if (sessionFactory == null) {
			logger.info("Creating a Neo4j SessionFactory");
			sessionFactory = new SessionFactory(getConfiguration(), "org.reactome.server.graph.domain");
		}
		return sessionFactory;
	}
}
