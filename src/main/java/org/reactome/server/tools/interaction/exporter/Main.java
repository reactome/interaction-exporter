package org.reactome.server.tools.interaction.exporter;

import org.reactome.server.graph.utils.ReactomeGraphCore;
import org.reactome.server.tools.interaction.exporter.filter.IncludeSimpleEntity;
import org.reactome.server.tools.interaction.exporter.util.GraphCoreConfig;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class Main {

	public static void main(String[] args) {
		ReactomeGraphCore.initialise("localhost", "7474", "neo4j", "reactome", GraphCoreConfig.class);
		try (PrintStream os = new PrintStream(new FileOutputStream("output.txt"))) {
			InteractionExporter.stream(exporter -> exporter
					.setSpecies("Homo sapiens")
					.setIncludeSimpleEntity(IncludeSimpleEntity.NON_TRIVIAL)
					.setMaxUnitSize(4)
					.setVerbose(true))
					.forEach(os::println);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
