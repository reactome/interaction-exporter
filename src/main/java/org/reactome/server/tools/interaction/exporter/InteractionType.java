package org.reactome.server.tools.interaction.exporter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class InteractionType {

	/** psi-mi:"MI:0915"(physical association) */
	public static final InteractionType PHYSICAL;
	private static final List<InteractionType> types = new LinkedList<>();
	private static final InteractionType DEFAULT_TYPE = new InteractionType("MI:0414", "enzymatic reaction");

	static {
		readTypes();
//		new GoTree().getTerms().forEach()
		PHYSICAL = fromPsiMi("MI:0915");

	}

	private static void readTypes() {
		final InputStream resource = InteractionType.class.getResourceAsStream("types.txt");
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource))) {
			reader.readLine();  // Skip header
			reader.lines()
					.map(line -> line.split("\t"))
					.map(line -> {
						final InteractionType type = new InteractionType(line[3], line[4]);
						type.getGoId().add(line[0]);
						return type;
					})
					.forEach(types::add);
		} catch (IOException e) {
			// Should never happen, it's a resource
			e.printStackTrace();
		}
	}


	private final Set<String> goId = new TreeSet<>();
	private final String psiId;
	private final String psiName;

	private InteractionType(String psiId, String psiName) {
		this.psiId = psiId;
		this.psiName = psiName;
	}

	public static InteractionType fromGo(String go) {
		return types.stream()
				.filter(type -> type.getGoId().contains(go))
				.findFirst()
				.orElse(DEFAULT_TYPE);
	}

	public static InteractionType fromPsiMi(String psiId) {
		return types.stream()
				.filter(type -> type.getPsiId().equals(psiId))
				.findFirst()
				.orElse(DEFAULT_TYPE);
	}

	public String getPsiId() {
		return psiId;
	}

	public String getPsiName() {
		return psiName;
	}

	public Set<String> getGoId() {
		return goId;
	}
}
