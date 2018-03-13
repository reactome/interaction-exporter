package org.reactome.server.tools.interaction.exporter;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reactome.server.graph.utils.ReactomeGraphCore;

class ExporterTest {


	@BeforeAll
	static void beforeAll() {
		ReactomeGraphCore.initialise("localhost", "7474", "neo4j", "reactome", GraphCoreConfig.class);
	}

	@Test
	void testOne() {
		InteractionExporter.export(interactionExporter -> interactionExporter
				.setOutput(System.out)
				.setFormat(Format.TSV)
				.setObject("R-HSA-5672710"));
	}

}
