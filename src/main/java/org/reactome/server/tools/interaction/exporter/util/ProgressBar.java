package org.reactome.server.tools.interaction.exporter.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ProgressBar {

	private final int chunks;
	private int last = -1;
	private long start;
	private final static DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");

	public ProgressBar() {
		this.chunks = 50;
	}

	public ProgressBar(int chunks) {
		this.chunks = chunks;
	}

	/**
	 * @param progress between 0 and 1
	 */
	public void setProgress(double progress, String message) {
		int completed = (int) (progress * chunks);
		final long elapsed = (System.nanoTime() - start) / 1000000;
		if (elapsed % 3000 > 2990 && completed <= last) return;
		if (last < 0) {
			start = System.nanoTime();
		}
		printProgresss(progress, message, completed, elapsed);
		last = completed;
		if (progress >= 1) System.out.println();
	}

	private void printProgresss(double progress, String message, int completed, long elapsed) {
		System.out.printf("\r%s %6.2f%% [", DATE_FORMAT.format(elapsed), progress * 100);
		for (int i = 0; i < completed; i++) System.out.print("=");
		int remaining = chunks - completed;
		if (remaining > 1) {
			remaining -= 1;
			System.out.print(">");
		}
		for (int i = 0; i < remaining; i++) System.out.print(" ");
		System.out.print("]");
		if (message != null) System.out.printf(" %s", message);
		System.out.flush();
	}

	public void restart() {
		last = -1;
	}
}
