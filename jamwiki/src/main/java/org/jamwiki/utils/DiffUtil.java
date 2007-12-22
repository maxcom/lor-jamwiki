/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the latest version of the GNU Lesser General
 * Public License as published by the Free Software Foundation;
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program (LICENSE.txt); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.jamwiki.utils;

import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import org.incava.util.diff.Diff;
import org.incava.util.diff.Difference;
import org.jamwiki.WikiBase;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.model.WikiDiff;

/**
 * Utility class for processing the difference between two topics and returing a Vector
 * of WikiDiff objects that can be used to display the diff.
 */
public class DiffUtil {

	protected static WikiLogger logger = WikiLogger.getLogger(DiffUtil.class.getName());
	/** The number of lines of unchanged text to display before and after each diff. */
	// FIXME - make this a property value
	private static final int DIFF_UNCHANGED_LINE_DISPLAY = 2;

	/**
	 *
	 */
	private DiffUtil() {
	}

	/**
	 * Split up a large String into an array of Strings made up of each line (indicated
	 * by a newline) of the original String.
	 */
	private static String[] buildArray(String original) {
		if (original == null) {
			return null;
		}
		StringTokenizer tokens = new StringTokenizer(original, "\n");
		int size = tokens.countTokens();
		String[] array = new String[size];
		int count = 0;
		while (tokens.hasMoreTokens()) {
			array[count] = tokens.nextToken();
			count++;
		}
		return array;
	}

	/**
	 * Utility method for determining whether or not a difference can be post-buffered.
	 */
	private static boolean canPostBuffer(Difference nextDiff, int current, String[] replacementArray, boolean adding) {
		if (current < 0 || current >= replacementArray.length) {
			// if out of a valid range, don't buffer
			return false;
		}
		if (nextDiff == null) {
			// if in a valid range and no next diff, buffer away
			return true;
		}
		int nextStart = nextDiff.getDeletedStart();
		if (adding) {
			nextStart = nextDiff.getAddedStart();
		}
		if (nextStart > current) {
			// if in a valid range and no next diff starts several lines away, buffer away
			return true;
		}
		// default is don't buffer
		return false;
	}

	/**
	 * Utility method for determining whether or not a difference can be pre-buffered.
	 */
	private static boolean canPreBuffer(Difference previousDiff, int current, int currentStart, String[] replacementArray, boolean adding) {
		if (current < 0 || current >= replacementArray.length) {
			// current position is out of range for buffering
			return false;
		}
		if (previousDiff == null) {
			// if no previous diff, buffer away
			return true;
		}
		int previousEnd = previousDiff.getDeletedEnd();
		int previousStart = previousDiff.getDeletedStart();
		if (adding) {
			previousEnd = previousDiff.getAddedEnd();
			previousStart = previousDiff.getAddedStart();
		}
		if (previousEnd != -1) {
			// if there was a previous diff but it was several lines previous, buffer away.
			// if there was a previous diff, and it overlaps with the current diff, don't buffer.
			return (current > (previousEnd + DIFF_UNCHANGED_LINE_DISPLAY));
		}
		if (current <= (previousStart + DIFF_UNCHANGED_LINE_DISPLAY)) {
			// the previous diff did not specify an end, and the current diff would overlap with
			// buffering from its start, don't buffer
			return false;
		}
		if (current >= 0 && currentStart > current) {
			// the previous diff did not specify an end, and the current diff will not overlap
			// with buffering from its start, buffer away
			return true;
		}
		// default is don't buffer
		return false;
	}

	/**
	 * Return a Vector of WikiDiff objects that can be used to create a display of the
	 * diff content.
	 *
	 * @param newVersion The String that is to be compared to, ie the later version of a topic.
	 * @param oldVersion The String that is to be considered as having changed, ie the earlier
	 *  version of a topic.
	 * @return Returns a Vector of WikiDiff objects that correspond to the changed text.
	 */
	public static Vector diff(String newVersion, String oldVersion) {
		if (oldVersion == null) {
			oldVersion = "";
		}
		if (newVersion == null) {
			newVersion = "";
		}
		if (newVersion.equals(oldVersion)) {
			return new Vector();
		}
		return DiffUtil.process(newVersion, oldVersion);
	}

