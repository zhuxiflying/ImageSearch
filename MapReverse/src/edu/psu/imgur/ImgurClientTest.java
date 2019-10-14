package edu.psu.imgur;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ImgurClientTest {

	private static String CLIENT_ID = "bc9c8f4dcc99ac6";
	private static String CLIENT_SECRET = "a7c94c0a041850461f6b10a0ff4beb255ab94bb9";

	// public static final String BASE_URI =
	// "https://api.imgur.com/3/gallery/tag_info/map";
	public static final String BASE_URI = "https://api.imgur.com/3/gallery/t/map/viral/";
	private static String dataFolder = "D:\\ViralMap\\data\\ImgurData\\";

	public static void main(String[] args) throws Exception {

		int total_item = 1383;
		int total_pages = total_item / 60 + 1;

		// HashSet<String> ids = new HashSet<String>();

		ArrayList<String[]> dataItem = new ArrayList<String[]>();

		for (int i = 0; i < 100; i++) {
//			 String queryString = BASE_URI + i;
//			 String message = queryImgur(queryString);
//			 PrintWriter out = new
//			 PrintWriter(dataFolder+"map_gallery_"+i+".json");
//			 out.println(message);
//			 out.close();

			String jsonFileName = "map_gallery_" + i + ".json";
			JSONObject gallery = loadJSONFile(jsonFileName);
			JSONObject data = (JSONObject) gallery.get("data");
			JSONArray items = (JSONArray) data.get("items");

			for (int j = 0; j < items.size(); j++) {
				JSONObject item = (JSONObject) items.get(j);
				String[] datalist = new String[13];
				datalist[0] = (String) item.get("id");
				datalist[1] = (String) item.get("title");
				datalist[2] = (String) item.get("descirption");
				datalist[3] = String.valueOf(item.get("datetime"));
				datalist[4] = (String) item.get("account_url");
				datalist[5] = String.valueOf(item.get("account_id"));
				datalist[6] = String.valueOf(item.get("views"));
				datalist[7] = (String) item.get("link");
				datalist[8] = String.valueOf(item.get("ups"));
				datalist[9] = String.valueOf(item.get("downs"));
				datalist[10] = String.valueOf(item.get("points"));
				datalist[11] = String.valueOf(item.get("score"));
				datalist[12] = String.valueOf(item.get("in_most_viral"));
				dataItem.add(datalist);
				// ids.add(id);
			}
			// System.out.println(items.size());

		}

		FileWriter out = new FileWriter(dataFolder + "Imgur_map_samples.csv");
		CSVPrinter printer = CSVFormat.DEFAULT.withHeader("id", "title", "descirption", "datetime", "account_url",
				"account_id", "views", "links", "ups", "downs", "points", "score", "in_most_viral").print(out);
		for (String[] datali : dataItem) {
			printer.printRecord(datali);
		}
		out.close();

	}

	private static JSONObject loadJSONFile(String jsonFileName) throws Exception {
		JSONParser parser = new JSONParser();
		String jsonFile = dataFolder + jsonFileName;
		Object obj = parser.parse(new FileReader(jsonFile));
		JSONObject gallery = (JSONObject) obj;
		return gallery;
	}

	private static String queryImgur(String queryString) throws IOException {
		// send request
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(queryString);
		request.addHeader("Authorization", "Client-ID " + CLIENT_ID);
		HttpResponse response = client.execute(request);
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		String line;
		String message = "";
		while ((line = rd.readLine()) != null) {
			message += line;
		}
		return message;
	}

}
