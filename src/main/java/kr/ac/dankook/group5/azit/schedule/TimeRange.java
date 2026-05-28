package kr.ac.dankook.group5.azit.schedule;

import lombok.Value;
import lombok.With;

import java.time.LocalTime;

@Value
@With
public class TimeRange {
	private LocalTime startTime;
	private LocalTime endTime;

	public TimeRange(LocalTime startTime, LocalTime endTime) {
		if (startTime.isAfter(endTime)) {
			throw new IllegalArgumentException("End time is earlier than start time");
		}

		this.startTime = startTime;
		this.endTime = endTime;
	}

	public boolean isBefore(LocalTime time) {
		return endTime.isBefore(time) || endTime.equals(time);
	}

	public boolean isBefore(TimeRange range) {
		LocalTime otherStartTime = range.getStartTime();
		return isBefore(otherStartTime);
	}

	public boolean isAfter(LocalTime time) {
		return startTime.isAfter(time);
	}

	public boolean isAfter(TimeRange range) {
		LocalTime otherEndTime = range.getEndTime();
		return isAfter(otherEndTime) || startTime.equals(otherEndTime);
	}

	public boolean contains(LocalTime time) {
		return !startTime.isAfter(time) && endTime.isAfter(time);
	}

	public boolean contains(TimeRange range) {
		return !startTime.isAfter(range.getStartTime()) && !endTime.isBefore(range.getEndTime());
	}

	public boolean isOverlap(TimeRange range) {
		return !isBefore(range) && !isAfter(range);
	}

	public boolean isAdjacent(TimeRange range) {
		return endTime.equals(range.getStartTime()) || startTime.equals(range.getEndTime());
	}

	public boolean isExtendable(TimeRange range) {
		return isOverlap(range) || isAdjacent(range);
	}

	public TimeRange extend(TimeRange range) {
		if (!isExtendable(range)) {
			throw new IllegalArgumentException("Cannot extend non-overlapping time ranges: " + this + ", " + range);
		}

		LocalTime start = startTime.isBefore(range.getStartTime()) ? startTime : range.getStartTime();
		LocalTime end = endTime.isAfter(range.getEndTime()) ? endTime : range.getEndTime();

		return new TimeRange(start, end);
	}
}