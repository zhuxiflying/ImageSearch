package edu.psu.geography;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * parse search result from tineye api (json file), the json file parsing is
 * based on a understanding of structure of json file returned by tineye api
 * 
 * @author zhuxi
 *
 */
public class SearchResultParsing {

	private static String file_path = "D:\\ViralMap\\";
	private static String exampleName = "test4";
	private static JSONObject search_result = null;

	public static void main(String[] args) throws Exception {
		loadJSONFile();
		// resultInfo();
		// resultAnalyzer();
	    iconImageDownload();
		originImageDownload();
	}

	/**
	 * parse json file into json object;
	 * 
	 * @throws Exception
	 */
	private static void loadJSONFile() throws Exception {
		JSONParser parser = new JSONParser();
		String jsonFile = file_path + exampleName + "\\"+exampleName+".json";
		Object obj = parser.parse(new FileReader(jsonFile));
		search_result = (JSONObject) obj;

	}

	/**
	 * out print basic information of search results
	 */
	private static void resultInfo() {
		JSONObject result = (JSONObject) search_result.get("results");
		JSONArray matches = (JSONArray) result.get("matches");
		System.out.println("#results:" + matches.size());

		for (int i = 0; i < matches.size(); i++) {
			System.out.println("#########################################################");
			System.out.println("ResultID: " + i);
			JSONObject match = (JSONObject) matches.get(i);
			String domainName = (String) match.get("domain");
			System.out.println("Domain Name:" + domainName);
			JSONArray backlinks = (JSONArray) match.get("backlinks");
			System.out.println("Number of Backlinks:" + backlinks.size());
			for (int j = 0; j < backlinks.size(); j++) {
				System.out.println("**********************************************************");
				System.out.println("BackLink ID: " + j);
				JSONObject backlink = (JSONObject) backlinks.get(j);
				String url = (String) backlink.get("url");
				System.out.println("URL:" + url);
				String crawl_date = (String) backlink.get("crawl_date");
				System.out.println("Crawl Date:" + crawl_date);
				String link = (String) backlink.get("backlink");
				System.out.println("Link:" + link);
			}
			double score = (double) match.get("score");
			System.out.println("Score:" + score);
			String image_url = (String) match.get("image_url");
			System.out.println("Image URL:" + image_url);
		}
	}

	private static void resultAnalyzer() {
		HashMap<String, ArrayList<String[]>> unique_images = new HashMap<String, ArrayList<String[]>>();
		JSONObject result = (JSONObject) search_result.get("results");
		JSONArray matches = (JSONArray) result.get("matches");
		for (int i = 0; i < matches.size(); i++) {
			JSONObject match = (JSONObject) matches.get(i);
			JSONArray backlinks = (JSONArray) match.get("backlinks");
			for (int j = 0; j < backlinks.size(); j++) {
				JSONObject backlink = (JSONObject) backlinks.get(j);
				String url = (String) backlink.get("url");
				String crawl_date = (String) backlink.get("crawl_date");
				String citeLink = (String) backlink.get("backlink");
				if (unique_images.get(url) != null) {
					ArrayList<String[]> links = unique_images.get(url);
					String[] link = new String[3];
					link[0] = String.valueOf(i);
					link[1] = crawl_date;
					link[2] = citeLink;
					links.add(link);
					unique_images.put(url, links);
				} else {
					ArrayList<String[]> links = new ArrayList<String[]>();
					String[] link = new String[3];
					link[0] = String.valueOf(i);
					link[1] = crawl_date;
					link[2] = citeLink;
					links.add(link);
					unique_images.put(url, links);
				}
			}
		}
		System.out.println(unique_images.size());
		for (String url : unique_images.keySet()) {
			System.out.println(url);
			System.out.println("#########################################################");
			ArrayList<String[]> links = unique_images.get(url);
			for (int i = 0; i < links.size(); i++) {
				String[] citeLink = links.get(i);
				System.out.println(citeLink[0] + "," + citeLink[1] + "," + citeLink[2]);
			}
		}
	}

	/*
	 * download original image; add a hashmap to avoid duplicate download; mark
	 * unavailable image url;
	 */
	private static void originImageDownload() throws IOException {
		int id = 0;
		// add a hashmap to avoid duplicate download
		HashMap<String, String> origin_images = new HashMap<String, String>();
		JSONObject result = (JSONObject) search_result.get("results");
		JSONArray matches = (JSONArray) result.get("matches");
		// retrieve the backlinks of the json file to download original images
		for (int i = 0; i < matches.size(); i++) {
			JSONObject match = (JSONObject) matches.get(i);
			JSONArray backlinks = (JSONArray) match.get("backlinks");
			for (int j = 0; j < backlinks.size(); j++) {
				JSONObject backlink = (JSONObject) backlinks.get(j);
				String url = (String) backlink.get("url");
				if (origin_images.get(url) == null) {

					try {
						String local_url = file_path+exampleName+"\\origin\\" + id;
						downloadImage(url, local_url);
						origin_images.put(url, local_url);
						id++;
						System.out.println(url + "," + local_url);
					} catch (Exception e) {
						// mark the image as unavailable if throw exception when downloading
						origin_images.put(url, "unavailable");
						System.out.println(url + ",unavailable");
					}
				}
			}
		}

		// record the mapping in the file;
		String filename = file_path+exampleName+"\\origin\\image_mapping.csv";
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
	private static void iconImageDownload() throws IOException {
		JSONObject result = (JSONObject) search_result.get("results");
		JSONArray matches = (JSONArray) result.get("matches");
		for (int i = 0; i < matches.size(); i++) {
			JSONObject match = (JSONObject) matches.get(i);
			String image_url = (String) match.get("image_url");
			//use the same file name as the name for local files
			String local_url = image_url.replace("http://img.tineye.com/result/",
					file_path + exampleName + "\\tineye\\");
			System.out.println(i + ":" + image_url + "," + local_url);
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
		httpcon.setConnectTimeout(10000);
		// consider the link as dead if the read time longer than 10 seconds
		httpcon.setReadTimeout(10000);

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

		FileOutputStream fos = new FileOutputStream(outputfile);
		fos.write(response);
		fos.close();
	}
}
