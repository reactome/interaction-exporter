package org.reactome.server.tools.interaction.exporter;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reactome.server.graph.domain.model.*;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.graph.utils.ReactomeGraphCore;

import java.util.*;

class ExporterTest {

	private static final String format = "%s %s:%s(%d)";

	@BeforeAll
	static void beforeAll() {
		ReactomeGraphCore.initialise("localhost", "7474", "neo4j", "reactome", GraphCoreConfig.class);
	}

	@Test
	void printTree() {
		ReactomeGraphCore.initialise("localhost", "7474", "neo4j", "reactome", GraphCoreConfig.class);
		DatabaseObjectService OBJECT_SERVICE = ReactomeGraphCore.getService(DatabaseObjectService.class);

		String stId = "R-HSA-5672710";
		final DatabaseObject object = OBJECT_SERVICE.findById(stId);
		expand(object, 1, 0, null);
		System.out.println();
		InteractionExporter.export(exporter -> exporter
				.setOutput(System.out)
				.setFormat(Format.TSV)
				.setObject(stId));
	}

	private void expand(DatabaseObject entity, int stoichiometry, int level, String prefix) {
		final Map<PhysicalEntity, Integer> childs = participants(collect(entity));
		for (int i = 0; i < level; i++) System.out.print("|    ");
		if (prefix == null || !childs.isEmpty())
			prefix = childs.isEmpty() ? "-" : "+";
		System.out.println(String.format(format, prefix, simpleSchemaClass(entity), entity.getStId(), stoichiometry));
		final String nextPrefix = entity instanceof EntitySet ? "o" : null;
		childs.forEach((child, s) -> expand(child, s, level + 1, nextPrefix));
	}

	private String simpleSchemaClass(DatabaseObject entity) {
		if (entity.getSchemaClass().equals("EntityWithAccessionedSequence"))
			return "EWAS";
		return entity.getSchemaClass();
	}

	private Map<PhysicalEntity, Integer> participants(Collection<PhysicalEntity> entities) {
		final Map<PhysicalEntity, Integer> participants = new TreeMap<>();
		entities.forEach(physicalEntity -> participants.put(physicalEntity, participants.getOrDefault(physicalEntity, 0) + 1));
		return participants;
	}


	private Collection<PhysicalEntity> collect(DatabaseObject object) {
		if (object instanceof Complex) {
			final Complex complex = (Complex) object;
			if (complex.getHasComponent() != null)
				return complex.getHasComponent();
		} else if (object instanceof EntitySet) {
			final EntitySet set = (EntitySet) object;
			if (set.getHasMember() != null)
				return set.getHasMember();
		} else if (object instanceof Polymer) {
			final Polymer polymer = (Polymer) object;
			if (polymer.getRepeatedUnit() != null)
				return polymer.getRepeatedUnit();
		} else if (object instanceof ReactionLikeEvent) {
			final ReactionLikeEvent reaction = (ReactionLikeEvent) object;
			final List<PhysicalEntity> objects = new LinkedList<>();
			if (reaction.getInput() != null)
				objects.addAll(reaction.getInput());
			if (reaction.getCatalystActivity() != null)
				for (CatalystActivity activity : reaction.getCatalystActivity()) {
					objects.add(activity.getPhysicalEntity());
					if (activity.getActiveUnit() != null)
						objects.addAll(activity.getActiveUnit());
				}

			return objects;
		}
		return Collections.emptyList();
	}
}
