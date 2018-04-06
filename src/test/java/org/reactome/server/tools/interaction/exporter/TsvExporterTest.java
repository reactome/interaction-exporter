package org.reactome.server.tools.interaction.exporter;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactome.server.tools.interaction.exporter.writer.TsvWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import static org.reactome.server.tools.interaction.exporter.TestUtils.hasConnection;

public class TsvExporterTest {

	@BeforeEach
	public void setUp() {
		Assumptions.assumeTrue(hasConnection());
	}

	@Test
	public void testReaction() {
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(2048);
		final TsvWriter writer = new TsvWriter(outputStream);
		InteractionExporter.stream(exporter -> exporter.setObject("R-HSA-5213466"))
				.forEach(writer::write);
		final InputStream result = new ByteArrayInputStream(outputStream.toByteArray());
		final InputStream expected = TsvExporterTest.class.getResourceAsStream("tsv-result-1.txt");
		TestUtils.assertEquals(expected, result);
	}

	@Test
	public void testAnotherReaction() {
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
		TestUtils.assertEquals(expected, result);
	}
}
