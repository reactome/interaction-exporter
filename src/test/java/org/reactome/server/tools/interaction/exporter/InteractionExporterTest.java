package org.reactome.server.tools.interaction.exporter;

import org.junit.*;
import org.reactome.server.tools.interaction.exporter.filter.SimpleEntityPolicy;
import org.reactome.server.tools.interaction.exporter.model.Interaction;
import org.reactome.server.tools.interaction.exporter.model.InteractionType;
import org.reactome.server.tools.interaction.exporter.model.Interactor;
import org.reactome.server.tools.interaction.exporter.util.Constants;

import java.util.*;
import java.util.stream.Collectors;

import static org.reactome.server.tools.interaction.exporter.TestUtils.getById;
import static org.reactome.server.tools.interaction.exporter.TestUtils.hasConnection;

public class InteractionExporterTest {

	private static final String HOMO_SAPIENS = "Homo sapiens";

	@Before
	public void setUp() {
		Assume.assumeTrue(hasConnection());
	}

	@Test
	public void testPolymer() {
		final String stId = "R-HSA-182548";
		final List<Interaction> interactions = InteractionExporter.stream(exporter ->
				exporter.setObject(stId))
				.collect(Collectors.toList());
		final List<Interaction> expected = Collections.singletonList(
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-182548"),
						new Interactor(getById("R-HSA-173751"), 0L, Constants.UNSPECIFIED_ROLE),
						new Interactor(getById("R-HSA-173751"), 0L, Constants.UNSPECIFIED_ROLE)));
		assertEquals(expected, interactions);
	}

	@Test
	public void testOlygomer() {
		final String stId = "R-HSA-110576";
		final List<Interaction> interactions = InteractionExporter.stream(exporter ->
				exporter.setSpecies(HOMO_SAPIENS)
						.setSimpleEntityPolicy(SimpleEntityPolicy.NON_TRIVIAL)
						.setMaxUnitSize(4)
						.setObject(stId))
				.collect(Collectors.toList());
		final List<Interaction> expected = Collections.singletonList(
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-110576"),
						new Interactor(getById("R-HSA-60024"), 6L, Constants.UNSPECIFIED_ROLE),
						new Interactor(getById("R-HSA-60024"), 0L, Constants.UNSPECIFIED_ROLE))
		);
		assertEquals(expected, interactions);
	}

	@Test
	public void testComplexWithSet() {
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
	public void testLimit() {
		// + Polymer:R-HSA-2564685
		// |    - Complex:R-HSA-2468137
		// |    |    + DefinedSet:R-HSA-2468257
		// |    |    |    o EWAS:R-HSA-2468208
		// |    |    |    o EWAS:R-HSA-2468209
		// |    |    |    o EWAS:R-HSA-2468212
		// |    |    |    o ...
		// |    |    + DefinedSet:R-HSA-2468319
		// |    |    |    o EWAS:R-HSA-2468299
		// |    |    |    o EWAS:R-HSA-2468300
		// |    |    |    o EWAS:R-HSA-2468301
		// |    |    |    o ...
		// |    |    + DefinedSet:R-HSA-2468333
		// |    |    |    o EWAS:R-HSA-2468304
		// |    |    |    o EWAS:R-HSA-2468306
		// |    |    |    o EWAS:R-HSA-2468309
		// |    |    |    o ...
		final List<Interaction> expected = Collections.emptyList();
		final List<Interaction> interactions = InteractionExporter.stream(interactionExporter ->
				interactionExporter.setObject("R-HSA-2564685")
						.setMaxUnitSize(3)
						.setSpecies(HOMO_SAPIENS))
				.collect(Collectors.toList());
		assertEquals(expected, interactions);
	}

	@Test
	public void testSet() {
		// + CandidateSet:R-HSA-5357900
		// |    o EWAS:R-HSA-50845
		// |    o EWAS:R-HSA-50847
		final List<Interaction> interactions = InteractionExporter.stream(interactionExporter ->
				interactionExporter.setObject("R-HSA-5357900")
						.setMaxUnitSize(4))
				.collect(Collectors.toList());
		Assert.assertEquals(0, interactions.size());
	}

	@Test
	public void testReactionInputs() {
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
	public void testReactionCatalyst() {
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

	@Test
	public void testDuplicates() {
		// + Polymer:R-HSA-391092
		// |    - Complex:R-HSA-391091
		// |    |    + 3 x DefinedSet:R-HSA-391094
		// |    |    |    o EWAS:R-HSA-391093
		// |    |    |    o EWAS:R-HSA-391095
		// This is a polymer made out of trimers, trimers are combinations of two elements
		final List<Interaction> expected = Arrays.asList(
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-391092"),
						new Interactor(getById("R-HSA-391093"), 0L, Constants.UNSPECIFIED_ROLE),
						new Interactor(getById("R-HSA-391093"), 0L, Constants.UNSPECIFIED_ROLE)),
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-391092"),
						new Interactor(getById("R-HSA-391095"), 0L, Constants.UNSPECIFIED_ROLE),
						new Interactor(getById("R-HSA-391095"), 0L, Constants.UNSPECIFIED_ROLE)),
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-391092"),
						new Interactor(getById("R-HSA-391093"), 0L, Constants.UNSPECIFIED_ROLE),
						new Interactor(getById("R-HSA-391095"), 0L, Constants.UNSPECIFIED_ROLE)),
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-391091"),
						new Interactor(getById("R-HSA-391093"), 3L, Constants.UNSPECIFIED_ROLE),
						new Interactor(getById("R-HSA-391093"), 0L, Constants.UNSPECIFIED_ROLE)),
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-391091"),
						new Interactor(getById("R-HSA-391095"), 3L, Constants.UNSPECIFIED_ROLE),
						new Interactor(getById("R-HSA-391095"), 0L, Constants.UNSPECIFIED_ROLE)),
				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-391091"),
						new Interactor(getById("R-HSA-391093"), 3L, Constants.UNSPECIFIED_ROLE),
						new Interactor(getById("R-HSA-391095"), 0L, Constants.UNSPECIFIED_ROLE))
