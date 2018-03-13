package org.reactome.server.tools.interaction.exporter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.reactome.server.graph.domain.model.*;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.graph.utils.ReactomeGraphCore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


class CheckUniquenessTest {

	private static final String format = "%s %s:%s(%d)";

	@Test
	void testUniqueness() {
		final Set<String> hashes = new HashSet<>();
		try (BufferedReader reader = new BufferedReader(new FileReader("output.txt"))) {
			long[] line = new long[]{0};
			reader.lines().forEach(s -> {
				final String[] row = s.split("\t");
				String hashCode = row[0].split(":")[1];
				final String a = row[1].split(":")[1];
				final String b = row[2].split(":")[1];
				if (a.compareTo(b) < 0) hashCode += a + b;
				else hashCode += b + a;
				if (hashes.contains(hashCode))
					Assertions.fail(line[0] + " " + s);
				hashes.add(hashCode);
				line[0]++;
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	void testEquality() {
		final File standard = new File("/home/pascual/proyectos/reactome/psicquic/stids.txt");
		final File result = new File("output.txt");
		final Set<String> standardLines = new TreeSet<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(standard))) {
			reader.lines()
					.map(line -> line.split("\t"))
					.map(line -> {
						String code = line[0];
						if (line[2].compareTo(line[4]) > 0)
							code += line[2] + line[4];
						else code += line[4] + line[2];
						return code;
					})
					.forEach(standardLines::add);
		} catch (IOException e) {
			e.printStackTrace();
		}

		final int standardl = standardLines.size();
		final AtomicInteger matches = new AtomicInteger();
		final AtomicInteger total = new AtomicInteger();
		final AtomicInteger s = new AtomicInteger();

		try (BufferedReader reader = new BufferedReader(new FileReader(result))) {
			reader.lines()
					.forEach(line -> {
						final String[] split = line.split("\t");
						final String stId = split[0].split(":")[1];
						final String aStId = split[1].split(":")[1];
						final String bStId = split[2].split(":")[1];
						String code = stId;
						if (aStId.compareTo(bStId) > 0)
							code += aStId + bStId;
						else code += bStId + aStId;
						if (standardLines.contains(code)) {
							matches.incrementAndGet();
							standardLines.remove(code);
						} else {
							if (s.incrementAndGet() < 5)
								System.out.println(code);
						}
						total.getAndIncrement();
					});
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Standard = " + standardl);
		System.out.println("Result   = " + total.get());
		System.out.println("matches  = " + matches.get());
		System.out.println("missing  = " + standardLines.size());
		System.out.println("new      = " + (total.get() - matches.get()));
		standardLines.stream()
				.filter(line -> line.matches("R-HSA-\\d*R-(HSA|ALL)-\\d*R-(HSA|ALL)-\\d*"))
				.map(line -> line.replaceAll("(\\d)R", "$1 R"))
				.forEach(System.out::println);
	}

	@Test
	void printTree() {
		ReactomeGraphCore.initialise("localhost", "7474", "neo4j", "reactome", GraphCoreConfig.class);
		DatabaseObjectService OBJECT_SERVICE = ReactomeGraphCore.getService(DatabaseObjectService.class);

		String stId = "R-HSA-1299475";
		final DatabaseObject object = OBJECT_SERVICE.findById(stId);
		expand(object, 1, 0, null);
		System.out.println();
		InteractionExporter.export(interactionExporter -> interactionExporter
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
