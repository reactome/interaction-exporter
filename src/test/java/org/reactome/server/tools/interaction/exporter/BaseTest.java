package org.reactome.server.tools.interaction.exporter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.reactome.server.graph.domain.model.DatabaseObject;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.graph.utils.ReactomeGraphCore;
import org.reactome.server.tools.interaction.exporter.util.InteractionExporterNeo4jConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

@SpringBootTest
@ContextConfiguration(classes = {InteractionExporterNeo4jConfig.class})
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BaseTest {

	private static DatabaseObjectService object_service;
	private static Map<String, ? extends DatabaseObject> cache = new LinkedHashMap<>();
	private static boolean connection;


	@BeforeAll
	public static void initGraph(
			@Value("${spring.neo4j.uri}") String neo4jURI,
			@Value("${spring.neo4j.authentication.username}") String neo4jUser,
			@Value("${spring.neo4j.authentication.password}") String neo4jPassword,
			@Value("${spring.data.neo4j.database}") String database
	) {
		ReactomeGraphCore.initialise(neo4jURI, neo4jUser, neo4jPassword, database, InteractionExporterNeo4jConfig.class);
		object_service = ReactomeGraphCore.getService(DatabaseObjectService.class);
		try {
			object_service.findById("R-HSA-110576");
			connection = true;
		} catch (Exception e) {
			connection = false;
		}
	}

//	static {
////		ReactomeGraphCore.initialise(
////				neo4jURI,
////				neo4jUser,
////				neo4jPassword,
////				database,
////				InteractionExporterNeo4jConfig.class);
//		object_service = ReactomeGraphCore.getService(DatabaseObjectService.class);
//		try {
//			object_service.findById("R-HSA-110576");
//			connection = true;
//		} catch (Exception e) {
//			connection = false;
//		}
//	}

	static void assertEquals(InputStream expected, InputStream result) {
		try {
			final BufferedReader expectedReader = new BufferedReader(new InputStreamReader(expected));
			final BufferedReader resultReader = new BufferedReader(new InputStreamReader(result));
			int n = 1;
			String expectedLine;
			String resultLine;
			while ((expectedLine = expectedReader.readLine()) != null) {
				resultLine = resultReader.readLine();
				if (resultLine == null)
					Assertions.fail("Expected more lines at " + n);
				if (!resultLine.equals(expectedLine)) {
					final String message = String.format("At line %d%n - Expected:[%s]%n - Actual  :[%s]%n", n, expectedLine, resultLine);
					Assertions.fail(message);
				}
				n += 1;
			}
			if (resultReader.readLine() != null)
				Assertions.fail("No more lines expected at " + n);
		} catch (IOException e) {
			e.printStackTrace();
			Assertions.fail(e.getMessage());
		}
	}


	public static <T extends DatabaseObject> T getById(String identifier) {
		return (T) cache.computeIfAbsent(identifier, id -> object_service.findByIdNoRelations(id));
	}


	public static boolean hasConnection() {
		return connection;
	}
}
