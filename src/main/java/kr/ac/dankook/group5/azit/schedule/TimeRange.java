package kr.ac.dankook.group5.azit.schedule;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import java.time.LocalTime;

@Value
@With
@AllArgsConstructor
public class TimeRange {
	private LocalTime startTime;
	private LocalTime endTime;
}