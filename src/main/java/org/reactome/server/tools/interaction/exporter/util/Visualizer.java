package org.reactome.server.tools.interaction.exporter.util;

import org.reactome.server.graph.domain.model.*;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.graph.utils.ReactomeGraphCore;

import java.io.PrintStream;
import java.util.Map;
import java.util.TreeMap;

/**
 * Allows you to visualise the hierarchy tree of any DatabaseObject.
 */
@SuppressWarnings("unused")
public class Visualizer {

	// FORMAT = prefix [stoichiometry]class:id[trivial]
	// prefix = +-oic
	// stoichiometry = s x
	// class = entity.schemaClass
	// id = entity.stId
	// trivial = (trivial)
	private static final String FORMAT = "%s %s%s:%s%s";

	static void printTree(String stId) {
		printTree(stId, System.out);
	}

	static void printTree(String stId, PrintStream printStream) {
		final DatabaseObjectService service = ReactomeGraphCore.getService(DatabaseObjectService.class);
		final DatabaseObject object = service.findById(stId);
		expand(printStream, object, 1, 0, "");
		printStream.println();
	}

	private static void expand(PrintStream printStream, DatabaseObject entity, int stoichiometry, int level, String prefix) {
		final Map<String, Map<PhysicalEntity, Integer>> children = participants(entity);
		for (int i = 0; i < level; i++) printStream.print("|    ");
		if (prefix.isEmpty()) prefix = children.isEmpty() ? "-" : "+";
		final String st = stoichiometry == 1 ? "" : stoichiometry + " x ";
		final String t = (entity instanceof SimpleEntity)
				&& ((SimpleEntity) entity).getReferenceEntity() != null
				&& ((SimpleEntity) entity).getReferenceEntity().getTrivial() != null
				&& ((SimpleEntity) entity).getReferenceEntity().getTrivial()
				? " (trivial)" : "";
		printStream.println(String.format(FORMAT, prefix, st, simpleSchemaClass(entity), entity.getStId(), t));
		children.forEach((rol, map) -> map.forEach((child, s) -> expand(printStream, child, s, level + 1, rol)));
	}

	private static String simpleSchemaClass(DatabaseObject entity) {
		if (entity.getSchemaClass().equals("EntityWithAccessionedSequence"))
			return "EWAS";
		return entity.getSchemaClass();
	}

	private static Map<String, Map<PhysicalEntity, Integer>> participants(DatabaseObject object) {
		final Map<String, Map<PhysicalEntity, Integer>> participants = new TreeMap<>();
		if (object instanceof Complex) {
			final Complex complex = (Complex) object;
			if (complex.getHasComponent() != null) {
				final Map<PhysicalEntity, Integer> map = participants.computeIfAbsent("", k -> new TreeMap<>());
				complex.getHasComponent().forEach(entity -> map.put(entity, map.getOrDefault(entity, 0) + 1));
			}
		} else if (object instanceof EntitySet) {
			final EntitySet set = (EntitySet) object;
			if (set.getHasMember() != null) {
				final Map<PhysicalEntity, Integer> map = participants.computeIfAbsent("o", k -> new TreeMap<>());
				set.getHasMember().forEach(entity -> map.put(entity, map.getOrDefault(entity, 0) + 1));
			}
		} else if (object instanceof Polymer) {
			final Polymer polymer = (Polymer) object;
			if (polymer.getRepeatedUnit() != null) {
				final Map<PhysicalEntity, Integer> map = participants.computeIfAbsent("-", k -> new TreeMap<>());
				polymer.getRepeatedUnit().forEach(entity -> map.put(entity, map.getOrDefault(entity, 0) + 1));
			}
		} else if (object instanceof ReactionLikeEvent) {
			final ReactionLikeEvent reaction = (ReactionLikeEvent) object;
			if (reaction.getInput() != null) {
				final Map<PhysicalEntity, Integer> map = participants.computeIfAbsent("i", k -> new TreeMap<>());
				reaction.getInput().forEach(entity -> map.put(entity, map.getOrDefault(entity, 0) + 1));
			}
			if (reaction.getCatalystActivity() != null) {
				final Map<PhysicalEntity, Integer> map = participants.computeIfAbsent("c", k -> new TreeMap<>());
				for (CatalystActivity activity : reaction.getCatalystActivity()) {
					final Map<PhysicalEntity, Integer> active = participants.computeIfAbsent("a", k -> new TreeMap<>());
					if (activity.getActiveUnit() != null)
						activity.getActiveUnit().forEach(activeUnit -> active.put(activeUnit, map.getOrDefault(activeUnit, 0) + 1));
					map.put(activity.getPhysicalEntity(), map.getOrDefault(activity.getPhysicalEntity(), 0) + 1);
				}
			}
		}

		return participants;
	}


}
