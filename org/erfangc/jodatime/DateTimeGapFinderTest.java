package org.erfangc.jodatime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Test;

public class DateTimeGapFinderTest {

	List<Interval> existingIntervals;

	@Before
	public void setUpTestIntervals() {
		existingIntervals = new ArrayList<Interval>();
		existingIntervals.add(new Interval(DateTime.parse("2014-01-01"), DateTime.parse("2014-01-31")));
		existingIntervals.add(new Interval(DateTime.parse("2014-03-01"), DateTime.parse("2014-03-31")));
		existingIntervals.add(new Interval(DateTime.parse("2014-05-01"), DateTime.parse("2014-05-30")));
	}

	@Test
	public void test() {
		DateTimeGapFinder dateTimeGapFinder = new DateTimeGapFinder();
		// the search interval overshadows the extremities of existing intervals
		Interval bigSearch = new Interval(DateTime.parse("2013-12-01"), DateTime.parse("2014-12-15"));
		List<Interval> bigSearchResults = dateTimeGapFinder.findGaps(existingIntervals, bigSearch);
		assertEquals(4, bigSearchResults.size());
		assertTrue("end of first gap", bigSearchResults.get(0).isBefore(DateTime.parse("2014-01-01")));
		assertTrue("2nd gap should contain end of last interval", bigSearchResults.get(1).contains(DateTime.parse("2014-01-31")));
		assertTrue("last gap should end on the search interval end", bigSearchResults.get(bigSearchResults.size() - 1).getEnd().equals(DateTime.parse("2014-12-15")));
		// the search interval is smaller than the extremities of existing
		// intervals
		Interval smallSearch = new Interval(DateTime.parse("2014-01-15"), DateTime.parse("2014-03-15"));
		List<Interval> smallSearchResults = dateTimeGapFinder.findGaps(existingIntervals, smallSearch);
		assertEquals(1, smallSearchResults.size());
		assertTrue("end of the only gap should be the end start interval", existingIntervals.get(1).getStart().equals(smallSearchResults.get(0).getEnd()));
		// search interval end exceeds existing end
		Interval rightExceed = new Interval(DateTime.parse("2014-02-15"), DateTime.parse("2014-06-30"));
		List<Interval> rightExceedResults = dateTimeGapFinder.findGaps(existingIntervals, rightExceed);
		assertEquals(3, rightExceedResults.size());
		assertTrue("anything earlier than the earliest search date is not included", rightExceedResults.get(0).getStart().equals(rightExceed.getStart()));
		// search interval start earlier than existing start
		Interval leftExceed = new Interval(DateTime.parse("2013-12-01"), DateTime.parse("2014-01-15"));
		List<Interval> leftExceedResults = dateTimeGapFinder.findGaps(existingIntervals, leftExceed);
		assertEquals(1, leftExceedResults.size());
	}
}
