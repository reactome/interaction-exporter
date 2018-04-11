package org.reactome.server.tools.interaction.exporter.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads the GO file, and create the hierarchy of GoTerms only following the
 * is_a field.
 */
public class GoTree {

	private final static Pattern IS_A_PATTERN = Pattern.compile("is_a:\\s+(GO:\\d+)");
	private final static Pattern ID_PATTERN = Pattern.compile("id:\\s+(GO:\\d+)");

	public static List<Term> readGo() {
		final InputStream resource = GoTree.class.getResourceAsStream("go.obo");
		final List<Term> terms = new LinkedList<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource))) {
			final AtomicReference<String> currentTerm = new AtomicReference<>();
			final Set<String> parents = new TreeSet<>();
			reader.lines().forEach(line -> {
				if (line.equals("[Term]")) {
					if (currentTerm.get() != null)
						terms.add(new Term(currentTerm.get(), parents));
					parents.clear();
				} else if (line.startsWith("id:")) {
					final Matcher matcher = ID_PATTERN.matcher(line);
					if (matcher.find())
						currentTerm.set(matcher.group(1));
				} else if (line.startsWith("is_a:")) {
					final Matcher matcher = IS_A_PATTERN.matcher(line);
					if (matcher.find())
						parents.add(matcher.group(1));
				}
			});
			if (currentTerm.get() != null)
				terms.add(new Term(currentTerm.get(), parents));
		} catch (IOException e) {
			// Should never happen, it's a resource
			e.printStackTrace();
		}
		return terms;
	}

	public static class Term {
		String id;
		Set<String> parents;

		Term(String id, Set<String> parents) {
			this.id = id;
			this.parents = new TreeSet<>(parents);
		}

		public Set<String> getParents() {
			return parents;
		}

		public String getId() {
			return id;
		}
	}
}
