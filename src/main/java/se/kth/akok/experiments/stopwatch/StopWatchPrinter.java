package se.kth.akok.experiments.stopwatch;

import org.javasimon.Stopwatch;
import org.javasimon.utils.SimonUtils;

/**
 * Prints a java simon stopwatch.
 * 
 * @author Andreas Kokkalis <a.kokkalis@kth.se>
 * 
 */
public class StopWatchPrinter {
	/**
	 * Prints the results in the stdout.
	 * 
	 * @param stopwatch
	 */
	public static void printStopWatch(Stopwatch stopwatch) {
		System.out.println("\n=========================================================");
		final StringBuilder sb = new StringBuilder();
		sb.append("------StopwatchSample-----");
		if (stopwatch.getName() != null) {
			sb.append("\n name:\t").append(stopwatch.getName()).append("\n");
		}
		sb.append("\n total:\t\t\t").append(SimonUtils.presentNanoTime(stopwatch.getTotal()));
		sb.append("\n counter:\t\t").append(stopwatch.getCounter());
		sb.append("\n max:\t\t\t").append(SimonUtils.presentNanoTime(stopwatch.getMax()));
		sb.append("\n min:\t\t\t").append(SimonUtils.presentNanoTime(stopwatch.getMin()));
		sb.append("\n maxTimestamp:\t\t").append(SimonUtils.presentTimestamp(stopwatch.getMaxTimestamp()));
		sb.append("\n minTimestamp:\t\t").append(SimonUtils.presentTimestamp(stopwatch.getMaxTimestamp()));
		sb.append("\n active:\t\t").append(stopwatch.getActive());
		sb.append("\n maxActive:\t\t").append(stopwatch.getMaxActive());
		sb.append("\n maxActiveTimestamp:\t").append(SimonUtils.presentTimestamp(stopwatch.getMaxActiveTimestamp()));
		sb.append("\n last:\t\t\t").append(SimonUtils.presentNanoTime(stopwatch.getLast()));
		sb.append("\n mean:\t\t\t").append(SimonUtils.presentNanoTime((long) stopwatch.getMean()));
		sb.append("\n standardDeviation:\t").append(SimonUtils.presentNanoTime((long) stopwatch.getStandardDeviation()));
		sb.append("\n variance:\t\t").append(stopwatch.getVariance());
		sb.append("\n varianceN:\t\t").append(stopwatch.getVarianceN());
		System.out.println(sb.toString());
		System.out.println("=========================================================");
	}

	/**
	 * Returns the string of the stopwatche's results.
	 * 
	 * @param stopwatch
	 * @return
	 */
	public static String getStopWatch(Stopwatch stopwatch) {
		StringBuilder sb = new StringBuilder();
		sb.append("\n=========================================================");
		sb.append("------StopwatchSample-----");
		if (stopwatch.getName() != null) {
			sb.append("\n name:\t").append(stopwatch.getName()).append("\n");
		}
		sb.append("\n total:\t\t\t").append(SimonUtils.presentNanoTime(stopwatch.getTotal()));
		sb.append("\n counter:\t\t").append(stopwatch.getCounter());
		sb.append("\n max:\t\t\t").append(SimonUtils.presentNanoTime(stopwatch.getMax()));
		sb.append("\n min:\t\t\t").append(SimonUtils.presentNanoTime(stopwatch.getMin()));
		sb.append("\n maxTimestamp:\t\t").append(SimonUtils.presentTimestamp(stopwatch.getMaxTimestamp()));
		sb.append("\n minTimestamp:\t\t").append(SimonUtils.presentTimestamp(stopwatch.getMaxTimestamp()));
		sb.append("\n active:\t\t").append(stopwatch.getActive());
		sb.append("\n maxActive:\t\t").append(stopwatch.getMaxActive());
		sb.append("\n maxActiveTimestamp:\t").append(SimonUtils.presentTimestamp(stopwatch.getMaxActiveTimestamp()));
		sb.append("\n last:\t\t\t").append(SimonUtils.presentNanoTime(stopwatch.getLast()));
		sb.append("\n mean:\t\t\t").append(SimonUtils.presentNanoTime((long) stopwatch.getMean()));
		sb.append("\n standardDeviation:\t").append(SimonUtils.presentNanoTime((long) stopwatch.getStandardDeviation()));
		sb.append("\n variance:\t\t").append(stopwatch.getVariance());
		sb.append("\n varianceN:\t\t").append(stopwatch.getVarianceN());
		sb.append("=========================================================");
		return sb.toString();
	}
}
