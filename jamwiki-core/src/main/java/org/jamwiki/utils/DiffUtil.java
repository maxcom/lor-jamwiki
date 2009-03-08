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

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.incava.util.diff.Diff;
import org.incava.util.diff.Difference;
import org.jamwiki.WikiBase;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.model.WikiDiff;

/**
 * Utility class for processing the difference between two topics and returing a list
 * of WikiDiff objects that can be used to display the diff.
 */
public class DiffUtil {

	private static final WikiLogger logger = WikiLogger.getLogger(DiffUtil.class.getName());
	/** The number of lines of unchanged text to display before and after each diff. */
	// FIXME - make this a property value
	private static final int DIFF_UNCHANGED_LINE_DISPLAY = 2;

	/**
	 *
	 */
	private DiffUtil() {
	}

	/**
	 * Utility method for determining whether or not to append lines of context around a diff.
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
		int nextStart = (adding) ? nextDiff.getAddedStart() : nextDiff.getDeletedStart();
		// if in a valid range and the next diff starts several lines away, buffer away.  otherwise
		// the default is not to diff.
		return (nextStart > current);
	}

	/**
	 * Utility method for determining whether or not to prepend lines of context around a diff.
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
		int previousEnd = (adding) ? previousDiff.getAddedEnd() : previousDiff.getDeletedEnd();
		if (previousEnd != -1) {
			// if there was a previous diff but it was several lines previous, buffer away.
			// if there was a previous diff, and it overlaps with the current diff, don't buffer.
			return (current > (previousEnd + DIFF_UNCHANGED_LINE_DISPLAY));
		}
		int previousStart = (adding) ? previousDiff.getAddedStart() : previousDiff.getDeletedStart();
		if (current <= (previousStart + DIFF_UNCHANGED_LINE_DISPLAY)) {
			// the previous diff did not specify an end, and the current diff would overlap with
			// buffering from its start, don't buffer
			return false;
		}
		// the previous diff did not specify an end, and the current diff will not overlap
		// with buffering from its start, buffer away.  otherwise the default is not to buffer.
		return (current >= 0 && currentStart > current);
	}

	/**
	 * Return a list of WikiDiff objects that can be used to create a display of the
	 * diff content.
	 *
	 * @param newVersion The String that is to be compared to, ie the later version of a topic.
	 * @param oldVersion The String that is to be considered as having changed, ie the earlier
	 *  version of a topic.
	 * @return Returns a list of WikiDiff objects that correspond to the changed text.
	 */
	public static List<WikiDiff> diff(String newVersion, String oldVersion) {
		if (oldVersion == null) {
			oldVersion = "";
		}
		if (newVersion == null) {
			newVersion = "";
		}
		// remove line-feeds to avoid unnecessary noise in the diff due to
		// cut & paste or other issues
		oldVersion = StringUtils.remove(oldVersion, '\r');
		newVersion = StringUtils.remove(newVersion, '\r');
		return DiffUtil.process(newVersion, oldVersion);
	}

	/**
	 * Execute a diff between two versions of a topic, returning a list
	 * of WikiDiff objects indicating what has changed between the versions.
	 *
	 * @param topicName The name of the topic for which a diff is being
	 *  performed.
	 * @param topicVersionId1 The version ID for the old version being
	 *  compared against.
	 * @param topicVersionId2 The version ID for the old version being
	 *  compared to.
	 * @return A list of WikiDiff objects indicating what has changed
	 *  between the versions.  An empty list is returned if there are
	 *  no differences.
	 * @throws Exception Thrown if any error occurs during method execution.
	 */
	public static List<WikiDiff> diffTopicVersions(String topicName, int topicVersionId1, int topicVersionId2) throws Exception {
		TopicVersion version1 = WikiBase.getDataHandler().lookupTopicVersion(topicVersionId1);
		TopicVersion version2 = WikiBase.getDataHandler().lookupTopicVersion(topicVersionId2);
		if (version1 == null && version2 == null) {
			String msg = "Versions " + topicVersionId1 + " and " + topicVersionId2 + " not found for " + topicName;
			logger.severe(msg);
			throw new Exception(msg);
		}
		String contents1 = (version1 != null) ? version1.getVersionContent() : null;
		String contents2 = (version2 != null) ? version2.getVersionContent() : null;
		if (contents1 == null && contents2 == null) {
			String msg = "No versions found for " + topicVersionId1 + " against " + topicVersionId2;
			logger.severe(msg);
			throw new Exception(msg);
		}
		return DiffUtil.diff(contents1, contents2);
	}

