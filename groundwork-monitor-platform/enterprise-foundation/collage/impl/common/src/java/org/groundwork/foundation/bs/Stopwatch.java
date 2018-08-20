/*
 * Collage - The ultimate data integration framework.
 * Copyright (C) 2004-2007  GroundWork Open Source Solutions info@groundworkopensource.com

 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of version 2 of the GNU General Public License
 *     as published by the Free Software Foundation.

 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.

 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.groundwork.foundation.bs;

/** Time the execution of any block of code. */
public final class Stopwatch {

	private static Stopwatch stopwatch;

	private Stopwatch() {

	}

	public static Stopwatch getInstance() {
		if (stopwatch == null) {
			stopwatch = new Stopwatch();
		}
		return stopwatch;
	}

	/**
	 * Start the stopwatch.
	 * 
	 * @throws IllegalStateException
	 *             if the stopwatch is already running.
	 */
	public void start() {
		if (fIsRunning) {
			throw new IllegalStateException(
					"Must stop before calling start again.");
		}
		// reset both start and stop
		fStart = System.currentTimeMillis();
		fStop = 0;
		fIsRunning = true;
		fHasBeenUsedOnce = true;
	}

	/**
	 * Stop the stopwatch.
	 * 
	 * @throws IllegalStateException
	 *             if the stopwatch is not already running.
	 */
	public void stop() {
		if (!fIsRunning) {
			throw new IllegalStateException(
					"Cannot stop if not currently running.");
		}
		fStop = System.currentTimeMillis();
		fIsRunning = false;
	}

	/**
	 * Express the "reading" on the stopwatch.
	 * 
	 * @throws IllegalStateException
	 *             if the Stopwatch has never been used, or if the stopwatch is
	 *             still running.
	 */
	public String toString() {
		validateIsReadable();
		StringBuilder result = new StringBuilder();
		result.append(fStop - fStart);
		result.append(" ms");
		return result.toString();
	}

	/**
	 * Express the "reading" on the stopwatch as a numeric type.
	 * 
	 * @throws IllegalStateException
	 *             if the Stopwatch has never been used, or if the stopwatch is
	 *             still running.
	 */
	public long toValue() {
		validateIsReadable();
		return fStop - fStart;
	}

	// PRIVATE ////
	private long fStart;
	private long fStop;

	private boolean fIsRunning;
	private boolean fHasBeenUsedOnce;

	/**
	 * Throws IllegalStateException if the watch has never been started, or if
	 * the watch is still running.
	 */
	private void validateIsReadable() {
		if (fIsRunning) {
			String message = "Cannot read a stopwatch which is still running.";
			throw new IllegalStateException(message);
		}
		if (!fHasBeenUsedOnce) {
			String message = "Cannot read a stopwatch which has never been started.";
			throw new IllegalStateException(message);
		}
	}
}
