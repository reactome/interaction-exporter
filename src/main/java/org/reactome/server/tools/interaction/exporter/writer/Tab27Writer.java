package org.reactome.server.tools.interaction.exporter.writer;

import org.reactome.server.tools.interaction.exporter.Interaction;
import org.reactome.server.tools.interaction.exporter.psi.InteractionFactory;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.builder.MitabWriterUtils;
import psidev.psi.mi.tab.model.builder.PsimiTabVersion;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public class Tab27Writer implements InteractionWriter {


	private final PrintStream writer;

	public Tab27Writer(OutputStream output) {
		final OutputStream outputStream = output instanceof BufferedOutputStream
				? output
				: new BufferedOutputStream(output);
		writer = new PrintStream(outputStream);
		writer.println(MitabWriterUtils.buildHeader(PsimiTabVersion.v2_7));
	}

	@Override
	public void write(Interaction interaction) {
		final BinaryInteraction psiInteraction = InteractionFactory.toBinaryInteraction(interaction);
		// Line is trimmed because a hard \n is inserted in buildLine
		final String trimmed = MitabWriterUtils.buildLine(psiInteraction, PsimiTabVersion.v2_7).trim();
		// The system will decide which lineSeparator to use
		writer.println(trimmed);
	}


}
