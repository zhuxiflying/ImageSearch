package edu.psu.geography;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class MapReverseRunner {

	static void mapGetQuery(String imageUrl, String outputFolder, Boolean imageSearch, Boolean imageDownload,
			Boolean labelDection, Boolean entityDetection) {

		try {
			if (imageSearch) {
				String jsonString = ImageSearch.tinEyeGetQuery(imageUrl);
				JSONParser parser = new JSONParser();
				JSONObject search_result = (JSONObject) parser.parse(jsonString);
				resultInfo(search_result);
				saveTineyeResult(outputFolder, search_result);
				if(imageDownload)
				{
					iconImageDownload(outputFolder, search_result );
					originImageDownload(outputFolder, search_result);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	static void mapPostQuery(String imageUrl, String outputFolder, Boolean imageSearch, Boolean imageDownload,
			Boolean labelDection, Boolean entityDetection) {

		System.out.println("Post");
		System.out.println(imageUrl);
		System.out.println(outputFolder);
		System.out.println(imageSearch);
		System.out.println(imageDownload);
		System.out.println(labelDection);
		System.out.println(entityDetection);

		try {
			if (imageSearch) {
				String jsonString = ImageSearch.tinEyePostQuery(imageUrl);
				JSONParser parser = new JSONParser();
				JSONObject search_result = (JSONObject) parser.parse(jsonString);
				System.out.println(search_result.toJSONString());
				resultInfo(search_result);
				saveTineyeResult(outputFolder, search_result);
				if(imageDownload)
				{
					iconImageDownload(outputFolder, search_result );
					originImageDownload(outputFolder, search_result);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * out print basic information of search results
	 */
	private static void resultInfo(JSONObject search_result) {
		JSONObject result = (JSONObject) search_result.get("results");
		JSONArray matches = (JSONArray) result.get("matches");
		MapReverseDataCollector.log.append(matches.size() + " matches found"+"\n");
		
		

		// for (int i = 0; i < matches.size(); i++) {
		// String newline = "\n";
		// MapReverseDataCollector.log.append("#########################################################"
		// + newline);
		// MapReverseDataCollector.log.append("ResultID: " + i + newline);
		// JSONObject match = (JSONObject) matches.get(i);
		// String domainName = (String) match.get("domain");
		// MapReverseDataCollector.log.append("Domain Name:" + domainName +
		// newline);
		// JSONArray backlinks = (JSONArray) match.get("backlinks");
		// MapReverseDataCollector.log.append("Number of Backlinks:" +
		// backlinks.size() + newline);
		// for (int j = 0; j < backlinks.size(); j++) {
		// MapReverseDataCollector.log.append("**********************************************************"
		// + newline);
		// MapReverseDataCollector.log.append("BackLink ID: " + j + newline);
		// JSONObject backlink = (JSONObject) backlinks.get(j);
		// String url = (String) backlink.get("url");
		// MapReverseDataCollector.log.append("URL:" + url + newline);
		// String crawl_date = (String) backlink.get("crawl_date");
		// MapReverseDataCollector.log.append("Crawl Date:" + crawl_date +
		// newline);
		// String link = (String) backlink.get("backlink");
		// MapReverseDataCollector.log.append("Link:" + link + newline);
		// }
		// double score = (double) match.get("score");
		// MapReverseDataCollector.log.append("Score:" + score + newline);
		// String image_url = (String) match.get("image_url");
		// MapReverseDataCollector.log.append("Image URL:" + image_url +
		// newline);
		// }
	}

	private static void saveTineyeResult(String outputFolder, JSONObject search_result) {
		
		try (PrintWriter out = new PrintWriter(outputFolder+"\\match_results.json")) {
		    out.println(search_result.toJSONString());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	/*
	 * download original image; add a hashmap to avoid duplicate download; mark
	 * unavailable image url;
	 */
	private static void originImageDownload(String outputFolder,JSONObject search_result) throws IOException {
		int id = 0;
		// add a hashmap to avoid duplicate download
		HashMap<String, String> origin_images = new HashMap<String, String>();
		JSONObject result = (JSONObject) search_result.get("results");
		JSONArray matches = (JSONArray) result.get("matches");
		// retrieve the backlinks of the json file to download original images
		
		Path path = Paths.get(outputFolder+"\\origin");
		Files.createDirectories(path);
		
		for (int i = 0; i < matches.size(); i++) {
			JSONObject match = (JSONObject) matches.get(i);
			JSONArray backlinks = (JSONArray) match.get("backlinks");
			for (int j = 0; j < backlinks.size(); j++) {
				JSONObject backlink = (JSONObject) backlinks.get(j);
				String url = (String) backlink.get("url");
				if (origin_images.get(url) == null) {

					try {
						String local_url = outputFolder+"\\origin\\" + id;
						downloadImage(url, local_url);
						origin_images.put(url, local_url);
						id++;
						MapReverseDataCollector.log.append("Downloading original image...."+"\n");
						MapReverseDataCollector.log.update(MapReverseDataCollector.log.getGraphics());
						MapReverseDataCollector.log.setCaretPosition(MapReverseDataCollector.log.getDocument().getLength());
					} catch (Exception e) {
						// mark the image as unavailable if throw exception when downloading
						origin_images.put(url, "unavailable");
						MapReverseDataCollector.log.append("Downloading original image: Unavailable"+"\n");
						MapReverseDataCollector.log.update(MapReverseDataCollector.log.getGraphics());
						MapReverseDataCollector.log.setCaretPosition(MapReverseDataCollector.log.getDocument().getLength());
					}
				}
			}
		}

		// record the mapping in the file;
		String filename = outputFolder+"\\origin\\image_mapping.csv";
		FileWriter out = new FileWriter(filename);
		CSVPrinter printer = CSVFormat.DEFAULT.withHeader("image_url", "loca_url").print(out);

		for (String url : origin_images.keySet()) {
			printer.printRecord(url, origin_images.get(url));
		}
		out.close();
	}

	/*
	 * download icon images from tineye, name the downloaded as the same name as
	 * tineye image
	 */
	private static void iconImageDownload(String outputFolder,JSONObject search_result) throws IOException {
		
		Path path = Paths.get(outputFolder+"\\tineye");
		Files.createDirectories(path);
		
		
		JSONObject result = (JSONObject) search_result.get("results");
		JSONArray matches = (JSONArray) result.get("matches");
		for (int i = 0; i < matches.size(); i++) {
			JSONObject match = (JSONObject) matches.get(i);
			String image_url = (String) match.get("image_url");
			//use the same file name as the name for local files
			String local_url = image_url.replace("http://img.tineye.com/result/",
					outputFolder + "\\tineye\\");
			MapReverseDataCollector.log.append("Downloading tineye icon image: "+i+"\n");
			MapReverseDataCollector.log.update(MapReverseDataCollector.log.getGraphics());
			MapReverseDataCollector.log.setCaretPosition(MapReverseDataCollector.log.getDocument().getLength());
			downloadImage(image_url, local_url);
		}
	}

	/*
	 * download image by given url
	 */
	private static void downloadImage(String url_string, String outputfile) throws IOException {

		URL url = new URL(url_string);
		HttpURLConnection httpcon = (HttpURLConnection) url.openConnection();
		// Some websites don't like programmatic access so pretend to be a browser
		httpcon.addRequestProperty("User-Agent", "Mozilla/4.0");
		// consider the link as dead if no response within 5 seconds
		httpcon.setConnectTimeout(30000);
		// consider the link as dead if the read time longer than 10 seconds
		httpcon.setReadTimeout(50000);

		// if the request redirect the requested resources to another location, open a
		// new connection to new url;
		String redirect = httpcon.getHeaderField("Location");
		if (redirect != null) {
			httpcon = (HttpURLConnection) new URL(redirect).openConnection();
		}
		InputStream in = httpcon.getInputStream();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int n = 0;
		while (-1 != (n = in.read(buf))) {
			out.write(buf, 0, n);
		}
		out.close();
		in.close();
		byte[] response = out.toByteArray();

		FileOutputStream fos = new FileOutputStream(outputfile+".png");
		FileOutputStream fos2 = new FileOutputStream(outputfile+".jpg");
		fos.write(response);
		fos2.write(response);
		fos.close();
		fos2.close();
	}
}
