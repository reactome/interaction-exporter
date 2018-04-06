package org.reactome.server.tools.interaction.exporter.util;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

public class ProgressBar {

	private final static DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
	private final int chunks;
	private long start;
	private String message;
	private double progress;
	private boolean started = false;
	private Timer timer;
	private PrintStream printStream = System.out;

	public ProgressBar() {
		this.chunks = 50;
	}

	public ProgressBar(int chunks) {
		this.chunks = chunks;
	}

	/**
	 * @param progress between 0 and 1
	 */
	public synchronized void setProgress(double progress, String message) {
		this.message = message;
		this.progress = progress;
		if (!started) {
			started = true;
			timer = new Timer(true);
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					printProgress();
				}
			}, 250, 250);
			start = System.nanoTime();
		}
	}

	private void printProgress() {
		int completed = (int) (progress * chunks);
		final long elapsed = (System.nanoTime() - start) / 1000000;
		printProgress(progress, message, completed, elapsed);
		if (progress >= 1) timer.cancel();

	}

	private void printProgress(double progress, String message, int completed, long elapsed) {
		printStream.printf("\r%s %6.2f%% [", DATE_FORMAT.format(elapsed), progress * 100);
		for (int i = 0; i < completed; i++) printStream.print("=");
		int remaining = chunks - completed;
		if (remaining > 0) {
			remaining -= 1;
			printStream.print(">");
		}
		for (int i = 0; i < remaining; i++) printStream.print(" ");
		printStream.print("]");
		if (message != null) printStream.printf(" %s", message);
		printStream.flush();
	}

	public void restart() {
		if (timer != null){
			timer.cancel();
			printProgress();
			printStream.println();
		}
		started = false;
	}
}
