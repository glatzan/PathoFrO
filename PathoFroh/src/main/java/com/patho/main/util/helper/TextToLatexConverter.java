package com.patho.main.util.helper;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class TextToLatexConverter {

    /**
     * \& \% \$ \# \_ \{ \}
     *
     * @author andi
     */
    Map<String, String> charMap = new HashMap<String, String>();

    public TextToLatexConverter() {
        charMap.put("\r\n", "\\\\ \r\n");
        charMap.put("&", "\\&");
        charMap.put("%", "\\%");
        charMap.put("$", "\\$");
        charMap.put("#", "\\#");
        charMap.put("{", "\\{");
        charMap.put("}", "\\}");
        charMap.put("\"", "\'\'");
        charMap.put("_", "\\_");
        charMap.put("^", "\\^");
        charMap.put("~", "\\~");
    }

    public String convertToTex(String string) {
        if (string == null)
            return "";

        for (Map.Entry<String, String> entry : charMap.entrySet()) {
            string = StringUtils.replace(string, entry.getKey(), entry.getValue());
        }
        return string;
    }
}
