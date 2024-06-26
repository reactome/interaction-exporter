package org.reactome.server.tools.interaction.exporter.psi;

import org.reactome.server.tools.interaction.exporter.neo4j.ContextResult;
import org.reactome.server.tools.interaction.exporter.neo4j.GraphHelper;
import org.reactome.server.tools.interaction.exporter.neo4j.InteractorResult;
import org.reactome.server.tools.interaction.exporter.util.Constants;
import psidev.psi.mi.tab.model.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class InteractionFactory {

	private static final List<String> validDatabases = Arrays.asList("ChEBI",
			"chembl", "EMBL", "ENSEMBL", "entrezgene/locuslink", "go", "IntAct",
			"pdbe", "psi-mi", "pubmed", "reactome", "refseq", "uniprotkb");

	public static Interactor createInteractor(org.reactome.server.tools.interaction.exporter.model.Interactor interactor) {
		final InteractorResult result = GraphHelper.queryInteractor(interactor.getEntity().getStId());
		if (result == null)
			throw new NullPointerException("No database entry for " + interactor);
		final Interactor psiInteractor = new Interactor();
		final List<CrossReference> identifiers = new LinkedList<>(result.getIdentifiers())
				.stream().filter(o -> validDatabases.contains(o.getDatabase()))
				.collect(Collectors.toList());
		final CrossReference primaryIdentifier = primaryIdentifier(result.getSchemaClass(), identifiers);
		identifiers.remove(primaryIdentifier);
		psiInteractor.setIdentifiers(Collections.singletonList(primaryIdentifier));
		psiInteractor.setAlternativeIdentifiers(identifiers);
		psiInteractor.setAliases(result.getAliases());
		psiInteractor.setFeatures(result.getFeatures());
		psiInteractor.setOrganism(new OrganismImpl(result.getSpecies()));
		psiInteractor.setXrefs(result.getCrossReferences());
		psiInteractor.setExperimentalRoles(Collections.singletonList(Constants.UNSPECIFIED_ROLE));
		psiInteractor.setParticipantIdentificationMethods(Collections.singletonList(Constants.INFERRED_BY_CURATOR));
		psiInteractor.setInteractorTypes(Collections.singletonList(getInteractorType(result.getSchemaClass(), result.getReferenceType())));
		psiInteractor.setStoichiometry(Collections.singletonList(interactor.getStoichiometry().intValue()));
		psiInteractor.setBiologicalRoles(Collections.singletonList(interactor.getBiologicalRole()));

		return psiInteractor;
	}

	public static CrossReference primaryIdentifier(String schemaClass, List<CrossReference> identifiers) {
		if (schemaClass.equals("EntityWithAccessionedSequence")) {
			// uniprot
			final CrossReference uniprot = identifiers.stream()
					.filter(reference -> reference.getDatabase() != null)
					.filter(reference -> reference.getDatabase().equalsIgnoreCase("uniprotkb"))
					.findFirst().orElse(null);
			if (uniprot != null)
				return uniprot;
		} else if (schemaClass.equals("SimpleEntity")) {
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

	private static CrossReference getInteractorType(String schemaClass, String referenceType) {
		switch (schemaClass) {
			case "EntityWithAccessionedSequence":
				switch (referenceType) {
					case "ReferenceGeneProduct":
					case "ReferenceIsoform":
					case "ReferenceMolecule":
						return Constants.PROTEIN;
					case "ReferenceRNASequence":
						return Constants.RNA;
					case "ReferenceDNASequence":
						return Constants.DNA;
				}
			case "Complex":
				return Constants.COMPLEX;
			case "Polymer":
				return Constants.BIOPOLYMER;
			case "SimpleEntity":
				return Constants.SMALL_MOLECULE;
			default:
				return Constants.UNKNOWN_PARTICIPANT;
			//		"GenomeEncodedEntity"
			//		"ChemicalDrug"
			//		"OtherEntity"
		}
	}

	public static void configureContext(BinaryInteraction psiInteraction, String stId) {
		final ContextResult result = GraphHelper.queryContext(stId);
		psiInteraction.setCreationDate(singletonOrNull(result.getCreated()));
		psiInteraction.setUpdateDate(singletonOrNull(result.getModified()));
		psiInteraction.setAuthors(result.getAuthors());
		psiInteraction.setHostOrganism(new OrganismImpl(result.getSpecies()));
		psiInteraction.setPublications(result.getPublications());
		psiInteraction.setComplexExpansion(singletonOrNull(result.getComplexExpansion()));
		psiInteraction.setDetectionMethods(Collections.singletonList(Constants.INFERRED_BY_CURATOR));
		psiInteraction.setSourceDatabases(Collections.singletonList(Constants.REACTOME_DATABASE));
		psiInteraction.setInteractionAcs(Collections.singletonList(new SimpleCrossReference(Constants.REACTOME, stId)));
		psiInteraction.setConfidenceValues(Collections.singletonList(getConfidence(result.getInferred())));
		psiInteraction.setXrefs(result.getCrossReferences());
		psiInteraction.setAnnotations(result.getPathwayAnnotations());
	}

	private static <T>  List<T> singletonOrNull(T object) {
		return object == null
				? null
				: Collections.singletonList(object);
	}

	private static Confidence getConfidence(boolean isInferred) {
		return isInferred
				? Constants.CONFIDENCE_INFERRED
				: Constants.CONFIDENCE_DEFAULT;
	}
}
