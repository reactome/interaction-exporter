package org.reactome.server.tools.interaction.exporter;

import org.reactome.server.tools.interaction.exporter.psi.SimpleCrossReference;
import org.reactome.server.tools.interaction.exporter.util.Constants;
import org.reactome.server.tools.interaction.exporter.util.GoTree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InteractionType extends SimpleCrossReference {

	/** psi-mi:"MI:0915"(physical association) */
	public static final InteractionType PHYSICAL;
	private static final List<InteractionType> types = new LinkedList<>();
	private static final InteractionType DEFAULT_TYPE = new InteractionType("MI:0414", "enzymatic reaction");

	static {
		readTypes();
		readGoTree();
		PHYSICAL = fromPsiMi("MI:0915");
	}

	private final Set<String> goId = new TreeSet<>();

	private InteractionType(String psiId, String psiName) {
		super(Constants.PSI_MI, psiId, psiName);
	}

	private static void readTypes() {
		final InputStream resource = InteractionType.class.getResourceAsStream("types.txt");
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource))) {
			reader.readLine();  // Skip header
			reader.lines()
					.map(line -> line.split("\t"))
					.map(line -> {
						final InteractionType type = getOrCreate(line[3], line[4]);
						type.getGoId().add(line[0]);
						return type;
					})
					.distinct()
					.forEach(types::add);
		} catch (IOException e) {
			// Should never happen, it's a resource
			e.printStackTrace();
		}
	}

	private static InteractionType getOrCreate(String id, String name) {
		return types.stream()
				.filter(type -> type.getIdentifier().equals(id))
				.findFirst()
				.orElse(new InteractionType(id, name));
	}

	private static void readGoTree() {
		final List<GoTree.Term> terms = GoTree.readGo();
		final Map<String, GoTree.Term> index = terms.stream()
				.collect(Collectors.toMap(GoTree.Term::getId, Function.identity()));
		terms.forEach(term -> {
			final String id = term.getId();
			InteractionType interactionType = DEFAULT_TYPE;
			while (interactionType == DEFAULT_TYPE && term != null) {
				interactionType = fromGo(term.getId());
				term = term.getParents().stream()
						.map(index::get)
						.filter(Objects::nonNull)
						.findFirst().orElse(null);
			}
			interactionType.getGoId().add(id);
		});

	}

	static InteractionType fromGo(String go) {
		return types.stream()
				.filter(type -> type.getGoId().contains(go))
				.findFirst()
				.orElse(DEFAULT_TYPE);
	}

	static InteractionType fromPsiMi(String psiId) {
		return types.stream()
				.filter(type -> type.getIdentifier().equals(psiId))
				.findFirst()
				.orElse(DEFAULT_TYPE);
	}

	private Set<String> getGoId() {
		return goId;
	}

}
