package org.reactome.server.tools.interaction.exporter;

import org.reactome.server.graph.domain.model.PhysicalEntity;
import psidev.psi.mi.tab.model.CrossReference;

import java.util.Objects;

public class Interactor {
	private final PhysicalEntity entity;
	private final Long stoichiometry;
	private final CrossReference biologicalRole;

	public Interactor(PhysicalEntity entity, Long stoichiometry, CrossReference biologicalRole) {
		this.entity = entity;
		this.stoichiometry = stoichiometry;
		this.biologicalRole = biologicalRole;
	}

	public CrossReference getBiologicalRole() {
		return biologicalRole;
	}

	public Long getStoichiometry() {
		return stoichiometry;
	}

	public PhysicalEntity getEntity() {
		return entity;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Interactor)) return false;
		final Interactor that = (Interactor) obj;
		return entity.equals(that.entity)
				&& stoichiometry.equals(that.stoichiometry)
				&& biologicalRole.equals(that.biologicalRole);
	}

	public boolean equalsIgnoreStoichiometry(Interactor that) {
		return entity.equals(that.entity)
				&& biologicalRole.equals(that.biologicalRole);
	}

	@Override
	public String toString() {
		return "{" +
				String.join(",",
						"class=" + entity.getClassName(),
						"id=" + entity.getStId(),
						"stoichiometry=" + stoichiometry,
						"role=" + biologicalRole.getText())
				+ "}";
	}

	@Override
	public int hashCode() {
		return Objects.hash(entity, stoichiometry, biologicalRole);
	}
}
