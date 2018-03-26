package org.reactome.server.tools.interaction.exporter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reactome.server.graph.utils.ReactomeGraphCore;
import org.reactome.server.tools.interaction.exporter.util.GraphCoreConfig;
import org.reactome.server.tools.interaction.exporter.writer.InteractionWriter;
import org.reactome.server.tools.interaction.exporter.writer.Tab27Writer;

import java.io.*;

public class PsiMitabWriterTest {

	@BeforeAll
	static void beforeAll() {
		ReactomeGraphCore.initialise("localhost", "7474", "neo4j", "reactome", GraphCoreConfig.class);
	}

	@Test
	void testReaction() {
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(2048);
		final InteractionWriter writer;
		try {
			writer = new Tab27Writer(new PrintStream(outputStream));
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
