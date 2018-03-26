package org.reactome.server.tools.interaction.exporter;

import org.reactome.server.graph.domain.model.*;
import org.reactome.server.tools.interaction.exporter.filter.IncludeSimpleEntity;

import java.util.*;

public class InteractionCollector {

	private IncludeSimpleEntity includeSimpleEntity;
	private String species;
	private int maxUnitSize;
	private Collection<Interaction> interactions;

	InteractionCollector(IncludeSimpleEntity includeSimpleEntity, String species, int maxUnitSize) {
		this.includeSimpleEntity = includeSimpleEntity;
		this.species = species;
		this.maxUnitSize = maxUnitSize;
	}

	public Collection<Interaction> explore(DatabaseObject object) {
		interactions = new LinkedHashSet<>();
		if (object instanceof Polymer)
			explorePolymer((Polymer) object);
		else if (object instanceof Complex)
			exploreComplex((Complex) object);
		else if (object instanceof ReactionLikeEvent)
			exploreReaction((ReactionLikeEvent) object);
		return interactions;
	}

	private void explorePolymer(Polymer polymer) {
		if (polymer.getRepeatedUnit() != null)
			for (PhysicalEntity entity : polymer.getRepeatedUnit())
				addInteraction(polymer, InteractionType.PHYSICAL, entity, 0, entity, 0);
	}

	private void exploreComplex(Complex complex) {
		final Unit unit = new Unit(complex, includeSimpleEntity);
		if (unit.getChildren().isEmpty()) return;
		matrixExpansion(complex, unit);
		olygomers(complex, unit);
	}

	private void exploreReaction(ReactionLikeEvent reaction) {
		final Unit unit = new Unit(reaction, includeSimpleEntity);
		if (unit.getChildren().isEmpty()) return;
		matrixExpansion(reaction, unit);
		olygomers(reaction, unit);
		exploreCatalystInteractions(reaction, unit);
	}

	private void olygomers(DatabaseObject context, Unit unit) {
		unit.getChildren().forEach((entity, stoichiometry) -> {
			if (stoichiometry > 1)
				addInteraction(context, InteractionType.PHYSICAL, entity, stoichiometry, entity, 0);
		});
	}

	private void matrixExpansion(DatabaseObject context, Unit unit) {
		if (unit.getUnitSize() > maxUnitSize) return;
		final ArrayList<PhysicalEntity> components = new ArrayList<>(unit.getChildren().keySet());
		for (int i = 0; i < components.size(); i++)
			for (int j = i + 1; j < components.size(); j++)
				addInteraction(context, InteractionType.PHYSICAL,
						components.get(i), unit.getChildren().get(components.get(i)),
						components.get(j), unit.getChildren().get(components.get(j)));

	}

	private void exploreCatalystInteractions(ReactionLikeEvent reaction, Unit unit) {
		if (reaction.getCatalystActivity() == null || reaction.getCatalystActivity().size() > 1)
			return;
		final List<PhysicalEntity> macro = new LinkedList<>();
		final List<PhysicalEntity> small = new LinkedList<>();
		final List<PhysicalEntity> other = new LinkedList<>();
		unit.getChildren().forEach((input, stoichiometry) -> {
			if (input instanceof SimpleEntity)
				small.add(input);
			else if (input instanceof Complex || input instanceof EntityWithAccessionedSequence || input instanceof EntitySet)
				macro.add(input);
			else other.add(input);
		});
		/* Conditions for the input:
		 * EWAS | Complex | SimpleEntity | Other
		 * -----|---------|--------------|--------
		 *   1  |    0    |     any      |  0
		 *   0  |    1    |     any      |  0
		 *   0  |    0    |      1       |  0
		 */
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
		final InteractionType type = resolveType(catalystActivity);
		addInteraction(reaction, type, catalyst, 1, input, unit.getChildren().get(input));
	}

	private InteractionType resolveType(CatalystActivity catalystActivity) {
		return InteractionType.fromGo("GO:" + catalystActivity.getActivity().getAccession());
	}

	private void addInteraction(DatabaseObject context, InteractionType type, PhysicalEntity a, long as, PhysicalEntity b, long bs) {
		if (a instanceof EntitySet) {
			final Unit unit = new Unit(a, includeSimpleEntity);
			unit.getChildren()
					.forEach((child, s) -> addInteraction(context, type, child, s * as, b, bs));
		} else if (b instanceof EntitySet) {
			final Unit unit = new Unit(b, includeSimpleEntity);
			unit.getChildren()
					.forEach((child, s) -> addInteraction(context, type, a, as, child, s * bs));
		} else if (a instanceof Complex) {
			final Unit unit = new Unit(a, includeSimpleEntity);
			if (unit.getChildren().isEmpty())
				writeInteraction(type, context, a, as, b, bs);
			else unit.getChildren()
					.forEach((child, s) -> addInteraction(context, type, child, s * as, b, bs));
		} else if (b instanceof Complex) {
			final Unit unit = new Unit(b, includeSimpleEntity);
			if (unit.getChildren().isEmpty())
				writeInteraction(type, context, a, as, b, bs);
			else unit.getChildren()
					.forEach((child, s) -> addInteraction(context, type, a, as, child, s * bs));
		} else writeInteraction(type, context, a, as, b, bs);
	}

	private void writeInteraction(InteractionType type, DatabaseObject context, PhysicalEntity A, long Ast, PhysicalEntity B, long Bst) {
		if (A instanceof SimpleEntity && B instanceof SimpleEntity) return;
		if (A.getStId().compareTo(B.getStId()) > 0)
			writeInteraction(type, context, B, Bst, A, Ast);
		else interactions.add(new Interaction(type, context, A, Ast, B, Bst));
	}

}
