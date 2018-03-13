package org.reactome.server.tools.interaction.exporter;

import org.reactome.server.graph.utils.ReactomeGraphCore;

import java.io.FileOutputStream;
import java.io.IOException;

public class Main {

	public static void main(String[] args) {
		ReactomeGraphCore.initialise("localhost", "7474", "neo4j", "reactome", GraphCoreConfig.class);
		try (FileOutputStream os = new FileOutputStream("output.txt")) {
			InteractionExporter.export(exporter -> exporter
					.setOutput(os)
					.setSpecies("Homo sapiens")
					.setIncludeSimpleEntity(IncludeSimpleEntity.NON_TRIVIAL)
					.setMaxSetSize(4)
					.setFormat(Format.TSV));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
