package org.reactome.server.tools.interaction.exporter.writer;

import org.reactome.server.tools.interaction.exporter.Interaction;
import psidev.psi.mi.tab.PsimiTabWriter;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.BinaryInteractionImpl;
import psidev.psi.mi.tab.model.builder.PsimiTabVersion;

import java.io.IOException;
import java.io.PrintStream;

public class Tab27Writer implements InteractionWriter {
	private final PrintStream output;

	private final PsimiTabWriter writer;

	public Tab27Writer(PrintStream output) throws IOException {
		this.output = output;
		this.writer = new PsimiTabWriter(PsimiTabVersion.v2_7);
		this.writer.writeMitabHeader(output);
	}

	@Override
	public void write(Interaction interaction) throws IOException {
		final BinaryInteraction psiInteraction = new BinaryInteractionImpl();

		writer.write(psiInteraction, output);
	}
}
