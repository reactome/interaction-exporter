package org.reactome.server.tools.interaction.exporter.psi;

import org.reactome.server.graph.domain.model.*;
import org.reactome.server.tools.interaction.exporter.Interaction;
import org.reactome.server.tools.interaction.exporter.InteractionType;
import org.reactome.server.tools.interaction.exporter.util.Constants;
import psidev.psi.mi.tab.model.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class InteractionFactory {
	private static final Logger logger = Logger.getLogger("interaction-exporter");
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");

	public static BinaryInteraction toBinaryInteraction(Interaction interaction) {
		final Interactor a = InteractionFactoryFast.createInteractor(interaction.getA().getEntity().getStId());
		a.setStoichiometry(Collections.singletonList(interaction.getA().getStoichiometry().intValue()));
		a.setBiologicalRoles(Collections.singletonList(interaction.getA().getBiologicalRole()));
		final Interactor b = InteractionFactoryFast.createInteractor(interaction.getB().getEntity().getStId());
		b.setStoichiometry(Collections.singletonList(interaction.getB().getStoichiometry().intValue()));
		b.setBiologicalRoles(Collections.singletonList(interaction.getB().getBiologicalRole()));
		final BinaryInteraction psiInteraction = new BinaryInteractionImpl(a, b);
		InteractionFactoryFast.configureContext(psiInteraction, interaction.getContext().getStId());
		psiInteraction.setInteractionTypes(Collections.singletonList(interaction.getType()));
		// Sort by primary identifier
		if (a.getIdentifiers().get(0).getIdentifier().compareTo(b.getIdentifiers().get(0).getIdentifier()) > 0)
			psiInteraction.flip();
		return psiInteraction;
	}


	private static Organism getOrganism(DatabaseObject entity) {
		List<Taxon> taxons = new LinkedList<>();
		if (entity instanceof Complex)
			taxons.addAll(((Complex) entity).getSpecies());
		else if (entity instanceof EntityWithAccessionedSequence)
			taxons.add(((EntityWithAccessionedSequence) entity).getSpecies());
		else if (entity instanceof ReactionLikeEvent)
			taxons.addAll(((ReactionLikeEvent) entity).getSpecies());
		final List<CrossReference> references = taxons.stream()
				.map(taxon -> new SimpleCrossReference(Constants.TAXID, taxon.getTaxId(), taxon.getName().get(0)))
				.collect(Collectors.toList());
		return new OrganismImpl(references);
	}



	private static List<CrossReference> getXrefs(DatabaseObject entity) {
		// molecular function
		// cellular components
		final List<CrossReference> references = new LinkedList<>();
		if (entity instanceof ReactionLikeEvent) {
			final ReactionLikeEvent reaction = (ReactionLikeEvent) entity;
			if (reaction.getCatalystActivity() != null) {
				final GO_MolecularFunction activity = reaction.getCatalystActivity().get(0).getActivity();
				if (activity != null)
					references.add(new SimpleCrossReference(activity.getDatabaseName(), activity.getAccession(), activity.getName()));
			}
		}
		List<? extends Compartment> compartments = null;
		if (entity instanceof PhysicalEntity) {
			compartments = ((PhysicalEntity) entity).getCompartment();
		} else if (entity instanceof ReactionLikeEvent)
			compartments = ((ReactionLikeEvent) entity).getCompartment();
		if (compartments != null)
			compartments.stream()
					.map(compartment -> new SimpleCrossReference(compartment.getDatabaseName(), compartment.getAccession(), compartment.getName()))
					.forEach(references::add);
		return references;
	}


	private static void configure(BinaryInteraction psiInteraction, Interaction interaction) {
		psiInteraction.setCreationDate(getDate(interaction.getContext().getCreated()));
		psiInteraction.setUpdateDate(getDate(interaction.getContext().getModified()));
		psiInteraction.setAuthors(getAuthors(interaction.getContext()));
		psiInteraction.setHostOrganism(getOrganism(interaction.getContext()));
		psiInteraction.setPublications(getPublications(interaction.getContext()));
		psiInteraction.setComplexExpansion(getComplexExpansion(interaction));
		psiInteraction.setDetectionMethods(Collections.singletonList(Constants.INFERRED_BY_CURATOR));
		psiInteraction.setSourceDatabases(Collections.singletonList(Constants.REACTOME_DATABASE));
		psiInteraction.setConfidenceValues(getConfidence(interaction.getContext()));
		psiInteraction.setInteractionAcs(Collections.singletonList(new SimpleCrossReference(Constants.REACTOME, interaction.getContext().getStId())));
		psiInteraction.setXrefs(getXrefs(interaction.getContext()));
//		psiInteraction.setNegativeInteraction(false);
//		psiInteraction.setChecksums();
//		psiInteraction.setParameters();
//		psiInteraction.setAnnotations();
	}

	private static List<Date> getDate(InstanceEdit created) {
		if (created == null)
			return null;
		try {
			final Date result = DATE_FORMAT.parse(created.getDateTime());
			return Collections.singletonList(result);
		} catch (ParseException e) {
			logger.warning("Date format not valid: " + created.getDateTime());
		}
		return null;
	}

	private static List<CrossReference> getPublications(DatabaseObject context) {
		final List<Publication> references = getReferences(context);
		return references.stream()
				.filter(LiteratureReference.class::isInstance)
				.map(LiteratureReference.class::cast)
				.filter(publication -> publication.getPubMedIdentifier() != null)
				.map(publication -> new SimpleCrossReference(Constants.PUBMED, publication.getPubMedIdentifier().toString(), null))
				.collect(Collectors.toList());
	}

	private static List<Author> getAuthors(DatabaseObject context) {
		final List<Publication> references = getReferences(context);
		return references.stream()
				.filter(LiteratureReference.class::isInstance)
				.map(LiteratureReference.class::cast)
				.map(publication -> String.format("%s et al. (%d)", publication.getAuthor().get(0).getSurname(), publication.getYear()))
				.map(SimpleAuthor::new)
				.collect(Collectors.toList());
	}

	private static List<Publication> getReferences(DatabaseObject context) {
		final List<Publication> references;
		if (context instanceof PhysicalEntity) {
			references = ((PhysicalEntity) context).getLiteratureReference();
		} else if (context instanceof ReactionLikeEvent)
			references = ((ReactionLikeEvent) context).getLiteratureReference();
		else references = Collections.emptyList();
		return references == null ? Collections.singletonList(Constants.REACTOME_PUBLICATION) : references;
	}

	private static List<Confidence> getConfidence(DatabaseObject context) {
		boolean isInferred = false;
		if (context instanceof ReactionLikeEvent) {
			isInferred = ((ReactionLikeEvent) context).getIsInferred();
		} else if (context instanceof PhysicalEntity)
			isInferred = ((PhysicalEntity) context).getInferredFrom() != null && !((PhysicalEntity) context).getInferredFrom().isEmpty();
		return Collections.singletonList(isInferred ? Constants.CONFIDENCE_INFERRED : Constants.CONFIDENCE_DEFAULT);
	}

	private static List<CrossReference> getComplexExpansion(Interaction interaction) {
		if (interaction.getType() == InteractionType.PHYSICAL)
			return Collections.singletonList(Constants.MATRIX_EXPANSION);
		else return null;
	}

}
