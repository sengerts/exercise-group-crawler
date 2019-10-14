package de.sengerts.groupcrawler.group;

import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import de.sengerts.groupcrawler.Main;
import de.sengerts.groupcrawler.appointment.Appointment;
import de.sengerts.groupcrawler.appointment.AppointmentAlreadyAddedException;
import de.sengerts.groupcrawler.weekday.Weekday;
import de.sengerts.groupcrawler.weekday.WeekdayNotFoundException;

public class GroupCrawling {

	// Constants
	private static final String MODULE_NAME_TD_CLASS = " MaskRenderer";
	private static final String MODULE_NAME_SPAN_CLASS = "bold ";
	private static final String TABLE_ID = "tabLvTermine";
	private static final String GROUP_HEADER_CLASS = "coRow coTableGR1   ";

	// Web Client to crawl the pages
	private static WebClient client;
	static {
		client = new WebClient();
		client.getOptions().setCssEnabled(false);
		client.getOptions().setJavaScriptEnabled(false);
		client.getOptions().setUseInsecureSSL(true);
		client.getOptions().setUseInsecureSSL(true);
	}

	private final String moduleName;
	private final String groupsUrl;
	private final List<ExerciseGroup> exerciseGroups;
	private final HtmlPage page;

	public GroupCrawling(final String groupsUrl)
			throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		super();
//		this.groupsUrl = "https://de.4everproxy.com/direct/" + new String(Base64.encodeBase64(groupsUrl.getBytes()));
		this.groupsUrl = groupsUrl;
		this.exerciseGroups = new LinkedList<>();
		this.page = client.getPage(groupsUrl);
		this.moduleName = fetchModuleName();
	}

	public void crawlGroups() throws WeekdayNotFoundException, AppointmentAlreadyAddedException {
		for (HtmlElement groupHeader : getGroupHeaders()) {
			String groupName = groupHeader.asText().replace("Gruppe ", "");
			if (groupName.contains("Nicht vorhanden"))
				continue;

			List<DomElement> appointmentInformation = new ArrayList<>();
			groupHeader.getParentNode().getNextElementSibling().getChildElements().forEach(appointmentInformation::add);

			Weekday weekday = Weekday.getWeekday(appointmentInformation.get(0).asText());
			LocalTime startTime = LocalTime.parse(appointmentInformation.get(2).asText() + ":00");
			String location = appointmentInformation.get(4).asText();

			ExerciseGroup group = new ExerciseGroup(moduleName, groupName, new Appointment(weekday, startTime, location));
			exerciseGroups.add(group);
		}
	}

	private String fetchModuleName() {
		List<HtmlElement> nameElements = page.getByXPath(
				"/html/.//td[@class='" + MODULE_NAME_TD_CLASS + "']/.//span[@class='" + MODULE_NAME_SPAN_CLASS + "']");
		return nameElements.get(1).asText();
	}

	private List<HtmlElement> getGroupHeaders() {
		return page.getHtmlElementById(TABLE_ID)
				.getByXPath("/html/.//tbody/.//tr[@class='" + GROUP_HEADER_CLASS + "']/.//td");
	}

	public String toHTML() {
		StringBuilder builder = new StringBuilder("<tr><td>" + moduleName + "</td>");
		for(Appointment timeSlot : Main.getSortedTimeSlots()) {
			// Mark lecture slot pink: After td: " + (Main.isBannedByModule(timeSlot, moduleName) ? " class='slot-lecture'" : "") + "
			builder.append("<td>" + getGroupsFor(timeSlot).stream().map(g -> g.getName()).collect(Collectors.joining(", ")) + "</td>");
		}
		builder.append("</tr>");
		return builder.toString();
	}
	
	private List<ExerciseGroup> getGroupsFor(final Appointment appointment) {
		return exerciseGroups.stream().filter(g -> g.getAppointment().equals(appointment)).collect(Collectors.toList());
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("--- " + getModuleName() + " ---\n");
		getExerciseGroups().stream().sorted().forEach(group -> builder.append(" - " + group.toString() + "\n"));
		return builder.toString();
	}

	public String getModuleName() {
		return moduleName;
	}

	public String getGroupsUrl() {
		return groupsUrl;
	}

	public List<ExerciseGroup> getExerciseGroups() {
		return exerciseGroups;
	}

}
