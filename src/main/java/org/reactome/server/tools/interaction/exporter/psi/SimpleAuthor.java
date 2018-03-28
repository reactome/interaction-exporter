package org.reactome.server.tools.interaction.exporter.psi;

import psidev.psi.mi.tab.model.Author;

public class SimpleAuthor implements Author {
	private String name;

	SimpleAuthor(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
}
