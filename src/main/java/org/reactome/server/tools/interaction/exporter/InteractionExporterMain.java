package org.reactome.server.tools.interaction.exporter;

import com.martiansoftware.jsap.*;
import org.reactome.server.graph.domain.model.Species;
import org.reactome.server.graph.service.SpeciesService;
import org.reactome.server.graph.utils.ReactomeGraphCore;
import org.reactome.server.tools.interaction.exporter.filter.SimpleEntityPolicy;
import org.reactome.server.tools.interaction.exporter.util.GraphCoreConfig;
import org.reactome.server.tools.interaction.exporter.writer.InteractionWriter;
import org.reactome.server.tools.interaction.exporter.writer.Tab27Writer;
import org.reactome.server.tools.interaction.exporter.writer.TsvWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class InteractionExporterMain {

	private static final String HOST = "host";
	private static final String PORT = "port";
	private static final String USER = "user";
	private static final String PASSWORD = "password";
	private static final String MAX_UNIT_SIZE = "maxUnitSize";
	private static final String SPECIES = "species";
	private static final String SIMPLE_ENTITIES_POLICY = "simpleEntitiesPolicy";
	private static final String OUTPUT = "output";
	private static final String FORMAT = "format";
	private static final String OBJECT = "object";
	private static final String VERBOSE = "verbose";

	public static void main(String[] args) throws JSAPException, FileNotFoundException {
		final Parameter[] parameters = {
				new FlaggedOption(HOST, 					JSAP.STRING_PARSER,	"localhost",	JSAP.REQUIRED,	  'h',	HOST, 				"The neo4j host"),
				new FlaggedOption(PORT, 					JSAP.STRING_PARSER,	"7474",	 		JSAP.REQUIRED,	  'b',	PORT, 				"The neo4j port"),
				new FlaggedOption(USER, 					JSAP.STRING_PARSER,	"neo4j",	 	JSAP.REQUIRED,	  'u',	USER, 				"The neo4j user"),
				new FlaggedOption(PASSWORD, 				JSAP.STRING_PARSER,	"neo4j",	 	JSAP.REQUIRED,	  'p',	PASSWORD, 			"The neo4j password"),
				new FlaggedOption(MAX_UNIT_SIZE, 			JSAP.INTEGER_PARSER,"4",	 		JSAP.NOT_REQUIRED,'m',	MAX_UNIT_SIZE,		"The maximum size of complexes/sets from which interactions are considered significant."),
				new FlaggedOption(SPECIES, 					JSAP.STRING_PARSER,	"Homo sapiens", JSAP.NOT_REQUIRED,'s',	SPECIES,			"1 or more species from which the interactions will be fetched. All for all").setAllowMultipleDeclarations(true),
				new FlaggedOption(OBJECT, 					JSAP.STRING_PARSER,	null,			JSAP.NOT_REQUIRED,'O',	OBJECT,				"Export interactions under this objects, species will be ignored").setAllowMultipleDeclarations(true),
				new FlaggedOption(SIMPLE_ENTITIES_POLICY, 	JSAP.STRING_PARSER,	"non_trivial",	JSAP.NOT_REQUIRED,'t',	SIMPLE_ENTITIES_POLICY,"Set if simple entities are exported as well"),
				new FlaggedOption(OUTPUT, 					JSAP.STRING_PARSER,	null, 			JSAP.REQUIRED,	  'o',	OUTPUT,				"Name of the output file"),
				new FlaggedOption(FORMAT, 					JSAP.STRING_PARSER,	"PSI",	 		JSAP.NOT_REQUIRED,'f',	FORMAT, 			"PSI or TSV"),
				new QualifiedSwitch(VERBOSE, 				JSAP.BOOLEAN_PARSER,   JSAP.NO_DEFAULT, JSAP.NOT_REQUIRED,'v',	VERBOSE,			"Requests verbose output" )
		};
		final SimpleJSAP jsap = new SimpleJSAP("Reactome interaction exporter", "A tool for exporting molecular interactions from the Reactome database",
				parameters);
		final JSAPResult config = jsap.parse(args);
		if (jsap.messagePrinted()) System.exit(1);

		final boolean verbose = config.getBoolean(VERBOSE);
		final int maxUnitSize = config.getInt(MAX_UNIT_SIZE);

		final String policy = config.getString(SIMPLE_ENTITIES_POLICY);
		final SimpleEntityPolicy simpleEntityPolicy = getSimpleEntityPolicy(policy);

		final String format = config.getString(FORMAT);
		final String output = config.getString(OUTPUT);
		final InteractionWriter writer = getWriter(format, output, verbose);

		ReactomeGraphCore.initialise(config.getString(HOST),
				config.getString(PORT),
				config.getString(USER),
				config.getString(PASSWORD),
				GraphCoreConfig.class);

		final String[] objects = config.getStringArray(OBJECT);
		if (objects == null || objects.length == 0) {
			final String[] speciesArg = config.getStringArray(SPECIES);
			exportSpecies(maxUnitSize, simpleEntityPolicy, writer, verbose, speciesArg);
		} else {
			exportObjects(maxUnitSize, simpleEntityPolicy, writer, verbose, objects);
		}
		writer.close();
	}

	private static void exportObjects(int maxUnitSize, SimpleEntityPolicy simpleEntityPolicy, InteractionWriter writer, boolean verbose, String[] objects) {
		if (verbose) {
			System.out.println("maxUnitSize        = " + maxUnitSize);
			System.out.println("simpleEntityPolicy = " + simpleEntityPolicy);
			System.out.println("objects            = " + Arrays.toString(objects));
		}
		for (String object : objects) {
			InteractionExporter.stream(exporter -> exporter
					.setObject(object)
					.setVerbose(verbose)
					.setMaxUnitSize(maxUnitSize)
					.setSimpleEntityPolicy(simpleEntityPolicy))
					.forEach(writer::write);
		}
	}

	private static void exportSpecies(int maxUnitSize, SimpleEntityPolicy simpleEntityPolicy, InteractionWriter writer, boolean verbose, String[] speciesArg) {
		if (verbose) {
			System.out.println("maxUnitSize        = " + maxUnitSize);
			System.out.println("simpleEntityPolicy = " + simpleEntityPolicy);
			System.out.println("species            = " + Arrays.toString(speciesArg));
		}
		final SpeciesService speciesService = ReactomeGraphCore.getService(SpeciesService.class);
		final List<Species> species;
		if (speciesArg.length == 1 && speciesArg[0].equalsIgnoreCase("all"))
			species = speciesService.getSpecies();
		else species = Arrays.stream(speciesArg)
				.map(speciesService::getSpecies)
				.collect(Collectors.toList());
		species.stream().
				map(Species::getDisplayName)
				.forEach(specie -> InteractionExporter.stream(exporter -> exporter
						.setSpecies(specie)
						.setVerbose(verbose)
						.setMaxUnitSize(maxUnitSize)
						.setSimpleEntityPolicy(simpleEntityPolicy))
						.forEach(writer::write));
	}

	private static InteractionWriter getWriter(String format, String output, boolean verbose) throws FileNotFoundException {
		if (verbose)
			System.out.println("output = " + new File(output).getAbsolutePath());
		final InteractionWriter writer;
		switch (format.toLowerCase()) {
			case "txt":
			case "tsv":
				writer = new TsvWriter(new FileOutputStream(output));
				break;
			case "tab":
			case "psi":
			case "psi-mitab":
			case "mitab":
			default:
				writer = new Tab27Writer(new FileOutputStream(output));
		}
		return writer;
	}

	private static SimpleEntityPolicy getSimpleEntityPolicy(String policy) {
		final SimpleEntityPolicy simpleEntityPolicy;
		switch (policy.toLowerCase()) {
			case "all":
				simpleEntityPolicy = SimpleEntityPolicy.ALL;
				break;
			case "none":
				simpleEntityPolicy = SimpleEntityPolicy.NONE;
				break;
			case "non_trivial":
			default:
				simpleEntityPolicy = SimpleEntityPolicy.NON_TRIVIAL;
				break;
		}
		return simpleEntityPolicy;
	}
}
