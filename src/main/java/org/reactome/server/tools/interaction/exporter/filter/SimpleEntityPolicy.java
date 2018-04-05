package org.reactome.server.tools.interaction.exporter.filter;

import org.reactome.server.graph.domain.model.PhysicalEntity;
import org.reactome.server.graph.domain.model.SimpleEntity;

import java.util.function.Predicate;

public enum SimpleEntityPolicy {

	ALL(entity -> true),
	NONE(entity -> !(entity instanceof SimpleEntity)),
	NON_TRIVIAL(entity -> {
		if (!(entity instanceof SimpleEntity)) return true;
		final SimpleEntity simpleEntity = (SimpleEntity) entity;
		return simpleEntity.getReferenceEntity().getTrivial() == null
				|| !simpleEntity.getReferenceEntity().getTrivial();
	});
	private final Predicate<PhysicalEntity> filter;

	SimpleEntityPolicy(Predicate<PhysicalEntity> filter) {
		this.filter = filter;
	}

	public Predicate<PhysicalEntity> getFilter() {
		return filter;
	}
}
