package org.reactome.server.tools.interaction.exporter.collector;

import org.reactome.server.graph.domain.model.*;
import org.reactome.server.tools.interaction.exporter.Context;
import org.reactome.server.tools.interaction.exporter.Format;
import org.reactome.server.tools.interaction.exporter.IncludeSimpleEntity;
import org.reactome.server.tools.interaction.exporter.Interaction;
import org.reactome.server.tools.interaction.exporter.util.ProgressBar;

import java.io.PrintStream;
import java.util.*;

public class InteractionCollector {

	private final Format format;
	private final PrintStream output;
	private final IncludeSimpleEntity includeSimpleEntity;
	private String species;
	private Collection<DatabaseObject> objects;
	private Set<String> interactions = new TreeSet<>();

	private InteractionCollector(Collection<DatabaseObject> objects, Format format, IncludeSimpleEntity includeSimpleEntity, String species, PrintStream output) {
		this.objects = objects;
		this.format = format;
		this.includeSimpleEntity = includeSimpleEntity;
		this.species = species;
		this.output = output;
	}

	public static void export(Collection<DatabaseObject> objects, Format format, PrintStream output, IncludeSimpleEntity includeSimpleEntity, String speciesName) {
		new InteractionCollector(objects, format, includeSimpleEntity, speciesName, output).export();
	}

	private void export() {
		final ProgressBar bar = new ProgressBar();
		int i = 1;
		for (DatabaseObject object : objects) {
			final List<Context> contexts = collectContexts(object);
			contexts.forEach(this::explore);
			bar.setProgress((double) i / objects.size(), object.getSchemaClass() + ":" + object.getStId());
			i += 1;
		}
	}

	private List<Context> collectContexts(DatabaseObject entity) {
		final List<Context> contexts = new LinkedList<>();
		if (entity instanceof Complex) {
			final Complex complex = (Complex) entity;
			contexts.add(new Context(complex));
			if (complex.getHasComponent() != null)
				complex.getHasComponent().forEach(child -> {
					final List<Context> subContexts = collectContexts(child);
					for (Context subContext : subContexts)
						subContext.getAncestors().add(0, complex);
					contexts.addAll(subContexts);
				});
		} else if (entity instanceof EntitySet) {
			final EntitySet entitySet = (EntitySet) entity;
			if (entitySet.getHasMember() != null)
				entitySet.getHasMember().forEach(child ->
						contexts.addAll(collectContexts(child)));
		} else if (entity instanceof Polymer) {
			final Polymer polymer = (Polymer) entity;
			contexts.add(new Context(polymer));
			if (polymer.getRepeatedUnit() != null) {
				polymer.getRepeatedUnit().forEach(child -> {
					final List<Context> subContexts = collectContexts(child);
					subContexts.forEach(context -> context.getAncestors().add(0, polymer));
					contexts.addAll(subContexts);
				});
			}
		} else if (entity instanceof ReactionLikeEvent) {
			final ReactionLikeEvent reaction = (ReactionLikeEvent) entity;
			contexts.add(new Context(reaction));
			if (reaction.getInput() != null) {
				reaction.getInput().forEach(child -> {
					final List<Context> subContexts = collectContexts(child);
					subContexts.forEach(context -> context.getAncestors().add(0, reaction));
					contexts.addAll(subContexts);
				});
			}
		}
		return contexts;
	}

