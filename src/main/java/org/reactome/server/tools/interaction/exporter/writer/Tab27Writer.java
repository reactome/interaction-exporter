package org.reactome.server.tools.interaction.exporter.writer;

import org.reactome.server.graph.domain.model.EntityWithAccessionedSequence;
import org.reactome.server.graph.domain.model.PhysicalEntity;
import org.reactome.server.graph.domain.model.SimpleEntity;
import org.reactome.server.tools.interaction.exporter.Interaction;
import org.reactome.server.tools.interaction.exporter.psi.CrossReference;
import org.reactome.server.tools.interaction.exporter.util.IdentifierResolver;
import psidev.psi.mi.tab.PsimiTabWriter;
import psidev.psi.mi.tab.model.Alias;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.BinaryInteractionImpl;
import psidev.psi.mi.tab.model.Interactor;
import psidev.psi.mi.tab.model.builder.PsimiTabVersion;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Tab27Writer implements InteractionWriter {
	private final PrintStream output;

	private final PsimiTabWriter writer;

	public Tab27Writer(PrintStream output) throws IOException {
		this.output = output;
		this.writer = new PsimiTabWriter(PsimiTabVersion.v2_7);
		this.writer.writeMitabHeader(output);
	}

	@Override
	public void write(Interaction interaction) throws IOException {
		final Interactor a = createInteractor(interaction.getA(), interaction.getAst());
		final Interactor b = createInteractor(interaction.getB(), interaction.getBst());
		final BinaryInteraction psiInteraction = new BinaryInteractionImpl(a, b);
		writer.write(psiInteraction, output);
	}

	private Interactor createInteractor(PhysicalEntity entity, Long stoichometry) {
		final Interactor interactor = new Interactor();
		final List<CrossReference> identifiers = IdentifierResolver.getIdentifiers(entity);
		final CrossReference primaryIdentifier = primaryIdentifier(entity, identifiers);
		identifiers.remove(primaryIdentifier);
		interactor.setIdentifiers(Collections.singletonList(primaryIdentifier));
		interactor.setAlternativeIdentifiers(new LinkedList<>(identifiers));
		interactor.setAliases(getAlias(entity));
		interactor.setStoichiometry(Collections.singletonList(stoichometry.intValue()));
		return interactor;
	}

	private List<Alias> getAlias(PhysicalEntity entity) {
		return null;
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
				.filter(reference -> reference.getDatabase().equalsIgnoreCase("reactome"))
				.findFirst().orElse(null);
		if (reactome != null)
			return reactome;
		return identifiers.get(0);
	}
}
