package org.reactome.server.tools.interaction.exporter;

import org.reactome.server.graph.domain.model.DatabaseObject;
import org.reactome.server.graph.domain.model.PhysicalEntity;

public class Interaction {
	private final DatabaseObject context;
	private final PhysicalEntity a;
	private final Integer ast;
	private final PhysicalEntity b;
	private final Integer bst;
	private String type;

	public Interaction(String type, DatabaseObject context, PhysicalEntity a, Integer ast, PhysicalEntity b, Integer bst) {
		this.type = type;
		this.context = context;
		this.a = a;
		this.ast = ast;
		this.b = b;
		this.bst = bst;
	}

	public DatabaseObject getContext() {
		return context;
	}

	public PhysicalEntity getA() {
		return a;
	}

	public Integer getAst() {
		return ast;
	}

	public PhysicalEntity getB() {
		return b;
	}

	public Integer getBst() {
		return bst;
	}

	public String getType() {
		return type;
	}
}
