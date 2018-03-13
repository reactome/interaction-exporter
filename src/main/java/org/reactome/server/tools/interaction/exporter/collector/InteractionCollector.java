package org.reactome.server.tools.interaction.exporter.collector;

import org.reactome.server.graph.domain.model.*;
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
	private int maxSetSize;

	private InteractionCollector(Collection<DatabaseObject> objects, Format format, IncludeSimpleEntity includeSimpleEntity, String species, PrintStream output, int maxSetSize) {
		this.objects = objects;
		this.format = format;
		this.includeSimpleEntity = includeSimpleEntity;
		this.species = species;
		this.output = output;
		this.maxSetSize = maxSetSize;
	}

	public static void export(Collection<DatabaseObject> objects, Format format, PrintStream output, IncludeSimpleEntity includeSimpleEntity, String speciesName, int maxSetSize) {
		new InteractionCollector(objects, format, includeSimpleEntity, speciesName, output, maxSetSize).export();
	}

	private void export() {
		final ProgressBar bar = new ProgressBar();
		int i = 1;
		for (DatabaseObject object : objects) {
			explore(object, object);
			final String progress = String.format("%d/%d %s:%s", i, objects.size(), object.getSchemaClass(), object.getStId());
			bar.setProgress((double) i / objects.size(), progress);
			i += 1;
		}
	}

	private void explore(DatabaseObject context, DatabaseObject object) {
		if (object instanceof Polymer)
			explorePolymer(context, (Polymer) object);
		else if (object instanceof Complex)
			exploreComplex(context, (Complex) object);
		else if (object instanceof ReactionLikeEvent)
			exploreReaction(context, (ReactionLikeEvent) object);
		else if (object instanceof EntitySet)
			exploreEntitySet(context, (EntitySet) object);
	}

	private void explorePolymer(DatabaseObject context, Polymer polymer) {
		if (polymer.getRepeatedUnit() != null) {
			for (PhysicalEntity entity : polymer.getRepeatedUnit())
				addInteraction(context, "physical", entity, 0, entity, 0);
		}
	}

	private void exploreEntitySet(DatabaseObject context, EntitySet entitySet) {
		if (entitySet.getHasMember() != null) {
			if (entitySet.getHasMember().size() > maxSetSize) return;
			for (PhysicalEntity entity : entitySet.getHasMember())
				explore(context, entity);
		}
	}

	private void exploreComplex(DatabaseObject context, Complex complex) {
		if (complex.getHasComponent() == null) return;
		final Map<PhysicalEntity, Integer> participants = participants(complex.getHasComponent());
		final ArrayList<PhysicalEntity> components = new ArrayList<>(participants.keySet());
		if (importantParticipants(participants) > maxSetSize) return;
		for (int i = 0; i < components.size(); i++)
			for (int j = i + 1; j < components.size(); j++)
				addInteraction(context, "physical", components.get(i), participants.get(components.get(i)), components.get(j), participants.get(components.get(i)));
		// Oligomers
		participants.forEach((entity, stoichiometry) -> {
			if (stoichiometry > 1)
				addInteraction(context, "physical", entity, stoichiometry, entity, 0);
		});
		components.forEach(physicalEntity -> explore(context, physicalEntity));
	}

	private void exploreReaction(DatabaseObject context, ReactionLikeEvent reaction) {
		if (reaction.getInput() != null) {
			final Map<PhysicalEntity, Integer> participants = participants(reaction.getInput());
			final ArrayList<PhysicalEntity> components = new ArrayList<>(participants.keySet());
			if (importantParticipants(participants) > maxSetSize) return;
			for (int i = 0; i < components.size(); i++)
				for (int j = i + 1; j < components.size(); j++)
					addInteraction(context, "physical", components.get(i), participants.get(components.get(i)), components.get(j), participants.get(components.get(i)));
			// Oligomers
			participants.forEach((entity, stoichiometry) -> {
				if (stoichiometry > 1)
					addInteraction(context, "physical", entity, stoichiometry, entity, 0);
			});
			// Catalyses
			if (reaction.getCatalystActivity() != null)
				reaction.getCatalystActivity().forEach(catalystActivity -> {
					participants.forEach((participant, stoichiometry) ->
							addInteraction(context, "enzymatic", catalystActivity.getPhysicalEntity(), 0, participant, stoichiometry));
					if (catalystActivity.getActiveUnit() != null) {
						participants.forEach((participant, stoichiometry) ->
								catalystActivity.getActiveUnit().forEach(physicalEntity ->
										addInteraction(context, "physical", physicalEntity, 0, participant, stoichiometry)));
					}
				});
			components.forEach(entity -> explore(context, entity));
		}
	}

	private void addInteraction(DatabaseObject context, String type, PhysicalEntity a, int as, PhysicalEntity b, int bs) {
		if (a instanceof EntitySet) {
			final EntitySet set = (EntitySet) a;
			if (set.getHasMember() != null) {
				final Map<PhysicalEntity, Integer> participants = participants(set.getHasMember());
				if (type.equals("enzymatic") || importantParticipants(participants) <= maxSetSize)
					participants.forEach((child, s) -> addInteraction(context, "physical", child, s, b, bs));
			}
		} else if (b instanceof EntitySet) {
			final EntitySet set = (EntitySet) b;
			if (set.getHasMember() != null) {
				final Map<PhysicalEntity, Integer> participants = participants(set.getHasMember());
				if (importantParticipants(participants) <= maxSetSize)
					participants.forEach((child, s) -> addInteraction(context, "physical", a, as, child, s));
			}
		} else if (a instanceof Complex) {
			final Complex complex = (Complex) a;
			if (complex.getHasComponent() != null) {
				final Map<PhysicalEntity, Integer> participants = participants(complex.getHasComponent());
				if (importantParticipants(participants) <= maxSetSize)
					participants.forEach((child, s) -> addInteraction(context, "physical", child, s, b, bs));
			}
			// Optional, write interactions of complexes
			writeInteraction(context, a, as, b, bs);
		} else if (b instanceof Complex) {
			final Complex complex = (Complex) b;
			if (complex.getHasComponent() != null) {
				final Map<PhysicalEntity, Integer> participants = participants(complex.getHasComponent());
				if (importantParticipants(participants) <= maxSetSize)
					participants.forEach((child, s) -> addInteraction(context, "physical", a, as, child, s));
			}
			// Optional, write interactions of complexes
			writeInteraction(context, a, as, b, bs);
		} else writeInteraction(context, a, as, b, bs);
	}

	private long importantParticipants(Map<PhysicalEntity, Integer> participants) {
		return participants.keySet().stream()
				.filter(entity -> !(entity instanceof SimpleEntity))
				.filter(entity -> !(entity instanceof GenomeEncodedEntity))
				.count();
//		return participants.size();
	}

	private void writeInteraction(DatabaseObject context, PhysicalEntity A, Integer Ast, PhysicalEntity B, Integer Bst) {
		if (A.getStId().compareTo(B.getStId()) > 0)
			writeInteraction(context, B, Bst, A, Ast);
		else {
			final String uniqueId = context.getStId() + A.getStId() + B.getStId();
			if (interactions.contains(uniqueId)) return;
			format.write(new Interaction(context, A, Ast, B, Bst), output);
			interactions.add(uniqueId);
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
