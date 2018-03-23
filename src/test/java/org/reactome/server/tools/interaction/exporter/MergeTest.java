package org.reactome.server.tools.interaction.exporter;

import org.junit.jupiter.api.Test;
import org.reactome.server.graph.domain.model.Complex;
import org.reactome.server.graph.domain.model.EntitySet;
import org.reactome.server.graph.domain.model.PhysicalEntity;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.graph.utils.ReactomeGraphCore;

import java.util.*;
import java.util.stream.Collectors;

public class MergeTest {


	@Test
	void test() {
		ReactomeGraphCore.initialise("localhost", "7474", "neo4j", "reactome", GraphCoreConfig.class);
		DatabaseObjectService OBJECT_SERVICE = ReactomeGraphCore.getService(DatabaseObjectService.class);
		String stId = "R-HSA-1013017";
		final PhysicalEntity object = OBJECT_SERVICE.findById(stId);
		units(object).forEach(System.out::println);
	}

	private List<Unit> units(PhysicalEntity entity) {
		if (entity instanceof Complex) {
			final Complex complex = (Complex) entity;
			if (complex.getHasComponent() != null) {
				List<List<Unit>> all = new ArrayList<>();
				for (PhysicalEntity physicalEntity : complex.getHasComponent()) {
					List<Unit> units = units(physicalEntity);
					all.add(units);
				}
				return merge(all);
			}
		} else if (entity instanceof EntitySet) {
			final EntitySet set = (EntitySet) entity;
			if (set.getHasMember() != null)
				return set.getHasMember().stream()
						.map(this::units)
						.flatMap(Collection::stream)
						.collect(Collectors.toList());
		} else {
			final Unit unit = new Unit();
			unit.add(entity);
			return Collections.singletonList(unit);
		}
		return Collections.singletonList(new Unit());
	}

	private List<Unit> merge(List<List<Unit>> all) {
		if (all.size() == 1) return all.get(0);
		final List<Unit> result = new LinkedList<>();
		final List<Unit> units = all.get(0);
		final ArrayList<List<Unit>> tail = new ArrayList<>(all);
		tail.remove(units);
		final List<Unit> merge = merge(tail);
		units.forEach(a -> {
			merge.forEach(b -> {
				final Unit unit = new Unit(a);
				b.components.forEach(unit::add);
				result.add(unit);
			});
		});
		return result;
	}

	private class Unit {
		final Map<PhysicalEntity, Integer> components = new LinkedHashMap<>();

		public Unit() {
		}

		public Unit(Unit a) {
			components.putAll(a.components);
		}

		public void add(PhysicalEntity entity, Integer stoichiometry) {
			components.put(entity, components.getOrDefault(entity, 0) + stoichiometry);
		}

		public void add(PhysicalEntity entity) {
			add(entity, 1);
		}

		@Override
		public String toString() {
			return components.keySet().stream()
					.map(entity -> String.format("%s(%d)", entity.getStId(), components.get(entity)))
					.collect(Collectors.joining(", "));
		}
	}

}
