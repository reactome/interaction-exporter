package org.reactome.server.tools.interaction.exporter.neo4j;

import org.apache.commons.collections.map.LRUMap;
import org.reactome.server.graph.exception.CustomQueryException;
import org.reactome.server.graph.service.AdvancedDatabaseObjectService;
import org.reactome.server.graph.utils.ReactomeGraphCore;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

public class GraphHelper {

	private static final AdvancedDatabaseObjectService SERVICE = ReactomeGraphCore.getService(AdvancedDatabaseObjectService.class);
	private static final String INTERACTOR_QUERY = "" +
			"MATCH (e:DatabaseObject{stId:{stId}}) " +
			"OPTIONAL MATCH (e)-[:species]->(species) " +
			"WITH e, collect(species) AS species " +
			"OPTIONAL MATCH (e)-[:referenceEntity]->(re) " +
			"OPTIONAL MATCH (re)-[:crossReference]->(recr) " +
			"WITH e, re, species, collect(recr) AS cr " +
			"OPTIONAL MATCH (e)-[:crossReference]->(ecr) " +
			"WITH e, re, species, cr + collect(ecr) AS cr " +
			"OPTIONAL MATCH (e)-[:compartment]->(comp) " +
			"WITH e, re, species, cr, collect(comp) AS comp " +
			"OPTIONAL MATCH (e)-[:hasModifiedResidue]-(mr)-[:psiMod]->(psi) " +
			"RETURN e.schemaClass AS schemaClass, e.referenceType AS referenceType," +
			"" + // Identifiers: (reactome:stId, re(db,id), cr(db,id), re.otherIdentifier, re.otherIdentifiers)
			"    [{database:\"reactome\", identifier:e.stId}] + " +
			"    CASE " +
			"	   WHEN cr IS NULL THEN []" +
			"      ELSE [ref IN cr | {database:ref.databaseName, identifier:ref.identifier}] " +
			"    END + " +
			"    CASE " +
			"      WHEN re IS NULL THEN [] " +
			"      ELSE [{database:re.databaseName, identifier:re.identifier}] + " +
			"           CASE WHEN re.otherIdentifier IS NULL THEN [] " +
			"           ELSE [id IN re.otherIdentifier WHERE id STARTS WITH \"ENSG\" | {database:\"ENSEMBL\", identifier:id}] + [id IN re.otherIdentifier WHERE id STARTS WITH \"EntrezGene:\" | {database:\"entrezgene/locuslink\", identifier:split(id, \":\")[1]}]" +
			"      END " +
			"    END AS identifiers, " +
			     // Alias (entity.name)
			"    [name IN e.name WHERE NOT name CONTAINS \"\\n\" AND NOT name CONTAINS \":\" | {dbSource:\"reactome\", name:name, aliasType:\"name\"}] AS aliases, " +
			     // features (psi)
			"    CASE " +
			"      WHEN psi IS NULL THEN []" +
			"      ELSE collect({featureType:psi.name[0], text:\"MOD:\" + psi.identifier, range: CASE WHEN mr.coordinate IS NULL THEN [\"?-?\"] ELSE [mr.coordinate + \"-\" + mr.coordinate] END}) " +
			"    END + " +
			"    CASE " +
			"      WHEN e.startCoordinate IS NULL AND e.endCoordinate IS NULL " +
			"      THEN [] ELSE [{featureType:\"sufficient to bind\", range: CASE WHEN e.startCoordinate IS NULL THEN \"?\" WHEN e.startCoordinate < 0 THEN \"n\" ELSE e.startCoordinate END + \"-\" + CASE WHEN e.endCoordinate IS NULL THEN \"?\" WHEN e.endCoordinate < 0 THEN \"c\" ELSE e.endCoordinate END}] " +
			"    END AS features, " +
				 // species
			"    [sp IN species | {database:\"taxid\", identifier:sp.taxId, text:sp.name[0]}] AS species, " +
				 // crossReferences
			"    [c IN comp | {database:c.databaseName, identifier:c.accession, text:c.name}] AS crossReferences";
	private static final String CONTEXT_QUERY = "" +
			"MATCH (context:DatabaseObject{stId:{stId}}) " +
			"OPTIONAL MATCH (p1:Pathway)-[:hasEvent]->(context) WHERE (context:ReactionLikeEvent) " +
			"OPTIONAL MATCH (p2:Pathway)-[:hasEvent]->(:ReactionLikeEvent)-[:input|output|catalystActivity|physicalEntity|regulatedBy|regulator|hasComponent|hasMember|repeatedUnit*]->(context) WHERE (context:PhysicalEntity) " +
			"WITH context, COLLECT(DISTINCT p1.stId) + COLLECT(DISTINCT p2.stId) AS ps " +
			"OPTIONAL MATCH (context)-[:species]->(species) " +
			"WITH context, ps, collect(species) AS species " +
			"OPTIONAL MATCH (context)-[:literatureReference]-(publication1:LiteratureReference) " +
			"OPTIONAL MATCH (context)-[:output]-(:ReactionLikeEvent)-[:literatureReference]-(publication2:LiteratureReference) " +
			"WITH context, ps, species, collect(publication1) + collect(publication2) AS publications " +
			"OPTIONAL MATCH (context)-[:compartment]->(compartment) " +
			"WITH context, ps, species, publications, collect(compartment) AS compartments " +
			"OPTIONAL MATCH (context)-[:catalystActivity]->(ca)-[:activity]-(activity)" +
			"OPTIONAL MATCH (context)<-[:inferredTo]-(inferred) " +
			"OPTIONAL MATCH (context)<-[:created]-(created:InstanceEdit) " +
			"OPTIONAL MATCH (context)<-[:modified]-(modified:InstanceEdit) " +
			"RETURN created.dateTime AS created, modified.dateTime AS modified, " +
			"   ps AS pathways, " +
			"   CASE WHEN species IS NULL" +
			"       THEN []" +
			"       ELSE [sp IN species | {database:\"taxid\", identifier:sp.taxId, text:sp.name[0]}]" +
			"   END AS species," +
			"   publications, context.schemaClass as schemaClass," +
			"   inferred IS NOT NULL AS inferred," +
			"   CASE WHEN ca IS NULL" +
			"       THEN []" +
			"       ELSE [{database:activity.databaseName, identifier:activity.accession, text:activity.name}]" +
			"   END" +
			"   + [c IN compartments | {database:c.databaseName, identifier:c.accession, text:c.name}] AS crossReferences";
	private static Map INTERACTOR_CACHE = new LRUMap(50);
	private static Map CONTEXT_CACHE = new LRUMap(10);


