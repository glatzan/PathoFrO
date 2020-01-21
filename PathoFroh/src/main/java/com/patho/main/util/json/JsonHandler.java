package com.patho.main.util.json;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Getter
@Setter
public class JsonHandler {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Returns a json string grabbed from the given url.
     *
     * @param url
     * @return
     */
    public String requestJsonData(String url) {

        StringBuffer response = new StringBuffer();

        try {

            logger.debug("Fetching patient json from clinic backend: " + url);

            URL obj = new URL(url);

            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            // add request header
            con.setRequestProperty("User-Agent", "Mozilla/5.0");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        logger.debug("Fetch string from clinic: " + response.toString());
        return response.toString();
    }
}
