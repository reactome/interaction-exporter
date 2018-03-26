package org.reactome.server.tools.interaction.exporter.psi;


import java.util.Map;
import java.util.TreeMap;

public final class CrossReference implements psidev.psi.mi.tab.model.CrossReference {

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


	private String database;
	private String identifier;
	private String text;

	public CrossReference(String database, String identifier, String text) {
		setDatabase(database);
		this.identifier = identifier;
		this.text = text;
	}

	@Override
	public String getDatabase() {
		return database;
	}

	@Override
	public void setDatabase(String database) {
		this.database = database == null
				? null
				: databases.getOrDefault(database, database);
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
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

}