	/**
	 * Execute a diff between two versions of a topic, returning a collection
	 * of WikiDiff objects indicating what has changed between the versions.
	 *
	 * @param topicName The name of the topic for which a diff is being
	 *  performed.
	 * @param topicVersionId1 The version ID for the old version being
	 *  compared against.
	 * @param topicVersionId2 The version ID for the old version being
	 *  compared to.
	 * @return A collection of WikiDiff objects indicating what has changed
	 *  between the versions.  An empty collection is returned if there are
	 *  no differences.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public static Collection diffTopicVersions(String topicName, int topicVersionId1, int topicVersionId2) throws Exception {
		TopicVersion version1 = WikiBase.getDataHandler().lookupTopicVersion(topicVersionId1, null);
		TopicVersion version2 = WikiBase.getDataHandler().lookupTopicVersion(topicVersionId2, null);
		if (version1 == null && version2 == null) {
			String msg = "Versions " + topicVersionId1 + " and " + topicVersionId2 + " not found for " + topicName;
			logger.severe(msg);
			throw new Exception(msg);
		}
		String contents1 = null;
		if (version1 != null) {
			contents1 = version1.getVersionContent();
		}
		String contents2 = null;
		if (version2 != null) {
			contents2 = version2.getVersionContent();
		}
		if (contents1 == null && contents2 == null) {
			String msg = "No versions found for " + topicVersionId1 + " against " + topicVersionId2;
			logger.severe(msg);
			throw new Exception(msg);
		}
		return DiffUtil.diff(contents1, contents2);
	}

	/**
	 *
	 */
	private static boolean hasMoreDiffLines(int addedCurrent, int deletedCurrent, Difference currentDiff) {
		if (addedCurrent == -1) {
			addedCurrent = 0;
		}
		if (deletedCurrent == -1) {
			deletedCurrent = 0;
		}
		return (addedCurrent <= currentDiff.getAddedEnd() || deletedCurrent <= currentDiff.getDeletedEnd());
	}

	/**
	 * If possible, try to append a few lines of unchanged text to the diff output to
	 * be used for context.
	 */
	private static void postBufferDifference(Difference currentDiff, Difference nextDiff, Vector wikiDiffs, String[] oldArray, String[] newArray) {
		if (DIFF_UNCHANGED_LINE_DISPLAY <= 0) {
			return;
		}
		int deletedCurrent = (currentDiff.getDeletedEnd() + 1);
		int addedCurrent = (currentDiff.getAddedEnd() + 1);
		if (currentDiff.getDeletedEnd() == -1) {
			deletedCurrent = (currentDiff.getDeletedStart());
		}
		if (currentDiff.getAddedEnd() == -1) {
			addedCurrent = (currentDiff.getAddedStart());
		}
		for (int i = 0; i < DIFF_UNCHANGED_LINE_DISPLAY; i++) {
			int lineNumber = ((deletedCurrent < 0) ? 0 : deletedCurrent);
			String oldLine = null;
			String newLine = null;
			boolean buffered = false;
			if (canPostBuffer(nextDiff, deletedCurrent, oldArray, false)) {
				oldLine = oldArray[deletedCurrent];
				deletedCurrent++;
				buffered = true;
			}
			if (canPostBuffer(nextDiff, addedCurrent, newArray, true)) {
				newLine = newArray[addedCurrent];
				addedCurrent++;
				buffered = true;
			}
			if (!buffered) {
				continue;
			}
			WikiDiff wikiDiff = new WikiDiff(oldLine, newLine, lineNumber + 1, false);
			wikiDiffs.add(wikiDiff);
		}
	}

