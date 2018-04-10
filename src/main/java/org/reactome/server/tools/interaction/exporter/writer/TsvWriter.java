package org.reactome.server.tools.interaction.exporter.writer;

import org.reactome.server.graph.domain.model.*;
import org.reactome.server.tools.interaction.exporter.model.Interaction;
import org.reactome.server.tools.interaction.exporter.util.Constants;
import org.reactome.server.tools.interaction.exporter.util.IdentifierResolver;
import psidev.psi.mi.tab.model.CrossReference;

import java.io.OutputStream;
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
	private static final String SECONDARY_SEPARATOR = "|";
	private PrintStream output;

	public TsvWriter(OutputStream output) {
		this.output = new PrintStream(output);
		this.output.println("# " + String.join(SEPARATOR, COLUMNS));
	}

	@Override
	public void write(Interaction interaction) {
		final String[] line = new String[COLUMNS.size()];
		List<CrossReference> aIdentifiers = IdentifierResolver.getIdentifiers(interaction.getA().getEntity());
		List<CrossReference> bIdentifiers = IdentifierResolver.getIdentifiers(interaction.getB().getEntity());
		final CrossReference aPrimaryIdentifier = primaryIdentifier(interaction.getA().getEntity(), aIdentifiers);
		final CrossReference bPrimaryIdentifier = primaryIdentifier(interaction.getB().getEntity(), bIdentifiers);
		line[0] = toString(aPrimaryIdentifier);
		line[1] = ensemblIdentifier(aIdentifiers);
		line[2] = entrezGeneIdentifier(aIdentifiers);
		line[3] = toString(bPrimaryIdentifier);
		line[4] = ensemblIdentifier(bIdentifiers);
		line[5] = entrezGeneIdentifier(bIdentifiers);
		line[6] = type(interaction.getContext());
		line[7] = "reactome:" + interaction.getContext().getStId();
		line[8] = pubmeds(interaction.getContext());
		output.println(String.join(SEPARATOR, line));
	}

	@Override
	public void close() {
		output.close();
	}

	private String type(DatabaseObject context) {
		if (context instanceof Complex)
			return "complex";
		else if (context instanceof ReactionLikeEvent)
			return "reaction";
		else return EMPTY;
	}

	private String pubmeds(DatabaseObject context) {
		List<Publication> references = null;
		if (context instanceof PhysicalEntity)
			references = ((PhysicalEntity) context).getLiteratureReference();
		else if (context instanceof ReactionLikeEvent)
			references = ((ReactionLikeEvent) context).getLiteratureReference();
		if (references == null)
			return EMPTY;
		return references.stream()
				.filter(LiteratureReference.class::isInstance)
				.map(LiteratureReference.class::cast)
				.map(LiteratureReference::getPubMedIdentifier)
				.map(String::valueOf)
				.collect(Collectors.joining(SECONDARY_SEPARATOR));
	}

	private String entrezGeneIdentifier(List<CrossReference> identifiers) {
		final String result = identifiers.stream()
				.filter(entityIdentifier -> entityIdentifier.getDatabase() != null)
				.filter(entityIdentifier -> entityIdentifier.getDatabase().equalsIgnoreCase("entrezgene/locuslink"))
				.map(this::toString)
				.collect(Collectors.joining(SECONDARY_SEPARATOR));
		return result.isEmpty() ? EMPTY : result;
	}

	private String ensemblIdentifier(List<CrossReference> identifiers) {
		final String result = identifiers.stream()
				.filter(entityIdentifier -> entityIdentifier.getDatabase() != null)
				.filter(entityIdentifier -> entityIdentifier.getDatabase().equalsIgnoreCase("ensembl"))
				.map(this::toString)
				.collect(Collectors.joining(SECONDARY_SEPARATOR));
		return result.isEmpty() ? EMPTY : result;
	}

	private CrossReference primaryIdentifier(PhysicalEntity entity, List<CrossReference> identifiers) {
		if (entity instanceof EntityWithAccessionedSequence) {
			// uniprot
			final CrossReference uniprot = identifiers.stream()
					.filter(reference -> reference.getDatabase() != null)
					.filter(reference -> reference.getDatabase().equalsIgnoreCase("uniprotkb"))
					.findFirst().orElse(null);
			if (uniprot != null)
				return uniprot;
		} else if (entity instanceof SimpleEntity) {
			// ChEBI
			final CrossReference chebi = identifiers.stream()
					.filter(reference -> reference.getDatabase() != null)
					.filter(reference -> reference.getDatabase().equalsIgnoreCase("chebi"))
					.findFirst().orElse(null);
			if (chebi != null)
				return chebi;
		}
		final CrossReference reactome = identifiers.stream()
				.filter(reference -> reference.getDatabase() != null)
				.filter(reference -> reference.getDatabase().equals(Constants.REACTOME))
				.findFirst().orElse(null);
		if (reactome != null)
			return reactome;
		return identifiers.get(0);
	}

	private String toString(CrossReference reference) {
		return (reference.getDatabase() == null ? "" : reference.getDatabase() + ":") + reference.getIdentifier();
	}
}
