package de.sengerts.groupcrawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;

import de.sengerts.groupcrawler.appointment.Appointment;
import de.sengerts.groupcrawler.appointment.AppointmentAlreadyAddedException;
import de.sengerts.groupcrawler.group.GroupCrawling;
import de.sengerts.groupcrawler.weekday.Weekday;
import de.sengerts.groupcrawler.weekday.WeekdayNotFoundException;

public class Main {
	
	private static final String MATH2_URL = "https://campus.uni-stuttgart.de/cusonline/wbLv.wbShowLVDetail?pStpSpNr=224994";
	private static final String THEO2_URL = "https://campus.uni-stuttgart.de/cusonline/wbLv.wbShowLVDetail?pStpSpNr=224044";
	private static final String DSA_URL = "https://campus.uni-stuttgart.de/cusonline/wbLv.wbShowLVDetail?pStpSpNr=224290&pSpracheNr=1";
	private static final String RO1_URL = "https://campus.uni-stuttgart.de/cusonline/wbLv.wbShowLVDetail?pStpSpNr=225544";
	
	private static List<String> groupsUrls;
	private static List<GroupCrawling> groupCrawlings;
	private static List<Appointment> bannedTimeSlots;
	
	static {
		groupsUrls = Arrays.asList(MATH2_URL, THEO2_URL, DSA_URL, RO1_URL);
		groupCrawlings = new ArrayList<>();
		
		Appointment dsa1 = new Appointment(Weekday.MONDAY, LocalTime.parse("15:45:00"), "PWR 47 - V 47.02 (PF47/EG/V 47.02)");
		Appointment dsa2 = new Appointment(Weekday.WEDNESDAY, LocalTime.parse("15:45:00"), "PWR 47 - V 47.02 (PF47/EG/V 47.02)");
		Appointment theo1 = new Appointment(Weekday.TUESDAY, LocalTime.parse("15:45:00"), "PWR 47 - V 47.02 (PF47/EG/V 47.02)");
		Appointment theo2 = new Appointment(Weekday.THURSAY, LocalTime.parse("14:00:00"), "PWR 47 - V 47.02 (PF47/EG/V 47.02)");
		Appointment math1 = new Appointment(Weekday.WEDNESDAY, LocalTime.parse("09:45:00"), "PWR 47 - V 47.02 (PF47/EG/V 47.02)");
		Appointment math2 = new Appointment(Weekday.WEDNESDAY, LocalTime.parse("14:00:00"), "PWR 07 - V 7.02 (PF07/EG/V 7.02)");
		Appointment math3 = new Appointment(Weekday.FRIDAY, LocalTime.parse("11:30:00"), "PWR 47 - V 47.02 (PF47/EG/V 47.02)");
		Appointment ro1 = new Appointment(Weekday.WEDNESDAY, LocalTime.parse("11:30:00"), "Universität 38 - V 38.04 (UN38/EG/V 38.04");
		Appointment ro2 = new Appointment(Weekday.THURSAY, LocalTime.parse("17:30:00"), "Universität 38 - V 38.04 (UN38/EG/V 38.04");
		bannedTimeSlots = Arrays.asList(dsa1, dsa2, theo1, theo2, math1, math2, math3, ro1, ro2);
	}
	
	public static void main(String[] args) {
		for(String groupsUrl : groupsUrls) {
			crawlModuleGroups(groupsUrl);
		}
		generateHTMLOutputFile();
	}
	
	private static void crawlModuleGroups(final String groupsUrl) {
		System.out.println("Crawling groups at " + groupsUrl + "..");
		try {
			GroupCrawling crawling = new GroupCrawling(groupsUrl);
			crawling.crawlGroups();
			groupCrawlings.add(crawling);
			System.out.println("Done. " + crawling.getExerciseGroups().size() + " groups found.");
		} catch (FailingHttpStatusCodeException | IOException | WeekdayNotFoundException | AppointmentAlreadyAddedException e) {
			e.printStackTrace();
		}
	}
	
	private static void generateHTMLOutputFile() {
		System.out.println("Generating output HTML table..");
		File file = new File("D:/OneDrive/Informatik Studium/Semester 2/Uebungsgruppen.html");
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			bw.write(getOutputHTML());
			bw.close();
			System.out.println("Done.");
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private static String getOutputHTML() {
		String html = "<html><head><style>.slot-banned { background-color: red; } .slot-free { background-color: #84F26D; }</style></head>";
		html += "<body><table border=\"1\"><thead><tr><td>Modulname</td>";
		for(Appointment timeSlot : getSortedTimeSlots()) {
			html += "<td class='slot-" + (isBanned(timeSlot) ? "banned'" : "free") + "'>" + timeSlot.toString() + "</td>";
		}
		html += "</tr></thead><tbody>";
		for(GroupCrawling groupCrawling : groupCrawlings) {
			html += groupCrawling.toHTML();
		}
		return html + "</tbody></table></body></html>";
	}
	
	private static boolean isBanned(final Appointment timeSlot) {
		return bannedTimeSlots.stream().filter(t -> t.equals(timeSlot)).count() > 0;
	}
	
	public static List<Appointment> getSortedTimeSlots() {
		List<Appointment> appointments = new ArrayList<>();
		for(GroupCrawling crawling : groupCrawlings) {
			appointments.addAll(crawling.getExerciseGroups().stream().map(g -> g.getAppointment()).collect(Collectors.toList()));
		}
		return appointments.stream().distinct().sorted().collect(Collectors.toList());
	}
	
}
