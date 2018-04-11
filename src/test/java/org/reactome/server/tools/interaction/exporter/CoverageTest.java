package org.reactome.server.tools.interaction.exporter;

import org.junit.Ignore;
import org.reactome.server.tools.interaction.exporter.filter.SimpleEntityPolicy;

public class CoverageTest {

	@Ignore
	public void coverageTest() {
		InteractionExporter.stream(exporter -> exporter
				.setVerbose(true)
				.setMaxUnitSize(8)
				.setSimpleEntityPolicy(SimpleEntityPolicy.NON_TRIVIAL)
				.setSpecies("Homo sapiens"));
	}

}
