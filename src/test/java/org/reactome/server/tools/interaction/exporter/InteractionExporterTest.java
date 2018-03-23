package org.reactome.server.tools.interaction.exporter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reactome.server.graph.domain.model.DatabaseObject;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.graph.utils.ReactomeGraphCore;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class InteractionExporterTest {

	private static final String HOMO_SAPIENS = "Homo sapiens";
	private static final String PHYSICAL = "physical";
	private static DatabaseObjectService object_service;
	private Map<String, ? extends DatabaseObject> cache = new LinkedHashMap<>();

	@BeforeAll
	static void beforeAll() {
		ReactomeGraphCore.initialise("localhost", "7474", "neo4j", "reactome", GraphCoreConfig.class);
		object_service = ReactomeGraphCore.getService(DatabaseObjectService.class);
	}

	@Test
	void testOlygomer() {
		final String stId = "R-HSA-110576";
		final List<Interaction> interactions = InteractionExporter.stream(exporter ->
				exporter.setSpecies(HOMO_SAPIENS)
						.setIncludeSimpleEntity(IncludeSimpleEntity.NON_TRIVIAL)
						.setMaxSetSize(4)
						.setObject(stId))
				.collect(Collectors.toList());
		Assertions.assertEquals(1, interactions.size());
		Assertions.assertEquals("R-HSA-60024", interactions.get(0).getA().getStId());
		Assertions.assertEquals("R-HSA-60024", interactions.get(0).getB().getStId());
		Assertions.assertEquals(new Integer(6), interactions.get(0).getAst());
		Assertions.assertEquals(new Integer(0), interactions.get(0).getBst());
	}

	@Test
	void testComplex() {
		/*
		+ Complex:R-HSA-1911487
		|    + Complex:R-HSA-1852570
		|    |    + Complex:R-HSA-157027
		|    |    |    - EWAS:R-HSA-157239
		|    |    |    - EWAS:R-HSA-1983670
		|    |    + CandidateSet:R-HSA-1604454
		|    |    |    o EWAS:R-HSA-1604437
		|    + DefinedSet:R-HSA-1911410
		|    |    o EWAS:R-HSA-264470
		|    |    o EWAS:R-HSA-416464
		 */
		final List<Interaction> expected = Arrays.asList(
				new Interaction(PHYSICAL, getById("R-HSA-1911487"), getById("R-HSA-157239"), 1, getById("R-HSA-264470"), 1),
				new Interaction(PHYSICAL, getById("R-HSA-1911487"), getById("R-HSA-1983670"), 1, getById("R-HSA-264470"), 1),
				new Interaction(PHYSICAL, getById("R-HSA-1911487"), getById("R-HSA-1604437"), 1, getById("R-HSA-264470"), 1),
				new Interaction(PHYSICAL, getById("R-HSA-1911487"), getById("R-HSA-157239"), 1, getById("R-HSA-416464"), 1),
				new Interaction(PHYSICAL, getById("R-HSA-1911487"), getById("R-HSA-1983670"), 1, getById("R-HSA-416464"), 1),
				new Interaction(PHYSICAL, getById("R-HSA-1911487"), getById("R-HSA-1604437"), 1, getById("R-HSA-416464"), 1),
				new Interaction(PHYSICAL, getById("R-HSA-1852570"), getById("R-HSA-157239"), 1, getById("R-HSA-1604437"), 1),
				new Interaction(PHYSICAL, getById("R-HSA-1852570"), getById("R-HSA-1604437"), 1, getById("R-HSA-1983670"), 1),
				new Interaction(PHYSICAL, getById("R-HSA-157027"), getById("R-HSA-157239"), 1, getById("R-HSA-1983670"), 1)
		);
		final List<Interaction> interactions = InteractionExporter.stream(interactionExporter ->
				interactionExporter.setObject("R-HSA-1911487")
						.setMaxSetSize(5)
						.setSpecies(HOMO_SAPIENS))
				.collect(Collectors.toList());
		Assertions.assertEquals(expected.size(), interactions.size(), "Not the exact number of interactions");
		for (int i = 0; i < expected.size(); i++)
			Assertions.assertEquals(expected.get(i), interactions.get(i));
	}

	@Test
	void testLimit() {
		/*
		+ Complex:R-HSA-1911487
		|    + Complex:R-HSA-1852570
		|    |    + Complex:R-HSA-157027
		|    |    |    - EWAS:R-HSA-157239
		|    |    |    - EWAS:R-HSA-1983670
		|    |    + CandidateSet:R-HSA-1604454
		|    |    |    o EWAS:R-HSA-1604437
		|    + DefinedSet:R-HSA-1911410
		|    |    o EWAS:R-HSA-264470
		|    |    o EWAS:R-HSA-416464
		 */
		final List<Interaction> expected = Arrays.asList(
				new Interaction(PHYSICAL, getById("R-HSA-1852570"), getById("R-HSA-157239"), 1, getById("R-HSA-1604437"), 1),
				new Interaction(PHYSICAL, getById("R-HSA-1852570"), getById("R-HSA-1604437"), 1, getById("R-HSA-1983670"), 1),
				new Interaction(PHYSICAL, getById("R-HSA-157027"), getById("R-HSA-157239"), 1, getById("R-HSA-1983670"), 1)
		);
		final List<Interaction> interactions = InteractionExporter.stream(interactionExporter ->
				interactionExporter.setObject("R-HSA-1911487")
						.setMaxSetSize(4)
						.setSpecies(HOMO_SAPIENS))
				.collect(Collectors.toList());
		Assertions.assertEquals(expected.size(), interactions.size(), "Not the exact number of interactions");
		for (int i = 0; i < expected.size(); i++)
			Assertions.assertEquals(expected.get(i), interactions.get(i));
	}

	@Test

	private <T extends DatabaseObject> T getById(String identifier) {
		return (T) cache.computeIfAbsent(identifier, id -> object_service.findById(id));
	}

}
