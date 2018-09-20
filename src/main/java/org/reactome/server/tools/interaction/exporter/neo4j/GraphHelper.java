package org.reactome.server.tools.interaction.exporter.neo4j;

import org.apache.commons.collections.map.LRUMap;
import org.reactome.server.graph.exception.CustomQueryException;
import org.reactome.server.graph.service.AdvancedDatabaseObjectService;
import org.reactome.server.graph.utils.ReactomeGraphCore;

import java.util.Collections;
import java.util.Map;

public class GraphHelper {

	private static final AdvancedDatabaseObjectService SERVICE = ReactomeGraphCore.getService(AdvancedDatabaseObjectService.class);
	private static final String INTERACTOR_QUERY = "" +
			"MATCH (e:DatabaseObject{stId:{stId}}) " +
			"OPTIONAL MATCH (e)-[:species]->(species) " +
			"WITH e, collect(species) AS species " +
			"OPTIONAL MATCH (e)-[:referenceEntity]->(re)" +
			"OPTIONAL MATCH (re)-[:crossReference]->(recr)" +
			"WITH e, re, species, collect(recr) AS cr " +
			"OPTIONAL MATCH (e)-[:crossReference]->(ecr) " +
			"WITH e, re, species, cr + collect(ecr) AS cr " +
			"OPTIONAL MATCH (e)-[:compartment]->(comp) " +
			"WITH e, re, species, cr, collect(comp) AS comp " +
			"OPTIONAL MATCH (e)-[:hasModifiedResidue]-(mr)-[:psiMod]->(psi) " +
			"RETURN e.schemaClass AS schemaClass, e.referenceType AS referenceType," +
			"" + // Identifiers: (reactome:stId, re(db,id), cr(db,id), re.otherIdentifier, re.otherIdentifiers)
			"    [{database:\"reactome\", identifier:e.stId}] " +
			"    + CASE WHEN cr IS NULL" +
			"    THEN []" +
			"    ELSE [ref IN cr | {database:ref.databaseName, identifier:ref.identifier}] END " +
			"    + CASE WHEN re IS NULL" +
			"    THEN [] " +
			"    ELSE [{database:re.databaseName, identifier:re.identifier}]" +
			"        + CASE WHEN re.otherIdentifier IS NULL " +
			"        THEN [] " +
			"        ELSE [id IN re.otherIdentifier WHERE id STARTS WITH \"ENSG\" | {database:\"ENSEMBL\", identifier:id}]  " +
			"            + [id IN re.otherIdentifier WHERE id STARTS WITH \"EntrezGene:\" | {database:\"entrezgene/locuslink\", identifier:split(id, \":\")[1]}]" +
			"        END" +
			"    END AS identifiers, " +
			"" + // Alias (entity.name)
			"    [name IN e.name WHERE NOT name CONTAINS \"\\n\" AND NOT name CONTAINS \":\" | {dbSource:\"reactome\", name:name, aliasType:\"name\"}] AS aliases, " +
			"" +   // features (psi)
			"    CASE WHEN psi IS NULL" +
			"    THEN []" +
			"    ELSE collect({featureType:psi.name[0], text:\"MOD:\" + psi.identifier,range:CASE WHEN mr.coordinate IS NULL THEN [] ELSE [mr.coordinate + \"-\" + mr.coordinate] END}) " +
			"    END AS features, " +
			"" +  // species
			"    [sp IN species | {database:\"taxid\", identifier:sp.taxId, text:sp.name[0]}] AS species, " +
			"" + // crossReferences
			"    [c IN comp | {database:c.databaseName, identifier:c.accession, text:c.name}] AS crossReferences";
	private static final String CONTEXT_QUERY = "" +
			"MATCH (context:DatabaseObject{stId:{stId}}) " +
			" OPTIONAL MATCH (context)-[:species]->(species) " +
			" WITH context, collect(species) AS species " +
			" OPTIONAL MATCH (context)-[:literatureReference]-(publication1:LiteratureReference) " +
			" OPTIONAL MATCH (context)-[:output]-(:ReactionLikeEvent)-[:literatureReference]-(publication2:LiteratureReference) " +
			" WITH context, species, collect(publication1) + collect(publication2) AS publications " +
			" OPTIONAL MATCH (context)-[:compartment]->(compartment) " +
			" WITH context, species, publications, collect(compartment) AS compartments " +
			" OPTIONAL MATCH (context)-[:catalystActivity]->(ca)-[:activity]-(activity)" +
			" OPTIONAL MATCH (context)<-[:inferredTo]-(inferred) " +
			" OPTIONAL MATCH (context)<-[:created]-(created:InstanceEdit) " +
			" OPTIONAL MATCH (context)<-[:modified]-(modified:InstanceEdit) " +
			" RETURN created.dateTime AS created, modified.dateTime AS modified, " +
			"    CASE WHEN species IS NULL" +
			"        THEN []" +
			"        ELSE [sp IN species | {database:\"taxid\", identifier:sp.taxId, text:sp.name[0]}]" +
			"    END AS species," +
			"    publications, context.schemaClass as schemaClass," +
			"    inferred IS NOT NULL AS inferred," +
			"    CASE WHEN ca IS NULL" +
			"        THEN []" +
			"        ELSE [{database:activity.databaseName, identifier:activity.accession, text:activity.name}]" +
			"    END" +
			"    + [c IN compartments | {database:c.databaseName, identifier:c.accession, text:c.name}] AS crossReferences";
	private static Map INTERACTOR_CACHE = new LRUMap(50);
	private static Map CONTEXT_CACHE = new LRUMap(10);


	public static InteractorResult interactor(String stId) {
		return (InteractorResult) INTERACTOR_CACHE.computeIfAbsent(stId, GraphHelper::queryInteractor);
	}

	private static InteractorResult queryInteractor(Object stId) {
		try {
			return SERVICE.getCustomQueryResult(InteractorResult.class, INTERACTOR_QUERY, Collections.singletonMap("stId", stId));
		} catch (CustomQueryException e) {

			// TODO: 10/04/18 create exception Database
			e.printStackTrace();
		}
		return null;
	}

	public static ContextResult context(String stId) {
		return (ContextResult) CONTEXT_CACHE.computeIfAbsent(stId, GraphHelper::queryContext);
	}

	private static ContextResult queryContext(Object stableIdentifier) {
		try {
			return SERVICE.getCustomQueryResult(ContextResult.class, CONTEXT_QUERY, Collections.singletonMap("stId", stableIdentifier));
		} catch (CustomQueryException e) {
			e.printStackTrace();
		}
		return null;
	}
}
