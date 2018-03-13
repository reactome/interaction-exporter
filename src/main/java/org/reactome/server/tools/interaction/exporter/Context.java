package org.reactome.server.tools.interaction.exporter;

import org.reactome.server.graph.domain.model.DatabaseObject;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Context {
	private DatabaseObject databaseObject;
	private List<DatabaseObject> ancestors = new LinkedList<>();

	public Context(DatabaseObject databaseObject) {
		this.databaseObject = databaseObject;
	}

	public DatabaseObject getDatabaseObject() {
		return databaseObject;
	}

	public List<DatabaseObject> getAncestors() {
		return ancestors;
	}

	@Override
	public String toString() {
		return "[" + ancestors.stream().map(DatabaseObject::getStId).collect(Collectors.joining(", ")) + "] -> " + databaseObject.getStId();
	}
}
