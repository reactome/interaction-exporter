package org.reactome.server.tools.interaction.exporter.writer;

import org.reactome.server.tools.interaction.exporter.model.Interaction;
import org.reactome.server.tools.interaction.exporter.psi.InteractionFactory;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.BinaryInteractionImpl;
import psidev.psi.mi.tab.model.Interactor;
import psidev.psi.mi.tab.model.builder.MitabWriterUtils;
import psidev.psi.mi.tab.model.builder.PsimiTabVersion;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collections;

public class Tab27Writer implements InteractionWriter {


	private final PrintStream writer;

	public Tab27Writer(OutputStream output) {
		final OutputStream outputStream = output instanceof BufferedOutputStream
				? output
				: new BufferedOutputStream(output);
		writer = new PrintStream(outputStream);
		writer.println(MitabWriterUtils.buildHeader(PsimiTabVersion.v2_7).trim());
	}

	public static BinaryInteraction toBinaryInteraction(Interaction interaction) {
		final Interactor a = InteractionFactory.createInteractor(interaction.getA());
		final Interactor b = InteractionFactory.createInteractor(interaction.getB());

		final BinaryInteraction psiInteraction = new BinaryInteractionImpl(a, b);
		InteractionFactory.configureContext(psiInteraction, interaction.getContext().getStId());
		psiInteraction.setInteractionTypes(Collections.singletonList(interaction.getType()));

		// Sort by primary identifier
		if (a.getIdentifiers().get(0).getIdentifier().compareTo(b.getIdentifiers().get(0).getIdentifier()) > 0)
			psiInteraction.flip();
		return psiInteraction;
	}

	@Override
	public void write(Interaction interaction) {
		final BinaryInteraction psiInteraction = toBinaryInteraction(interaction);
		// Line is trimmed because a hard \n is inserted in buildLine
		// The system will decide which lineSeparator to use
		writer.println(MitabWriterUtils.buildLine(psiInteraction, PsimiTabVersion.v2_7).trim());
	}

	@Override
	public void close() {
		writer.close();
	}


}
