package edu.psu.geography;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;

public class ImageSearch {

	private static String request_url = "https://api.tineye.com/rest/search/";
	private static String pub_key = "";
	private static String pri_key = "";

	private static int limit = 100;
	private static int offset = 0;

	public static void main(String[] args) throws Exception {


		String image_url = "D:\\ViralMap\\Test5.jpg";
		tinEyePostQuery(image_url);

		// String image_url = "https://tineye.com/images/meloncat.jpg";
		// tinEyeGetQuerry(image_url);

	}

	/*
	 * send a query by GET method;
	 */
	public static String tinEyeGetQuerry(String image_url) throws Exception {

		// the date and time when the request was made.
		long date = System.currentTimeMillis() / 1000L;
		
		// generate a unique random ID from client side to identify the request
		String nonce = generateNonce(12);
		
		//encode the image path
		String enocde_path = URLEncoder.encode(image_url, StandardCharsets.UTF_8.toString());
		String parameters = "image_url=" + enocde_path + "&limit=" + limit + "&offset=" + offset;
		
		// authentication string, see https://services.tineye.com/developers/tineyeapi/authentication for more information
		String sign_string = pri_key + "GET" + date + nonce + request_url + parameters;
		
		//encode the sign string with private key by HMAC-SHA256 algorithm 
		String api_sig = encode(pri_key, sign_string);
		String url = request_url + "?api_key=" + pub_key + "&offset=" + offset + "&limit=" + limit + "&image_url="
				+ enocde_path + "&date=" + date + "&nonce=" + nonce + "&api_sig=" + api_sig;

		//send request
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);
		HttpResponse response = client.execute(request);
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		String line;
		String message = "";
		while ((line = rd.readLine()) != null) {
			message += line;
		}
		return message;
	}

	/*
	 * send a query by POST method;
	 * return JSON object by Tineye API
	 */
	public static String tinEyePostQuery(String image_url) throws Exception {

		// the date and time when the request was made.
		long date = System.currentTimeMillis() / 1000L;
		
		// generate a unique random ID from client side to identify the request
		String nonce = generateNonce(12);
		File image = new File(image_url);
		
		//transfer the file name to lowercase before encoding
		String fileName = image.getName().toLowerCase();
		String enocde_path = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
		
		//boundary string for multipart data
		String boundary = "boundary---------------boundary";
		String content_type = "multipart/form-data; boundary=" + boundary;

		String parameters = "limit=" + limit + "&offset=" + offset;
		
		// authentication string, see https://services.tineye.com/developers/tineyeapi/authentication for more information
		String sign_string = pri_key + "POST" + content_type + enocde_path + date + nonce + request_url + parameters;
		
		//encode the sign string with private key by HMAC-SHA256 algorithm 
		String api_sig = encode(pri_key, sign_string);
		
		//set request header
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(request_url);
		post.setHeader("Content-type", content_type);

		//build multipart entity with parameters
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.setBoundary(boundary);
		builder.addBinaryBody("image_upload", image, ContentType.APPLICATION_OCTET_STREAM, fileName);
		builder.addTextBody("api_key", pub_key);
		builder.addTextBody("date", String.valueOf(date));
		builder.addTextBody("limit", String.valueOf(limit));
		builder.addTextBody("offset", String.valueOf(offset));
		builder.addTextBody("nonce", nonce);
		builder.addTextBody("api_sig", api_sig);
		HttpEntity entity = builder.build();
		post.setEntity(entity);
		
		//send request
		HttpResponse response = client.execute(post);
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		String line;
		String message = "";
		while ((line = rd.readLine()) != null) {
			message += line;
		}
		return message;
	}

	/*
	 * encode data with private key by HMAC-SHA256 algorithm
	 */
	private static String encode(String key, String data) throws Exception {
		Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
		SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
		sha256_HMAC.init(secret_key);
		return Hex.encodeHexString(sha256_HMAC.doFinal(data.getBytes("UTF-8")));
	}

	/*
	 * generate a random unique client id
	 */
	private static String generateNonce(int length) {
		String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
		String CHAR_UPPER = CHAR_LOWER.toUpperCase();
		String NUMBER = "0123456789";
		String DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER + NUMBER;
		SecureRandom random = new SecureRandom();
		if (length < 1)
			throw new IllegalArgumentException();
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			int rndCharAt = random.nextInt(DATA_FOR_RANDOM_STRING.length());
			char rndChar = DATA_FOR_RANDOM_STRING.charAt(rndCharAt);
			sb.append(rndChar);
		}
		return sb.toString();
	}
}
