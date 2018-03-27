package org.reactome.server.tools.interaction.exporter;

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.jupiter.api.*;
import org.neo4j.ogm.drivers.http.request.HttpRequestException;
import org.reactome.server.graph.domain.model.DatabaseObject;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.graph.utils.ReactomeGraphCore;
import org.reactome.server.tools.interaction.exporter.filter.IncludeSimpleEntity;
import org.reactome.server.tools.interaction.exporter.util.GraphCoreConfig;

import java.util.*;
import java.util.stream.Collectors;

class InteractionExporterTest {

	private static final String HOMO_SAPIENS = "Homo sapiens";
	private static DatabaseObjectService object_service;
	private static boolean connection;
	private Map<String, ? extends DatabaseObject> cache = new LinkedHashMap<>();
	private Comparator<? super Interaction> sorter = (a, b) -> {
		int compare = a.getA().compareTo(b.getA());
		if (compare != 0) return compare;
		compare = a.getB().compareTo(b.getB());
		if (compare != 0) return compare;
		return a.getContext().getStId().compareTo(b.getContext().getStId());
	};

	@BeforeAll
	static void beforeAll() {
		ReactomeGraphCore.initialise("localhost", "7474", "neo4j", "reactome", GraphCoreConfig.class);
		object_service = ReactomeGraphCore.getService(DatabaseObjectService.class);
		try {
			object_service.findById("R-HSA-110576");
			connection = true;
		} catch (HttpRequestException e) {
			connection = false;
		}
	}

	@BeforeEach
	void beforeEach() {
		Assumptions.assumeTrue(connection);
	}

	@Test
	void testOlygomer() {
		final String stId = "R-HSA-110576";
		final List<Interaction> interactions = InteractionExporter.stream(exporter ->
				exporter.setSpecies(HOMO_SAPIENS)
						.setIncludeSimpleEntity(IncludeSimpleEntity.NON_TRIVIAL)
						.setMaxUnitSize(4)
						.setObject(stId))
				.collect(Collectors.toList());
		final List<Interaction> expected = Arrays.asList(
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-110576"), getById("R-HSA-60024"), 6L, getById("R-HSA-60024"), 0L)
		);
		assertEquals(expected, interactions);
	}

