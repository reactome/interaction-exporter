package org.reactome.server.tools.interaction.exporter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reactome.server.tools.interaction.exporter.psi.InteractionFactory;
import org.reactome.server.tools.interaction.exporter.util.Constants;
import psidev.psi.mi.tab.model.builder.MitabWriterUtils;
import psidev.psi.mi.tab.model.builder.PsimiTabVersion;

import static org.reactome.server.tools.interaction.exporter.TestUtils.getById;

/**
 * Tests to check that proper PSI-MITAB interactions are created.
 */
public class PsiMiTabWriterTest {

	@BeforeAll
	public static void setUp() {
		Assumptions.assumeTrue(TestUtils.hasConnection());
	}

	@Test
	public void testPhysical() {
		final String expected = "" +
				"uniprotkb:O60542\tuniprotkb:Q9GZZ7" +
				"\treactome:R-HSA-434907|refseq:NP_004149.1|ENSEMBL:ENSG00000125650|entrezgene/locuslink:5623" +
				"\treactome:R-HSA-434900|refseq:NP_071422.1|refseq:NP_665705.1|refseq:XP_005260850.1|ENSEMBL:ENSG00000125861|entrezgene/locuslink:64096" +
				"\treactome:PSPN(name)|reactome:Persephin(name)|reactome:PSPN_HUMAN(name)" +
				"\treactome:GFRA4(name)|reactome:GDNF family receptor alpha-4(name)|reactome:GFRA4_HUMAN(name)" +
				"\tpsi-mi:\"MI:0364\"(inferred by curator)" +
				"\tFabregat et al. (2015)\tpubmed:24243840" +
				"\ttaxid:9606(Homo sapiens)\ttaxid:9606(Homo sapiens)" +
				"\tpsi-mi:\"MI:0915\"(physical association)" +
				"\tpsi-mi:\"MI:0467\"(reactome)" +
				"\treactome:R-HSA-8853798" +
				"\treactome-score:0.5" +
				"\tpsi-mi:\"MI:1061\"(matrix expansion)" +
				"\tpsi-mi:\"MI:0499\"(unspecified role)\tpsi-mi:\"MI:0499\"(unspecified role)" +
				"\tpsi-mi:\"MI:0499\"(unspecified role)\tpsi-mi:\"MI:0499\"(unspecified role)" +
				"\tpsi-mi:\"MI:0326\"(protein)\tpsi-mi:\"MI:0326\"(protein)" +
				"\tgo:\"GO:0005576\"(extracellular region)" +
				"\tgo:\"GO:0005886\"(plasma membrane)" +
				"\tgo:\"GO:0005886\"(plasma membrane)" +
				"\t-\t-\t-" +
				"\ttaxid:9606(Homo sapiens)\t-" +
				"\t2016/01/25\t2016/05/19" +
				"\t-\t-\t-" +
				"\tfalse" +
				"\t-\t-" +
				"\t1\t1" +
				"\tpsi-mi:\"MI:0364\"(inferred by curator)\tpsi-mi:\"MI:0364\"(inferred by curator)";
		final Interaction interaction = new Interaction(InteractionType.PHYSICAL, getById("R-HSA-8853798"),
				new Interactor(getById("R-HSA-434907"), 1L, Constants.UNSPECIFIED_ROLE),
				new Interactor(getById("R-HSA-434900"), 1L, Constants.UNSPECIFIED_ROLE));
		final String actual = toString(interaction);
		Assertions.assertEquals(expected, actual);
	}

