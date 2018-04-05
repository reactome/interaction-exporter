package org.reactome.server.tools.interaction.exporter;

import org.reactome.server.graph.domain.model.DatabaseObject;

import java.util.Objects;

/**
 * Data class with the basic information of an interaction: context, type and
 * interactors.
 * <p>
 * Interactions are not oriented. This means that <code>new Interaction(type,
 * context, a, b) == new Interaction(type, context, b, a)</code>. Only equality
 * and hashing are implemented, so it is only warranted to work with hash sets
 * or hash maps, but not with tree sets and maps, as Interaction is not
 * comparable, neither sortable.
 */
public class Interaction {
	private final DatabaseObject context;
	private final InteractionType type;
	private final Interactor a;
	private final Interactor b;

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
		return this.getContext().equals(that.getContext())
				&& this.getType().equals(that.getType())
				&& (this.getA().equals(that.getA()) && this.getB().equals(that.getB())
				|| this.getA().equals(that.getB()) && this.getB().equals(that.getA()));
	}

	public boolean equalsIgnoreStoichiometry(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Interaction)) return false;
		final Interaction that = (Interaction) obj;
		return this.getContext().equals(that.getContext())
				&& this.getType().equals(that.getType())
				&& (this.getA().equalsIgnoreStoichiometry(that.getA()) && this.getB().equalsIgnoreStoichiometry(that.getB())
				|| this.getA().equalsIgnoreStoichiometry(that.getB()) && this.getB().equalsIgnoreStoichiometry(that.getA()));
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

	@Override
	public int hashCode() {
		return a.getEntity().getStId().compareTo(b.getEntity().getStId()) < 0
				? Objects.hash(context, type, a, b)
				: Objects.hash(context, type, b, a);
	}
}
