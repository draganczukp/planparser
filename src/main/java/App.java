import java.net.URI;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class App {

	private static int day = -1;
	private static Map<String, Integer> colorMap = new HashMap<>();
	private static JsonObject root = new JsonObject();
	private static final String id = "22121";

	public static void main(String[] args) {
		try {
			Document doc = Jsoup.connect("http://www.plan.uz.zgora.pl/grupy_plan.php?pId_Obiekt=" + id).get();
			// System.out.println(doc.charset().toString());
			Element table = doc.getElementsByTag("table").first();

			root.add("daysOff", new JsonArray());
			root.add("lessons", new JsonArray());

			table.children()
			     .first()
			     .children()
			     .stream()
			     .filter(e -> isGroupA(e))
			     .forEach(App::parseElement);

			root.addProperty("name", "Generator");

			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String out = gson.toJson(root);
			System.out.println(out);
			String fileName = String.format("Generator_%s.json", LocalDate.now().toString());
			Files.writeString(Path.of(fileName), out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static boolean isGroupA(Element e){
		String text = e.child(0).text();
		return !text.equals("B") && !text.equals("C");
	}

	private static void parseElement(Element e) {
		if (e.className().equals("gray")) {
			day++;
			return;
		}
		List<Element> tds = new ArrayList<>(e.children());

		PlanElement pe = new PlanElement();

		pe.from(tds.get(1).text());
		pe.to(tds.get(2).text());
		pe.name = tds.get(3).text();
		pe.teacher = tds.get(5).child(0).text();
		pe.loc = tds.get(6).text();
		pe.term(tds.get(7).text());

		int color;

		if (colorMap.containsKey(pe.name)) {
			color = colorMap.get(pe.name);
		} else {
			int r = (int) Math.abs(Math.floor(Math.random() * 255f));
			int g = (int) Math.abs(Math.floor(Math.random() * 255f));
			int b = (int) Math.abs(Math.floor(Math.random() * 255f));

			color = 0xff000000 | r << 16 | g << 8 | b;
			colorMap.put(pe.name, color);
		}

		JsonObject lesson = new JsonObject();
		lesson.addProperty("color", color);
		lesson.add("exams", new JsonArray());
		lesson.add("grades", new JsonArray());
		lesson.addProperty("name", pe.name);

		JsonObject lessonDet = new JsonObject();
		lessonDet.add("date", JsonNull.INSTANCE);
		lessonDet.addProperty("day", day);
		lessonDet.addProperty("hourStart", pe.from);
		lessonDet.addProperty("hourEnd", pe.to);
		lessonDet.addProperty("place", pe.loc);
		lessonDet.addProperty("week", pe.term);

		JsonObject teacher = new JsonObject();
		teacher.addProperty("consultations", "");
		teacher.addProperty("email", "");
		teacher.add("imageName", JsonNull.INSTANCE);
		teacher.addProperty("name", pe.teacher);
		teacher.addProperty("phone", "");
		teacher.addProperty("room", "");

		lessonDet.add("teacher", teacher);

		lesson.add("lessonDetails", new JsonArray());
		lesson.get("lessonDetails").getAsJsonArray().add(lessonDet);
		lesson.add("lessonType", new JsonObject());
		lesson.get("lessonType").getAsJsonObject().addProperty("name", pe.name);
		lesson.add("unprepared", new JsonArray());

		root.get("lessons").getAsJsonArray().add(lesson);
	}

	public static class PlanElement {
		int from, to;
		String name, teacher;
		String loc;
		int term;

		void from(String t) {
			String[] split = t.split(":");
			int h = Integer.parseInt(split[0]) * 60;
			int m = Integer.parseInt(split[1]);
			from = h + m;
		}

		void to(String t) {
			String[] split = t.split(":");
			int h = Integer.parseInt(split[0]) * 60;
			int m = Integer.parseInt(split[1]);
			to = h + m;
		}

		void term(String t) {
			switch (t) {
				case "D/":
				case "DI/":
				case "DII/":
					term = 0;
					break;
				case "DP/":
					term = 1;
					break;
				case "DN/":
					term = 2;
					break;
				default:
					System.out.println("Daty w " + this.name);
			}
		}

		@Override
		public String toString() {
			return String.format("[From->To]=>%d->%d\n[Name]=>%s\n[Teacher]=>%s\n[Loc]=>%s\n[Term]=>%d",
			                     from,
			                     to,
			                     name,
			                     teacher,
			                     loc,
			                     term);
		}
	}
}
