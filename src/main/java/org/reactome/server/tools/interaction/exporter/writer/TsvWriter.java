package org.reactome.server.tools.interaction.exporter.writer;

import org.reactome.server.graph.domain.model.EntityWithAccessionedSequence;
import org.reactome.server.graph.domain.model.PhysicalEntity;
import org.reactome.server.graph.domain.model.SimpleEntity;
import org.reactome.server.tools.interaction.exporter.EntityIdentifier;
import org.reactome.server.tools.interaction.exporter.Interaction;
import org.reactome.server.tools.interaction.exporter.util.IdentifierResolver;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TsvWriter implements InteractionWriter {
	private static final List<String> COLUMNS = Arrays.asList(
			"Interactor 1 uniprot id",
			"Interactor 1 Ensembl gene id",
			"Interactor 1 Entrez Gene id",
			"Interactor 2 uniprot id",
			"Interactor 2 Ensembl gene id",
			"Interactor 2 Entrez Gene id",
			"Interaction type",
			"Interaction context",
			"Pubmed references");
	private static final String SEPARATOR = "\t";
	private static final String EMPTY = "-";
	private static final String SECONDARY_SEPARATOR = "-";
	private PrintStream output;

	public TsvWriter(PrintStream output) {
		this.output = output;
	}

	@Override
	public void write(Interaction interaction) {
		final String[] line = new String[COLUMNS.size()];
		List<EntityIdentifier> aIdentifiers = IdentifierResolver.getIdentifiers(interaction.getA());
		List<EntityIdentifier> bIdentifiers = IdentifierResolver.getIdentifiers(interaction.getB());
		final EntityIdentifier aPrimaryIdentifier = primaryIdentifier(interaction.getA(), aIdentifiers);
		final EntityIdentifier bPrimaryIdentifier = primaryIdentifier(interaction.getB(), bIdentifiers);
		line[0] = aPrimaryIdentifier.toString();
		line[1] = ensemblIdentifier(aIdentifiers);
		line[2] = entrezGeneIdentifier(aIdentifiers);
		line[3] = bPrimaryIdentifier.toString();
		line[4] = ensemblIdentifier(bIdentifiers);
		line[5] = entrezGeneIdentifier(bIdentifiers);
		line[6] = interaction.getType();
		line[7] = "reactome:" + interaction.getContext().getStId();
		line[8] = EMPTY;
		output.println(String.join(SEPARATOR, line));
	}

	private String entrezGeneIdentifier(List<EntityIdentifier> identifiers) {
		final String result = identifiers.stream()
				.filter(entityIdentifier -> entityIdentifier.getDatabaseName() != null)
				.filter(entityIdentifier -> entityIdentifier.getDatabaseName().equalsIgnoreCase("entrezgene/locuslink"))
				.map(EntityIdentifier::toString)
				.collect(Collectors.joining("\t"));
		return result.isEmpty() ? EMPTY : result;
	}

	private String ensemblIdentifier(List<EntityIdentifier> identifiers) {
		final String result = identifiers.stream()
				.filter(entityIdentifier -> entityIdentifier.getDatabaseName() != null)
				.filter(entityIdentifier -> entityIdentifier.getDatabaseName().equalsIgnoreCase("ensembl"))
				.map(EntityIdentifier::toString)
				.collect(Collectors.joining("\t"));
		return result.isEmpty() ? EMPTY : result;
	}

	private EntityIdentifier primaryIdentifier(PhysicalEntity entity, List<EntityIdentifier> identifiers) {
		if (entity instanceof EntityWithAccessionedSequence) {
			// 1 uniprot
			final EntityIdentifier uniprot = identifiers.stream()
					.filter(entityIdentifier -> entityIdentifier.getDatabaseName().equalsIgnoreCase("uniprot"))
					.findFirst().orElse(null);
			if (uniprot != null)
				return uniprot;
		} else if (entity instanceof SimpleEntity) {
			// 1 ChEBI
			final EntityIdentifier chebi = identifiers.stream()
					.filter(entityIdentifier -> entityIdentifier.getDatabaseName().equalsIgnoreCase("chebi"))
					.findFirst().orElse(null);
			if (chebi != null)
				return chebi;
		}
		final EntityIdentifier reactome = identifiers.stream()
				.filter(entityIdentifier -> entityIdentifier.getDatabaseName().equalsIgnoreCase("reactome"))
				.findFirst().orElse(null);
		if (reactome != null)
			return reactome;
		return identifiers.get(0);
	}


}
