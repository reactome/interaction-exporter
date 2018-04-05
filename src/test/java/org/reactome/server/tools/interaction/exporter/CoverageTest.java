package org.reactome.server.tools.interaction.exporter;

import org.junit.jupiter.api.Disabled;
import org.reactome.server.tools.interaction.exporter.filter.SimpleEntityPolicy;

public class CoverageTest {

	@Disabled
	public void coverageTest() {
		InteractionExporter.stream(exporter -> exporter
				.setVerbose(true)
				.setMaxUnitSize(8)
				.setSimpleEntityPolicy(SimpleEntityPolicy.NON_TRIVIAL)
				.setSpecies("Homo sapiens"));
	}

}
