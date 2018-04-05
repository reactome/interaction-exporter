package org.reactome.server.tools.interaction.exporter;

import org.junit.jupiter.api.Disabled;
import org.reactome.server.tools.interaction.exporter.filter.IncludeSimpleEntity;

public class CoverageTest {

	@Disabled
	public void coverageTest() {
		InteractionExporter.stream(exporter -> exporter
				.setVerbose(true)
				.setMaxUnitSize(8)
				.setIncludeSimpleEntity(IncludeSimpleEntity.NON_TRIVIAL)
				.setSpecies("Homo sapiens"));
	}

}
