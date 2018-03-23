package org.reactome.server.tools.interaction.exporter.util;

import org.reactome.server.graph.domain.model.*;
import org.reactome.server.tools.interaction.exporter.EntityIdentifier;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IdentifierResolver {

	private static final String REACTOME = "reactome";
	private static final List<SubResolver> IDENTIFIER_RESOLVERS = Arrays.asList(
			new EntrezGeneResolver(),
			new EnsemblResolver()
	);

	public static List<EntityIdentifier> getIdentifiers(PhysicalEntity entity) {
		// (entity{stId})-[:crossReference]-(cr{databaseName, identifier})
		final List<EntityIdentifier> identifiers = new LinkedList<>();
		identifiers.add(new EntityIdentifier(REACTOME, entity.getStId()));
		if (entity.getCrossReference() != null)
			entity.getCrossReference().stream()
					.map(reference -> new EntityIdentifier(reference.getDatabaseName(), reference.getIdentifier()))
					.forEach(identifiers::add);
		// (entity{stId})-[:referenceEntity]-(re{databaseName,identifier,otherIdentifier[]})
		ReferenceEntity re = null;
		if (entity instanceof EntityWithAccessionedSequence) {
			re = ((EntityWithAccessionedSequence) entity).getReferenceEntity();
		} else if (entity instanceof SimpleEntity)
			re = ((SimpleEntity) entity).getReferenceEntity();
		if (re != null) {
			// (entity{stId})-[:referenceEntity]-(re{databaseName,identifier})
			final String id = re instanceof ReferenceIsoform && ((ReferenceIsoform) re).getVariantIdentifier() != null
					? ((ReferenceIsoform) re).getVariantIdentifier()
					: re.getIdentifier();
			identifiers.add(new EntityIdentifier(re.getDatabaseName(), id));
			if (re.getOtherIdentifier() != null) {
				// (entity{stId})-[:referenceEntity]-(re{otherIdentifier[]})
				re.getOtherIdentifier().stream()
						.map(s -> IDENTIFIER_RESOLVERS.stream()
								.map(resolver -> resolver.resolve(s))
								.filter(Objects::nonNull)
								.findAny()
								.orElse(null))
						.filter(Objects::nonNull)
						.forEach(identifiers::add);


			}
		}
		return identifiers;
	}

	private interface SubResolver {
		EntityIdentifier resolve(String identifier);
	}

	private static class EnsemblResolver implements SubResolver {
		private static final String database = null;
		private Pattern pattern = Pattern.compile("ENSG\\d+");

		@Override
		public EntityIdentifier resolve(String identifier) {
			final Matcher matcher = pattern.matcher(identifier);
			if (matcher.matches())
				return new EntityIdentifier(database, identifier);
			return null;
		}
	}

	private static class EntrezGeneResolver implements SubResolver {

		private static final String database = "entrezgene/locuslink";
		private final Pattern pattern = Pattern.compile("EntrezGene:(\\d+)");

		@Override
		public EntityIdentifier resolve(String identifier) {
			final Matcher matcher = pattern.matcher(identifier);
			if (matcher.matches())
				return new EntityIdentifier(database, matcher.group(1));
			return null;
		}
	}
}
