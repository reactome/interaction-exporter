package org.reactome.server.tools.interaction.exporter.writer;

import org.reactome.server.tools.interaction.exporter.Interaction;

import java.io.PrintStream;

public class ConsoleWriter implements InteractionWriter {

	private final PrintStream output;

	public ConsoleWriter(PrintStream output) {
		this.output = output;
	}

	@Override
	public void write(Interaction interaction) {
		output.println(String.join("\t",
				interaction.getType().getPsiName(),
				String.format("%s:%s", interaction.getContext().getSchemaClass(), interaction.getContext().getStId()),
				String.format("%s:%s", interaction.getA().getClass().getSimpleName(), interaction.getA().getStId(), interaction.getAst()),
				String.format("%s:%s", interaction.getB().getClass().getSimpleName(), interaction.getB().getStId(), interaction.getBst())));
	}
}
