package com.patho.main.template.print;

import com.patho.main.template.PrintDocument;

/**
 * slideNumber, slideName, slideText, date
 * 
 * @author andi
 *
 */
public class SlideLable extends PrintDocument {
	public SlideLable(PrintDocument document) {
		super(document);
	}
}

// String result = getFileContent().replaceAll("%slideNumber%",
// task.getTaskID() + HistoUtil.fitString(slide.getUniqueIDinTask(), 3, '0'));
// result = result.replaceAll("%slideName%", task.getTaskID() + " " +
// slide.getSlideID());
// result = result.replaceAll("%slideText%", slide.getCommentary());
// LocalDateTime ldt = LocalDateTime.ofInstant(date.toInstant(),
// ZoneId.systemDefault());
// result = result.replaceAll("%date%",
// ldt.format(DateTimeFormatter.ofPattern(DateFormat.GERMAN_DATE.getDateFormat())));
