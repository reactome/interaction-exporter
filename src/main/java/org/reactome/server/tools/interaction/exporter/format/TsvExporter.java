package org.reactome.server.tools.interaction.exporter.format;

import org.reactome.server.tools.interaction.exporter.Interaction;

import java.io.PrintStream;
import java.util.function.BiConsumer;

public class TsvExporter implements BiConsumer<Interaction, PrintStream> {

	@Override
	public void accept(Interaction interaction, PrintStream output) {
		output.println(String.join("\t",
				String.format("%s:%s", interaction.getContext().getClassName(), interaction.getContext().getStId()),
				String.format("%s:%s", interaction.getA().getClass().getSimpleName(), interaction.getA().getStId(), interaction.getAst()),
				String.format("%s:%s", interaction.getB().getClass().getSimpleName(), interaction.getB().getStId(), interaction.getBst())));

	}
}
