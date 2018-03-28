package org.reactome.server.tools.interaction.exporter;

import org.reactome.server.graph.domain.model.DatabaseObject;

public class Interaction {
	private final DatabaseObject context;
	private InteractionType type;
	private Interactor a;
	private Interactor b;

	public Interaction(InteractionType type, DatabaseObject context, Interactor a, Interactor b) {
		this.type = type;
		this.context = context;
		this.a = a;
		this.b = b;
	}

	public DatabaseObject getContext() {
		return context;
	}

	public Interactor getA() {
		return a;
	}

	public Interactor getB() {
		return b;
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
				&& that.getB().equals(this.getB());
	}

	@Override
	public String toString() {
		return "["
				+ "type=" + type
				+ ", context=" + context.getSchemaClass() + ":" + context.getStId()
				+ ", a=" + a
				+ ", b=" + b
				+ "]";
	}
}