	/**
	 * Format the list of Difference objects into a list of WikiDiff objects, which will
	 * include information about what values are different and also include some unchanged
	 * values surrounded the changed values, thus giving some context.
	 */
	private static List<WikiDiff> generateWikiDiffs(List<Difference> diffs, String[] oldArray, String[] newArray) {
		List<WikiDiff> wikiDiffs = new ArrayList<WikiDiff>();
		Difference previousDiff = null;
		Difference nextDiff = null;
		int i = 1;
		for (Difference currentDiff : diffs) {
			i++;
			wikiDiffs.addAll(DiffUtil.preBufferDifference(currentDiff, previousDiff, oldArray, newArray));
			wikiDiffs.addAll(DiffUtil.processDifference(currentDiff, oldArray, newArray));
			nextDiff = (i < diffs.size()) ? diffs.get(i) : null;
			wikiDiffs.addAll(DiffUtil.postBufferDifference(currentDiff, nextDiff, oldArray, newArray));
			previousDiff = currentDiff;
		}
		return wikiDiffs;
	}

	/**
	 *
	 */
	private static boolean hasMoreDiffInfo(int addedCurrent, int deletedCurrent, Difference currentDiff) {
		if (addedCurrent == -1) {
			addedCurrent = 0;
		}
		if (deletedCurrent == -1) {
			deletedCurrent = 0;
		}
		return (addedCurrent <= currentDiff.getAddedEnd() || deletedCurrent <= currentDiff.getDeletedEnd());
	}

	/**
	 * If possible, append a few lines of unchanged text that appears after to the changed line
	 * in order to add context to the current list of WikiDiff objects.
	 *
	 * @param currentDiff The current diff object.
	 * @param nextDiff The diff object that immediately follows this object (if any).
	 * @param oldArray The original array of string objects that was compared from in order to
	 *  generate the diff.
	 * @param newArray The original array of string objects that was compared to in order to
	 *  generate the diff.
	 */
	private static List<WikiDiff> postBufferDifference(Difference currentDiff, Difference nextDiff, String[] oldArray, String[] newArray) {
		List<WikiDiff> wikiDiffs = new ArrayList<WikiDiff>();
		if (DIFF_UNCHANGED_LINE_DISPLAY <= 0) {
			return wikiDiffs;
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
			int position = ((deletedCurrent < 0) ? 0 : deletedCurrent);
			String oldText = null;
			String newText = null;
			boolean buffered = false;
			if (canPostBuffer(nextDiff, deletedCurrent, oldArray, false)) {
				oldText = oldArray[deletedCurrent];
				deletedCurrent++;
				buffered = true;
			}
			if (canPostBuffer(nextDiff, addedCurrent, newArray, true)) {
				newText = newArray[addedCurrent];
				addedCurrent++;
				buffered = true;
			}
			if (!buffered) {
				continue;
			}
			WikiDiff wikiDiff = new WikiDiff(oldText, newText, position);
			wikiDiffs.add(wikiDiff);
		}
		return wikiDiffs;
	}

