package org.reactome.server.tools.interaction.exporter;

import org.reactome.server.graph.domain.model.DatabaseObject;
import org.reactome.server.graph.domain.model.PhysicalEntity;

public class Interaction {
	private final DatabaseObject context;
	private final PhysicalEntity a;
	private final Long ast;
	private final PhysicalEntity b;
	private final Long bst;
	private InteractionType type;

	public Interaction(InteractionType type, DatabaseObject context, PhysicalEntity a, Long ast, PhysicalEntity b, Long bst) {
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

	public Long getAst() {
		return ast;
	}

	public PhysicalEntity getB() {
		return b;
	}

	public Long getBst() {
		return bst;
	}

	public InteractionType getType() {
		return type;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Interaction)) return false;
		final Interaction that = (Interaction) obj;
		return that.getContext().equals(this.getContext())
				&& that.getType().equals(this.getType())
				&& that.getA().equals(this.getA())
				&& that.getAst().equals(this.getAst())
				&& that.getB().equals(this.getB())
				&& that.getBst().equals(this.getBst());
	}

	@Override
	public String toString() {
		return "["
				+ "type=" + type.getPsiName()
				+ ", context=" + context.getSchemaClass() + ":" + context.getStId()
				+ ", a=" + a.getSchemaClass() + ":" + a.getStId() + "(" + ast + ")"
				+ ", b=" + b.getSchemaClass() + ":" + b.getStId() + "(" + bst + ")"
				+ "]";
	}
}
