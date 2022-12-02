package org.reactome.server.tools.interaction.exporter.writer;

import org.reactome.server.graph.domain.model.Complex;
import org.reactome.server.graph.domain.model.DatabaseObject;
import org.reactome.server.graph.domain.model.ReactionLikeEvent;
import org.reactome.server.tools.interaction.exporter.model.Interaction;
import org.reactome.server.tools.interaction.exporter.neo4j.ContextResult;
import org.reactome.server.tools.interaction.exporter.neo4j.GraphHelper;
import org.reactome.server.tools.interaction.exporter.neo4j.InteractorResult;
import org.reactome.server.tools.interaction.exporter.psi.InteractionFactory;
import psidev.psi.mi.jami.model.Xref;
import psidev.psi.mi.jami.tab.extension.MitabXref;


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
		final InteractorResult a = GraphHelper.queryInteractor(interaction.getA().getEntity().getStId());
		final InteractorResult b = GraphHelper.queryInteractor(interaction.getB().getEntity().getStId());
		final ContextResult context = GraphHelper.queryContext(interaction.getContext().getStId());
		final List<Xref> aIdentifiers = a.getIdentifiers();
		final List<Xref> bIdentifiers = b.getIdentifiers();
		final MitabXref aPrimaryIdentifier = InteractionFactory.primaryIdentifier(interaction.getA().getEntity().getSchemaClass(), aIdentifiers);
		final MitabXref bPrimaryIdentifier = InteractionFactory.primaryIdentifier(interaction.getB().getEntity().getSchemaClass(), bIdentifiers);
		line[0] = toString(aPrimaryIdentifier);
		line[1] = ensemblIdentifier(aIdentifiers);
		line[2] = entrezGeneIdentifier(aIdentifiers);
		line[3] = toString(bPrimaryIdentifier);
		line[4] = ensemblIdentifier(bIdentifiers);
		line[5] = entrezGeneIdentifier(bIdentifiers);
		line[6] = interaction.getType().getText();
		line[7] = "reactome:" + interaction.getContext().getStId();
		line[8] = pubmeds(context.getPublications());
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

	private String pubmeds(List<MitabXref> references) {
		return references.stream()
				.map(MitabXref::getId)
				.collect(Collectors.joining(SECONDARY_SEPARATOR));
	}

	private String entrezGeneIdentifier(List<MitabXref> identifiers) {
		final String result = identifiers.stream()
				.filter(entityIdentifier -> entityIdentifier.getDatabase() != null)
				.filter(entityIdentifier -> entityIdentifier.getDatabase().getShortName().equalsIgnoreCase("entrezgene/locuslink"))
				.map(this::toString)
				.collect(Collectors.joining(SECONDARY_SEPARATOR));
		return result.isEmpty() ? EMPTY : result;
	}

	private String ensemblIdentifier(List<MitabXref> identifiers) {
		final String result = identifiers.stream()
				.filter(entityIdentifier -> entityIdentifier.getDatabase() != null)
				.filter(entityIdentifier -> entityIdentifier.getDatabase().getShortName().equalsIgnoreCase("ensembl"))
				.map(this::toString)
				.collect(Collectors.joining(SECONDARY_SEPARATOR));
		return result.isEmpty() ? EMPTY : result;
	}

	private String toString(MitabXref reference) {
		return (reference.getDatabase() == null ? "" : reference.getDatabase().getShortName() + ":") + reference.getId();
	}
}
