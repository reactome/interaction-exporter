package org.reactome.server.tools.interaction.exporter;

import org.apache.commons.cli.*;
import org.reactome.server.graph.utils.ReactomeGraphCore;
import org.reactome.server.tools.interaction.exporter.filter.IncludeSimpleEntity;
import org.reactome.server.tools.interaction.exporter.util.GraphCoreConfig;
import org.reactome.server.tools.interaction.exporter.util.Visualizer;
import org.reactome.server.tools.interaction.exporter.writer.InteractionWriter;
import org.reactome.server.tools.interaction.exporter.writer.Tab27Writer;
import org.reactome.server.tools.interaction.exporter.writer.TsvWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class Main {

	public static void main(String[] args) {
		final Options options = getCommandLine();
		if (args.length == 0 || args[0].equals("-h") || args[0].equals("--help")) {
			printHelp(options);
			return;
		}
		try {
			CommandLineParser parser = new DefaultParser();
			final CommandLine commandLine = parser.parse(options, args);
			final String host = getValue(commandLine, "host", String.class, null);
			final Number port = getValue(commandLine, "port", Number.class, null);
			final String user = getValue(commandLine, "user", String.class, null);
			final String password = getValue(commandLine, "password", String.class, null);
			final String tree = getValue(commandLine, "tree", String.class, null);
			final Number maxUnitSize = getValue(commandLine, "m", Number.class, 4);
			final String[] objects = getValue(commandLine, "O", String[].class, null);
			final String[] species = getValue(commandLine, "s", String[].class, new String[]{"Homo sapiens"});
			final String format = getValue(commandLine, "f", String.class, "PSI-MITAB");
			final String include = getValue(commandLine, "i", String.class, "NON_TRIVIAL");
			final File output = getValue(commandLine, "o", File.class, null);
			final Boolean verbose = commandLine.hasOption("V") && output != null;

			ReactomeGraphCore.initialise(host, port.toString(), user, password, GraphCoreConfig.class);

			if (tree != null) {
				Visualizer.printTree(tree);
				return;
			}
			final OutputStream os = output == null ? System.out : new FileOutputStream(output);

			final IncludeSimpleEntity includeSimpleEntity;
			switch (include.toLowerCase()) {
				case "all":
					includeSimpleEntity = IncludeSimpleEntity.ALL;
					break;
				case "none":
					includeSimpleEntity = IncludeSimpleEntity.NONE;
					break;
				default:
					includeSimpleEntity = IncludeSimpleEntity.NON_TRIVIAL;
			}

			final String f;
			switch (format.toLowerCase()) {
				case "txt":
				case "tsv":
				case "text":
					f = "tsv";
					break;
				case "psi-mitab":
				case "mitab":
				case "psi":
				default:
					f = "psi-mitab";
			}

			System.err.println("verbose     = " + verbose);
			System.err.println("maxUnitSize = " + maxUnitSize);
			System.err.println("species     = " + Arrays.toString(species));
			System.err.println("objects     = " + (objects == null ? "all" : Arrays.toString(objects)));
			System.err.println("format      = " + f);
			System.err.println("include     = " + includeSimpleEntity);
			System.err.println("output      = " + (output == null ? "stdout" : output));
			System.err.println("database    = " + String.format("%s:%d(%s,***)%n", host, port.intValue(), user));


			// We put this here because Tab27 prints the header when created
			final InteractionWriter writer = f.equals("tsv")
					? new TsvWriter(os)
					: new Tab27Writer(os);

			if (objects == null) {
				for (String specie : species) {
					System.err.println(specie);
					InteractionExporter.stream(exporter -> exporter
							.setSpecies(specie)
							.setIncludeSimpleEntity(includeSimpleEntity)
							.setMaxUnitSize(maxUnitSize.intValue())
							.setVerbose(verbose))
							.forEach(writer::write);
				}
			} else {
				for (String object : objects) {
					InteractionExporter.stream(exporter -> exporter
							.setIncludeSimpleEntity(includeSimpleEntity)
							.setMaxUnitSize(maxUnitSize.intValue())
							.setVerbose(verbose)
							.setObject(object))
							.forEach(writer::write);
				}
			}
		} catch (ParseException | IOException e) {
			System.err.println(e.getMessage());
			printHelp(options);
		}
	}

	private static void printHelp(Options options) {
		final HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java -jar interaction-exporter.jar", options, true);
	}

	private static <T> T getValue(CommandLine commandLine, String name, Class<T> clazz, T defaultValue) throws ParseException {
		final T value;
		if (clazz == String[].class)
			value = (T) commandLine.getOptionValues(name);
		else value = (T) commandLine.getParsedOptionValue(name);
		return value == null ? defaultValue : value;
	}

	private static Options getCommandLine() {
		final Options options = new Options();
		options.addOption(Option.builder("h")
				.desc("print this help")
				.argName("help")
				.longOpt("help")
				.numberOfArgs(1)
				.build());
		options.addOption(Option.builder("H")
				.desc("graph database host")
				.argName("host")
				.longOpt("host")
				.numberOfArgs(1)
				.required()
				.build());
		options.addOption(Option.builder("P")
				.desc("graph database port")
				.argName("port")
				.longOpt("port")
				.type(Number.class)
				.numberOfArgs(1)
				.required()
				.build());
		options.addOption(Option.builder("U")
				.desc("graph database user")
				.argName("user")
				.longOpt("user")
				.numberOfArgs(1)
				.required()
				.build());
		options.addOption(Option.builder("W")
				.desc("graph database password")
				.argName("password")
				.longOpt("password")
				.numberOfArgs(1)
				.required()
				.build());
		options.addOption(Option.builder("m")
				.desc("maximum unit size. Complexes and sets with more than m children will not be expanded.")
				.argName("maxUnitSize")
				.longOpt("maxUnitSize")
				.type(Number.class)
				.numberOfArgs(1)
				.build());
		options.addOption(Option.builder("V")
				.desc("verbose mode. Progress will be shown")
				.argName("verbose")
				.longOpt("verbose")
				.type(Boolean.class)
				.hasArg(false)
				.build());
		options.addOption(Option.builder("O")
				.desc("database object. Export only interactions within this object (complex/polymer/reaction)")
				.argName("object")
				.longOpt("object")
				.numberOfArgs(Option.UNLIMITED_VALUES)
				.hasArg()
				.build());
		options.addOption(Option.builder("s")
				.desc("only interactions for this/these species will be exported")
				.argName("species")
				.longOpt("species")
				.numberOfArgs(Option.UNLIMITED_VALUES)
				.build());
		options.addOption(Option.builder("f")
				.desc("output format. TSV(tsv, txt, text) or PSI-MITAB (psi-mitab, mitab, psi). Default is PSI-MITAB")
				.argName("format")
				.longOpt("format")
				.numberOfArgs(1)
				.build());
		options.addOption(Option.builder("i")
				.desc("include small molecules. " +
						"ALL will export interactions for all small molecules. " +
						"NONE will export interactions where no small molecules are present. " +
						"NON_TRIVIAL will export interactions where, if present, the small molecules are not trivial (H20, ATP...). Default is NON_TRIVIAL")
				.argName("include")
				.longOpt("includeSmall")
				.numberOfArgs(1)
				.build());
		options.addOption(Option.builder("o")
				.desc("output file. If not specified output will be thrown to stdout")
				.argName("output")
				.longOpt("output")
				.type(File.class)
				.numberOfArgs(1)
				.build());
		options.addOption(Option.builder("t")
				.desc("print tree of database object")
				.argName("tree")
				.longOpt("tree")
				.numberOfArgs(1)
				.build());
		return options;
	}

}
