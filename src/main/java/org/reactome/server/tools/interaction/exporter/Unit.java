package org.reactome.server.tools.interaction.exporter;

import org.reactome.server.graph.domain.model.*;
import org.reactome.server.tools.interaction.exporter.filter.SimpleEntityPolicy;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

class Unit {

	private final Map<PhysicalEntity, Long> children;

	Unit(DatabaseObject object, SimpleEntityPolicy simpleEntityPolicy) {
		children = children(object).stream()
				.filter(simpleEntityPolicy.getFilter())
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
	}

	/**
	 * Returns a map, where each entry is a unique PhysicalEntity with the
	 * repetitions of it in the list of children.
	 */
	Map<PhysicalEntity, Long> getChildren() {
		return children;
	}

	private Collection<PhysicalEntity> children(DatabaseObject object) {
		List<PhysicalEntity> children = null;
		if (object instanceof Complex)
			children = ((Complex) object).getHasComponent();
		else if (object instanceof EntitySet)
			children = ((EntitySet) object).getHasMember();
		else if (object instanceof ReactionLikeEvent)
			children = ((ReactionLikeEvent) object).getInput();
		else if (object instanceof Polymer)
			children = ((Polymer) object).getRepeatedUnit();
		return children == null
				? Collections.emptyList()
				: children;
	}

}
