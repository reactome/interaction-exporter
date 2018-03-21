package org.reactome.server.tools.interaction.exporter;

import org.reactome.server.graph.domain.model.Complex;
import org.reactome.server.graph.domain.model.DatabaseObject;
import org.reactome.server.graph.domain.model.Polymer;
import org.reactome.server.graph.domain.model.ReactionLikeEvent;
import org.reactome.server.graph.service.DatabaseObjectService;
import org.reactome.server.graph.service.SchemaService;
import org.reactome.server.graph.utils.ReactomeGraphCore;
import org.reactome.server.tools.interaction.exporter.collector.InteractionCollector;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.function.Consumer;

public class InteractionExporter {

	private static final DatabaseObjectService OBJECT_SERVICE = ReactomeGraphCore.getService(DatabaseObjectService.class);
	private static final SchemaService SCHEMA_SERVICE = ReactomeGraphCore.getService(SchemaService.class);

	private String species = "Homo sapiens";
	private IncludeSimpleEntity includeSimpleEntity = IncludeSimpleEntity.NON_TRIVIAL;
	private PrintStream output = System.out;
	private Format format = Format.TAB27;
	private String stId;
	private int maxSetSize = Integer.MAX_VALUE;

	private InteractionExporter() {

	}

	public static void export(Consumer<InteractionExporter> consumer) {
		final InteractionExporter exporter = new InteractionExporter();
		consumer.accept(exporter);
		exporter.export();
	}


	public InteractionExporter setSpecies(String species) {
		this.species = species;
		return this;
	}

	public InteractionExporter setIncludeSimpleEntity(IncludeSimpleEntity includeSimpleEntity) {
		this.includeSimpleEntity = includeSimpleEntity;
		return this;
	}

	public InteractionExporter setOutput(OutputStream output) {
		this.output = new PrintStream(output);
		return this;
	}

	public InteractionExporter setFormat(Format format) {
		this.format = format;
		return this;
	}

	public InteractionExporter setObject(String stId) {
		this.stId = stId;
		return this;
	}

	public InteractionExporter setMaxSetSize(int maxSetSize) {
		this.maxSetSize = maxSetSize;
		return this;
	}

	private void export() {
		if (stId != null) {
			final DatabaseObject object = OBJECT_SERVICE.findById(stId);
			if (object instanceof Complex) {
				final Collection<DatabaseObject> complexes = Collections.singletonList(object);
				InteractionCollector.export(complexes, format, output, includeSimpleEntity, species, maxSetSize);
			} else if (object instanceof Polymer) {
				final List<DatabaseObject> polymers = Collections.singletonList(object);
				InteractionCollector.export(polymers, format, output, includeSimpleEntity, species, maxSetSize);
			} else if (object instanceof ReactionLikeEvent) {
				final List<DatabaseObject> reactions = Collections.singletonList(object);
				InteractionCollector.export(reactions, format, output, includeSimpleEntity, species, maxSetSize);
			}
		} else {
			final Collection<DatabaseObject> complexes = new ArrayList<>(SCHEMA_SERVICE.getByClass(Complex.class, species));
			InteractionCollector.export(complexes, format, output, includeSimpleEntity, species, maxSetSize);

			final Collection<DatabaseObject> polymers = new LinkedList<>(SCHEMA_SERVICE.getByClass(Polymer.class, species));
			InteractionCollector.export(polymers, format, output, includeSimpleEntity, species, maxSetSize);

			final Collection<DatabaseObject> reactions = new LinkedList<>(SCHEMA_SERVICE.getByClass(ReactionLikeEvent.class, species));
			InteractionCollector.export(reactions, format, output, includeSimpleEntity, species, maxSetSize);
		}
	}


}
