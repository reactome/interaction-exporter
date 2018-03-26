package org.reactome.server.tools.interaction.exporter;

import java.util.Map;
import java.util.TreeMap;

public class EntityIdentifier {
	/**
	 * This dictionary is case insensitive. Keys are the aliases, values are the
	 * standard name of the database. If no entry in this dictionary for an
	 * alias, us ethe alias.
	 */
	private static final Map<String, String> databases = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	static {
		databases.put("uniprot", "uniprotkb");
		databases.put("chebi", "ChEBI");
		databases.put("chembl", "chembl");
		databases.put("embl", "EMBL");
		databases.put("ensembl", "ENSEMBL");
		databases.put("entrezgene", "entrezgene/locuslink");
		databases.put("entrezgene/locuslink", "entrezgene/locuslink");
		databases.put("entrez gene/locuslink", "entrezgene/locuslink");
		databases.put("go", "go");
		databases.put("intact", "IntAct");
		databases.put("protein data bank", "pdbe");
		databases.put("reactome", "reactome");
		databases.put("refseq", "refseq");
	}

	private final String databaseName;
	private final String identifier;

	public EntityIdentifier(String databaseName, String identifier) {
		this.databaseName = standardDatabaseName(databaseName);
		this.identifier = identifier;
	}

	private String standardDatabaseName(String databaseName) {
		if (databaseName == null) return null;
		return databases.getOrDefault(databaseName, databaseName);
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public String getIdentifier() {
		return identifier;
	}

	@Override
	public String toString() {
		return (databaseName == null ? "" : databaseName + ":") + identifier;
	}
}
