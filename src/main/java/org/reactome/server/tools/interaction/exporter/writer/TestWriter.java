package org.reactome.server.tools.interaction.exporter.writer;

import org.reactome.server.tools.interaction.exporter.Interaction;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Just for testing purposes, use with care,as all of the exported interactions
 * are stored in memory.
 */
public class TestWriter implements InteractionWriter {

	private List<Interaction> interactions = new LinkedList<>();

	public TestWriter(PrintStream output) {
	}

	@Override
	public void write(Interaction interaction) {
		this.interactions.add(interaction);
	}

	public List<Interaction> getInteractions() {
		return interactions;
	}
}
