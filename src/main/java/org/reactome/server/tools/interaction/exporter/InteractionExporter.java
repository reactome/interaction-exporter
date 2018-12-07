package org.reactome.server.tools.interaction.exporter;

import org.reactome.server.graph.domain.model.*;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.graph.service.SchemaService;
import org.reactome.server.graph.utils.ReactomeGraphCore;
import org.reactome.server.tools.interaction.exporter.filter.SimpleEntityPolicy;
import org.reactome.server.tools.interaction.exporter.model.Interaction;
import org.reactome.server.tools.interaction.exporter.util.ProgressBar;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/**
 * High level runner: iterate over species and classes.
 */
class InteractionExporter {

	private static final DatabaseObjectService OBJECT_SERVICE = ReactomeGraphCore.getService(DatabaseObjectService.class);
	private static final SchemaService SCHEMA_SERVICE = ReactomeGraphCore.getService(SchemaService.class);

	private InteractionExporter() {
	}

	/**
	 * Calls per each species and object type (Polymer, Complex and Reaction)
	 * {@link ContextExplorer#explore(DatabaseObject)}, converting results into streams to avoid memory issues and
	 * allowing this method to be part of pipelines.
	 */
	static Stream<Interaction> streamSpecies(Collection<String> species, SimpleEntityPolicy simpleEntityPolicy, int maxUnitSize, boolean verbose) {
		final ContextExplorer contextExplorer = new ContextExplorer(simpleEntityPolicy, maxUnitSize);
		if (!verbose) {
			// For every object of type Polymer, Complex or Reaction in every species, we call contextExplorer.explore and stream the result
			// for spc in species:
			//     for reactomeClass in (Polymer.Class, Complex.class, ReactionLikeEvent.class)
			//         for object in SCHEMA_SERVICE.getByClass(reactomeClass, spc)
			//             contextExplorer.explore(object)
			return species.stream()
					.flatMap(spc -> Stream.of(Polymer.class, Complex.class, ReactionLikeEvent.class)
							.flatMap(reactomeClass -> SCHEMA_SERVICE.getByClass(reactomeClass, spc).stream())
							.flatMap(object -> contextExplorer.explore(object).stream()));
		} else {
			// In verbose mode we need to add 4 peeks to the stream:
			//   1) to print species name
			//   2) to start progress bar
			//   3) to update progress bar
			//   4) to close progress bar
			final AtomicLong total = new AtomicLong();
			final AtomicLong count = new AtomicLong();
			final ProgressBar bar = new ProgressBar();
			final AtomicLong speciesCount = new AtomicLong();
			return species.stream()
					.peek(spc -> System.out.printf("%n%s (%d/%d)%n", spc, speciesCount.incrementAndGet(), species.size()))
					.flatMap(spc -> Stream.of(Polymer.class, Complex.class, ReactionLikeEvent.class)
							.map(reactomeClass -> SCHEMA_SERVICE.getByClass(reactomeClass, spc))
							.peek(collection -> initProgressBar(total, count, bar, collection))
							.flatMap(Collection::stream)
							.peek(o -> updateProgressBar(total, count, bar, o))
							.flatMap(object -> contextExplorer.explore(object).stream())
							.onClose(bar::clear));
		}
	}

	/**
	 * @return a stream with the interactions inferred from this object, with a {@link SimpleEntityPolicy#NON_TRIVIAL} and maxUnitSize of 4.
	 */
	static Stream<Interaction> streamObject(String stId) {
		return streamObject(stId, SimpleEntityPolicy.NON_TRIVIAL, 4, false);
	}

	/**
	 * @return a stream with the interactions inferred from the object represented by stId
	 */
	static Stream<Interaction> streamObject(String stId, SimpleEntityPolicy simpleEntityPolicy, int maxUnitSize, boolean verbose) {
		return streamObjects(Collections.singletonList(stId), simpleEntityPolicy, maxUnitSize, verbose);
	}

	/**
	 * @return a stream with the interactions inferred from these objects
	 */
	static Stream<Interaction> streamObjects(List<String> stIds, SimpleEntityPolicy simpleEntityPolicy, int maxUnitSize, boolean verbose) {
		final ContextExplorer contextExplorer = new ContextExplorer(simpleEntityPolicy, maxUnitSize);
		if (!verbose) {
			return stIds.stream()
					.map(stId -> (DatabaseObject) OBJECT_SERVICE.findById(stId))
					.flatMap(object -> collectContexts(object).stream())
					.flatMap(context -> contextExplorer.explore(context).stream());
		} else {
			return stIds.stream()
					.peek(s -> System.out.printf("%n%s (%d/%d)%n", s, stIds.indexOf(s) + 1, stIds.size()))
					.map(stId -> (DatabaseObject) OBJECT_SERVICE.findById(stId))
					.flatMap(object -> collectContexts(object).stream())
					.flatMap(context -> contextExplorer.explore(context).stream());
		}

	}

	private static void updateProgressBar(AtomicLong total, AtomicLong count, ProgressBar bar, DatabaseObject o) {
		count.incrementAndGet();
		final String progress = String.format(Locale.ENGLISH, "%,6d / %,6d \t%s:%s", count.get(), total.get(), o.getSchemaClass(), o.getStId());
		bar.setProgress(count.doubleValue() / total.doubleValue(), progress);
	}

	private static void initProgressBar(AtomicLong total, AtomicLong count, ProgressBar bar, Collection<? extends DatabaseObject> collection) {
		total.set(collection.size());
		bar.clear();
		count.set(0);
	}

	/**
	 * Search for elements that can be contexts in this object.
	 */
	private static Collection<DatabaseObject> collectContexts(DatabaseObject object) {
		final Set<DatabaseObject> contexts = new LinkedHashSet<>();
		if (object instanceof Complex) {
			final Complex complex = (Complex) object;
			contexts.add(complex);
			if (complex.getHasComponent() != null)
				complex.getHasComponent().stream()
						.map(InteractionExporter::collectContexts)
						.forEach(contexts::addAll);
		} else if (object instanceof EntitySet) {
			final EntitySet entitySet = (EntitySet) object;
			if (entitySet.getHasMember() != null)
				entitySet.getHasMember().stream()
						.map(InteractionExporter::collectContexts)
						.forEach(contexts::addAll);
		} else if (object instanceof Polymer) {
			final Polymer polymer = (Polymer) object;
			contexts.add(polymer);
			if (polymer.getRepeatedUnit() != null)
				polymer.getRepeatedUnit().stream()
						.map(InteractionExporter::collectContexts)
						.forEach(contexts::addAll);
		} else if (object instanceof ReactionLikeEvent) {
			final ReactionLikeEvent reaction = (ReactionLikeEvent) object;
			contexts.add(reaction);
			if (reaction.getInput() != null)
				reaction.getInput().stream()
						.map(InteractionExporter::collectContexts)
						.forEach(contexts::addAll);
			if (reaction.getCatalystActivity() != null)
				reaction.getCatalystActivity().forEach(catalystActivity -> {
					if (catalystActivity.getActiveUnit() != null)
						catalystActivity.getActiveUnit().stream()
								.map(InteractionExporter::collectContexts)
								.forEach(contexts::addAll);
					if (catalystActivity.getPhysicalEntity() != null)
						contexts.addAll(collectContexts(catalystActivity.getPhysicalEntity()));
				});
		}
		return contexts;
	}
}
