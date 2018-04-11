package org.reactome.server.tools.interaction.exporter;

import org.junit.Assert;
import org.neo4j.ogm.drivers.http.request.HttpRequestException;
import org.reactome.server.graph.domain.model.DatabaseObject;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.graph.utils.ReactomeGraphCore;
import org.reactome.server.tools.interaction.exporter.util.GraphCoreConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

public class TestUtils {

	private static DatabaseObjectService object_service;
	private static Map<String, ? extends DatabaseObject> cache = new LinkedHashMap<>();
	private static boolean connection;

	static {
		ReactomeGraphCore.initialise("localhost", "7474", "neo4j", "reactome", GraphCoreConfig.class);
		object_service = ReactomeGraphCore.getService(DatabaseObjectService.class);
		try {
			object_service.findById("R-HSA-110576");
			connection = true;
		} catch (HttpRequestException e) {
			connection = false;
		}
	}

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
					Assert.fail("Expected more lines at " + n);
				if (!resultLine.equals(expectedLine)) {
					final String message = String.format("At line %d%n - Expected:[%s]%n - Actual  :[%s]%n", n, expectedLine, resultLine);
					Assert.fail(message);
				}
				n += 1;
			}
			if (resultReader.readLine() != null)
				Assert.fail("No more lines expected at " + n);
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}


	public static <T extends DatabaseObject> T getById(String identifier) {
		return (T) cache.computeIfAbsent(identifier, id -> object_service.findById(id));
	}


	public static boolean hasConnection() {
		return connection;
	}
}
