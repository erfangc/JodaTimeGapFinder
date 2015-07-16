package org.erfangc.jodatime;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * 
 * @author Erfang
 *
 */
public class DateTimeGapFinder {

	/**
	 * Finds gaps on the time line between a list of existing {@link Interval}
	 * and a search {@link Interval}
	 * 
	 * @param existingIntervals
	 * @param searchInterval
	 * @return The list of gaps
	 */
	public List<Interval> findGaps(List<Interval> existingIntervals, Interval searchInterval) {
		List<Interval> gaps = new ArrayList<Interval>();

		DateTime searchStart = searchInterval.getStart();
		DateTime searchEnd = searchInterval.getEnd();

		if (existingIntervals.isEmpty() || hasNoOverlap(existingIntervals, searchInterval, searchStart, searchEnd)) {
			gaps.add(searchInterval);
			return gaps;
		}

		// create a sub-list that excludes interval which does not overlap with
		// searchInterval
		List<Interval> subExistingList = removeNoneOverlappingIntervals(existingIntervals, searchInterval);
		DateTime subEarliestStart = subExistingList.get(0).getStart();
		DateTime subLatestStop = subExistingList.get(subExistingList.size() - 1).getEnd();

		// in case the searchInterval is wider than the union of the existing
		// include searchInterval.start => earliestExisting.start
		if (searchStart.isBefore(subEarliestStart)) {
			gaps.add(new Interval(searchStart, subEarliestStart));
		}

		// get all the gaps in the existing list
		gaps.addAll(getExistingIntervalGaps(subExistingList));

		// include latestExisting.stop => searchInterval.stop
		if (searchEnd.isAfter(subLatestStop)) {
			gaps.add(new Interval(subLatestStop, searchEnd));
		}
		return gaps;
	}

	private List<Interval> getExistingIntervalGaps(List<Interval> existingList) {
		List<Interval> gaps = new ArrayList<Interval>();
		Interval current = existingList.get(0);
		for (int i = 1; i < existingList.size(); i++) {
			Interval next = existingList.get(i);
			Interval gap = current.gap(next);
			if (gap != null)
				gaps.add(gap);
			current = next;
		}
		return gaps;
	}

	private List<Interval> removeNoneOverlappingIntervals(List<Interval> existingIntervals, Interval searchInterval) {
		List<Interval> subExistingList = new ArrayList<Interval>();
		for (Interval interval : existingIntervals) {
			if (interval.overlaps(searchInterval)) {
				subExistingList.add(interval);
			}
		}
		return subExistingList;
	}

	private boolean hasNoOverlap(List<Interval> existingIntervals, Interval searchInterval, DateTime searchStart, DateTime searchEnd) {
		DateTime earliestStart = existingIntervals.get(0).getStart();
		DateTime latestStop = existingIntervals.get(existingIntervals.size() - 1).getEnd();
		// return the entire search interval if it does not overlap with
		// existing at all
		if (searchEnd.isBefore(earliestStart) || searchStart.isAfter(latestStop)) {
			return true;
		}
		return false;
	}
}
