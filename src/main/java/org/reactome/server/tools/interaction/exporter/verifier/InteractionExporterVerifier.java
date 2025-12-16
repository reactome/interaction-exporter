package org.reactome.server.tools.interaction.exporter.verifier;

import org.reactome.release.verifier.DefaultVerifier;
import org.reactome.release.verifier.Verifier;

import java.io.IOException;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 12/14/2025
 */
public class InteractionExporterVerifier {

	public static void main(String[] args) throws IOException {
		Verifier verifier = new DefaultVerifier("interactions_exporter");
		verifier.parseCommandLineArgs(args);
		verifier.run();
	}
}
