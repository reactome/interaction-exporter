package org.reactome.server.tools.interaction.exporter.writer;

import org.reactome.server.graph.domain.model.*;
import org.reactome.server.tools.interaction.exporter.Interaction;
import org.reactome.server.tools.interaction.exporter.InteractionType;
import org.reactome.server.tools.interaction.exporter.psi.SimpleAlias;
import org.reactome.server.tools.interaction.exporter.psi.SimpleAuthor;
import org.reactome.server.tools.interaction.exporter.psi.SimpleCrossReference;
import org.reactome.server.tools.interaction.exporter.psi.SimpleFeature;
import org.reactome.server.tools.interaction.exporter.util.Constants;
import org.reactome.server.tools.interaction.exporter.util.IdentifierResolver;
import psidev.psi.mi.tab.PsimiTabWriter;
import psidev.psi.mi.tab.model.*;
import psidev.psi.mi.tab.model.builder.PsimiTabVersion;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Tab27Writer implements InteractionWriter {


	private static final Logger logger = Logger.getLogger("interaction-exporter");
	private final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");

	private final Writer output;
	private final PsimiTabWriter writer;

	public Tab27Writer(OutputStream output) throws IOException {
		this.output = new OutputStreamWriter(output);
		this.writer = new PsimiTabWriter(PsimiTabVersion.v2_7);
		this.writer.writeMitabHeader(output);
	}

	@Override
	public void write(Interaction interaction) throws IOException {
		CrossReference aRole;
		CrossReference bRole;
		GO_MolecularFunction activity = null;
		boolean negative = false;
		if (interaction.getType() != InteractionType.PHYSICAL) {
			aRole = Constants.ENZYME;
			bRole = Constants.ENZYME_TARGET;
			ReactionLikeEvent reaction = (ReactionLikeEvent) interaction.getContext();
			activity = reaction.getCatalystActivity().get(0).getActivity();
			negative = activity.getNegativelyRegulate() != null && !activity.getNegativelyRegulate().isEmpty();
		} else {
			aRole = Constants.UNSPECIFIED_ROLE;
			bRole = Constants.UNSPECIFIED_ROLE;
		}
		final Interactor a = createInteractor(interaction.getA(), interaction.getAst(), aRole, activity);
		final Interactor b = createInteractor(interaction.getB(), interaction.getBst(), bRole, null);
		final BinaryInteraction psiInteraction = new BinaryInteractionImpl(a, b);
		configure(psiInteraction, interaction, negative);
		// Sort by primary identifier
		if (a.getIdentifiers().get(0).getIdentifier().compareTo(b.getIdentifiers().get(0).getIdentifier()) > 0)
			psiInteraction.flip();
		writer.write(psiInteraction, output);
	}

	private Interactor createInteractor(PhysicalEntity entity, Long stoichometry, CrossReference role, GO_MolecularFunction activity) {
		final Interactor interactor = new Interactor();
		final List<CrossReference> identifiers = IdentifierResolver.getIdentifiers(entity);
		final CrossReference primaryIdentifier = primaryIdentifier(entity, identifiers);
		identifiers.remove(primaryIdentifier);
		interactor.setIdentifiers(Collections.singletonList(primaryIdentifier));
		interactor.setAlternativeIdentifiers(new LinkedList<>(identifiers));
		interactor.setAliases(getAlias(entity));
		interactor.setStoichiometry(Collections.singletonList(stoichometry.intValue()));
		interactor.setBiologicalRoles(Collections.singletonList(role));
		interactor.setExperimentalRoles(Collections.singletonList(Constants.UNSPECIFIED_ROLE));
		interactor.setOrganism(getOrganism(entity));
		interactor.setParticipantIdentificationMethods(Collections.singletonList(Constants.INFERRED_BY_CURATOR));
		interactor.setInteractorTypes(Collections.singletonList(getInteractorType(entity)));
		interactor.setXrefs(getXrefs(entity, activity));
		interactor.setFeatures(getFeatures(entity));
//		interactor.setAnnotations();
//		interactor.setChecksums();
		return interactor;
	}

	private CrossReference getInteractorType(PhysicalEntity entity) {
		if (entity instanceof EntityWithAccessionedSequence)
			switch (((EntityWithAccessionedSequence) entity).getReferenceType()) {
				case "ReferenceGeneProduct":
				case "ReferenceIsoform":
					return Constants.PROTEIN;
				case "ReferenceRNASequence":
					return Constants.RNA;
				case "ReferenceDNASequence":
					return Constants.DNA;
				default:
					logger.warning("Unknown reference type " + ((EntityWithAccessionedSequence) entity).getReferenceType());
			}
		else if (entity instanceof Complex)
			return Constants.COMPLEX;
		else if (entity instanceof Polymer)
			return Constants.BIOPOLYMER;
		else if (entity instanceof SimpleEntity)
			return Constants.SMALL_MOLECULE;
		return null;
	}

	private Organism getOrganism(DatabaseObject entity) {
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

	private List<Alias> getAlias(PhysicalEntity entity) {
		if (entity.getName() != null)
			return entity.getName().stream()
					.filter(name -> !name.contains("\n") && !name.contains(":"))
					.map(name -> new SimpleAlias(Constants.REACTOME, name, "name"))
					.collect(Collectors.toList());
		return Collections.emptyList();
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

	private List<CrossReference> getXrefs(DatabaseObject entity, GO_MolecularFunction activity) {
		// molecular function
		// cellular components
		//
		final List<CrossReference> references = new LinkedList<>();
		if (activity != null)
			references.add(new SimpleCrossReference(activity.getDatabaseName(), activity.getAccession(), activity.getName()));
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

	private List<Feature> getFeatures(PhysicalEntity entity) {
		final List<Feature> features = new LinkedList<>();
		if (entity instanceof EntityWithAccessionedSequence) {
			final List<AbstractModifiedResidue> hasModifiedResidue = ((EntityWithAccessionedSequence) entity).getHasModifiedResidue();
			if (hasModifiedResidue != null) {
				hasModifiedResidue.stream()
						.filter(TranslationalModification.class::isInstance)
						.map(TranslationalModification.class::cast)
						.map(SimpleFeature::new)
						.forEach(features::add);
			}
		}
		return features;
	}

	private void configure(BinaryInteraction psiInteraction, Interaction interaction, boolean negative) {
		psiInteraction.setCreationDate(Collections.singletonList(createDate(interaction.getContext().getCreated())));
		psiInteraction.setUpdateDate(Collections.singletonList(createDate(interaction.getContext().getModified())));
		psiInteraction.setAuthors(getAuthors(interaction.getContext()));
		psiInteraction.setHostOrganism(getOrganism(interaction.getContext()));
		psiInteraction.setPublications(getPublications(interaction.getContext()));
		psiInteraction.setComplexExpansion(getComplexExpansion(interaction));
		psiInteraction.setDetectionMethods(Collections.singletonList(Constants.INFERRED_BY_CURATOR));
		psiInteraction.setSourceDatabases(Collections.singletonList(Constants.REACTOME_DATABASE));
		psiInteraction.setInteractionTypes(Collections.singletonList(interaction.getType()));
		psiInteraction.setConfidenceValues(getConfidence(interaction.getContext()));
		psiInteraction.setInteractionAcs(Collections.singletonList(new SimpleCrossReference(Constants.REACTOME, interaction.getContext().getStId())));
		psiInteraction.setNegativeInteraction(negative);
		psiInteraction.setXrefs(getXrefs(interaction.getContext(), null));
//		psiInteraction.setChecksums();
//		psiInteraction.setParameters();
//		psiInteraction.setAnnotations();
	}

	private List<CrossReference> getPublications(DatabaseObject context) {
		final List<Publication> references;
		if (context instanceof PhysicalEntity) {
			references = ((PhysicalEntity) context).getLiteratureReference();
		} else if (context instanceof ReactionLikeEvent)
			references = ((ReactionLikeEvent) context).getLiteratureReference();
		else references = Collections.emptyList();
		return references.stream()
				.filter(LiteratureReference.class::isInstance)
				.map(LiteratureReference.class::cast)
				.map(publication -> new SimpleCrossReference(Constants.PUBMED, publication.getPubMedIdentifier().toString(), null))
				.collect(Collectors.toList());
	}

	private List<Author> getAuthors(DatabaseObject context) {
		final List<Publication> references;
		if (context instanceof PhysicalEntity) {
			references = ((PhysicalEntity) context).getLiteratureReference();
		} else if (context instanceof ReactionLikeEvent)
			references = ((ReactionLikeEvent) context).getLiteratureReference();
		else references = Collections.emptyList();
		return references.stream()
				.filter(LiteratureReference.class::isInstance)
				.map(LiteratureReference.class::cast)
				.map(publication -> String.format("%s et al. (%d)", publication.getAuthor().get(0).getSurname(), publication.getYear()))
				.map(SimpleAuthor::new)
				.collect(Collectors.toList());
	}

	private List<Confidence> getConfidence(DatabaseObject context) {
		boolean isInferred = false;
		if (context instanceof ReactionLikeEvent) {
			isInferred = ((ReactionLikeEvent) context).getIsInferred();
		} else if (context instanceof PhysicalEntity)
			isInferred = ((PhysicalEntity) context).getInferredFrom() != null && !((PhysicalEntity) context).getInferredFrom().isEmpty();
		return Collections.singletonList(isInferred ? Constants.CONFIDENCE_INFERRED : Constants.CONFIDENCE_DEFAULT);
	}

	private List<CrossReference> getComplexExpansion(Interaction interaction) {
		if (interaction.getType() == InteractionType.PHYSICAL)
			return Collections.singletonList(Constants.MATRIX_EXPANSION);
		else return null;
	}

	private Date createDate(InstanceEdit instanceEdit) {
		try {
			return DATE_FORMAT.parse(instanceEdit.getDateTime());
		} catch (ParseException e) {
			logger.warning("Date format not valid: " + instanceEdit.getDateTime());
			return null;
		}
	}
}
