package org.reactome.server.tools.interaction.exporter;

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;


class CheckUniquenessTest {


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

	@Ignore
	void testHisUniqueness() {
		final File standard = new File("/home/pascual/proyectos/reactome/psicquic/stids.txt");
		final Set<String> hashes = new HashSet<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(standard))) {
			long[] line = new long[]{0};
			reader.lines().forEach(s -> {
				final String[] row = s.split("\t");
				String hashCode = row[0];
				final String a = row[2];
				final String b = row[4];
				if (a.compareTo(b) > 0) hashCode += a + b;
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
							if (s.incrementAndGet() < 15)
								System.out.println(code.replaceAll("(\\d)R", "$1 R"));
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
//				.filter(line -> line.matches("R-HSA-\\d*R-(HSA|ALL)-\\d*R-(HSA|ALL)-\\d*"))
				.map(line -> line.replaceAll("(\\d)R", "$1 R"))
				.forEach(System.out::println);
	}

}
