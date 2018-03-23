package org.reactome.server.tools.interaction.exporter;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reactome.server.graph.domain.model.*;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.graph.utils.ReactomeGraphCore;
import org.reactome.server.tools.interaction.exporter.writer.ConsoleWriter;
import org.reactome.server.tools.interaction.exporter.writer.InteractionWriter;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

class ExporterTest {

	private static final String format = "%s %s%s:%s";

	@BeforeAll
	static void beforeAll() {
		ReactomeGraphCore.initialise("localhost", "7474", "neo4j", "reactome", GraphCoreConfig.class);
	}

	/**
	 * Utility to see the tree of a Complex/Reaction/Polymer in the System.out.
	 */
	@Test
	void printTree() {
		ReactomeGraphCore.initialise("localhost", "7474", "neo4j", "reactome", GraphCoreConfig.class);
		DatabaseObjectService OBJECT_SERVICE = ReactomeGraphCore.getService(DatabaseObjectService.class);
		String stId = "R-HSA-1911487";
		final DatabaseObject object = OBJECT_SERVICE.findById(stId);
		expand(object, 1, 0, "");
		System.out.println();
		InteractionWriter writer = new ConsoleWriter(System.out);
		InteractionExporter.stream(exporter -> exporter
				.setMaxSetSize(4)
				.setObject(stId))
				.forEach(interaction -> {
					try {
						writer.write(interaction);
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
	}

	private void expand(DatabaseObject entity, int stoichiometry, int level, String prefix) {
		final Map<String, Map<PhysicalEntity, Integer>> children = participants(entity);
		for (int i = 0; i < level; i++) System.out.print("|    ");
		if (prefix.isEmpty()) prefix = children.isEmpty() ? "-" : "+";
		String st = stoichiometry == 1 ? "" : stoichiometry + " x ";
		System.out.println(String.format(format, prefix, st, simpleSchemaClass(entity), entity.getStId()));
		children.forEach((rol, map) -> map.forEach((child, s) -> expand(child, s, level + 1, rol)));
	}

	private String simpleSchemaClass(DatabaseObject entity) {
		if (entity.getSchemaClass().equals("EntityWithAccessionedSequence"))
			return "EWAS";
		return entity.getSchemaClass();
	}

	private Map<String, Map<PhysicalEntity, Integer>> participants(DatabaseObject object) {
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
