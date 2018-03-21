package org.reactome.server.tools.interaction.exporter;

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


class CheckUniquenessTest {


	@Test
	void testUniqueness() {

		final Set<String> hashes = new HashSet<>();
		try (BufferedReader reader = new BufferedReader(new FileReader("output.txt"))) {
			long[] line = new long[]{0};
			reader.lines().forEach(s -> {
				final String[] row = s.split("\t");
				final String a = row[2].split(":")[1];
				final String b = row[3].split(":")[1];
				final String hashCode = row[1].split(":")[1] + getCode(a, b);
				if (hashes.contains(hashCode))
					Assertions.fail(line[0] + " " + s);
				hashes.add(hashCode);
				line[0]++;
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String getCode(String a, String b) {
		return a.compareTo(b) > 0
				? a + b
				: b + a;
	}

	@Ignore
	void testHisUniqueness() {
		final File standard = new File("/home/pascual/proyectos/reactome/psicquic/stids.txt");
		final Set<String> tripleHashes = new HashSet<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(standard))) {
			long[] line = new long[]{0};
			reader.lines().forEach(s -> {
				final String[] row = s.split("\t");
				final String a = row[2];
				final String b = row[4];
				final String doubleHashCode = getCode(a, b);
				final String tripleHashCode = row[0] + doubleHashCode;
				if (tripleHashes.contains(tripleHashCode))
					Assertions.fail(line[0] + " " + s);
				tripleHashes.add(tripleHashCode);
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
		final Set<String> tripleHashCodes = new TreeSet<>();
		final Set<String> doubleHashCodes = new TreeSet<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(standard))) {
			reader.lines()
					.map(line -> line.split("\t"))
					.forEach(line -> {
						final String doubleHashCode = getCode(line[2], line[4]);
						final String tripleHashCode = line[0] + doubleHashCode;
						doubleHashCodes.add(doubleHashCode);
						tripleHashCodes.add(tripleHashCode);
					});

		} catch (IOException e) {
			e.printStackTrace();
		}

		final Set<String> pDoubleHashCodes = new TreeSet<>();
		final Set<String> pTripleHashCodes = new TreeSet<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(result))) {
			reader.lines()
					.forEach(line -> {
						final String[] split = line.split("\t");
						final String stId = split[1].split(":")[1];
						final String aStId = split[2].split(":")[1];
						final String bStId = split[3].split(":")[1];
						final String doubleHashCode = getCode(aStId, bStId);
						final String tripleHashCode = stId + doubleHashCode;
						pDoubleHashCodes.add(doubleHashCode);
						pTripleHashCodes.add(tripleHashCode);
					});
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Uniques:");
		venn(doubleHashCodes, pDoubleHashCodes);
		System.out.println("With context:");
		venn(tripleHashCodes, pTripleHashCodes);
		customVenn(doubleHashCodes, pDoubleHashCodes, tripleHashCodes, pTripleHashCodes);
	}

	private void customVenn(Set<String> aDouble, Set<String> bDouble, Set<String> aContext, Set<String> bContext) {
		final Set<String> common = new TreeSet<>(aDouble);
		common.retainAll(bDouble);
		final Set<String> justA = new TreeSet<>(aDouble);
		justA.removeAll(bDouble);
		final Set<String> justB = new TreeSet<>(bDouble);
		justB.removeAll(aDouble);
		System.out.println("A:");
		randomWithContext(justA, aContext);
		System.out.println("B:");
		randomWithContext(justB, bContext);
	}

	private void randomWithContext(Set<String> interactions, Set<String> context) {
		final ArrayList<String> list = new ArrayList<>(interactions);
		final Random random = new Random(0);
		for (int i = 0; i < 15; i++) {
			final String interaction = list.get(random.nextInt(list.size()));
			context.stream()
					.filter(s -> s.endsWith(interaction))
					.forEach(s -> System.out.println(s.replaceAll("(\\d+)R", "$1 R")));
		}
	}

	private void venn(Set<String> a, Set<String> b) {
		final Set<String> common = new TreeSet<>(a);
		common.retainAll(b);
		final Set<String> justA = new TreeSet<>(a);
		justA.removeAll(b);
		final Set<String> justB = new TreeSet<>(b);
		justB.removeAll(a);
		System.out.println(" - common = " + common.size());
		System.out.println(" -      a = " + justA.size());
		System.out.println(" -      b = " + justB.size());
	}

	private void printRandom(Set<String> justA) {
		final ArrayList<String> list = new ArrayList<>(justA);
		final Random random = new Random(0);
		for (int i = 0; i < 15; i++) {
			final String interaction = list.get(random.nextInt(list.size()));
			System.out.println(interaction.replaceAll("(\\d+)R", "$1 R"));
		}
	}

}
