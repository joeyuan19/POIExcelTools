package excelUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
 * class Helper
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Helper methods and miscellaneous tools for Extension of Apache POI
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * To do:
 * 		[ ] work on date handling
 * 		[ ] work on CSV parsing to further generalize
 * 		[ ] String < - > Date 
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

public class Helper {
	public final static String DATE_REGEX = "("
			+ "(19|20)?[0-9]{2}"
			+ "([-/.\\\\]{1})"
			+ "[0?[1-9]|[1-9]|1[012]]{1,2}"
			+ "\\3"
			+ "([0?[1-9]|[1-9]|1[0-9]|2[0-9]|3[01]]{1,2})"
			+ ")"
			+ "|"
			+ "("
			+ "[0?[1-9]|[1-9]|1[012]]{1,2}"
			+ "([-/.\\\\]{1})"
			+ "([0?[1-9]|[1-9]|1[0-9]|2[0-9]|3[01]]{1,2})"
			+ "\\6"
			+ "(19|20)?[0-9]{2}"
			+ ")";
	/* Date Handling tools */
	public static boolean isWeekend(Calendar date) {
		return date.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
				date.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
	}
	public static Calendar getLastWeekday(Calendar date) {
		Calendar _date = Calendar.getInstance();
		_date.setTime(date.getTime());
		while (isWeekend(_date)) {
			_date.add(Calendar.DATE,-1);
		}
		return _date;
	}
	public static boolean isDate(String s) {
		Pattern pattern = Pattern.compile(DATE_REGEX);
		String[] terms = s.split(" ");
		Matcher matcher;
		for (String term: terms) {
			matcher = pattern.matcher(term);
			while (matcher.find()) {
				return true;
			}
		}
		return false;
	}
	public static String getDate(String entry) {
		Pattern pattern = Pattern.compile(DATE_REGEX);
		String[] terms = entry.split(" ");
		Matcher matcher;
		for (String term: terms) {
			matcher = pattern.matcher(term);
			while (matcher.find()) {
				return matcher.group();
			}
		}
		return "";
	}
	public static int compareDates(String date1, String date2, String format) {
		/*
		// A Nice Regex Dream, maybe one day
		System.out.println("ORIGINAL: " + format);
		String format_regex = format.replaceAll("(yy(yy)?)", "(?<year>$1)");
		format_regex = format_regex.replaceAll("(mm(mm)?)","(?<month>$1)");
		format_regex = format_regex.replaceAll("(d(d)?)", "(?<day>$1)");
		format_regex = format_regex.replaceAll("(?<!\\<)d","\\\\\\\\d");
		format_regex = format_regex.replaceAll("(?<!\\<)m","\\\\\\\\d");
		format_regex = format_regex.replaceAll("(?<!\\<)y","\\\\\\\\d");
		System.out.println("FINAL: " + format_regex);
		Pattern p = Pattern.compile(format_regex);
		Matcher m = p.matcher(date1);
		while (m.find()){
			System.out.println(m.group());
		}*/
		if (format.length() == 0) {
			return 0;
		}
		char c;
		int m_start = 0,m_count = 0,
				d_start = 0,d_count = 0,
				y_start = 0,y_count = 0;
		for (int i = 0; i < format.length(); i++) {
			c = format.charAt(i);
			if (c == 'm') { 
				if (m_count == 0) {
					m_start = i;
				}
				m_count++;
			}
			if (c == 'd') { 
				if (d_count == 0) {
					d_start = i;
				}
				d_count++;
			}
			if (c == 'y') { 
				if (y_count == 0) {
					y_start = i;
				}
				y_count++;
			}
		}
		if (y_count > 0) {
			if (Integer.parseInt(date1.substring(y_start, y_start+y_count)) > Integer.parseInt(date2.substring(y_start,y_start+y_count))) {
				return 1;
			} else if (Integer.parseInt(date1.substring(y_start, y_start+y_count)) < Integer.parseInt(date2.substring(y_start,y_start+y_count))) {
				return -1;
			}
		}
		if (m_count > 0) {
			if (Integer.parseInt(date1.substring(m_start, m_start+m_count)) > Integer.parseInt(date2.substring(m_start,m_start+m_count))) {
				return 1;
			} else if (Integer.parseInt(date1.substring(m_start, m_start+m_count)) < Integer.parseInt(date2.substring(m_start,m_start+m_count))) {
				return -1;
			}
		}
		if (d_count > 0) {
			if (Integer.parseInt(date1.substring(d_start, d_start+d_count)) > Integer.parseInt(date2.substring(d_start,d_start+d_count))) {
				return 1;
			} else if (Integer.parseInt(date1.substring(d_start, d_start+d_count)) < Integer.parseInt(date2.substring(d_start,d_start+d_count))) {
				return -1;
			}
		}
		return 0;
	}
	public static Calendar stringToCalendar(String date) {
		return stringToCalendar(date,"mm/dd/yy");
	}
	public static Calendar stringToCalendar(String date, String pattern) {
		return null;
	}
	public static Date stringToDate(String date) {
		return stringToDate(date,"mm/dd/yy");
	}
	public static Date stringToDate(String date, String pattern) {
		return null;
	}
	/**/
	public static ArrayList<String> parseCSVLine(String CSVLine,char delimChar,char quotChar) {
		char itr;
		boolean inQuotedValue = false;
		String buffer = "";
		ArrayList<String> parsedLine = new ArrayList<String>();
		for (int i = 0; i < CSVLine.length(); i++) {
			itr = CSVLine.charAt(i);
			if (itr == delimChar) {
				if (!inQuotedValue) {
					parsedLine.add(buffer);
					buffer = "";
				}
			} else if (itr == quotChar) {
				inQuotedValue = !inQuotedValue;
			} else {
				buffer += itr;
			}
		}
		if (buffer.length() > 0) {
			parsedLine.add(buffer);
		}
		String entry;
		for (int i = 0; i < parsedLine.size(); i++) {
			entry = parsedLine.get(i);
			entry.trim();
			if (entry.startsWith("(") && entry.endsWith(")") && isNumeric(entry.substring(1,entry.length()-1))) {
				parsedLine.set(i, "-" + entry.substring(1,entry.length()-1)); 
			}
		}
		return parsedLine;
	}
	public static boolean isNumeric(String s) {
		try {
			Float.parseFloat(s);
			return true;
		} catch (NumberFormatException e) {
			try {
				Integer.parseInt(s);
				return true;
			} catch (NumberFormatException e1) {
				try {
					Long.parseLong(s);
					return true;
				} catch (NumberFormatException e2) {
					return false;
				}
			}
		}
	}
	public static void main(String args[]) {
		File f = new File(FileUtils.joinPath(FileUtils.getPWD(),"bpmon.csv.140123110802.csv"));
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(f));
			String line;
			char d = ',', q='\"';
			try {
				while ((line = reader.readLine()) != null) {
					Helper.parseCSVLine(line,d,q);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
}
