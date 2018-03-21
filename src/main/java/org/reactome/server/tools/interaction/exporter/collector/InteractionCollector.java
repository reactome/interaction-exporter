package org.reactome.server.tools.interaction.exporter.collector;

import org.reactome.server.graph.domain.model.*;
import org.reactome.server.tools.interaction.exporter.Format;
import org.reactome.server.tools.interaction.exporter.IncludeSimpleEntity;
import org.reactome.server.tools.interaction.exporter.Interaction;
import org.reactome.server.tools.interaction.exporter.util.ProgressBar;

import java.io.PrintStream;
import java.util.*;

public class InteractionCollector {

	public static final String ENZYMATIC = "enzymatic";
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
			interactions = new TreeSet<>();
			explore(object, object);
			final String progress = String.format("%d/%d %s:%s", i, objects.size(), object.getSchemaClass(), object.getStId());
			bar.setProgress((double) i / objects.size(), progress);
			i += 1;
		}
		System.out.println();
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
				addInteraction(context, "physical", components.get(i), participants.get(components.get(i)), components.get(j), participants.get(components.get(j)));
		// Oligomers
		participants.forEach((entity, stoichiometry) -> {
			if (stoichiometry > 1)
				addInteraction(context, "physical", entity, stoichiometry, entity, 0);
		});
		components.forEach(physicalEntity -> explore(context, physicalEntity));
	}

	private void exploreReaction(DatabaseObject context, ReactionLikeEvent reaction) {
		if (reaction.getInput() != null) {
			final Map<PhysicalEntity, Integer> inputs = participants(reaction.getInput());
			if (importantParticipants(inputs) > maxSetSize) return;
			// Input as complex
			final ArrayList<PhysicalEntity> components = new ArrayList<>(inputs.keySet());
			for (int i = 0; i < components.size(); i++)
				for (int j = i + 1; j < components.size(); j++)
					addInteraction(context, "physical", components.get(i), inputs.get(components.get(i)), components.get(j), inputs.get(components.get(j)));
			components.stream()
					.filter(entity -> entity instanceof EntitySet)
					.forEach(entity -> explore(context, entity));
			// Oligomers
			inputs.forEach((input, stoichiometry) -> {
				if (stoichiometry > 1)
					addInteraction(context, "physical", input, stoichiometry, input, 0);
			});
			// Catalyst
			addCatalystInteractions(context, reaction, inputs);

		}
	}

	private void addCatalystInteractions(DatabaseObject context, ReactionLikeEvent reaction, Map<PhysicalEntity, Integer> inputs) {
		if (reaction.getCatalystActivity() == null || reaction.getCatalystActivity().size() > 1)
			return;
		/* Conditions for the input:
		 * EWAS | Complex | SimpleEntity | Other
		 * -----|---------|--------------|--------
		 *   1  |    0    |     any      |  0
		 *   0  |    1    |     any      |  0
		 *   0  |    0    |      1       |  0
		 */
		final List<PhysicalEntity> macro = new LinkedList<>();
		final List<PhysicalEntity> small = new LinkedList<>();
		final List<PhysicalEntity> other = new LinkedList<>();
		inputs.forEach((input, integer) -> {
			if (input instanceof SimpleEntity)
				small.add(input);
			else if (input instanceof Complex || input instanceof EntityWithAccessionedSequence)
				macro.add(input);
			else other.add(input);
		});

		final PhysicalEntity input = other.isEmpty()
				? macro.size() == 1
				? macro.get(0)
				: macro.isEmpty() && small.size() == 1
				? small.get(0)
				: null
				: null;

		if (input == null) return;

		/*
		 * Conditions for the catalyst
		 * 1 active unit ? active unit[0] : physical entity
		 */
		// Catalysts
		final CatalystActivity catalystActivity = reaction.getCatalystActivity().get(0);
		final PhysicalEntity catalyst = catalystActivity.getActiveUnit() != null && catalystActivity.getActiveUnit().size() == 1
				? catalystActivity.getActiveUnit().iterator().next()
				: catalystActivity.getPhysicalEntity();
		addInteraction(context, ENZYMATIC, catalyst, 1, input, inputs.get(input));
//		if (reaction.getCatalystActivity() != null)
//			reaction.getCatalystActivity().forEach(catalystActivity -> {
//				// Active unit []
//				if (catalystActivity.getActiveUnit() != null && catalystActivity.getActiveUnit().size() == 1) {
//					final PhysicalEntity catalyst = catalystActivity.getActiveUnit().iterator().next();
//					addInteraction(context, "enzymatic", catalyst, 1, input, inputs.get(input));
////					inputs.forEach((input, stoichiometry) ->
////								catalystActivity.getActiveUnit().forEach(activeUnit ->
////												addInteraction(context, "enzymatic", activeUnit, 1, input, stoichiometry)
////							explore(context, physicalEntity);
//					);
//				} else {
//					// Physical entity
////					    explore(context, catalystActivity.getPhysicalEntity());
//					inputs.forEach((participant, stoichiometry) ->
//							addInteraction(context, "enzymatic", catalystActivity.getPhysicalEntity(), 0, participant, stoichiometry));
//				}
//			});
	}

	public void addInteraction(DatabaseObject context, String type, PhysicalEntity a, int as, PhysicalEntity b, int bs) {
		if (a instanceof EntitySet) {
			final EntitySet set = (EntitySet) a;
			if (set.getHasMember() != null) {
				final Map<PhysicalEntity, Integer> participants = participants(set.getHasMember());
				if (type.equals(ENZYMATIC) || importantParticipants(participants) <= maxSetSize)
					participants.forEach((child, s) -> addInteraction(context, type, child, s, b, bs));
			}
		} else if (b instanceof EntitySet) {
			final EntitySet set = (EntitySet) b;
			if (set.getHasMember() != null) {
				final Map<PhysicalEntity, Integer> participants = participants(set.getHasMember());
				if (importantParticipants(participants) <= maxSetSize)
					participants.forEach((child, s) -> addInteraction(context, type, a, as, child, s));
			}
		} else if (a instanceof Complex) {
			final Complex complex = (Complex) a;
			if (type.equals(ENZYMATIC))
				writeInteraction(type, context, a, as, b, bs);
			else if (complex.getHasComponent() != null) {
				final Map<PhysicalEntity, Integer> participants = participants(complex.getHasComponent());
				if (importantParticipants(participants) <= maxSetSize)
					participants.forEach((child, s) -> addInteraction(context, type, child, s, b, bs));
			}
			// Optional, write interactions of complexes
//			writeInteraction(context, a, as, b, bs);
		} else if (b instanceof Complex) {
			final Complex complex = (Complex) b;
			// Input complexes of a reaction are not expanded
			if (type.equals(ENZYMATIC))
				writeInteraction(type, context, a, as, b, bs);
			else if (complex.getHasComponent() != null) {
				final Map<PhysicalEntity, Integer> participants = participants(complex.getHasComponent());
				if (importantParticipants(participants) <= maxSetSize)
					participants.forEach((child, s) -> addInteraction(context, type, a, as, child, s));
			}
		} else writeInteraction(type, context, a, as, b, bs);
	}

	private long importantParticipants(Map<PhysicalEntity, Integer> participants) {
		return participants.keySet().stream()
				.filter(entity -> !(entity instanceof SimpleEntity))
				.filter(entity -> !(entity instanceof GenomeEncodedEntity))
				.count();
//		return participants.size();
	}

	private void writeInteraction(String type, DatabaseObject context, PhysicalEntity A, Integer Ast, PhysicalEntity B, Integer Bst) {
		if (A instanceof SimpleEntity && B instanceof SimpleEntity) return;
		int size = 0;
		if (!(A instanceof SimpleEntity)) size += Ast;
		if (!(B instanceof SimpleEntity)) size += Bst;
		// A == B -> olygomer
		if (!type.equals(ENZYMATIC) && A != B && size > maxSetSize) return;
		if (A.getStId().compareTo(B.getStId()) > 0)
			writeInteraction(type, context, B, Bst, A, Ast);
		else {
			final String uniqueId = context.getStId() + A.getStId() + B.getStId();
			if (interactions.contains(uniqueId)) return;
			format.write(new Interaction(type, context, A, Ast, B, Bst), output);
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
