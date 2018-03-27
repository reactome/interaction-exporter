package org.reactome.server.tools.interaction.exporter.psi;

import org.reactome.server.graph.domain.model.TranslationalModification;
import psidev.psi.mi.tab.model.Feature;

import java.util.Collections;
import java.util.List;

public class SimpleFeature implements Feature {

	private String type;
	private List<String> ranges;
	private String text;

	public SimpleFeature(TranslationalModification residue) {
		setFeatureType(residue.getPsiMod().getName().get(0));
		setText("MOD:" + residue.getPsiMod().getIdentifier());
		if (residue.getCoordinate() != null)
			setRange(Collections.singletonList(String.format("%1$d-%1$d", residue.getCoordinate())));

	}

	@Override
	public String getFeatureType() {
		return type;
	}

	@Override
	public void setFeatureType(String featureType) {
		this.type = featureType;
	}

	@Override
	public List<String> getRanges() {
		return ranges;
	}

	@Override
	public void setRange(List<String> ranges) {
		this.ranges = ranges;
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
