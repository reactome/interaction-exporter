package org.reactome.server.tools.interaction.exporter.neo4j;

import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.reactome.server.graph.domain.result.CustomQuery;
import org.reactome.server.tools.interaction.exporter.psi.SimpleCrossReference;
import org.reactome.server.tools.interaction.exporter.psi.SimpleFeature;
import psidev.psi.mi.jami.model.Alias;
import psidev.psi.mi.jami.model.Feature;
import psidev.psi.mi.jami.model.Xref;
import psidev.psi.mi.jami.tab.extension.MitabXref;


import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class InteractorResult implements CustomQuery {

	private String schemaClass;
	private String referenceType;
	private List<Xref> identifiers = Collections.emptyList();
	private List<Alias> aliases = Collections.emptyList();
	private List<Xref> species = Collections.emptyList();
	private List<Feature> features = Collections.emptyList();
	private List<Xref> Xrefs = Collections.emptyList();

	public void setIdentifiers(List<Xref> identifiers) {
		this.identifiers = new LinkedList<>(identifiers);
	}

	public void setAliases(List<Alias> aliases) {
		this.aliases = new LinkedList<>(aliases);
	}

	public void setSpecies(List<Xref> species) {
		this.species = new LinkedList<>(species);
	}

	public void setFeatures(List<SimpleFeature> features) {
		this.features = new LinkedList<>(features);
	}

	public void setXrefs(List<Xref> Xrefs) {
		this.Xrefs = new LinkedList<>(Xrefs);
	}

	public void setSchemaClass(String schemaClass) {
		this.schemaClass = schemaClass;
	}

	public void setReferenceType(String referenceType) {
		this.referenceType = referenceType;
	}

	public String getSchemaClass() {
		return schemaClass;
	}

	public List<Xref> getIdentifiers() {
		return identifiers;
	}

	public List<Alias> getAliases() {
		return aliases;
	}

	public List<Xref> getSpecies() {
		return species;
	}

	public List<Feature> getFeatures() {
		return features;
	}

	public List<Xref> getXrefs() {
		return Xrefs;
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
				+ ", Xrefs: " + Xrefs.toString();
	}

	@Override
	public CustomQuery build(Record r) {
		InteractorResult ir = new InteractorResult();
		ir.setSchemaClass(r.get("schemaClass").asString(null));
		ir.setReferenceType(r.get("referenceType").asString(null));
		ir.setIdentifiers(r.get("identifiers").asList(SimpleCrossReference::build));
		ir.setAliases(r.get("aliases").asList(v -> new AliasImpl(v.get("dbSource").asString(null), v.get("name").asString(null), v.get("aliasType").asString(null))));
		ir.setFeatures(r.get("features").asList(v -> new SimpleFeature(v.get("featureType").asString(null), v.get("range").asList(Value::asString), v.get("text").asString(null))));
		ir.setSpecies(r.get("species").asList(Xref::build));
		return ir;
	}
}