	@Test
	void testComplexWithSet() {
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
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-1911487"), getById("R-HSA-157239"), 1L, getById("R-HSA-264470"), 1L),
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-1911487"), getById("R-HSA-1983670"), 1L, getById("R-HSA-264470"), 1L),
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-1911487"), getById("R-HSA-1604437"), 1L, getById("R-HSA-264470"), 1L),
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-1911487"), getById("R-HSA-157239"), 1L, getById("R-HSA-416464"), 1L),
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-1911487"), getById("R-HSA-1983670"), 1L, getById("R-HSA-416464"), 1L),
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-1911487"), getById("R-HSA-1604437"), 1L, getById("R-HSA-416464"), 1L),
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-1852570"), getById("R-HSA-1604437"), 1L, getById("R-HSA-157239"), 1L),
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-1852570"), getById("R-HSA-1604437"), 1L, getById("R-HSA-1983670"), 1L),
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-157027"), getById("R-HSA-1983670"), 1L, getById("R-HSA-157239"), 1L)
		);
		final List<Interaction> interactions = InteractionExporter.stream(interactionExporter ->
				interactionExporter.setObject("R-HSA-1911487")
						.setMaxUnitSize(5)
						.setSpecies(HOMO_SAPIENS))
				.collect(Collectors.toList());
		assertEquals(expected, interactions);
	}

	@Test
	void testLimit() {
		// + Complex:R-HSA-1911487
		// |    + Complex:R-HSA-1852570
		// |    |    + Complex:R-HSA-157027
		// |    |    |    - EWAS:R-HSA-157239
		// |    |    |    - EWAS:R-HSA-1983670
		// |    |    + CandidateSet:R-HSA-1604454
		// |    |    |    o EWAS:R-HSA-1604437
		// |    + DefinedSet:R-HSA-1911410
		// |    |    o EWAS:R-HSA-264470
		// |    |    o EWAS:R-HSA-416464
		final List<Interaction> expected = Arrays.asList(
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-1852570"), getById("R-HSA-1604437"), 1L, getById("R-HSA-157239"), 1L),
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-1852570"), getById("R-HSA-1604437"), 1L, getById("R-HSA-1983670"), 1L),
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-157027"), getById("R-HSA-1983670"), 1L, getById("R-HSA-157239"), 1L)
		);
		final List<Interaction> interactions = InteractionExporter.stream(interactionExporter ->
				interactionExporter.setObject("R-HSA-1911487")
						.setMaxUnitSize(3)
						.setSpecies(HOMO_SAPIENS))
				.collect(Collectors.toList());
		assertEquals(expected, interactions);
	}

	@Test
	void testSet() {
		// + CandidateSet:R-HSA-5357900
		// |    o EWAS:R-HSA-50845
		// |    o EWAS:R-HSA-50847
		final List<Interaction> interactions = InteractionExporter.stream(interactionExporter ->
				interactionExporter.setObject("R-HSA-5357900")
						.setMaxUnitSize(4))
				.collect(Collectors.toList());
		Assertions.assertEquals(0, interactions.size());
	}

	@Test
	void testReactionInputs() {
		// + Reaction:R-HSA-5210918(1)
		// |    i Complex:R-BAN-5205716(1)
		// |    |    - SimpleEntity:R-ALL-74112(2)
		// |    |    - EWAS:R-BAN-5205707(1)
		// |    i CandidateSet:R-HSA-5209996(1)
		// |    |    o EWAS:R-HSA-5205722(1)
		final List<Interaction> expected = Arrays.asList(
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-5210918"), getById("R-ALL-74112"), 2L, getById("R-HSA-5205722"), 1L),
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-5210918"), getById("R-BAN-5205707"), 1L, getById("R-HSA-5205722"), 1L),
				new Interaction(InteractionType.PHYSICAL, getById("R-BAN-5205716"), getById("R-BAN-5205707"), 1L, getById("R-ALL-74112"), 2L)
		);
		final List<Interaction> interactions = InteractionExporter.stream(exporter ->
				exporter.setObject("R-HSA-5210918")).collect(Collectors.toList());
		assertEquals(expected, interactions);
	}

	@Ignore
	void testReactionSameCatalystAndInput() {
		// TODO: In this reaction the catalyst and the input is the same
		// + Reaction:R-HSA-1222723(1)
		// |    c Complex:R-MTU-1222294(1)
		// |    |    - SimpleEntity:R-ALL-71185(2)
		// |    |    - EWAS:R-MTU-1222724(2)
		// |    i SimpleEntity:R-ALL-29368(2) trivial
		// |    i Complex:R-MTU-1222294(1)
		// |    |    - SimpleEntity:R-ALL-71185(2)
		// |    |    - EWAS:R-MTU-1222724(2)
		// |    i SimpleEntity:R-ALL-1222525(2)
		final List<Interaction> expected = Arrays.asList(
				new Interaction(InteractionType.PHYSICAL, getById("R-MTU-1222294"), getById("R-ALL-71185"), 2L, getById("R-MTU-1222724"), 2L),
				new Interaction(InteractionType.PHYSICAL, getById("R-MTU-1222294"), getById("R-MTU-1222724"), 2L, getById("R-MTU-1222724"), 0L),
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-1222723"), getById("R-ALL-1222525"), 2L, getById("R-MTU-1222724"), 2L),
				new Interaction(InteractionType.fromGo("GO:0008941"), getById("R-HSA-1222723"), getById("R-ALL-71185"), 2L, getById("R-MTU-1222724"), 2L),
				new Interaction(InteractionType.fromGo("GO:0008941"), getById("R-HSA-1222723"), getById("R-MTU-1222724"), 2L, getById("R-MTU-1222724"), 2L)
		);
		final List<Interaction> interactions = InteractionExporter.stream(exporter ->
				exporter.setObject("R-HSA-1222723")).collect(Collectors.toList());
		assertEquals(expected, interactions);
	}

	@Test
	void testReactionCatalyst() {
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
		final List<Interaction> expected = Arrays.asList(
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-5218862"), getById("R-HSA-450328"), 1L, getById("R-HSA-168651"), 1L),
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-5218868"), getById("R-HSA-5218872"), 1L, getById("R-HSA-450328"), 1L),

				new Interaction(InteractionType.fromGo("GO:0004674"), getById("R-HSA-5213466"), getById("R-HSA-168651"), 1L, getById("R-HSA-450328"), 1L),
				new Interaction(InteractionType.fromGo("GO:0004674"), getById("R-HSA-5213466"), getById("R-HSA-168651"), 1L, getById("R-HSA-5218872"), 1L),
				new Interaction(InteractionType.fromGo("GO:0004674"), getById("R-HSA-5213466"), getById("R-HSA-450328"), 1L, getById("R-HSA-450328"), 1L),
				new Interaction(InteractionType.fromGo("GO:0004674"), getById("R-HSA-5213466"), getById("R-HSA-450328"), 1L, getById("R-HSA-5218872"), 1L)
		);
		final List<Interaction> interactions = InteractionExporter.stream(exporter ->
				exporter.setObject("R-HSA-5213466")).collect(Collectors.toList());
		assertEquals(expected, interactions);
	}


	private <T extends DatabaseObject> T getById(String identifier) {
		return (T) cache.computeIfAbsent(identifier, id -> object_service.findById(id));
	}

	private void assertEquals(List<Interaction> expected, List<Interaction> interactions) {
		Set<Interaction> a = new TreeSet<>(sorter);
		a.addAll(expected);
		a.removeAll(interactions);
		Set<Interaction> b = new TreeSet<>(sorter);
		b.addAll(interactions);
		b.removeAll(expected);
		if (!a.isEmpty() || !b.isEmpty()) {
			String message = "Result does not match expected\n";
			if (!a.isEmpty())
				message += "These interactions are expected\n" + a.stream().map(String::valueOf).collect(Collectors.joining("\n")) + "\n";
			if (!b.isEmpty())
				message += "These interactions are not expected\n" + b.stream().map(String::valueOf).collect(Collectors.joining("\n")) + "\n";
			Assertions.fail(message);
		}
	}
}
