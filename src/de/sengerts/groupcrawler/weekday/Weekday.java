package de.sengerts.groupcrawler.weekday;

public enum Weekday {
	
	MONDAY("Mo"), TUESDAY("Di"), WEDNESDAY("Mi"), THURSAY("Do"), FRIDAY("Fr");

	private final String shortForm;
	
	Weekday(final String shortForm) {
		this.shortForm = shortForm;
	}
	
	public String getShortForm() {
		return shortForm;
	}
	
	public static Weekday getWeekday(final String shortForm) throws WeekdayNotFoundException {
		for(Weekday weekday : Weekday.values()) {
			if(weekday.getShortForm().equals(shortForm)) {
				return weekday;
			}
		}
		throw new WeekdayNotFoundException();
	}
	
}
