package org.reactome.server.tools.interaction.exporter;

import java.util.LinkedHashMap;
import java.util.Map;

public class EntityIdentifier {
	private final String databaseName;
	private final String identifier;
	private static final Map<String, String> databases = new LinkedHashMap<>();

	static {

	}

	public EntityIdentifier(String databaseName, String identifier) {
		this.databaseName = standardDatabaseName(databaseName);
		this.identifier = identifier;
	}

	private String standardDatabaseName(String databaseName) {
		return databases.get(databaseName);
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public String getIdentifier() {
		return identifier;
	}

	@Override
	public String toString() {
		return (databaseName == null ? "" : databaseName + ":") + identifier;
	}
}
