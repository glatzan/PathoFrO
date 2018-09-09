package com.patho.main.util.helper;

import static org.assertj.core.api.Assertions.entry;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class TextToLatexConverter {

	public String convertToTex(String string) {
		if (string == null)
			return "";

		for (Map.Entry<String, String> entry : charMap.entrySet()) {
			string = StringUtils.replace(string, entry.getKey(), entry.getValue());
		}
		return string;
	}

	/**
	 * \& \% \$ \# \_ \{ \}
	 * 
	 * @author andi
	 *
	 */
	Map<String, String> charMap = null;
//	Map.ofEntries(
//			 entry( "\r\n", "\\\\ \r\n" ), 
//			 entry( "&", "\\&" ), 
//			 entry( "%", "\\%" ), 
//			 entry( "$", "\\$" ), 
//			 entry( "#", "\\#" ),
//			 entry( "{", "\\{" ), 
//			 entry( "}", "\\}" ),
//			 entry( "\"", "\'\'" ), 
//			 entry( "_", "\\_" ) 
//		);
}
