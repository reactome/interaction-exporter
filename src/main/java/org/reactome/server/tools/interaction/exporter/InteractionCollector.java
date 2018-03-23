package org.reactome.server.tools.interaction.exporter;

import org.reactome.server.graph.domain.model.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class InteractionCollector {

	private static final String CHEMICAL = "ch";
	private final IncludeSimpleEntity includeSimpleEntity;
	private String species;
	private int maxSetSize;
	private Collection<Interaction> interactions = new LinkedHashSet<>();

	public InteractionCollector(IncludeSimpleEntity includeSimpleEntity, String species, int maxSetSize) {
		this.includeSimpleEntity = includeSimpleEntity;
		this.species = species;
		this.maxSetSize = maxSetSize;
	}

	public Collection<Interaction> explore(DatabaseObject object) {
		interactions.clear();
		if (object instanceof Polymer)
			explorePolymer((Polymer) object);
		else if (object instanceof Complex)
			exploreComplex((Complex) object);
		else if (object instanceof ReactionLikeEvent)
			exploreReaction((ReactionLikeEvent) object);
		return interactions;
	}

	private void explorePolymer(Polymer polymer) {
		if (polymer.getRepeatedUnit() != null) {
			for (PhysicalEntity entity : polymer.getRepeatedUnit())
				addInteraction(polymer, "physical", entity, 0, entity, 0);
		}
	}

	private void exploreComplex(Complex complex) {
		if (complex.getHasComponent() == null) return;
		final Map<PhysicalEntity, Integer> participants = participants(complex.getHasComponent());
		final ArrayList<PhysicalEntity> components = new ArrayList<>(participants.keySet());
		if (importantParticipants(participants) > maxSetSize) return;
		for (int i = 0; i < components.size(); i++)
			for (int j = i + 1; j < components.size(); j++)
				addInteraction(complex, "physical", components.get(i), participants.get(components.get(i)), components.get(j), participants.get(components.get(j)));
		// Oligomers
		participants.forEach((entity, stoichiometry) -> {
			if (stoichiometry > 1)
				addInteraction(complex, "physical", entity, stoichiometry, entity, 0);
		});
	}

	private void exploreReaction(ReactionLikeEvent reaction) {
		if (reaction.getInput() != null) {
			final Map<PhysicalEntity, Integer> inputs = participants(reaction.getInput());
			if (importantParticipants(inputs) > maxSetSize) return;
			// Input as complex
			final ArrayList<PhysicalEntity> components = new ArrayList<>(inputs.keySet());
			for (int i = 0; i < components.size(); i++)
				for (int j = i + 1; j < components.size(); j++)
					addInteraction(reaction, "physical", components.get(i), inputs.get(components.get(i)), components.get(j), inputs.get(components.get(j)));
			// Oligomers
			inputs.forEach((input, stoichiometry) -> {
				if (stoichiometry > 1)
					addInteraction(reaction, "physical", input, stoichiometry, input, 0);
			});
			// Catalyst
			exploreCatalystInteractions(reaction, reaction, inputs);

		}
	}

	private void exploreCatalystInteractions(DatabaseObject context, ReactionLikeEvent reaction, Map<PhysicalEntity, Integer> inputs) {
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
		addInteraction(context, CHEMICAL, catalyst, 1, input, inputs.get(input));
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

	private void addInteraction(DatabaseObject context, String type, PhysicalEntity a, int as, PhysicalEntity b, int bs) {
		if (a instanceof EntitySet) {
			final EntitySet set = (EntitySet) a;
			if (set.getHasMember() != null) {
				final Map<PhysicalEntity, Integer> participants = participants(set.getHasMember());
				if (type.equals(CHEMICAL) || importantParticipants(participants) <= maxSetSize)
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
			if (type.equals(CHEMICAL))
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
			if (type.equals(CHEMICAL))
				writeInteraction(type, context, a, as, b, bs);
			else if (complex.getHasComponent() != null) {
				final Map<PhysicalEntity, Integer> participants = participants(complex.getHasComponent());
				if (importantParticipants(participants) <= maxSetSize)
					participants.forEach((child, s) -> addInteraction(context, type, a, as, child, s));
			}
		} else writeInteraction(type, context, a, as, b, bs);
	}

	private void writeInteraction(String type, DatabaseObject context, PhysicalEntity A, Integer Ast, PhysicalEntity B, Integer Bst) {
		if (A instanceof SimpleEntity && B instanceof SimpleEntity) return;
		int size = 0;
		if (!(A instanceof SimpleEntity)) size += Ast;
		if (!(B instanceof SimpleEntity)) size += Bst;
		// A == B -> olygomer
		if (!type.equals(CHEMICAL) && A != B && size > maxSetSize) return;
		if (A.getStId().compareTo(B.getStId()) > 0)
			writeInteraction(type, context, B, Bst, A, Ast);
		else interactions.add(new Interaction(type, context, A, Ast, B, Bst));
	}

	private Map<PhysicalEntity, Integer> participants(List<PhysicalEntity> entities) {
		final Map<PhysicalEntity, Integer> participants = new TreeMap<>();
		entities.stream()
				.filter(includeSimpleEntity.getFilter())
				.filter(entity -> !(entity instanceof OtherEntity))
				.forEach(physicalEntity -> participants.put(physicalEntity, participants.getOrDefault(physicalEntity, 0) + 1));
		return participants;
	}

	private long importantParticipants(Map<PhysicalEntity, Integer> participants) {
		final AtomicLong total = new AtomicLong();
		participants.forEach((entity, integer) -> {
			if (entity instanceof Complex) {
				final Complex complex = (Complex) entity;
				if (complex.getHasComponent() != null)
					total.addAndGet(importantParticipants(participants(complex.getHasComponent())));
			} else if (entity instanceof EntitySet) {
				final EntitySet set = (EntitySet) entity;
				if (set.getHasMember() != null)
					total.addAndGet(importantParticipants(participants(set.getHasMember())));
			} else if (!(entity instanceof SimpleEntity))
				total.incrementAndGet();
		});
		return total.get();
	}

}
