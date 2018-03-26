package org.reactome.server.tools.interaction.exporter.psi;

public class Alias implements psidev.psi.mi.tab.model.Alias {
	private String dbSource;
	private String name;
	private String aliasType;

	public Alias(String dbSource, String name, String aliasType) {
		this.dbSource = dbSource;
		this.name = name;
		this.aliasType = aliasType;
	}

	@Override
	public String getDbSource() {
		return dbSource;
	}

	@Override
	public void setDbSource(String dbSource) {
		this.dbSource = dbSource;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getAliasType() {
		return aliasType;
	}

	@Override
	public void setAliasType(String aliasType) {
		this.aliasType = aliasType;
	}
}
