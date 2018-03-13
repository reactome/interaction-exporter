package org.reactome.server.tools.interaction.exporter;

import org.reactome.server.tools.interaction.exporter.format.Tab27Exporter;
import org.reactome.server.tools.interaction.exporter.format.TsvExporter;

import java.io.PrintStream;
import java.util.function.BiConsumer;

public enum Format {
	TSV(new TsvExporter()),
	TAB27(new Tab27Exporter());

	private BiConsumer<Interaction, PrintStream> consumer;

	Format(BiConsumer<Interaction, PrintStream> consumer) {
		this.consumer = consumer;
	}

	public void write(Interaction interaction, PrintStream output) {
		consumer.accept(interaction, output);
	}
}
