package org.reactome.server.tools.interaction.exporter.util;

import java.io.PrintStream;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Asynchronous progress bar. A daemon is started at the first call to {@link ProgressBar#setProgress(double, String)}.
 * Every 250 milliseconds the bar is updated. Daemon stops automatically if progress reaches 100% or by calling
 * {@link ProgressBar#clear()}.
 */
public class ProgressBar {

	private static final int MILLISECONDS_PER_HOUR = 3600000;
	private static final int MILLISECONDS_PER_MINUTE = 60000;
	public static final int PERIOD = 250;
	private final int chunks;
	private long start;
	private String message;
	private double progress;
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
		if (timer == null) {
			timer = new Timer(true);
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					printProgress();
				}
			}, 250, PERIOD);
			start = System.nanoTime();
		}
	}

	private void printProgress() {
		int completed = (int) (progress * chunks);
		final long elapsed = (System.nanoTime() - start) / 1000000L;
		printProgress(progress, message, completed, elapsed);
		if (progress >= 1) clear();

	}

	private void printProgress(double progress, String message, int completed, long elapsed) {
		printStream.printf("\r%s %6.2f%% [", formatTime(elapsed), progress * 100);
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

	public static String formatTime(long millis) {
		final long hours = millis / MILLISECONDS_PER_HOUR;  // 60 * 60 * 1000
		millis -= hours * MILLISECONDS_PER_HOUR;
		final long minutes = millis / MILLISECONDS_PER_MINUTE;
		millis -= minutes * MILLISECONDS_PER_MINUTE;
		final long seconds = millis / 1000;
		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}

	public void clear() {
		if (timer != null) {
			timer.cancel();
			timer = null;
			printProgress();
			printStream.println();
		}
	}

}
