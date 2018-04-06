package org.reactome.server.tools.interaction.exporter.writer;

import org.reactome.server.tools.interaction.exporter.Interaction;

public interface InteractionWriter extends AutoCloseable {

	void write(Interaction interaction);
}