	private void explore(Context context) {
		if (context.getDatabaseObject() instanceof Polymer) {
			final Polymer polymer = (Polymer) context.getDatabaseObject();
			if (polymer.getRepeatedUnit() != null)
				polymer.getRepeatedUnit().forEach(physicalEntity ->
						addInteraction(context, physicalEntity, 0, physicalEntity, 0));
		} else if (context.getDatabaseObject() instanceof Complex) {
			final Complex complex = (Complex) context.getDatabaseObject();
			if (complex.getHasComponent() == null) return;
			final Map<PhysicalEntity, Integer> participants = participants(complex.getHasComponent());
			if (participants.size() > 4) return;
			final ArrayList<PhysicalEntity> components = new ArrayList<>(participants.keySet());
			for (int i = 0; i < components.size(); i++)
				for (int j = i + 1; j < components.size(); j++)
					addInteraction(context, components.get(i), participants.get(components.get(i)), components.get(j), participants.get(components.get(i)));
			// Oligomers
			participants.forEach((entity, stoichiometry) -> {
				if (stoichiometry > 1)
					addInteraction(context, entity, stoichiometry, entity, 0);
			});
		} else if (context.getDatabaseObject() instanceof ReactionLikeEvent) {
			final ReactionLikeEvent reaction = (ReactionLikeEvent) context.getDatabaseObject();
			if (reaction.getInput() != null) {
				final Map<PhysicalEntity, Integer> participants = participants(reaction.getInput());
				if (participants.size() > 4) return;
				final ArrayList<PhysicalEntity> components = new ArrayList<>(participants.keySet());
				for (int i = 0; i < components.size(); i++)
					for (int j = i + 1; j < components.size(); j++)
						addInteraction(context, components.get(i), participants.get(components.get(i)), components.get(j), participants.get(components.get(i)));
				// Oligomers
				participants.forEach((entity, stoichiometry) -> {
					if (stoichiometry > 1)
						addInteraction(context, entity, stoichiometry, entity, 0);
				});
				// Catalyses
				if (reaction.getCatalystActivity() != null)
					reaction.getCatalystActivity().forEach(catalystActivity -> {
						participants.forEach((participant, stoichiometry) ->
								addInteraction(context, catalystActivity.getPhysicalEntity(), 0, participant, stoichiometry));
						if (catalystActivity.getActiveUnit() != null) {
							participants.forEach((participant, stoichiometry) ->
									catalystActivity.getActiveUnit().forEach(physicalEntity ->
											addInteraction(context, physicalEntity, 0, participant, stoichiometry)));
						}
					});
			}
		}
	}

	private void addInteraction(Context context, PhysicalEntity a, int as, PhysicalEntity b, int bs) {
		if (a instanceof EntitySet) {
			final EntitySet set = (EntitySet) a;
			if (set.getHasMember() != null) {
				final Map<PhysicalEntity, Integer> participants = participants(set.getHasMember());
				if (participants.size() > 4) return;
				participants.forEach((child, s) -> addInteraction(context, child, s, b, bs));
			}
		} else if (b instanceof EntitySet) {
			final EntitySet set = (EntitySet) b;
			if (set.getHasMember() != null) {
				final Map<PhysicalEntity, Integer> participants = participants(set.getHasMember());
				if (participants.size() > 4) return;
				participants.forEach((child, s) -> addInteraction(context, a, as, child, s));
			}
		} else if (a instanceof Complex) {
			final Complex complex = (Complex) a;
			if (complex.getHasComponent() != null) {
				final Map<PhysicalEntity, Integer> participants = participants(complex.getHasComponent());
				if (participants.size() > 4) return;
				participants.forEach((child, s) -> addInteraction(context, child, s, b, bs));
			}
			// Optional, write interactions of complexes
			writeInteraction(context, a, as, b, bs);
		} else if (b instanceof Complex) {
			final Complex complex = (Complex) b;
			if (complex.getHasComponent() != null) {
				final Map<PhysicalEntity, Integer> participants = participants(complex.getHasComponent());
				if (participants.size() > 4) return;
				participants.forEach((child, s) -> addInteraction(context, a, as, child, s));
			}
			// Optional, write interactions of complexes
			writeInteraction(context, a, as, b, bs);
		} else writeInteraction(context, a, as, b, bs);
	}

	private void writeInteraction(Context context, PhysicalEntity A, Integer Ast, PhysicalEntity B, Integer Bst) {
		if (A.getStId().compareTo(B.getStId()) > 0)
			writeInteraction(context, B, Bst, A, Ast);
		else {
			final LinkedList<DatabaseObject> contexts = new LinkedList<>(context.getAncestors());
			contexts.add(context.getDatabaseObject());
			contexts.forEach(entity -> {
				final String uniqueId = entity.getStId() + A.getStId() + B.getStId();
				if (interactions.contains(uniqueId)) return;
				format.write(new Interaction(entity, A, Ast, B, Bst), output);
				interactions.add(uniqueId);
			});
		}
	}

	private Map<PhysicalEntity, Integer> participants(List<PhysicalEntity> entities) {
		final Map<PhysicalEntity, Integer> participants = new TreeMap<>();
		entities.stream()
				.filter(includeSimpleEntity.getFilter())
				.filter(entity -> !(entity instanceof OtherEntity))
				.forEach(physicalEntity -> participants.put(physicalEntity, participants.getOrDefault(physicalEntity, 0) + 1));
		return participants;
	}


}
