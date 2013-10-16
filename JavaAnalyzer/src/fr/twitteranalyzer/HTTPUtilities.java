package fr.twitteranalyzer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTTPUtilities {

	protected String getHTML(String urlToRead) {
		URL url = null;
		HttpURLConnection conn = null;
		BufferedReader rd = null;
		String line = null;
		String result = "";
		
		try {
			url = new URL(urlToRead);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while ((line = rd.readLine()) != null) {
				result += line;
			}
			
			rd.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

}