	/**
	 * If possible, prepend a few lines of unchanged text that before after to the changed line
	 * in order to add context to the current list of WikiDiff objects.
	 *
	 * @param currentDiff The current diff object.
	 * @param previousDiff The diff object that immediately preceded this object (if any).
	 * @param oldArray The original array of string objects that was compared from in order to
	 *  generate the diff.
	 * @param newArray The original array of string objects that was compared to in order to
	 *  generate the diff.
	 */
	private static List<WikiDiff> preBufferDifference(Difference currentDiff, Difference previousDiff, String[] oldArray, String[] newArray) {
		List<WikiDiff> wikiDiffs = new ArrayList<WikiDiff>();
		if (DIFF_UNCHANGED_LINE_DISPLAY <= 0) {
			return wikiDiffs;
		}
		int deletedCurrent = (currentDiff.getDeletedStart() - DIFF_UNCHANGED_LINE_DISPLAY);
		int addedCurrent = (currentDiff.getAddedStart() - DIFF_UNCHANGED_LINE_DISPLAY);
		if (previousDiff != null) {
			deletedCurrent = Math.max(previousDiff.getDeletedEnd() + 1, deletedCurrent);
			addedCurrent = Math.max(previousDiff.getAddedEnd() + 1, addedCurrent);
		}
		for (int i = 0; i < DIFF_UNCHANGED_LINE_DISPLAY; i++) {
			int position = ((deletedCurrent < 0) ? 0 : deletedCurrent);
			String oldText = null;
			String newText = null;
			boolean buffered = false;
			// if diffs are close together, do not allow buffers to overlap
			if (canPreBuffer(previousDiff, deletedCurrent, currentDiff.getDeletedStart(), oldArray, false)) {
				oldText = oldArray[deletedCurrent];
				deletedCurrent++;
				buffered = true;
			}
			if (canPreBuffer(previousDiff, addedCurrent, currentDiff.getAddedStart(), newArray, true)) {
				newText = newArray[addedCurrent];
				addedCurrent++;
				buffered = true;
			}
			if (!buffered) {
				continue;
			}
			WikiDiff wikiDiff = new WikiDiff(oldText, newText, position);
			wikiDiffs.add(wikiDiff);
		}
		return wikiDiffs;
	}

	/**
	 * @param newVersion The String that is being compared to.
	 * @param oldVersion The String that is being compared against.
	 */
	private static List<WikiDiff> process(String newVersion, String oldVersion) {
		logger.fine("Diffing: " + oldVersion + " against: " + newVersion);
		if (newVersion.equals(oldVersion)) {
			return new ArrayList<WikiDiff>();
		}
		String[] oldArray = DiffUtil.split(oldVersion);
		String[] newArray = DiffUtil.split(newVersion);
		Diff diffObject = new Diff(oldArray, newArray);
		List<Difference> diffs = diffObject.diff();
		return DiffUtil.generateWikiDiffs(diffs, oldArray, newArray);
	}

	/**
	 * Split up a String into an array of values using the specified string pattern.
	 *
	 * @param original The value that is being split.
	 */
	private static String[] split(String original) {
		if (original == null) {
			return new String[0];
		}
		return original.split("\n");
	}

	/**
	 * Process the diff object and add it to the output.  Text will either have been
	 * deleted or added (it cannot have remained the same, since a diff object represents
	 * a change).  This method steps through the diff result and converts it into an
	 * array of objects that can be used to easily represent the diff.
	 */
	private static List<WikiDiff> processDifference(Difference currentDiff, String[] oldArray, String[] newArray) {
		List<WikiDiff> wikiDiffs = new ArrayList<WikiDiff>();
		// if text was deleted then deletedCurrent represents the starting position of the deleted text.
		int deletedCurrent = currentDiff.getDeletedStart();
		// if text was added then addedCurrent represents the starting position of the added text.
		int addedCurrent = currentDiff.getAddedStart();
		// count is simply used to ensure that the loop is not infinite, which should never happen
		int count = 0;
		while (hasMoreDiffInfo(addedCurrent, deletedCurrent, currentDiff)) {
			// the position within the diff array (line number, character, etc) at which the change
			// started (starting at 0)
			int position = ((deletedCurrent < 0) ? 0 : deletedCurrent);
			// the text of the element that changed
			String oldText = null;
			// the text of what the element was changed to
			String newText = null;
			if (currentDiff.getDeletedEnd() >= 0 && currentDiff.getDeletedEnd() >= deletedCurrent) {
				oldText = oldArray[deletedCurrent];
				deletedCurrent++;
			}
			if (currentDiff.getAddedEnd() >= 0 && currentDiff.getAddedEnd() >= addedCurrent) {
				newText = newArray[addedCurrent];
				addedCurrent++;
			}
			WikiDiff wikiDiff = new WikiDiff(oldText, newText, position);
			wikiDiffs.add(wikiDiff);
			// FIXME - this shouldn't be necessary
			count++;
			if (count > 5000) {
				logger.warning("Infinite loop in DiffUtils.processDifference");
				break;
			}
		}
		return wikiDiffs;
	}
}
