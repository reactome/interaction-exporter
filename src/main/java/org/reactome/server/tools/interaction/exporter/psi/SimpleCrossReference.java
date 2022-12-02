package org.reactome.server.tools.interaction.exporter.psi;


import org.neo4j.driver.Value;
import psidev.psi.mi.jami.model.CvTerm;
import psidev.psi.mi.jami.model.Xref;
import psidev.psi.mi.jami.model.impl.DefaultCvTerm;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class SimpleCrossReference implements Xref {

	/**
	 * This dictionary is case insensitive. Keys are the aliases, values are the
	 * standard name of the database. If no entry in this dictionary for an
	 * alias, us ethe alias.
	 */
	private static final Map<String, String> databases = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	static {
		databases.put("uniprot", Xref.UNIPROTKB);
		databases.put("chebi", Xref.CHEBI);
		databases.put("go", Xref.GO);
		databases.put("ensembl", Xref.ENSEMBL);
		databases.put("entrezgene", Xref.ENTREZ_GENE);
		databases.put("entrezgene/locuslink", Xref.ENTREZ_GENE);
		databases.put("entrez gene/locuslink", Xref.ENTREZ_GENE);
		databases.put("refseq", Xref.REFSEQ);
		databases.put("chembl", "chembl");
		databases.put("embl", "EMBL");
		databases.put("intact", "IntAct");
		databases.put("protein data bank", "pdbe");
		databases.put("reactome", "reactome");
	}

	private CvTerm database;
	private String identifier;
	private String text;

	public SimpleCrossReference() {
	}

	public SimpleCrossReference(String database, String identifier) {
		setDatabase(database);
		setIdentifier(identifier);
	}

	public SimpleCrossReference(String database, String identifier, String text) {
		setDatabase(database);
		setIdentifier(identifier);
		setText(text);
	}

	@Override
	public CvTerm getDatabase() {
		return database;
	}

	@Override
	public void setDatabase(String database) {
		this.database = database == null
				? null
				: new DefaultCvTerm(databases.getOrDefault(database, database));
		// In case database is go
		if (identifier != null) setId(identifier);
	}

	@Override
	public String getId() {
		return identifier;
	}

	@Override
	public void setId(String identifier) {
		if (database != null && database.equals("go") && !identifier.startsWith("GO:"))
			this.identifier = "GO:" + identifier;
		else this.identifier = identifier;
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public void setText(String text) {
		this.text = text;
	}

	@Override
	public boolean hasText() {
		return text != null && !text.isEmpty();
	}

	@Override
	public String toString() {
		return String.join(":", database.getShortName(), identifier, text);
	}

	@Override
	public int hashCode() {
		return Objects.hash(database, identifier, text);
	}

	@Override
	public String getVersion() {
		return null;
	}

	@Override
	public CvTerm getQualifier() {
		return null;
	}

	public static SimpleCrossReference build(Value v) {
		return new SimpleCrossReference(v.get("database").asString(null), v.get("identifier").asString(null), v.get("text").asString(null));
	}
}
