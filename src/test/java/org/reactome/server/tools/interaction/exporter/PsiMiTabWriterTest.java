package org.reactome.server.tools.interaction.exporter;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reactome.server.tools.interaction.exporter.model.Interaction;
import org.reactome.server.tools.interaction.exporter.model.InteractionType;
import org.reactome.server.tools.interaction.exporter.model.Interactor;
import org.reactome.server.tools.interaction.exporter.util.Constants;
import org.reactome.server.tools.interaction.exporter.writer.Tab27Writer;
import psidev.psi.mi.tab.model.builder.MitabWriterUtils;
import psidev.psi.mi.tab.model.builder.PsimiTabVersion;

import java.io.IOException;
import java.nio.charset.Charset;

import static org.reactome.server.tools.interaction.exporter.TestUtils.getById;

/**
 * Tests to check that proper PSI-MITAB interactions are created.
 */
public class PsiMiTabWriterTest {

	@BeforeAll
	public static void setUp() {
		Assumptions.assumeTrue(TestUtils.hasConnection());
	}

//	@Test
//	public void testPhysicalInteraction() throws IOException {
//		final String expected = IOUtils.toString(PsiMiTabWriterTest.class.getResourceAsStream("psi-testPhysicalInteraction.txt"), Charset.defaultCharset().displayName()).trim();
//		final Interaction interaction = new Interaction(InteractionType.PHYSICAL, getById("R-HSA-8853798"),
//				new Interactor(getById("R-HSA-434907"), 1L, Constants.UNSPECIFIED_ROLE),
//				new Interactor(getById("R-HSA-434900"), 1L, Constants.UNSPECIFIED_ROLE));
//		final String actual = toString(interaction);
//		Assertions.assertEquals(expected, actual);
//	}
//
//	@Test
//	public void testEnzymaticReaction() throws IOException {
//		final String expected = IOUtils.toString(PsiMiTabWriterTest.class.getResourceAsStream("psi-testEnzymaticReaction.txt"), Charset.defaultCharset().displayName()).trim();
//		final Interaction interaction = new Interaction(InteractionType.fromPsiMi("MI:0414"), getById("R-HSA-445064"),
//				new Interactor(getById("R-HSA-442307"), 1L, Constants.ENZYME_TARGET),
//				new Interactor(getById("R-HSA-445010"), 1L, Constants.ENZYME));
//		Assertions.assertEquals(expected, toString(interaction));
//	}
//
//	@Test
//	public void testCleavageInteraction() throws IOException {
//		final String expected = IOUtils.toString(PsiMiTabWriterTest.class.getResourceAsStream("psi-testCleavageInteraction.txt"), Charset.defaultCharset().displayName()).trim();
//		final Interaction interaction = new Interaction(InteractionType.fromPsiMi("MI:0194"), getById("R-HSA-5340274"),
//				new Interactor(getById("R-HSA-158770"), 1L, Constants.ENZYME),
//				new Interactor(getById("R-HSA-1602455"), 1L, Constants.ENZYME_TARGET));
//		Assertions.assertEquals(expected, toString(interaction));
//	}


	private String toString(Interaction interaction) {
		return MitabWriterUtils.buildLine(Tab27Writer.toBinaryInteraction(interaction), PsimiTabVersion.v2_7).trim();
	}
}
