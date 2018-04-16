package org.reactome.server.tools.interaction.exporter.neo4j;

import org.reactome.server.tools.interaction.exporter.psi.SimpleCrossReference;
import org.reactome.server.tools.interaction.exporter.psi.SimpleFeature;
import psidev.psi.mi.tab.model.Alias;
import psidev.psi.mi.tab.model.AliasImpl;
import psidev.psi.mi.tab.model.CrossReference;
import psidev.psi.mi.tab.model.Feature;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class InteractorResult {

	private String schemaClass;
	private String referenceType;
	private List<CrossReference> identifiers = Collections.emptyList();
	private List<Alias> aliases = Collections.emptyList();
	private List<CrossReference> species = Collections.emptyList();
	private List<Feature> features = Collections.emptyList();
	private List<CrossReference> crossReferences = Collections.emptyList();

	public void setIdentifiers(List<SimpleCrossReference> identifiers) {
		this.identifiers = new LinkedList<>(identifiers);
	}

	public void setAliases(List<AliasImpl> aliases) {
		this.aliases = new LinkedList<>(aliases);
	}

	public void setSpecies(List<SimpleCrossReference> species) {
		this.species = new LinkedList<>(species);
	}

	public void setFeatures(List<SimpleFeature> features) {
		this.features = new LinkedList<>(features);
	}

	public void setCrossReferences(List<SimpleCrossReference> crossReferences) {
		this.crossReferences = new LinkedList<>(crossReferences);
	}

	public String getSchemaClass() {
		return schemaClass;
	}

	public List<CrossReference> getIdentifiers() {
		return identifiers;
	}

	public List<Alias> getAliases() {
		return aliases;
	}

	public List<CrossReference> getSpecies() {
		return species;
	}

	public List<Feature> getFeatures() {
		return features;
	}

	public List<CrossReference> getCrossReferences() {
		return crossReferences;
	}

	public String getReferenceType() {
		return referenceType;
	}

	@Override
	public String toString() {
		return schemaClass + "(" + referenceType + ")"
				+ "{identifiers: " + identifiers.toString()
				+ ", aliases: " + aliases.toString()
				+ ", species: " + species.toString()
				+ ", features: " + features.toString()
				+ ", crossReferences: " + crossReferences.toString();
	}
}
