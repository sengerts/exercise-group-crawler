package de.sengerts.groupcrawler.appointment;

import java.time.LocalTime;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import de.sengerts.groupcrawler.weekday.Weekday;

public final class Appointment implements Comparable<Appointment> {
	
	private final static long DURATION_IN_MINUTES = 90;

	private final String name;
	private final Weekday weekday;
	private final LocalTime startTime;
	private final LocalTime endTime;
	private final String location;

	public Appointment(final String name, final Weekday weekday, final LocalTime startTime, final String location) {
		super();
		this.name = name;
		this.weekday = weekday;
		this.startTime = startTime;
		this.endTime = startTime.plusMinutes(DURATION_IN_MINUTES);
		this.location = location;
	}
	
	public Appointment(final Weekday weekday, final LocalTime startTime, final String location) {
		this("", weekday, startTime, location);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Appointment o) {
		if(o.getWeekday() == getWeekday() && o.getStartTime().equals(getStartTime())) {
			return 0;
		}
		if(o.getWeekday() != getWeekday()) {
			return Integer.compare(getWeekday().ordinal(), o.getWeekday().ordinal());
//			return o.getWeekday().ordinal() > getWeekday().ordinal() ? -1 : 1;
		}
		return o.getStartTime().isAfter(getStartTime()) ? -1 : 1;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 31) // two randomly chosen prime numbers
	            .append(weekday.name())
	            .append(startTime.toString())
	            .append(endTime.toString())
	            .toHashCode();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof Appointment)) {
			return false;
		}

		Appointment a = (Appointment) o;
		return a.getWeekday() == weekday && a.getStartTime().equals(startTime) && a.getEndTime().equals(endTime);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getWeekday().getShortForm() + " " +  getStartTime().toString() + " - " + getEndTime();
	}

	public final Weekday getWeekday() {
		return weekday;
	}

	public final LocalTime getStartTime() {
		return startTime;
	}

	public String getLocation() {
		return location;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public String getName() {
		return name;
	}

}
