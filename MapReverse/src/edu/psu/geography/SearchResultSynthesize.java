package edu.psu.geography;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class SearchResultSynthesize {

	private static String file_path = "D:\\Projects\\ViralMap\\viral_examples\\";
	private static String exampleName = "NateSilver_Example";
	private static String ImageMapping_fileName = "D:\\Projects\\ViralMap\\viral_examples\\NateSilver_Example\\origin\\image_mapping.csv";
	private static JSONObject search_result = null;

	// the mapping file recording original image url and local image url;
	private static HashMap<String, String> originImageMapping = null;
	// the local image url and related web entities detected by google computer
	// vision;
	private static HashMap<String, HashMap<String, Double>> image_entities = null;
	// the local image url and related web labels detected by google computer
	// vision;
	private static HashMap<String, HashMap<String, Double>> image_labels = null;

	public static void main(String[] args) throws Exception {
		loadJSONFile();

		loadLocalImage();

		loadWebEntities();

		loadWebLabels();

		JSONObject result = (JSONObject) search_result.get("results");
		JSONArray matches = (JSONArray) result.get("matches");
		JSONArray matchJson = new JSONArray();
		for (int i = 0; i < matches.size(); i++) {
			JSONObject match = (JSONObject) matches.get(i);
			JSONObject match2 = new JSONObject();
			String domainName = (String) match.get("domain");
			match2.put("Domain", domainName);
//			System.out.println("Domain Name:" + domainName);
			double score = (double) match.get("score");
			match2.put("Score", score);
//			System.out.println("Score:" + score);
			String image_url = (String) match.get("image_url");
			String local_url = image_url.replace("http://img.tineye.com/result/", exampleName + "\\tineye\\");
			match2.put("Image_url", local_url);
//			System.out.println("Image URL:" + local_url);

			JSONArray backlinks = (JSONArray) match.get("backlinks");
			Date earliest_crawl_date = null;
			String localImage = null;
			String links = null;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//			System.out.println("Number of Backlinks:" + backlinks.size());
			HashSet<String> origin_image = new HashSet<String>();
			for (int j = 0; j < backlinks.size(); j++) {
//				System.out.println("**********************************************************");
//				System.out.println("BackLink ID: " + j);
				JSONObject backlink = (JSONObject) backlinks.get(j);
				String url = (String) backlink.get("url");
				String loc_url = originImageMapping.get(url);
				origin_image.add(loc_url);
//				System.out.println("URL:" + url);
				String crawl_date = (String) backlink.get("crawl_date");
				String link = (String) backlink.get("backlink");
				Date date = sdf.parse(crawl_date);
				if (earliest_crawl_date == null) {
					earliest_crawl_date = date;
					localImage = loc_url;
					links = link;
				} else {
					if (date.before(earliest_crawl_date)) {
						earliest_crawl_date = date;
						localImage = loc_url;
						links = link;
					}
				}

//				System.out.println("Link:" + link);
			}
			match2.put("Crawl_Date", sdf.format(earliest_crawl_date));

			if (image_entities.get(localImage) != null) {
				HashMap<String, Double> entity = image_entities.get(localImage);
				JSONObject entityJson = new JSONObject(entity);
				match2.put("entity", entityJson);
			} else {
				match2.put("entity", null);
			}

			if (image_labels.get(localImage) != null) {
				HashMap<String, Double> labels = image_labels.get(localImage);
				JSONObject labelJson = new JSONObject(labels);
				match2.put("label", labelJson);
			} else {
				match2.put("label", null);
			}

			if (!localImage.equals("unavailable")) {
				String origin_url = localImage.replace("D:\\Projects\\ViralMap\\viral_examples\\", "");
				match2.put("OriginImage", origin_url);
			}
			match2.put("Link", links);
//			System.out.println("Crawl Date:" + sdf.format(earliest_crawl_date));
//			System.out.println("Image:" + localImage);
//			System.out.println("Link:" + links);
			System.out.println(i + "," + backlinks.size() + "," + origin_image.size());
			matchJson.add(match2);
		}
//		System.out.println("#results:" + matches.size());
		PrintWriter out = new PrintWriter(file_path + exampleName + "\\NateSilver_Example2.json");
		out.println(matchJson.toString());
		out.close();
	}

	/**
	 * parse json file into json object;
	 * 
	 * @throws Exception
	 */
	private static void loadJSONFile() throws Exception {
		JSONParser parser = new JSONParser();
		String jsonFile = file_path + exampleName + "\\NateSilver_Example.json";
		Object obj = parser.parse(new FileReader(jsonFile));
		search_result = (JSONObject) obj;

	}

	/*
	 * For the image url in the search result file, we download the original images
	 * in advance and save them to local. This method used to load file recording
	 * the file paths of local images.
	 */
	private static void loadLocalImage() throws Exception {
		// TODO Auto-generated method stub
		originImageMapping = new HashMap<String, String>();
		Reader in = new FileReader(ImageMapping_fileName);
		Iterable<CSVRecord> records = CSVFormat.EXCEL.withFirstRecordAsHeader().parse(in);
		for (CSVRecord record : records) {
			String image_url = record.get("image_url");
			String loca_url = record.get("loca_url");
			originImageMapping.put(image_url, loca_url);
		}
	}

	/*
	 * Load web entities detected by google computer vision for each original image
	 */
	private static void loadWebEntities() throws FileNotFoundException, IOException, ParseException {
		image_entities = new HashMap<String, HashMap<String, Double>>();
		JSONParser parser = new JSONParser();
		String jsonFile = file_path + exampleName + "\\entity_json.json";
		JSONArray entityJson = (JSONArray) parser.parse(new FileReader(jsonFile));
		for (int i = 0; i < entityJson.size(); i++) {
			JSONObject ob = (JSONObject) entityJson.get(i);
			String url = (String) ob.get("url");
			JSONObject entity = (JSONObject) ob.get("entities");
			HashMap<String, Double> entity_mapping = new HashMap<String, Double>();
			for (Object key : entity.keySet()) {
				entity_mapping.put((String) key, (Double) entity.get(key));
			}
			image_entities.put(url, entity_mapping);
		}
	}

	/*
	 * Load web labels detected by google computer vision for each original image
	 */
	private static void loadWebLabels() throws FileNotFoundException, IOException, ParseException {
		image_labels = new HashMap<String, HashMap<String, Double>>();
		JSONParser parser = new JSONParser();
		String jsonFile = file_path + exampleName + "\\label_json.json";
		JSONArray entityJson = (JSONArray) parser.parse(new FileReader(jsonFile));
		for (int i = 0; i < entityJson.size(); i++) {
			JSONObject ob = (JSONObject) entityJson.get(i);
			String url = (String) ob.get("url");
			JSONObject entity = (JSONObject) ob.get("entities");
			HashMap<String, Double> entity_mapping = new HashMap<String, Double>();
			for (Object key : entity.keySet()) {
				entity_mapping.put((String) key, (Double) entity.get(key));
			}
			image_labels.put(url, entity_mapping);
		}
	}

}
