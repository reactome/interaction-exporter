package org.reactome.server.tools.interaction.exporter;

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactome.server.tools.interaction.exporter.filter.IncludeSimpleEntity;
import org.reactome.server.tools.interaction.exporter.util.Constants;

import java.util.*;
import java.util.stream.Collectors;

import static org.reactome.server.tools.interaction.exporter.TestUtils.getById;
import static org.reactome.server.tools.interaction.exporter.TestUtils.hasConnection;

class InteractionExporterTest {

	private static final String HOMO_SAPIENS = "Homo sapiens";
	private Comparator<? super Interaction> sorter = (a, b) -> {
		int compare = a.getA().getEntity().compareTo(b.getA().getEntity());
		if (compare != 0) return compare;
		compare = a.getB().getEntity().compareTo(b.getB().getEntity());
		if (compare != 0) return compare;
		return a.getContext().getStId().compareTo(b.getContext().getStId());
	};

	@Test
	void test() {
		System.out.println(FilenameUtils.normalize("./hello.pdf"));
	}
	@BeforeEach
	void beforeEach() {
		Assumptions.assumeTrue(hasConnection());
	}

	@Test
	void testPolymer() {
		final String stId = "R-HSA-182548";
		final List<Interaction> interactions = InteractionExporter.stream(exporter ->
				exporter.setObject(stId))
				.peek(System.out::println)
				.collect(Collectors.toList());
		final List<Interaction> expected = Arrays.asList(
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-182548"),
						new Interactor(getById("R-HSA-173751"), 0L, Constants.UNSPECIFIED_ROLE),
						new Interactor(getById("R-HSA-173751"), 0L, Constants.UNSPECIFIED_ROLE)));
		assertEquals(expected, interactions);
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
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-110576"),
						new Interactor(getById("R-HSA-60024"), 6L, Constants.UNSPECIFIED_ROLE),
						new Interactor(getById("R-HSA-60024"), 0L, Constants.UNSPECIFIED_ROLE))
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
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-1911487"),
						new Interactor(getById("R-HSA-157239"), 1L, Constants.UNSPECIFIED_ROLE),
						new Interactor(getById("R-HSA-264470"), 1L, Constants.UNSPECIFIED_ROLE)),
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-1911487"),
						new Interactor(getById("R-HSA-1983670"), 1L, Constants.UNSPECIFIED_ROLE),
						new Interactor(getById("R-HSA-264470"), 1L, Constants.UNSPECIFIED_ROLE)),
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-1911487"),
						new Interactor(getById("R-HSA-1604437"), 1L, Constants.UNSPECIFIED_ROLE),
						new Interactor(getById("R-HSA-264470"), 1L, Constants.UNSPECIFIED_ROLE)),
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-1911487"),
						new Interactor(getById("R-HSA-157239"), 1L, Constants.UNSPECIFIED_ROLE),
						new Interactor(getById("R-HSA-416464"), 1L, Constants.UNSPECIFIED_ROLE)),
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-1911487"),
						new Interactor(getById("R-HSA-1983670"), 1L, Constants.UNSPECIFIED_ROLE),
						new Interactor(getById("R-HSA-416464"), 1L, Constants.UNSPECIFIED_ROLE)),
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-1911487"),
						new Interactor(getById("R-HSA-1604437"), 1L, Constants.UNSPECIFIED_ROLE),
						new Interactor(getById("R-HSA-416464"), 1L, Constants.UNSPECIFIED_ROLE)),
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-1852570"),
						new Interactor(getById("R-HSA-1604437"), 1L, Constants.UNSPECIFIED_ROLE),
						new Interactor(getById("R-HSA-157239"), 1L, Constants.UNSPECIFIED_ROLE)),
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-1852570"),
						new Interactor(getById("R-HSA-1604437"), 1L, Constants.UNSPECIFIED_ROLE),
						new Interactor(getById("R-HSA-1983670"), 1L, Constants.UNSPECIFIED_ROLE)),
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-157027"),
						new Interactor(getById("R-HSA-1983670"), 1L, Constants.UNSPECIFIED_ROLE),
						new Interactor(getById("R-HSA-157239"), 1L, Constants.UNSPECIFIED_ROLE))
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
		// + Polymer:R-HSA-2564685
		//|    - Complex:R-HSA-2468137
		//|    |    + DefinedSet:R-HSA-2468257
		//|    |    |    o EWAS:R-HSA-2468208
		//|    |    |    o EWAS:R-HSA-2468209
		//|    |    |    o EWAS:R-HSA-2468212
		//|    |    |    o ...
		//|    |    + DefinedSet:R-HSA-2468319
		//|    |    |    o EWAS:R-HSA-2468299
		//|    |    |    o EWAS:R-HSA-2468300
		//|    |    |    o EWAS:R-HSA-2468301
		//|    |    |    o ...
		//|    |    + DefinedSet:R-HSA-2468333
		//|    |    |    o EWAS:R-HSA-2468304
		//|    |    |    o EWAS:R-HSA-2468306
		//|    |    |    o EWAS:R-HSA-2468309
		//|    |    |    o ...
		final List<Interaction> expected = Collections.emptyList();
		final List<Interaction> interactions = InteractionExporter.stream(interactionExporter ->
				interactionExporter.setObject("R-HSA-2564685")
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
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-5210918"),
						new Interactor(getById("R-ALL-74112"), 2L, Constants.UNSPECIFIED_ROLE),
						new Interactor(getById("R-HSA-5205722"), 1L, Constants.UNSPECIFIED_ROLE)),
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-5210918"),
						new Interactor(getById("R-BAN-5205707"), 1L, Constants.UNSPECIFIED_ROLE),
						new Interactor(getById("R-HSA-5205722"), 1L, Constants.UNSPECIFIED_ROLE)),
				new Interaction(InteractionType.PHYSICAL, getById("R-BAN-5205716"),
						new Interactor(getById("R-BAN-5205707"), 1L, Constants.UNSPECIFIED_ROLE),
						new Interactor(getById("R-ALL-74112"), 2L, Constants.UNSPECIFIED_ROLE))
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
				new Interaction(InteractionType.PHYSICAL, getById("R-MTU-1222294"),
						new Interactor(getById("R-ALL-71185"), 2L, Constants.UNSPECIFIED_ROLE),
						new Interactor(getById("R-MTU-1222724"), 2L, Constants.UNSPECIFIED_ROLE)),
				new Interaction(InteractionType.PHYSICAL, getById("R-MTU-1222294"),
						new Interactor(getById("R-MTU-1222724"), 2L, Constants.UNSPECIFIED_ROLE),
						new Interactor(getById("R-MTU-1222724"), 0L, Constants.UNSPECIFIED_ROLE)),
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-1222723"),
						new Interactor(getById("R-ALL-1222525"), 2L, Constants.UNSPECIFIED_ROLE),
						new Interactor(getById("R-MTU-1222724"), 2L, Constants.UNSPECIFIED_ROLE)),
				new Interaction(InteractionType.fromGo("GO:0008941"), getById("R-HSA-1222723"),
						new Interactor(getById("R-ALL-71185"), 2L, Constants.UNSPECIFIED_ROLE),
						new Interactor(getById("R-MTU-1222724"), 2L, Constants.UNSPECIFIED_ROLE)),
				new Interaction(InteractionType.fromGo("GO:0008941"), getById("R-HSA-1222723"),
						new Interactor(getById("R-MTU-1222724"), 2L, Constants.UNSPECIFIED_ROLE),
						new Interactor(getById("R-MTU-1222724"), 2L, Constants.UNSPECIFIED_ROLE))
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
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-5218862"),
						new Interactor(getById("R-HSA-450328"), 1L, Constants.UNSPECIFIED_ROLE),
						new Interactor(getById("R-HSA-168651"), 1L, Constants.UNSPECIFIED_ROLE)),
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-5218868"),
						new Interactor(getById("R-HSA-5218872"), 1L, Constants.UNSPECIFIED_ROLE),
						new Interactor(getById("R-HSA-450328"), 1L, Constants.UNSPECIFIED_ROLE)),

				new Interaction(InteractionType.fromGo("GO:0004674"), getById("R-HSA-5213466"),
						new Interactor(getById("R-HSA-168651"), 1L, Constants.ENZYME),
						new Interactor(getById("R-HSA-450328"), 1L, Constants.ENZYME_TARGET)),
				new Interaction(InteractionType.fromGo("GO:0004674"), getById("R-HSA-5213466"),
						new Interactor(getById("R-HSA-168651"), 1L, Constants.ENZYME),
						new Interactor(getById("R-HSA-5218872"), 1L, Constants.ENZYME_TARGET)),
				new Interaction(InteractionType.fromGo("GO:0004674"), getById("R-HSA-5213466"),
						new Interactor(getById("R-HSA-450328"), 1L, Constants.ENZYME),
						new Interactor(getById("R-HSA-450328"), 1L, Constants.ENZYME_TARGET)),
				new Interaction(InteractionType.fromGo("GO:0004674"), getById("R-HSA-5213466"),
						new Interactor(getById("R-HSA-450328"), 1L, Constants.ENZYME),
						new Interactor(getById("R-HSA-5218872"), 1L, Constants.ENZYME_TARGET))
		);
		final List<Interaction> interactions = InteractionExporter.stream(exporter ->
				exporter.setObject("R-HSA-5213466")).collect(Collectors.toList());
		assertEquals(expected, interactions);
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