	/**
	 * If possible, try to prepend a few lines of unchanged text to the diff output to
	 * be used for context.
	 */
	private static void preBufferDifference(Difference currentDiff, Difference previousDiff, Vector wikiDiffs, String[] oldArray, String[] newArray) {
		if (DIFF_UNCHANGED_LINE_DISPLAY <= 0) {
			return;
		}
		int deletedCurrent = (currentDiff.getDeletedStart() - DIFF_UNCHANGED_LINE_DISPLAY);
		int addedCurrent = (currentDiff.getAddedStart() - DIFF_UNCHANGED_LINE_DISPLAY);
		if (previousDiff != null) {
			deletedCurrent = Math.max(previousDiff.getDeletedEnd() + 1, deletedCurrent);
			addedCurrent = Math.max(previousDiff.getAddedEnd() + 1, addedCurrent);
		}
		for (int i = 0; i < DIFF_UNCHANGED_LINE_DISPLAY; i++) {
			int lineNumber = ((deletedCurrent < 0) ? 0 : deletedCurrent);
			String oldLine = null;
			String newLine = null;
			boolean buffered = false;
			// if diffs are close together, do not allow buffers to overlap
			if (canPreBuffer(previousDiff, deletedCurrent, currentDiff.getDeletedStart(), oldArray, false)) {
				oldLine = oldArray[deletedCurrent];
				deletedCurrent++;
				buffered = true;
			}
			if (canPreBuffer(previousDiff, addedCurrent, currentDiff.getAddedStart(), newArray, true)) {
				newLine = newArray[addedCurrent];
				addedCurrent++;
				buffered = true;
			}
			if (!buffered) {
				continue;
			}
			WikiDiff wikiDiff = new WikiDiff(oldLine, newLine, lineNumber + 1, false);
			wikiDiffs.add(wikiDiff);
		}
	}

	/**
	 *
	 */
	private static Vector process(String newVersion, String oldVersion) {
		logger.fine("Diffing: " + oldVersion + " against: " + newVersion);
		String[] oldArray = buildArray(oldVersion);
		String[] newArray = buildArray(newVersion);
		Diff diffObject = new Diff(oldArray, newArray);
		List diffs = diffObject.diff();
		Vector wikiDiffs = new Vector();
		Difference currentDiff = null;
		Difference previousDiff = null;
		Difference nextDiff = null;
		for (int i = 0; i < diffs.size(); i++) {
			currentDiff = (Difference)diffs.get(i);
			preBufferDifference(currentDiff, previousDiff, wikiDiffs, oldArray, newArray);
			processDifference(currentDiff, wikiDiffs, oldArray, newArray);
			nextDiff = null;
			if ((i+1) < diffs.size() ) {
				nextDiff = (Difference)diffs.get(i+1);
			}
			postBufferDifference(currentDiff, nextDiff, wikiDiffs, oldArray, newArray);
			previousDiff = currentDiff;
		}
		return wikiDiffs;
	}

	/**
	 * Process the diff object and add it to the output.
	 */
	private static void processDifference(Difference currentDiff, Vector wikiDiffs, String[] oldArray, String[] newArray) {
		int deletedCurrent = currentDiff.getDeletedStart();
		int addedCurrent = currentDiff.getAddedStart();
		int count = 0;
		while (hasMoreDiffLines(addedCurrent, deletedCurrent, currentDiff)) {
			int lineNumber = ((deletedCurrent < 0) ? 0 : deletedCurrent);
			String oldLine = null;
			String newLine = null;
			if (currentDiff.getDeletedEnd() >= 0 && currentDiff.getDeletedEnd() >= deletedCurrent) {
				oldLine = oldArray[deletedCurrent];
				deletedCurrent++;
			}
			if (currentDiff.getAddedEnd() >= 0 && currentDiff.getAddedEnd() >= addedCurrent) {
				newLine = newArray[addedCurrent];
				addedCurrent++;
			}
			WikiDiff wikiDiff = new WikiDiff(oldLine, newLine, lineNumber + 1, true);
			wikiDiffs.add(wikiDiff);
			// FIXME - this shouldn't be necessary
			count++;
			if (count > 5000) {
				logger.warning("Infinite loop in DiffUtils.processDifference");
				break;
			}
		}
	}
}
