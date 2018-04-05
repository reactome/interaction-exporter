package org.reactome.server.tools.interaction.exporter;

import org.reactome.server.graph.domain.model.*;
import org.reactome.server.tools.interaction.exporter.filter.SimpleEntityPolicy;
import org.reactome.server.tools.interaction.exporter.util.Constants;
import psidev.psi.mi.tab.model.CrossReference;

import java.util.*;

/**
 * Collects interaction from contexts.
 */
class InteractionExplorer {

	private final SimpleEntityPolicy simpleEntityPolicy;
	private final int maxUnitSize;
	/**
	 * Make sure only one interaction is exporter between two elements. This set
	 * is necessary because a physical entity can be several times in a context
	 * in different parts of the tree.
	 */
	private Collection<Interaction> interactions;

	/**
	 * Configures a collector with a specific behaviour. The collector can be
	 * reused.
	 */
	InteractionExplorer(SimpleEntityPolicy simpleEntityPolicy, int maxUnitSize) {
		this.simpleEntityPolicy = simpleEntityPolicy;
		this.maxUnitSize = maxUnitSize;
	}

	/**
	 * Collects all the interactions under this object, using the object as
	 * context. Only those interactions where object is the context are
	 * collected.
	 *
	 * @see InteractionExporter
	 */
	Collection<Interaction> explore(DatabaseObject object) {
		interactions = new HashSet<>();
		if (object instanceof Polymer)
			explorePolymer((Polymer) object);
		else if (object instanceof Complex)
			exploreComplex((Complex) object);
		else if (object instanceof ReactionLikeEvent)
			exploreReaction((ReactionLikeEvent) object);
		return interactions;
	}

	private void explorePolymer(Polymer polymer) {
		final Unit unit = new Unit(polymer, simpleEntityPolicy);
		if (unit.getChildren().isEmpty() || unit.getChildren().size() > maxUnitSize)
			return;
		unit.getChildren().forEach((entity, stoichiometry) ->
				addInteraction(polymer, InteractionType.PHYSICAL, entity, 0, entity, 0));
	}

	private void exploreComplex(Complex complex) {
		final Unit unit = new Unit(complex, simpleEntityPolicy);
		if (unit.getChildren().isEmpty() || unit.getChildren().size() > maxUnitSize)
			return;
		matrixExpansion(complex, unit);
		olygomers(complex, unit);
	}

	private void exploreReaction(ReactionLikeEvent reaction) {
		if (reaction instanceof BlackBoxEvent) return;
		final Unit unit = new Unit(reaction, simpleEntityPolicy);
		if (unit.getChildren().isEmpty() || unit.getChildren().size() > maxUnitSize)
			return;
		matrixExpansion(reaction, unit);
		olygomers(reaction, unit);
		exploreCatalystInteractions(reaction, unit);
	}

	private void matrixExpansion(DatabaseObject context, Unit unit) {
		final ArrayList<PhysicalEntity> components = new ArrayList<>(unit.getChildren().keySet());
		for (int i = 0; i < components.size(); i++)
			for (int j = i + 1; j < components.size(); j++)
				addInteraction(context, InteractionType.PHYSICAL,
						components.get(i), unit.getChildren().get(components.get(i)),
						components.get(j), unit.getChildren().get(components.get(j)));

	}

	private void olygomers(DatabaseObject context, Unit unit) {
		unit.getChildren().forEach((entity, stoichiometry) -> {
			if (stoichiometry > 1)
				addInteraction(context, InteractionType.PHYSICAL, entity, stoichiometry, entity, 0);
		});
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
		 * Macro | small | Other
		 * ------|-------|--------
		 *   1   |  any  |   0
		 *   0   |   1   |   0
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

		// Usually, input and catalyst are the same molecule.
		// In this case, interactions are not exported.
		if (catalystActivity.getPhysicalEntity().equals(input) || catalyst.equals(input))
			return;

		final InteractionType type = resolveType(catalystActivity);
		addInteraction(reaction, type, catalyst, 1, input, unit.getChildren().get(input));
	}

	private InteractionType resolveType(CatalystActivity catalystActivity) {
		return InteractionType.fromGo("GO:" + catalystActivity.getActivity().getAccession());
	}

	private void addInteraction(DatabaseObject context, InteractionType type, PhysicalEntity a, long as, PhysicalEntity b, long bs) {
		if (a instanceof EntitySet) {
			final Unit unit = new Unit(a, simpleEntityPolicy);
			if (unit.getChildren().isEmpty() || unit.getChildren().size() > maxUnitSize)
				return;
			unit.getChildren()
					.forEach((child, s) -> addInteraction(context, type, child, s * as, b, bs));
		} else if (b instanceof EntitySet) {
			final Unit unit = new Unit(b, simpleEntityPolicy);
			if (unit.getChildren().isEmpty() || unit.getChildren().size() > maxUnitSize)
				return;
			unit.getChildren()
					.forEach((child, s) -> addInteraction(context, type, a, as, child, s * bs));
		} else if (a instanceof Complex) {
			final Unit unit = new Unit(a, simpleEntityPolicy);
			if (unit.getChildren().size() > maxUnitSize) return;
			unit.getChildren()
					.forEach((child, s) -> addInteraction(context, type, child, s * as, b, bs));
		} else if (b instanceof Complex) {
			final Unit unit = new Unit(b, simpleEntityPolicy);
			if (unit.getChildren().size() > maxUnitSize) return;
			unit.getChildren()
					.forEach((child, s) -> addInteraction(context, type, a, as, child, s * bs));
		} else writeInteraction(type, context, a, as, b, bs);
	}

	private void writeInteraction(InteractionType type, DatabaseObject context, PhysicalEntity A, long Ast, PhysicalEntity B, long Bst) {
		// Skip interaction of simple entities
		if (A instanceof SimpleEntity && B instanceof SimpleEntity) return;
		final CrossReference aRole = type == InteractionType.PHYSICAL
				? Constants.UNSPECIFIED_ROLE : Constants.ENZYME;
		final CrossReference bRole = type == InteractionType.PHYSICAL
				? Constants.UNSPECIFIED_ROLE : Constants.ENZYME_TARGET;
		collect(new Interaction(type, context, new Interactor(A, Ast, aRole), new Interactor(B, Bst, bRole)));
	}

	private void collect(Interaction interaction) {
		// Is there a similar interaction with different stoichiometry?
		final Interaction other = interactions.stream()
				.filter(interaction::equalsIgnoreStoichiometry)
				.findFirst().orElse(null);
		if (other == null) interactions.add(interaction);
		else {
			interactions.remove(other);
			interactions.add(bestStoichiometry(interaction, other));
		}
	}

	/**
	 * Get the element with best stoichiometry. Best stoichiometry is defined as
	 * the lowest of the sums of the two interactor's stoichiometry, i.e:
	 *
	 * <pre>
	 * min(interactionA.a.stoichiometry + interactionA.b.stoichiometry,
	 *     interactionB.a.stoichiometry + interactionB.b.stoichiometry)
	 * </pre>.
	 */
	private Interaction bestStoichiometry(Interaction interactionA, Interaction interactionB) {
		final long a = interactionA.getA().getStoichiometry() + interactionA.getB().getStoichiometry();
		final long b = interactionB.getA().getStoichiometry() + interactionB.getB().getStoichiometry();
		return a < b ? interactionA : interactionB;
	}

}