	@Test
	public void testEnzymaticReaction() {
		final String expected = "" +
				"uniprotkb:P52735" +
				"\treactome:R-HSA-445010" +
				"\treactome:R-HSA-442307|refseq:NP_001127870.1|refseq:NP_003362.2|refseq:XP_005272270.1|ENSEMBL:ENSG00000160293|entrezgene/locuslink:7410" +
				"\t-" +
				"\treactome:p-Y172-VAV2(name)|reactome:" +
				"\"p-VAV2(Y172)\"(name)" +
				"\t-\tpsi-mi:\"MI:0364\"(inferred by curator)" +
				"\tSchmid et al. (2000)|Schmid et al. (2004)\tpubmed:10818153|pubmed:15597056" +
				"\ttaxid:9606(Homo sapiens)\ttaxid:9606(Homo sapiens)" +
				"\tpsi-mi:\"MI:0414\"(enzymatic reaction)" +
				"\tpsi-mi:\"MI:0467\"(reactome)" +
				"\treactome:R-HSA-445064" +
				"\treactome-score:0.5" +
				"\t-" +
				"\tpsi-mi:\"MI:0502\"(enzyme target)" +
				"\tpsi-mi:\"MI:0501\"(enzyme)" +
				"\tpsi-mi:\"MI:0499\"(unspecified role)\tpsi-mi:\"MI:0499\"(unspecified role)" +
				"\tpsi-mi:\"MI:0326\"(protein)\tpsi-mi:\"MI:0314\"(complex)" +
				"\tgo:\"GO:0005829\"(cytosol)" +
				"\tgo:\"GO:0005829\"(cytosol)" +
				"\tgo:\"GO:0005085\"(guanyl-nucleotide exchange factor activity)|go:\"GO:0005829\"(cytosol)" +
				"\t-\t-\t-" +
				"\ttaxid:9606(Homo sapiens)" +
				"\t-\t" +
				"2009/10/27\t2017/11/18" +
				"\t-\t-\t-" +
				"\tfalse" +
				"\tO4'-phospho-L-tyrosine:172-172(\"MOD:00048\")" +
				"\t-" +
				"\t1\t1" +
				"\tpsi-mi:\"MI:0364\"(inferred by curator)\tpsi-mi:\"MI:0364\"(inferred by curator)";

		final Interaction interaction = new Interaction(InteractionType.fromPsiMi("MI:0414"), getById("R-HSA-445064"),
				new Interactor(getById("R-HSA-442307"), 1L, Constants.ENZYME_TARGET),
				new Interactor(getById("R-HSA-445010"), 1L, Constants.ENZYME));
		Assertions.assertEquals(expected, toString(interaction));
	}

	@Test
	public void testCleavageInteraction() {
		final String expected = "" +
				"uniprotkb:P03956" +
				"\treactome:R-HSA-158770" +
				"\treactome:R-HSA-1602455|refseq:NP_002412.1|ENSEMBL:ENSG00000196611|entrezgene/locuslink:4312" +
				"\t-" +
				"\treactome:\"MMP1(20-469)\"(name)|reactome:proMMP1(name)|reactome:Pro Matrix metalloproteinase-1(name)|reactome:Interstitial collagenase(name)|reactome:Fibroblast collagenase(name)" +
				"\treactome:Plasmin(name)" +
				"\tpsi-mi:\"MI:0364\"(inferred by curator)" +
				"\tSuzuki et al. (1990)\tpubmed:2176865" +
				"\ttaxid:9606(Homo sapiens)\ttaxid:9606(Homo sapiens)" +
				"\tpsi-mi:\"MI:0194\"(cleavage reaction)" +
				"\tpsi-mi:\"MI:0467\"(reactome)" +
				"\treactome:R-HSA-1592316" +
				"\treactome-score:0.5" +
				"\t-" +
				"\tpsi-mi:\"MI:0502\"(enzyme target)\tpsi-mi:\"MI:0501\"(enzyme)" +
				"\tpsi-mi:\"MI:0499\"(unspecified role)\tpsi-mi:\"MI:0499\"(unspecified role)" +
				"\tpsi-mi:\"MI:0326\"(protein)\tpsi-mi:\"MI:0314\"(complex)" +
				"\tgo:\"GO:0005576\"(extracellular region)" +
				"\tgo:\"GO:0005576\"(extracellular region)" +
				"\tgo:\"GO:0004252\"(serine-type endopeptidase activity)|go:\"GO:0005576\"(extracellular region)" +
				"\t-\t-\t-" +
				"\ttaxid:9606(Homo sapiens)" +
				"\t-" +
				"\t2011/09/09\t2017/11/18" +
				"\t-\t-\t-" +
				"\tfalse" +
				"\t-\t-" +
				"\t1\t1" +
				"\tpsi-mi:\"MI:0364\"(inferred by curator)\tpsi-mi:\"MI:0364\"(inferred by curator)";
		final Interaction interaction = new Interaction(InteractionType.fromPsiMi("MI:0194"), getById("R-HSA-1592316"),
				new Interactor(getById("R-HSA-158770"), 1L, Constants.ENZYME),
				new Interactor(getById("R-HSA-1602455"), 1L, Constants.ENZYME_TARGET));
		Assertions.assertEquals(expected, toString(interaction));
	}

	private String toString(Interaction interaction) {
		return MitabWriterUtils.buildLine(InteractionFactory.toBinaryInteraction(interaction), PsimiTabVersion.v2_7).trim();
	}
}
