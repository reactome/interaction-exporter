package org.reactome.server.tools.interaction.exporter;

import org.reactome.server.graph.utils.ReactomeGraphCore;
import org.reactome.server.tools.interaction.exporter.writer.ConsoleWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class Main {

	public static void main(String[] args) {
		ReactomeGraphCore.initialise("localhost", "7474", "neo4j", "reactome", GraphCoreConfig.class);
		try (PrintStream os = new PrintStream(new FileOutputStream("output.txt"))) {
			final ConsoleWriter writer = new ConsoleWriter(os);
			InteractionExporter.stream(exporter -> exporter
					.setSpecies("Homo sapiens")
					.setIncludeSimpleEntity(IncludeSimpleEntity.NON_TRIVIAL)
					.setMaxSetSize(4)
					.setVerbose(true))
					.forEach(writer::write);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
