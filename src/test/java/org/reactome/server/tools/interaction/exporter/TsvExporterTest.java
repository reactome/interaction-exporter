package org.reactome.server.tools.interaction.exporter;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reactome.server.tools.interaction.exporter.filter.SimpleEntityPolicy;
import org.reactome.server.tools.interaction.exporter.writer.TsvWriter;

import java.io.*;

import static org.reactome.server.tools.interaction.exporter.TestUtils.hasConnection;

public class TsvExporterTest {

	@BeforeAll
	public void setUp() {
		Assumptions.assumeTrue(hasConnection());
	}

	@Test
	public void testReaction() {
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(2048);
		final TsvWriter writer = new TsvWriter(outputStream);
		InteractionExporter.streamObject(("R-HSA-5213466")).forEach(writer::write);
		final InputStream result = new ByteArrayInputStream(outputStream.toByteArray());
		final InputStream expected = TsvExporterTest.class.getResourceAsStream("tsv-testReaction.txt");
		TestUtils.assertEquals(expected, result);
	}

	@Test
	public void testReaction2() {
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
		InteractionExporter.streamObject(("R-HSA-3301345"), SimpleEntityPolicy.NON_TRIVIAL, 40, false)
				.forEach(writer::write);
		final InputStream result = new ByteArrayInputStream(outputStream.toByteArray());
		final InputStream expected = TsvExporterTest.class.getResourceAsStream("tsv-testReaction2.txt");
		TestUtils.assertEquals(expected, result);
	}
}
