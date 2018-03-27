package org.reactome.server.tools.interaction.exporter.psi;

import psidev.psi.mi.tab.model.Confidence;

public class SimpleConfidence implements Confidence {
	private String type;
	private String value;
	private String text;

	public SimpleConfidence(String type, String value, String text) {
		this.type = type;
		this.value = value;
		this.text = text;
	}

	public SimpleConfidence(String type, String value) {
		this.type = type;
		this.value = value;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public void setText(String text) {
		this.text = text;
	}
}
