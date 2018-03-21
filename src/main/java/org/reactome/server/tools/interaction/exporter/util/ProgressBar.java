package org.reactome.server.tools.interaction.exporter.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

public class ProgressBar {

	private final static DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
	private final TimerTask task = new TimerTask() {
		@Override
		public void run() {
			printProgress();
		}
	};
	private final int chunks;
	private long start;
	private String message;
	private double progress;
	private boolean started = false;
	private Timer timer = new Timer();

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
			timer.schedule(task, 250, 250);
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
		System.out.printf("\r%s %6.2f%% [", DATE_FORMAT.format(elapsed), progress * 100);
		for (int i = 0; i < completed; i++) System.out.print("=");
		int remaining = chunks - completed;
		if (remaining > 0) {
			remaining -= 1;
			System.out.print(">");
		}
		for (int i = 0; i < remaining; i++) System.out.print(" ");
		System.out.print("]");
		if (message != null) System.out.printf(" %s", message);
		System.out.flush();
	}

	public void flush() {
		task.run();
		timer.cancel();
	}
}
