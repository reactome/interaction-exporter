package org.reactome.server.tools.interaction.exporter.util;

import org.reactome.server.graph.domain.model.Publication;
import org.reactome.server.tools.interaction.exporter.psi.SimpleConfidence;
import org.reactome.server.tools.interaction.exporter.psi.SimpleCrossReference;
import org.reactome.server.tools.interaction.exporter.psi.SimplePublication;
import psidev.psi.mi.tab.model.Confidence;
import psidev.psi.mi.tab.model.CrossReference;

public class Constants {
	public static final String PSI_MI = "psi-mi";
	public static final String REACTOME = "reactome";
	public static final String TAXID = "taxid";
	public static final String PUBMED = "pubmed";
	public static final CrossReference INFERRED_BY_CURATOR = new SimpleCrossReference(PSI_MI, "MI:0364", "inferred by curator");
	public static final CrossReference ENZYME = new SimpleCrossReference(PSI_MI, "MI:0501", "enzyme");
	public static final CrossReference ENZYME_TARGET = new SimpleCrossReference(PSI_MI, "MI:0502", "enzyme target");
	public static final CrossReference UNSPECIFIED_ROLE = new SimpleCrossReference(PSI_MI, "MI:0499", "unspecified role");
	public static final CrossReference MATRIX_EXPANSION = new SimpleCrossReference(PSI_MI, "MI:1061", "matrix expansion");
	public static final CrossReference REACTOME_DATABASE = new SimpleCrossReference(PSI_MI, "MI:0467", "reactome");
	public static final Confidence CONFIDENCE_DEFAULT = new SimpleConfidence("reactome-score", "0.5");
	public static final Confidence CONFIDENCE_INFERRED = new SimpleConfidence("reactome-score", "0.4", "inferred");
	public static final CrossReference PROTEIN = new SimpleCrossReference(PSI_MI, "MI:0326", "protein");
	public static final CrossReference RNA = new SimpleCrossReference(PSI_MI, "MI:0320", "ribonucleic acid");
	public static final CrossReference DNA = new SimpleCrossReference(PSI_MI, "MI:0319", "desoxyribonucleic acid");
	public static final CrossReference SMALL_MOLECULE = new SimpleCrossReference(PSI_MI, "MI:0328", "small molecule");
	public static final CrossReference COMPLEX = new SimpleCrossReference(PSI_MI, "MI:0314", "complex");
	public static final CrossReference BIOPOLYMER = new SimpleCrossReference(PSI_MI, "MI:0383", "biopolymer");
	public static final Publication REACTOME_PUBLICATION = new SimplePublication(24243840, "Fabregat", 2015);
}
