package org.reactome.server.tools.interaction.exporter;

import org.reactome.server.graph.domain.model.*;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.graph.service.SchemaService;
import org.reactome.server.graph.utils.ReactomeGraphCore;
import org.reactome.server.tools.interaction.exporter.filter.IncludeSimpleEntity;
import org.reactome.server.tools.interaction.exporter.util.ProgressBar;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class InteractionExporter {

	private static final DatabaseObjectService OBJECT_SERVICE = ReactomeGraphCore.getService(DatabaseObjectService.class);
	private static final SchemaService SCHEMA_SERVICE = ReactomeGraphCore.getService(SchemaService.class);

	private String species = "Homo sapiens";
	private IncludeSimpleEntity includeSimpleEntity = IncludeSimpleEntity.NON_TRIVIAL;
	private String stId;
	private int maxUnitSize = 4;
	private boolean verbose;
	private InteractionExplorer collector;

	private InteractionExporter() {
	}

	public static Stream<Interaction> stream(Consumer<InteractionExporter> consumer) {
		final InteractionExporter exporter = new InteractionExporter();
		consumer.accept(exporter);
		return exporter.stream();
	}

	public InteractionExporter setSpecies(String species) {
		this.species = species;
		return this;
	}

	public InteractionExporter setIncludeSimpleEntity(IncludeSimpleEntity includeSimpleEntity) {
		this.includeSimpleEntity = includeSimpleEntity;
		return this;
	}

	public InteractionExporter setObject(String stId) {
		this.stId = stId;
		return this;
	}

	public InteractionExporter setMaxUnitSize(int maxUnitSize) {
		this.maxUnitSize = maxUnitSize;
		return this;
	}

	public InteractionExporter setVerbose(boolean verbose) {
		this.verbose = verbose;
		return this;
	}

	private Stream<Interaction> stream() {
		collector = new InteractionExplorer(includeSimpleEntity, maxUnitSize);
		if (stId != null) {
			final DatabaseObject object = OBJECT_SERVICE.findById(stId);
			final Collection<DatabaseObject> subContexts = collectContexts(object);
			return subContexts.stream()
					.map(context -> collector.explore(context))
					.flatMap(Collection::stream);
		} else {
			if (verbose) {
				final AtomicLong total = new AtomicLong();
				final AtomicLong count = new AtomicLong();
				final ProgressBar bar = new ProgressBar();
				return Stream.of(Polymer.class, Complex.class, ReactionLikeEvent.class)
						.map(aClass -> SCHEMA_SERVICE.getByClass(aClass, species))
						.peek(collection -> {
							total.set(collection.size());
							bar.restart();
							count.set(0);
						})
						.flatMap(Collection::stream)
						.peek(o -> {
							count.incrementAndGet();
							final String progress = String.format("%d/%d %s:%s", count.get(), total.get(), o.getSchemaClass(), o.getStId());
							bar.setProgress(count.doubleValue() / total.doubleValue(), progress);
						})
						.map(collector::explore)
						.flatMap(Collection::stream);
			} else {
				return Stream.of(Polymer.class, Complex.class, ReactionLikeEvent.class)
						.map(reactomeClass -> SCHEMA_SERVICE.getByClass(reactomeClass, species))
						.flatMap(Collection::stream)
						.map(collector::explore)
						.flatMap(Collection::stream);
			}
		}
	}

	/** Search for elements that can be contexts in this object. */
	private Collection<DatabaseObject> collectContexts(DatabaseObject object) {
		final Set<DatabaseObject> contexts = new LinkedHashSet<>();
		if (object instanceof Complex) {
			final Complex complex = (Complex) object;
			contexts.add(complex);
			if (complex.getHasComponent() != null)
				complex.getHasComponent().stream()
						.map(this::collectContexts)
						.forEach(contexts::addAll);
		} else if (object instanceof EntitySet) {
			final EntitySet entitySet = (EntitySet) object;
			if (entitySet.getHasMember() != null)
				entitySet.getHasMember().stream()
						.map(this::collectContexts)
						.forEach(contexts::addAll);
		} else if (object instanceof Polymer) {
			final Polymer polymer = (Polymer) object;
			contexts.add(polymer);
			if (polymer.getRepeatedUnit() != null)
				polymer.getRepeatedUnit().stream()
						.map(this::collectContexts)
						.forEach(contexts::addAll);
		} else if (object instanceof ReactionLikeEvent) {
			final ReactionLikeEvent reaction = (ReactionLikeEvent) object;
			contexts.add(reaction);
			if (reaction.getInput() != null)
				reaction.getInput().stream()
						.map(this::collectContexts)
						.forEach(contexts::addAll);
			if (reaction.getCatalystActivity() != null)
				reaction.getCatalystActivity().forEach(catalystActivity -> {
					if (catalystActivity.getActiveUnit() != null)
						catalystActivity.getActiveUnit().stream()
								.map(this::collectContexts)
								.forEach(contexts::addAll);
					if (catalystActivity.getPhysicalEntity() != null)
						contexts.addAll(collectContexts(catalystActivity.getPhysicalEntity()));
				});
		}
		return contexts;
	}
}
