import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document.OutputSettings;

/*
 * This Java source file was generated by the Gradle 'init' task.
 */
public class App {

	static int day = -1;
	static Map<String, Integer> colorMap = new HashMap<>();
	static StringBuffer out;
	public static void main(String[] args) {
		out = new StringBuffer();
		out.append("{\"daysOff\": [],\"lessons\": [");
		try {
			
			Document doc = Jsoup.parse(new URL("http://www.plan.uz.zgora.pl/grupy_plan.php?pId_Obiekt=19525").openStream(),"UTF-8","http://www.plan.uz.zgora.pl/grupy_plan.php?pId_Obiekt=19525");
			// doc.charset(Charset.forName("UTF-8"));;
			Element table = doc.getElementsByTag("table").first();//doc.selectFirst("table.table.table-condensed.table-bordered");

			table.children().first().children().stream().filter(e->!e.text().equals("B")).forEach(e->{
			
				// System.out.println("Class=> "+e.className());
				if(e.className().equals("gray")){
					// System.out.println("Gray");
					day++;
					return;
				}
				List<Element> tds = e.children().stream()
						// .peek(System.out::println)
						.collect(Collectors.toList());

				PlanElement pe = new PlanElement();
				

				pe.from(tds.get(1).text());
				pe.to(tds.get(2).text());
				pe.name = tds.get(3).text();
				pe.teacher = tds.get(5).child(0).text();
				pe.loc = tds.get(6).text();
				// System.out.println(tds.get(7).text());
				pe.term(tds.get(7).text());
				
				int color = 0xFFFFFFFF;
				if(colorMap.containsKey(pe.name)){
					color = colorMap.get(pe.name);
				}else{
					int r = (int)Math.abs(Math.floor(Math.random()*255f));
					int g = (int)Math.abs(Math.floor(Math.random()*255f));
					int b = (int)Math.abs(Math.floor(Math.random()*255f));

					color = 0xff | r << 16 | g << 8 | b;
					colorMap.put(pe.name, color);
				}

				out.append("\n").append("{")
				.append("\n").append("\"color\":"+ color)
				.append("\n").append(",\"exams\":[]")
				.append("\n").append(",\"grades\":[]")
				.append("\n").append(",\"lessonDetails\": [{")
					.append("\n").append("\"date\":null")
					.append("\n").append(",\"day\":"+day)
					.append("\n").append(",\"hourStart\": "+pe.from)
					.append("\n").append(",\"hourEnd\": "+pe.to)
					.append("\n").append(",\"place\": "+"\""+pe.loc+"\"")
					.append("\n").append(",\"teacher\": {")
						.append("\n").append("\"date\": null")
						.append("\n").append(",\"email\":\"\"")
						.append("\n").append(",\"imageName\":null")
						.append("\n").append(",\"name\":\""+pe.teacher+"\"")
						.append("\n").append(",\"phone\":\"\"")
						.append("\n").append(",\"room\":\"\"")
						.append("\n").append("},")
					.append("\n").append("\"week\": "+pe.term)
				.append("\n").append("}],")
				.append("\n").append("\"lessonType\": {")
					.append("\n").append("\"name\":\""+pe.name+"\"")
				.append("\n").append("},")
					.append("\n").append("\"name\":\""+pe.name+"\"")
				.append("\n").append(",\"unprepared\": []")
				.append("},")
				;
				
				// System.out.println(pe.toString());

			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		out.append("\n").append("],\"name\": \"Domyślny plan\"}");
		// System.out.println(out.toString());
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
			switch(t){
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
			StringBuffer sb = new StringBuffer();
			sb.append("[From->To]=>").append(from + "->" + to).append("\n").append("[Name]=>").append(name).append("\n")
					.append("[Teacher]=>").append(teacher).append("\n").append("[Loc]=>").append(loc).append("\n")
					.append("[Term]=>").append(term);
			return sb.toString();
		}
	}
}