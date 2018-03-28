package org.reactome.server.tools.interaction.exporter.psi;

import org.reactome.server.graph.domain.model.LiteratureReference;
import org.reactome.server.graph.domain.model.Person;

import java.util.Collections;

public class SimplePublication extends LiteratureReference {

	public SimplePublication(Integer identifier, String surname, int year) {
		setPubMedIdentifier(identifier);
		setAuthor(Collections.singletonList(new SimpleAuthor(surname)));
		setYear(year);
	}

	private class SimpleAuthor extends Person {
		SimpleAuthor(String surname) {
			setSurname(surname);
		}
	}
}
