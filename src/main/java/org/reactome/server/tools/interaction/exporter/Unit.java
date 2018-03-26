package org.reactome.server.tools.interaction.exporter;

import com.sun.istack.internal.NotNull;
import org.reactome.server.graph.domain.model.*;
import org.reactome.server.tools.interaction.exporter.filter.IncludeSimpleEntity;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Unit {

	private final Map<PhysicalEntity, Long> children;
	private final long unitSize;
	private final DatabaseObject object;

	public Unit(DatabaseObject object, IncludeSimpleEntity includeSimpleEntity) {
		this.object = object;
		children = children(object, includeSimpleEntity).stream()
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
		unitSize = unitSize(object);
	}

	/**
	 * Returns a map, where each entry is a unique PhysicalEntity with the
	 * repetitions of it in the list of children.
	 */
	public Map<PhysicalEntity, Long> getChildren() {
		return children;
	}

	public long getUnitSize() {
		return unitSize;
	}

	private long unitSize(DatabaseObject object) {
		return combinations(object).stream()
				.mapToLong(value -> value.getComponents().size())
				.min().orElse(0);
	}

//	/**
//	 * If this Unit is an entity set, or it contains one or more entity sets,
//	 * the element represent more than one combination of physical entities.
//	 * Here is the list of all combinations, sorted by length, and crop to max
//	 * set size.
//	 */
//	public List<Combination> getCombinations() {
//		return combinations;
//	}

	@NotNull
	private Collection<PhysicalEntity> children(DatabaseObject object, IncludeSimpleEntity includeSimpleEntity) {
		List<PhysicalEntity> result = null;
		if (object instanceof Complex)
			result = ((Complex) object).getHasComponent();
		else if (object instanceof EntitySet)
			result = ((EntitySet) object).getHasMember();
		else if (object instanceof ReactionLikeEvent)
			result = ((ReactionLikeEvent) object).getInput();
		return result == null ? Collections.emptyList()
				: result.stream()
				.filter(includeSimpleEntity.getFilter())
				.collect(Collectors.toList());
	}

	private List<Combination> combinations(DatabaseObject entity) {
		if (entity instanceof Complex) {
			final Complex complex = (Complex) entity;
			if (complex.getHasComponent() != null) {
				List<List<Combination>> all = new ArrayList<>();
				for (PhysicalEntity physicalEntity : complex.getHasComponent()) {
					List<Combination> combinations = combinations(physicalEntity);
					all.add(combinations);
				}
				return merge(all);
			}
		} else if (entity instanceof EntitySet) {
			final EntitySet set = (EntitySet) entity;
			if (set.getHasMember() != null)
				return set.getHasMember().stream()
						.map(this::combinations)
						.flatMap(Collection::stream)
						.collect(Collectors.toList());
		} else if (entity instanceof Polymer) {
			final Polymer polymer = (Polymer) entity;
			if (polymer.getRepeatedUnit() != null && polymer.getRepeatedUnit().size() == 1) {
				final List<Combination> combinations = combinations(polymer.getRepeatedUnit().get(0));
				return merge(Arrays.asList(combinations, combinations));
			}
		} else if (entity instanceof ReactionLikeEvent) {
			final ReactionLikeEvent reaction = (ReactionLikeEvent) entity;
			if (reaction.getInput() != null) {
				List<List<Combination>> all = new ArrayList<>();
				for (PhysicalEntity physicalEntity : reaction.getInput()) {
					List<Combination> combinations = combinations(physicalEntity);
					all.add(combinations);
				}
				return merge(all);
			}
		} else if (entity instanceof PhysicalEntity) {
			final Combination combination = new Combination();
			combination.add((PhysicalEntity) entity);
			return Collections.singletonList(combination);
		}
		return Collections.singletonList(new Combination());
	}

	private List<Combination> merge(List<List<Combination>> all) {
		if (all.size() == 1) return all.get(0);
		final List<Combination> result = new LinkedList<>();
		final List<Combination> combinations = all.get(0);
		final ArrayList<List<Combination>> tail = new ArrayList<>(all);
		tail.remove(combinations);
		final List<Combination> merge = merge(tail);
		combinations.forEach(a ->
				merge.forEach(b -> {
					final Combination combination = new Combination(a);
					b.components.forEach(combination::add);
					result.add(combination);
				}));
		return result;
	}


	public class Combination {
		private final Map<PhysicalEntity, Integer> components = new LinkedHashMap<>();

		Combination() {
		}

		Combination(Combination a) {
			components.putAll(a.components);
		}

		public Map<PhysicalEntity, Integer> getComponents() {
			return components;
		}

		void add(PhysicalEntity entity, Integer stoichiometry) {
			components.put(entity, components.getOrDefault(entity, 0) + stoichiometry);
		}

		void add(PhysicalEntity entity) {
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
