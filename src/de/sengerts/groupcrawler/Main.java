package de.sengerts.groupcrawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.Security;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;

import de.sengerts.groupcrawler.appointment.Appointment;
import de.sengerts.groupcrawler.appointment.AppointmentAlreadyAddedException;
import de.sengerts.groupcrawler.group.GroupCrawling;
import de.sengerts.groupcrawler.weekday.Weekday;
import de.sengerts.groupcrawler.weekday.WeekdayNotFoundException;

public class Main {
	
	private static final List<String> hexColors = Arrays.asList("#ac8daf", "#b2fcff", "#fff8cd", "#cc6a87", "#c9d99e", "#eb5f5d", "#3c9d9b", "#3c6f9c");
	
	private static final String OUTPUT_HTML_FILE_LOCATION = "D:/OneDrive/Informatik Studium/Semester 3/Uebungsgruppen.html";
	private static final String THEO3_URL = "https://campus.uni-stuttgart.de/cusonline/wbLv.wbShowLVDetail?pStpSpNr=238970";
	private static final String STAT_STOCH_URL = "https://campus.uni-stuttgart.de/cusonline/wbLv.wbShowLVDetail?pStpSpNr=239419";
	private static final String SYSKON_URL = "https://campus.uni-stuttgart.de/cusonline/wbLv.wbShowLVDetail?pStpSpNr=237347";

	private static List<String> groupsUrls;
	private static List<GroupCrawling> groupCrawlings;
	private static List<Appointment> lectures;
	
	private static HashMap<String, String> moduleColors;

	static {
		groupsUrls = Arrays.asList(THEO3_URL, STAT_STOCH_URL, SYSKON_URL);
		groupCrawlings = new ArrayList<>();
		moduleColors = new HashMap<>();

		lectures = new ArrayList<>();
		lectures.add(new Appointment("Ergänzungen zu Theoretische Informatik III", Weekday.MONDAY,
				LocalTime.parse("09:45:00"), "PWR 07 - V 7.04 (PF07/EG/V 7.04)"));
		lectures.add(new Appointment("Statistische und stochastische Grundlagen der Informatik", Weekday.MONDAY,
				LocalTime.parse("11:30:00"), "Universität 38 - V 38.01 (UN38/EG/V 38.01)"));
		lectures.add(new Appointment("Statistische und stochastische Grundlagen der Informatik", Weekday.TUESDAY,
				LocalTime.parse("09:45:00"), "Universität 38 - V 38.01 (UN38/EG/V 38.01)"));
		lectures.add(new Appointment("Systemkonzepte und -programmierung", Weekday.TUESDAY,
				LocalTime.parse("14:00:00"), "Universität 38 - V 38.01 (UN38/EG/V 38.01)"));
		lectures.add(new Appointment("Wissenschaftliche Methoden in der Informatik", Weekday.WEDNESDAY,
				LocalTime.parse("08:00:00"), "PWR 07 - V 7.02 (PF07/EG/V 7.02)"));
		lectures.add(new Appointment("Theoretische Informatik III", Weekday.WEDNESDAY,
				LocalTime.parse("15:45:00"), "Universität 38 - V 38.01 (UN38/EG/V 38.01)"));
		lectures.add(new Appointment("Systemkonzepte und -programmierung", Weekday.THURSAY,
				LocalTime.parse("14:00:00"), "Universität 38 - V 38.01 (UN38/EG/V 38.01)"));
		lectures.add(new Appointment("Theoretische Informatik III", Weekday.THURSAY, LocalTime.parse("17:30:00"),
				"Universität 38 - V 38.01 (UN38/EG/V 38.01)"));
		lectures.add(new Appointment("Programmierprojekt", Weekday.FRIDAY, LocalTime.parse("15:45:00"),
				"Universität 38 - V 38.03 (UN38/EG/V 38.03)"));
		lectures.add(new Appointment("PSE Tutorentreff", Weekday.WEDNESDAY, LocalTime.parse("13:00:00"),
				"?"));
		lectures.add(new Appointment("PSE Tutorium Do06", Weekday.THURSAY, LocalTime.parse("11:30:00"),
				"Universität 38 - 0.457"));
	}

	public static void main(String[] args) {
		Security.insertProviderAt(new BouncyCastleProvider(), 1);
		for (String groupsUrl : groupsUrls) {
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
		} catch (FailingHttpStatusCodeException | IOException | WeekdayNotFoundException
				| AppointmentAlreadyAddedException e) {
			e.printStackTrace();
		}
	}

