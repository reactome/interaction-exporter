package org.reactome.server.tools.interaction.exporter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reactome.server.graph.utils.ReactomeGraphCore;
import org.reactome.server.tools.interaction.exporter.util.GraphCoreConfig;
import org.reactome.server.tools.interaction.exporter.writer.InteractionWriter;
import org.reactome.server.tools.interaction.exporter.writer.Tab27Writer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

class PsiMiTabWriterTest {

	@BeforeAll
	static void beforeAll() {
		ReactomeGraphCore.initialise("localhost", "7474", "neo4j", "reactome", GraphCoreConfig.class);
	}

	@Test
	void testEwasReaction() {
		// + Reaction:R-HSA-5423117
		// |    c EWAS:R-HSA-5423096
		// |    i SimpleEntity:R-ALL-113671
		// |    i SimpleEntity:R-ALL-113685 (trivial)
		// |    i EWAS:R-HSA-5423110
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(2048);
		try {
			final InteractionWriter writer = new Tab27Writer(outputStream);
			InteractionExporter.stream(exporter -> exporter.setObject("R-HSA-5423117"))
					.forEach(interaction -> {
						try {
							writer.write(interaction);
						} catch (IOException e) {
							e.printStackTrace();
							Assertions.fail(e.getMessage());
						}
					});
			System.out.println(new String(outputStream.toByteArray()));
			final InputStream result = new ByteArrayInputStream(outputStream.toByteArray());
			final InputStream expected = TsvExporterTest.class.getResourceAsStream("psi-mitab-result-3.txt");
			TestUtils.assertEquals(expected, result);
		} catch (IOException e) {
			e.printStackTrace();
			Assertions.fail(e.getMessage());
		}
	}
	@Test
	void testReaction() {
		// + Reaction:R-HSA-5213466
		// |    a EWAS:R-HSA-168651
		// |    a EWAS:R-HSA-450328
		// |    c Complex:R-HSA-5218862
		// |    |    - EWAS:R-HSA-168651
		// |    |    - EWAS:R-HSA-450328
		// |    i 2 x SimpleEntity:R-ALL-113592 (trivial)
		// |    i Complex:R-HSA-5218868
		// |    |    - EWAS:R-HSA-450328
		// |    |    - EWAS:R-HSA-5218872
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(2048);
		final InteractionWriter writer;
		try {
			writer = new Tab27Writer(outputStream);
			InteractionExporter.stream(exporter -> exporter.setObject("R-HSA-5213466"))
					.forEach(interaction -> {
						try {
							writer.write(interaction);
						} catch (IOException e) {
							e.printStackTrace();
							Assertions.fail(e.getMessage());
						}
					});
			System.out.println(new String(outputStream.toByteArray()));
			final InputStream result = new ByteArrayInputStream(outputStream.toByteArray());
			final InputStream expected = TsvExporterTest.class.getResourceAsStream("psi-mitab-result-1.txt");
			TestUtils.assertEquals(expected, result);
		} catch (IOException e) {
			e.printStackTrace();
			Assertions.fail(e.getMessage());
		}
	}


}
