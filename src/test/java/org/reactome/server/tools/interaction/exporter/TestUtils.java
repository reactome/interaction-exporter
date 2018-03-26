package org.reactome.server.tools.interaction.exporter;

import org.junit.jupiter.api.Assertions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TestUtils {

	public static void assertEquals(InputStream expected, InputStream result) {
		try {
			final BufferedReader expectedReader = new BufferedReader(new InputStreamReader(expected));
			final BufferedReader resultReader = new BufferedReader(new InputStreamReader(result));
			int n = 1;
			String expectedLine;
			String resultLine;
			while ((expectedLine = expectedReader.readLine()) != null) {
				resultLine = resultReader.readLine();
				if (resultLine == null)
					Assertions.fail("Expected more lines at " + n);
				if (!resultLine.equals(expectedLine)) {
					final String message = String.format("At line %d%n - Expected:[%s]%n - Actual  :[%s]%n", n, expectedLine, resultLine);
					Assertions.fail(message);
				}
			}
			if (resultReader.readLine() != null)
				Assertions.fail("No more lines expected at " + n);
		} catch (IOException e) {
			e.printStackTrace();
			Assertions.fail(e.getMessage());
		}
	}

}
