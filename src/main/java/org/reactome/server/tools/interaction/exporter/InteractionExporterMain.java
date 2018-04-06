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
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InteractionExporterMain {

	private static final String HOST = "host";
	private static final String PORT = "port";
	private static final String USER = "user";
	private static final String PASSWORD = "password";
	private static final String MAX_UNIT_SIZE = "maxUnitSize";
	private static final String SPECIES = "species";
	private static final String SIMPLE_ENTITIES_POLICY = "simpleEntitiesPolicy";
	private static final String OUTPUT = "output";
	private static final String OBJECT = "object";
	private static final String VERBOSE = "verbose";

	public static void main(String[] args) throws JSAPException {
		final Parameter[] parameters = {
				new FlaggedOption(HOST,                     JSAP.STRING_PARSER,     "localhost",        JSAP.REQUIRED,      'h', HOST,                      "The neo4j host"),
				new FlaggedOption(PORT,                     JSAP.STRING_PARSER,     "7474",             JSAP.REQUIRED,      'b', PORT,                      "The neo4j port"),
				new FlaggedOption(USER,                     JSAP.STRING_PARSER,     "neo4j",            JSAP.REQUIRED,      'u', USER,                      "The neo4j user"),
				new FlaggedOption(PASSWORD,                 JSAP.STRING_PARSER,     "neo4j",            JSAP.REQUIRED,      'p', PASSWORD,                  "The neo4j password"),
				new FlaggedOption(MAX_UNIT_SIZE,            JSAP.INTEGER_PARSER,    "4",                JSAP.NOT_REQUIRED,  'm', MAX_UNIT_SIZE,             "The maximum size of complexes/sets from which interactions are considered significant."),
				new FlaggedOption(SPECIES,                  JSAP.STRING_PARSER,     "Homo sapiens",     JSAP.NOT_REQUIRED,  's', SPECIES,                   "1 or more species from which the interactions will be fetched. ALL to export all of the species").setAllowMultipleDeclarations(true),
				new FlaggedOption(OBJECT,                   JSAP.STRING_PARSER,     null,               JSAP.NOT_REQUIRED,  'O', OBJECT,                    "Export interactions under these objects, species will be ignored").setAllowMultipleDeclarations(true),
				new FlaggedOption(SIMPLE_ENTITIES_POLICY,   JSAP.STRING_PARSER,     "NON_TRIVIAL",      JSAP.NOT_REQUIRED,  't', SIMPLE_ENTITIES_POLICY,    "Set if simple entities are exported as well: ALL, NONE or NON_TRIVIAL."),
				new FlaggedOption(OUTPUT,                   JSAP.STRING_PARSER,     null,               JSAP.REQUIRED,      'o', OUTPUT,                    "Prefix of the output files"),
				new QualifiedSwitch(VERBOSE,                JSAP.BOOLEAN_PARSER,    JSAP.NO_DEFAULT,    JSAP.NOT_REQUIRED,  'v', VERBOSE,                   "Requests verbose output")
		};
		final SimpleJSAP jsap = new SimpleJSAP("Reactome interaction exporter", "Exports molecular interactions inferred from Reactome content",
				parameters);
		final JSAPResult config = jsap.parse(args);
		if (jsap.messagePrinted()) System.exit(1);

		final boolean verbose = config.getBoolean(VERBOSE);
		final int maxUnitSize = config.getInt(MAX_UNIT_SIZE);

		final String policy = config.getString(SIMPLE_ENTITIES_POLICY);
		final SimpleEntityPolicy simpleEntityPolicy = getSimpleEntityPolicy(policy);

		final String prefix = config.getString(OUTPUT);
		if (verbose) {
			System.out.println("prefix             = " + new File(prefix).getAbsolutePath());
			System.out.println("maxUnitSize        = " + maxUnitSize);
			System.out.println("simpleEntityPolicy = " + simpleEntityPolicy);
		}

		ReactomeGraphCore.initialise(config.getString(HOST),
				config.getString(PORT),
				config.getString(USER),
				config.getString(PASSWORD),
				GraphCoreConfig.class);

		try (InteractionWriter tabWriter = new Tab27Writer(new FileOutputStream(prefix + ".psi-mitab.txt"));
		     InteractionWriter tsvWriter = new TsvWriter(new FileOutputStream(prefix + ".tab-delimited.txt"))) {
			final String[] objects = config.getStringArray(OBJECT);
			final String[] species = config.getStringArray(SPECIES);
			final Stream<Interaction> stream = objects == null || objects.length == 0
					? streamSpecies(maxUnitSize, simpleEntityPolicy, verbose, species)
					: streamObjects(maxUnitSize, simpleEntityPolicy, verbose, objects);
			stream.forEach(interaction -> {
				tabWriter.write(interaction);
				tsvWriter.write(interaction);
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Stream<Interaction> streamSpecies(int maxUnitSize, SimpleEntityPolicy simpleEntityPolicy, boolean verbose, String[] speciesArg) {
		final SpeciesService speciesService = ReactomeGraphCore.getService(SpeciesService.class);
		final List<Species> species;
		if (speciesArg.length == 1 && speciesArg[0].equalsIgnoreCase("all"))
			species = speciesService.getSpecies();
		else species = Arrays.stream(speciesArg)
				.map(speciesService::getSpecies)
				.collect(Collectors.toList());
		if (verbose)
			System.out.println("species            = " + species.stream().map(Species::getDisplayName).collect(Collectors.joining(", ")));
		return species.stream()
				.map(Species::getDisplayName)
				.peek(s -> {
					if (verbose) System.out.printf("%n%s", s);
				})
				.flatMap(specie -> InteractionExporter.stream(exporter -> exporter
						.setSpecies(specie)
						.setVerbose(verbose)
						.setMaxUnitSize(maxUnitSize)
						.setSimpleEntityPolicy(simpleEntityPolicy)));
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

	private static Stream<Interaction> streamObjects(int maxUnitSize, SimpleEntityPolicy simpleEntityPolicy, boolean verbose, String[] objects) {
		if (verbose)
			System.out.println("objects            = " + Arrays.toString(objects));
		return Arrays.stream(objects).flatMap(object ->
				InteractionExporter.stream(exporter -> exporter
						.setObject(object)
						.setVerbose(verbose)
						.setMaxUnitSize(maxUnitSize)
						.setSimpleEntityPolicy(simpleEntityPolicy)));
	}
}