	private static void generateHTMLOutputFile() {
		System.out.println("Generating output HTML table..");
		File file = new File(OUTPUT_HTML_FILE_LOCATION);
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			String html = "<html><head><style>table { text-align: center; } table p { padding: .5rem .3rem; margin: 0; } .slot-banned { background-color: red; } .slot-free { background-color: #84F26D; } .slot-lecture { background-color: #ffa0d2; }</style></head>";
			html += getGroupsOutputHTML();
			html += "</br></br>";
			html += getTimetableOutputHTML();
			html += getVerticalColorTableOutputHTML();
			html += "</body></html>";
			bw.write(html);
			bw.close();
			System.out.println("Done.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String getGroupsOutputHTML() {
		String html = "<table border=\"1\"><thead><tr><td>Modulname</td>";
		// Header
		for (Appointment timeSlot : getSortedTimeSlots()) {
			html += "<td class='slot-" + (isBanned(timeSlot) ? "banned'" : "free") + "'>" + timeSlot.toString()
					+ "</td>";
		}
		html += "</tr></thead><tbody>";
		// Table body
		for (GroupCrawling groupCrawling : groupCrawlings) {
			html += groupCrawling.toHTML();
		}
		return html + "</tbody></table>";
	}

	private static String getTimetableOutputHTML() {
		String html = "<table border=\"1\" style=\" float: left; width: 88vw; \"><thead>";
		html += "<tr><td><b>Zeit/ Tag</b></td>";
		for(Weekday weekday : Weekday.values()) {
			html += "<td><b>" + weekday.getShortForm() + "</b></td>";
		}
		html += "</tr></thead><tbody>";
		for(Appointment timeSlot : getSortedTimeSlotsWithoutWeekday()) {
			html += "<tr><td><b>" + timeSlot.toString().replace("Mo ", "") + "</b></td>";
			for(Weekday weekday : Weekday.values()) {
				Appointment timeSlotOnWeekday = new Appointment(weekday, timeSlot.getStartTime(), timeSlot.getLocation());
				Optional<Appointment> lecture = getLectureInGivenTimeSlot(timeSlotOnWeekday);
				if(lecture.isPresent()) {
					html += "<td style=\" background: " + getModuleColor(lecture.get().getName()) + "\"><b>VL " + lecture.get().getName() + "</b></td>";
					continue;
				}
				html += "<td>"
						+ groupCrawlings.stream()
											.flatMap(gc -> gc.getExerciseGroups().stream())
											.filter(eg -> eg.getAppointment().equals(timeSlotOnWeekday))
											.map(eg -> eg.toString())
											.collect(Collectors.joining())
						+ "</td>";
			}
			
		}
		html += "</tr></tbody></table>";
		return html; 
	}
	
	private static String getColorTableOutputHTML() {
		String html = "<table border=\"1\"><thead><tr>";
		for(String module : moduleColors.keySet()) {
			html += "<td><b>" + module + "</b></td>";
		}
		html += "</tr></thead><tbody><tr>";
		for(String module : moduleColors.keySet()) {
			html += "<td><span style=\" background: " + moduleColors.get(module) + " \">" + moduleColors.get(module) + "</span></td>";
		}
		html += "</tr></tbody></table>";
		return html;
	}
	
	private static String getVerticalColorTableOutputHTML() {
		String html = "<table border=\"1\" style=\" float: right; width: 10vw; \"><thead>";
		for(String module : moduleColors.keySet()) {
			html += "<tr><td style=\" background: " + moduleColors.get(module) + "; padding: 0.5rem 0.3rem; \"><b>" + module + "</b></td></tr>";
		}
		return html;
	}
	
	public static String getModuleColor(String moduleName) {
		if(moduleColors.containsKey(moduleName)) {
			return moduleColors.get(moduleName);
		}
//		Color color = Color.getHSBColor(new Random().nextFloat(),
//	            new Random().nextFloat(), new Random().nextFloat()).brighter();
//		String colorCode = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
//		while(moduleColors.containsValue(colorCode)) {
//			color = Color.getHSBColor(new Random().nextFloat(),
//		            new Random().nextFloat(), new Random().nextFloat()).brighter();
//			colorCode = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
//		}
		for(String color : hexColors) {
			if(!moduleColors.containsValue(color)) {
				moduleColors.put(moduleName, color);
				return color;
			}
		}
		return "red";
	}

	public static boolean isBanned(final Appointment timeSlot) {
		return lectures.stream().filter(t -> t.equals(timeSlot)).count() > 0;
	}

	public static boolean isBannedByModule(final Appointment timeSlot, final String moduleName) {
		return lectures.stream().filter(t -> t.equals(timeSlot) && t.getName().equals(moduleName)).count() > 0;
	}
	
	public static Optional<Appointment> getLectureInGivenTimeSlot(final Appointment timeSlot) {
		return lectures.stream().filter(t -> t.equals(timeSlot) && t.getName().length() > 0).findFirst();
	}

	public static List<String> getModuleNames() {
		return lectures.stream().map(t -> t.getName()).collect(Collectors.toList());
	}

	public static List<Appointment> getSortedTimeSlotsWithoutWeekday() {
		List<Appointment> appointments = lectures;
		for (GroupCrawling crawling : groupCrawlings) {
			appointments.addAll(crawling
					.getExerciseGroups().stream().map(eg -> eg.getAppointment())
					.collect(Collectors.toList()));
		}
		return appointments.stream()
				.map(a -> new Appointment(Weekday.MONDAY, a.getStartTime(), a.getLocation()))
				.distinct()
				.sorted()
				.collect(Collectors.toList());
	}

	public static List<Appointment> getSortedTimeSlots() {
		List<Appointment> appointments = new ArrayList<>();
		for (GroupCrawling crawling : groupCrawlings) {
			appointments.addAll(
					crawling.getExerciseGroups().stream().map(g -> g.getAppointment()).collect(Collectors.toList()));
		}
		return appointments.stream().distinct().sorted().collect(Collectors.toList());
	}

}