//				new Interaction(InteractionType.PHYSICAL, getById("R-HSA-391091"),
//						new Interactor(getById("R-HSA-391095"), 3L, Constants.UNSPECIFIED_ROLE),
//						new Interactor(getById("R-HSA-391093"), 0L, Constants.UNSPECIFIED_ROLE))
		);
		final List<Interaction> interactions = InteractionExporter.stream(exporter ->
				exporter.setObject("R-HSA-391092")).collect(Collectors.toList());
		assertEquals(expected, interactions);
	}

	@Test
	public void testEquals() {
		final Interactor a = new Interactor(getById("R-HSA-2089965"), 0L, Constants.UNSPECIFIED_ROLE);
		final Interactor b = new Interactor(getById("R-HSA-2089965"), 0L, Constants.UNSPECIFIED_ROLE);
		Assert.assertEquals(a, b);
		final Interactor c = new Interactor(getById("R-HSA-2089966"), 0L, Constants.UNSPECIFIED_ROLE);
		final Interaction interaction1 = new Interaction(InteractionType.PHYSICAL, getById("R-HSA-2428940"), a, c);
		final Interaction interaction2 = new Interaction(InteractionType.PHYSICAL, getById("R-HSA-2428940"), c, a);
		Assert.assertEquals(interaction1, interaction2);
	}

	@Test
	public void testHash() {
		final Interactor a = new Interactor(getById("R-HSA-2089965"), 0L, Constants.UNSPECIFIED_ROLE);
		final Interactor b = new Interactor(getById("R-HSA-2089965"), 0L, Constants.UNSPECIFIED_ROLE);
		Assert.assertEquals(a.hashCode(), b.hashCode());
		final Interactor c = new Interactor(getById("R-HSA-2089966"), 0L, Constants.UNSPECIFIED_ROLE);
		final Interaction interaction1 = new Interaction(InteractionType.PHYSICAL, getById("R-HSA-2428940"), a, c);
		final Interaction interaction2 = new Interaction(InteractionType.PHYSICAL, getById("R-HSA-2428940"), c, a);
		Assert.assertEquals(interaction1.hashCode(), interaction2.hashCode());
	}

	private void assertEquals(List<Interaction> expected, List<Interaction> interactions) {
		Set<Interaction> a = new HashSet<>(expected);
		a.removeAll(interactions);
		Set<Interaction> b = new HashSet<>(interactions);
		b.removeAll(expected);
		if (!a.isEmpty() || !b.isEmpty()) {
			String message = "Result does not match expected\n";
			if (!a.isEmpty())
				message += "These interactions are expected\n" + a.stream().map(String::valueOf).collect(Collectors.joining("\n")) + "\n";
			if (!b.isEmpty())
				message += "These interactions are not expected\n" + b.stream().map(String::valueOf).collect(Collectors.joining("\n")) + "\n";
			Assert.fail(message);
		}
		if (expected.size() != interactions.size()) {
			final Set<Interaction> uniques = new TreeSet<>();
			final Set<Interaction> duplicates =
					interactions.stream()
							.filter(interaction -> !uniques.add(interaction))
							.collect(Collectors.toSet());
			if (!duplicates.isEmpty()) {
				final String message = "These interactions are duplicated\n" + duplicates.stream().map(String::valueOf).collect(Collectors.joining("\n")) + "\n";
				Assert.fail(message);
			}
		}
	}
}
