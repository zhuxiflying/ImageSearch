package edu.psu.geography;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONObject;

import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.WebDetection;
import com.google.cloud.vision.v1.WebDetection.WebEntity;
import com.google.cloud.vision.v1.WebDetection.WebImage;
import com.google.cloud.vision.v1.WebDetection.WebLabel;
import com.google.cloud.vision.v1.WebDetection.WebPage;
import com.google.protobuf.ByteString;

/*
 * detect web entities and image labels by google computer vision
 */
public class WebEntityDetection {

	private static String file_path = "D:\\ViralMap\\";
	private static String exampleName = "test4";
	private static String fileName = file_path+exampleName+"\\origin\\image_mapping.csv";
	private static HashSet<String> imageList = null;
	private static HashMap<String, HashMap<String, Float>> map_entities = null;

	public static void main(String[] args) throws Exception {

		loadImageList();

		System.out.println(imageList.size());

		map_entities = new HashMap<String, HashMap<String, Float>>();
		
		// follow the instruction to setup the google authentication
		// https://cloud.google.com/docs/authentication/getting-started#setting_the_environment_variable
		ImageAnnotatorClient client = ImageAnnotatorClient.create();

		for (String url : imageList) {

			if (!url.equals("unavailable")) {
				System.out.println(url);
				ByteString imgBytes = ByteString.readFrom(new FileInputStream(url));

				List<AnnotateImageRequest> requests = new ArrayList<>();
				Image img = Image.newBuilder().setContent(imgBytes).build();
				Feature feat = Feature.newBuilder().setType(Type.WEB_DETECTION).build();
//				Feature feat = Feature.newBuilder().setType(Type.LABEL_DETECTION).build();
				AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img)
						.build();
				requests.add(request);
				BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
				List<AnnotateImageResponse> responses = response.getResponsesList();
				HashMap<String, Float> entities = new HashMap<String, Float>();
				for (AnnotateImageResponse res : responses) {
					if (res.hasError()) {
						System.out.printf("Error: %s\n", res.getError().getMessage());
					}
					// Search the web for usages of the image. You could use these signals later
					// for user input moderation or linking external references.
					// For a full list of available annotations, see http://g.co/cloud/vision/docs
//					WebDetection annotation = res.getWebDetection();
//					for (WebEntity entity : annotation.getWebEntitiesList()) {
//						entities.put(entity.getDescription(), entity.getScore());
//						System.out.println(
//								entity.getDescription() + " : " + entity.getEntityId() + " : " + entity.getScore());
//					}
					
					// For full list of available annotations, see http://g.co/cloud/vision/docs
					for (EntityAnnotation annotation : res.getLabelAnnotationsList()) {
						entities.put(annotation.getDescription(), annotation.getScore());
						System.out.println(
								annotation.getDescription() + " : " + annotation.getScore());
//						annotation.getAllFields().forEach((k, v) -> System.out.printf("%s : %s\n", k, v.toString()));
					}
				}
				map_entities.put(url, entities);
			}
		}
//		writeEntityJSonFile(file_path+exampleName+"\\entity_json.json");
		writeEntityJSonFile(file_path+exampleName+"\\label_json.json");
	}

	private static void loadImageList() throws Exception {
		// TODO Auto-generated method stub
		imageList = new HashSet<String>();
		Reader in = new FileReader(fileName);
		Iterable<CSVRecord> records = CSVFormat.EXCEL.withFirstRecordAsHeader().parse(in);
		for (CSVRecord record : records) {
			String hashtags = record.get("loca_url");
			imageList.add(hashtags);
		}
	}

	/**
	 * This method write web entities detected by google computer vision to JSON
	 * file
	 * 
	 * @throws FileNotFoundException
	 */
	private static void writeEntityJSonFile(String fileName) throws FileNotFoundException {
		ArrayList<JSONObject> entity_json = new ArrayList<JSONObject>();

		for (String url : map_entities.keySet()) {
			JSONObject json = new JSONObject();
			HashMap<String, Float> entities = map_entities.get(url);
			JSONObject entity = new JSONObject(entities);
			json.put("url", url);
			json.put("entities", entity);
			entity_json.add(json);
		}
		PrintWriter out = new PrintWriter(fileName);
		out.println(entity_json.toString());
		out.close();
	}

	/*
	 * The example code of web entity detection provided by google computer vision
	 */
	public static void detectWebDetections(String filePath) throws Exception, IOException {
		List<AnnotateImageRequest> requests = new ArrayList<>();

		ByteString imgBytes = ByteString.readFrom(new FileInputStream(filePath));
		Image img = Image.newBuilder().setContent(imgBytes).build();
		Feature feat = Feature.newBuilder().setType(Type.WEB_DETECTION).build();
		AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
		requests.add(request);

		// follow the instruction to setup the google authentication
		// https://cloud.google.com/docs/authentication/getting-started#setting_the_environment_variable
		try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
			BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
			List<AnnotateImageResponse> responses = response.getResponsesList();

			for (AnnotateImageResponse res : responses) {
				if (res.hasError()) {
					System.out.printf("Error: %s\n", res.getError().getMessage());
					return;
				}

				// Search the web for usages of the image. You could use these signals later
				// for user input moderation or linking external references.
				// For a full list of available annotations, see http://g.co/cloud/vision/docs
				WebDetection annotation = res.getWebDetection();
				System.out.println("Entity:Id:Score");
				System.out.println("===============");
				for (WebEntity entity : annotation.getWebEntitiesList()) {
					System.out.println(
							entity.getDescription() + " : " + entity.getEntityId() + " : " + entity.getScore());
				}
				for (WebLabel label : annotation.getBestGuessLabelsList()) {
					System.out.format("\nBest guess label: %s", label.getLabel());
				}
				System.out.println("\nPages with matching images: Score\n==");
				for (WebPage page : annotation.getPagesWithMatchingImagesList()) {
					System.out.println(page.getUrl() + " : " + page.getScore());
				}
				System.out.println("\nPages with partially matching images: Score\n==");
				for (WebImage image : annotation.getPartialMatchingImagesList()) {
					System.out.println(image.getUrl() + " : " + image.getScore());
				}
				System.out.println("\nPages with fully matching images: Score\n==");
				for (WebImage image : annotation.getFullMatchingImagesList()) {
					System.out.println(image.getUrl() + " : " + image.getScore());
				}
				System.out.println("\nPages with visually similar images: Score\n==");
				for (WebImage image : annotation.getVisuallySimilarImagesList()) {
					System.out.println(image.getUrl() + " : " + image.getScore());
				}
			}
		}
	}

	/*
	 * The example code of image label detection provided by google computer vision
	 */
	public static void detectLabels(String filePath) throws Exception, IOException {
		List<AnnotateImageRequest> requests = new ArrayList<>();

		ByteString imgBytes = ByteString.readFrom(new FileInputStream(filePath));

		Image img = Image.newBuilder().setContent(imgBytes).build();
		Feature feat = Feature.newBuilder().setType(Type.LABEL_DETECTION).build();
		AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
		requests.add(request);

		try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
			BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
			List<AnnotateImageResponse> responses = response.getResponsesList();

			for (AnnotateImageResponse res : responses) {
				if (res.hasError()) {
					System.out.printf("Error: %s\n", res.getError().getMessage());
					return;
				}

				// For full list of available annotations, see http://g.co/cloud/vision/docs
				for (EntityAnnotation annotation : res.getLabelAnnotationsList()) {
					annotation.getAllFields().forEach((k, v) -> System.out.printf("%s : %s\n", k, v.toString()));
				}
			}
		}
	}

}