	/**
	 * Performs a query to the graph database to retrieve the information of an interactor.
	 *
	 * @param stId the interactor stId
	 * @return an InteractorResult or null if the query failed
	 */
	public static InteractorResult queryInteractor(String stId) {
		final Object result = INTERACTOR_CACHE.get(stId);
		if (result != null) return (InteractorResult) result;
		try {
			final InteractorResult interactorResult = SERVICE.getCustomQueryResult(InteractorResult.class, INTERACTOR_QUERY, Collections.singletonMap("stId", stId));
			INTERACTOR_CACHE.put(stId, interactorResult);
			return interactorResult;
		} catch (CustomQueryException e) {
			LoggerFactory.getLogger("interaction-exporter").error("Query error for object " + stId, e);
		}
		return null;
	}

	/**
	 * Performs a query to the graph database to retrieve the information of a context.
	 *
	 * @param stId the context stable identifier
	 * @return a ContextResult object or null if query failed
	 */
	public static ContextResult queryContext(String stId) {
		final Object result = CONTEXT_CACHE.get(stId);
		if (result != null) return (ContextResult) result;
		try {
			final ContextResult contextResult = SERVICE.getCustomQueryResult(ContextResult.class, CONTEXT_QUERY, Collections.singletonMap("stId", stId));
			CONTEXT_CACHE.put(stId, contextResult);
			return contextResult;
		} catch (CustomQueryException e) {
			LoggerFactory.getLogger("interaction-exporter").error("Query error for object " + stId, e);
		}
		return null;
	}

}
