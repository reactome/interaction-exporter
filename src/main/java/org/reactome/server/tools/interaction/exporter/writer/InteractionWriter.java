package org.reactome.server.tools.interaction.exporter.writer;

import org.reactome.server.tools.interaction.exporter.Interaction;

import java.io.IOException;

public interface InteractionWriter {

	void write(Interaction interaction) throws IOException;

}
