package org.reactome.server.tools.interaction.exporter.neo4j;

import org.reactome.server.graph.domain.model.LiteratureReference;
import org.reactome.server.tools.interaction.exporter.psi.SimpleAuthor;
import org.reactome.server.tools.interaction.exporter.psi.SimpleCrossReference;
import org.reactome.server.tools.interaction.exporter.util.Constants;
import psidev.psi.mi.tab.model.Annotation;
import psidev.psi.mi.tab.model.AnnotationImpl;
import psidev.psi.mi.tab.model.Author;
import psidev.psi.mi.tab.model.CrossReference;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class ContextResult {
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
	private List<CrossReference> crossReferences = Collections.emptyList();
	private String schemaClass;
	private Date created;
	private Date modified;
	private Boolean inferred;
	private List<LiteratureReference> publications = Collections.singletonList(Constants.REACTOME_PUBLICATION);
	private List<CrossReference> species = Collections.emptyList();
	private List<String> pathways;

	public void setSchemaClass(String schemaClass) {
		this.schemaClass = schemaClass;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(String created) {
		try {
			this.created = DATE_FORMAT.parse(created);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(String date) {
		if (date == null) return;
		try {
			this.modified = DATE_FORMAT.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public CrossReference getComplexExpansion() {
		switch (schemaClass) {
			case "Complex":
				return Constants.MATRIX_EXPANSION;
			default:
				return null;
		}
	}

	public List<CrossReference> getSpecies() {
		return species;
	}

	public void setSpecies(List<SimpleCrossReference> species) {
		this.species = new LinkedList<>(species);
	}

	public List<Annotation> getPathwayAnnotations() {
		return pathways.stream().map(stId -> new AnnotationImpl("pathway", stId)).collect(Collectors.toList());
	}

	public List<Author> getAuthors() {
		return publications.stream()
				.map(publication -> String.format("%s et al. (%d)", publication.getAuthor().get(0).getSurname(), publication.getYear()))
				.map(SimpleAuthor::new)
				.collect(Collectors.toList());
	}

	public List<CrossReference> getPublications() {
		return publications.stream()
				.filter(publication -> publication.getPubMedIdentifier() != null)
				.map(publication -> new SimpleCrossReference(Constants.PUBMED, publication.getPubMedIdentifier().toString(), null))
				.collect(Collectors.toList());
	}

	public void setPublications(List<LiteratureReference> publications) {
		if (publications != null && !publications.isEmpty())
			this.publications = publications;
	}

	public Boolean getInferred() {
		return inferred;
	}

	public void setInferred(Boolean inferred) {
		this.inferred = inferred;
	}

	@Override
	public String toString() {
		return schemaClass + "{"
				+ "created: " + created
				+ ", modified: " + modified
				+ ", inferred: " + inferred
				+ ", species: " + species
				+ ", pubs: " + (publications == null ? "[]" : publications.stream().map(this::shortPub).collect(Collectors.joining(", ")))
				+ "}";
	}

	private String shortPub(LiteratureReference literatureReference) {
		return literatureReference.getPubMedIdentifier()
				+ ":" + literatureReference.getAuthor().get(0).getSurname()
				+ "(" + literatureReference.getYear() + ")";
	}

	public List<CrossReference> getCrossReferences() {
		return crossReferences;
	}

	public void setCrossReferences(List<SimpleCrossReference> crossReferences) {
		this.crossReferences = new LinkedList<>(crossReferences);
	}
}
