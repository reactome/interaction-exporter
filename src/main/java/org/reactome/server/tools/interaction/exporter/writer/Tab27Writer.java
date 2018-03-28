package org.reactome.server.tools.interaction.exporter.writer;

import org.reactome.server.tools.interaction.exporter.Interaction;
import org.reactome.server.tools.interaction.exporter.psi.InteractionFactory;
import psidev.psi.mi.tab.PsimiTabWriter;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.builder.PsimiTabVersion;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class Tab27Writer implements InteractionWriter {


	private final Writer output;
	private final PsimiTabWriter writer;

	public Tab27Writer(OutputStream output) throws IOException {
		this.output = new OutputStreamWriter(output);
		this.writer = new PsimiTabWriter(PsimiTabVersion.v2_7);
		this.writer.writeMitabHeader(this.output);
	}

	@Override
	public void write(Interaction interaction) throws IOException {
		final BinaryInteraction psiInteraction = InteractionFactory.toBinaryInteraction(interaction);
		writer.write(psiInteraction, output);
	}


}
