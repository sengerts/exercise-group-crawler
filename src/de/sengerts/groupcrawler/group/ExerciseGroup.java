package de.sengerts.groupcrawler.group;

import de.sengerts.groupcrawler.Main;
import de.sengerts.groupcrawler.appointment.Appointment;

public class ExerciseGroup implements Comparable<ExerciseGroup> {
	
	private final String moduleName;
	private final String name;
	private final Appointment appointment;
	
	public ExerciseGroup(final String moduleName, final String name, final Appointment appointment) {
		super();
		this.moduleName = moduleName;
		this.name = name;
		this.appointment = appointment;
	}
	
	@Override
	public String toString() {
		return "<p style=\" background: " + Main.getModuleColor(moduleName) + "; \"><b>Ü " + moduleName + "</b>:</br>Gruppe " + name + " (" + appointment.toString() + ")</p>";
//		return "<p style=\" background: " + Main.getModuleColor(moduleName) + "; \"><b>" + name + " (" + appointment.toString() + ")</p>";
	}
	
	@Override
	public int compareTo(ExerciseGroup o) {
		return appointment.compareTo(o.getAppointment());
	}
	
	/*public void addAppointment(final Appointment appointment) throws AppointmentAlreadyAddedException {
		if(hasAppointment(appointment)) {
			throw new AppointmentAlreadyAddedException();
		}
		appointments.add(appointment);
	}
	
	public boolean hasAppointment(final Appointment appointment) {
		return appointments.stream().filter(a -> a.equals(appointment)).count() > 0;
	}*/

	public String getName() {
		return name;
	}

	public Appointment getAppointment() {
		return appointment;
	}

}
