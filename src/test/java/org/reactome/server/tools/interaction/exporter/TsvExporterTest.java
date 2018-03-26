package org.reactome.server.tools.interaction.exporter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reactome.server.graph.utils.ReactomeGraphCore;
import org.reactome.server.tools.interaction.exporter.util.GraphCoreConfig;
import org.reactome.server.tools.interaction.exporter.writer.TsvWriter;

import java.io.*;

public class TsvExporterTest {

	@BeforeAll
	static void beforeAll() {
		ReactomeGraphCore.initialise("localhost", "7474", "neo4j", "reactome", GraphCoreConfig.class);
	}

	@Test
	void testReaction() {
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(2048);
		final TsvWriter writer = new TsvWriter(new PrintStream(outputStream));
		InteractionExporter.stream(exporter -> exporter.setObject("R-HSA-5213466"))
				.forEach(writer::write);
		final InputStream result = new ByteArrayInputStream(outputStream.toByteArray());
		final InputStream expected = TsvExporterTest.class.getResourceAsStream("tsv-result-1.txt");
		assertEquals(expected, result);
	}

	@Test
	void testAnotherReaction() {
		// + Reaction:R-HSA-3301345
		// |    c Complex:R-HSA-3318420
		// |    |    + Complex:R-HSA-3318407
		// |    |    |    - EWAS:R-HSA-193948
		// |    |    |    - EWAS:R-HSA-3318361
		// |    |    |    - EWAS:R-HSA-3318364
		// |    |    + Complex:R-HSA-3318422
		// |    |    |    - EWAS:R-HSA-3318359
		// |    |    |    - EWAS:R-HSA-3318360
		// |    |    |    - EWAS:R-HSA-3318363
		// |    i SimpleEntity:R-ALL-113560
		// |    i DefinedSet:R-HSA-4549252
		// |    |    o EWAS:R-HSA-181902
		// |    |    o CandidateSet:R-HSA-4657030
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(2048);
		final TsvWriter writer = new TsvWriter(new PrintStream(outputStream));
		InteractionExporter.stream(exporter -> exporter
				.setObject("R-HSA-3301345")
				.setMaxUnitSize(40))
				.forEach(writer::write);
		final InputStream result = new ByteArrayInputStream(outputStream.toByteArray());
		final InputStream expected = TsvExporterTest.class.getResourceAsStream("tsv-result-2.txt");
		System.out.println(new String(outputStream.toByteArray()));
		assertEquals(expected, result);
	}


	private void assertEquals(InputStream expected, InputStream result) {
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
			}
			if (resultReader.readLine() != null)
				Assertions.fail("No more lines expected at " + n);
		} catch (IOException e) {
			e.printStackTrace();
			Assertions.fail(e.getMessage());
		}
	}

}
