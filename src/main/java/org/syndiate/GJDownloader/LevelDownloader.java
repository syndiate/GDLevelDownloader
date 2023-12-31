package org.syndiate.GJDownloader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
import org.apache.hc.client5.http.routing.HttpRoutePlanner;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.util.Timeout;



public class LevelDownloader {
	
	
	private static final String levelEndpoint = "http://www.boomlings.com/database/downloadGJLevel22.php";
	private static final String gdBrowserPrefix = "https://gdbrowser.com/";
	private static final String metadataEndpoint = "api/level/";
	private static final String commentsEndpoint = "api/comments/";
	private static final String leaderboardEndpoint = "api/leaderboardLevel/";
	private static final String searchEndpoint = "api/search/";
	private static final String leaderboardEntryCount = "200";
	
	
	private static final String secret = "Wmfd2893gb7";
	private static final String gameVersion = "21";
	private static final String binaryVersion = "35";
	
	
	private static CloseableHttpClient reqClient = HttpClients.createDefault();
	private static BasicHttpClientResponseHandler reqHandler = new BasicHttpClientResponseHandler();
	
	
	private static final int rateLimTimeout = 30000;
	
	
	
		
	
	public static String getRawLevel(String levelID) throws IOException {
		
        HttpPost httpPost = new HttpPost(levelEndpoint);
        
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("levelID", levelID));
        params.add(new BasicNameValuePair("secret", secret));
        params.add(new BasicNameValuePair("gameVersion", gameVersion));
        params.add(new BasicNameValuePair("binaryVersion", binaryVersion));
        
        
        httpPost.setEntity(new UrlEncodedFormEntity(params));
        httpPost.setHeader("User-Agent", "");
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

        String response = reqClient.execute(httpPost, reqHandler);
        return response;
        
	}
	
	// sorry gd cologne, ill eventually migrate to using boomlings.com itself, but as it stands I truly don't have the time to program that in (because RobTop decided to make it as painful as possible to grab any sort of any information from)
	
	// thanks gd cologne for having the server return 500 when a level doesn't exist (????)
	private static String handleGDBrowserRequest(String URL) {
		try {
			return reqClient.execute(new HttpGet(gdBrowserPrefix + URL), reqHandler);
		} catch (IOException ex) {
			return (ex instanceof HttpResponseException) ? "-1" : "-2";
		}
	}
	
	
	public static String getMetadata(String levelID) {
		return handleGDBrowserRequest(metadataEndpoint + levelID);
	}
	
	public static String getRegularLeaderboards(String levelID) {
		return handleGDBrowserRequest(leaderboardEndpoint + levelID + "?count=" + leaderboardEntryCount);
	}
	
	public static String getWeeklyLeaderboards(String levelID) {
		return handleGDBrowserRequest(leaderboardEndpoint + levelID + "?week&count=" + leaderboardEntryCount);
	}
	
	public static String getComments(String levelID, int page) {
		return handleGDBrowserRequest(commentsEndpoint + levelID + "?page=" + String.valueOf(page));
	}
	
	public static boolean levelExists(String levelID) {
		return !handleGDBrowserRequest(searchEndpoint + levelID).equals("-1");
	}
	
	
	
	
	// i dont care that its deprecated, there is no good documentation on using the new methods/classes to retrieve anything but the response string
	@SuppressWarnings("deprecation")
	public static Object[] checkRateLim() {
		try {
			
			
			
			CloseableHttpClient httpClient = HttpClients.custom()
					.setDefaultRequestConfig(
							RequestConfig.custom().setResponseTimeout(Timeout.of(rateLimTimeout, TimeUnit.MILLISECONDS)).build())
					.build();

			HttpHead req = new HttpHead(levelEndpoint);
			req.setHeader("User-Agent", "");

			CloseableHttpResponse response = httpClient.execute(req);
			Header retryAfterHeader = response.getHeader("Retry-After");

			
			return new Object[] { 
					Boolean.valueOf(response.getCode() == 429),
					(retryAfterHeader != null ? retryAfterHeader.getValue() : "??????")
			};

			
	    
		} catch (Exception e) {
			return new Object[] { Boolean.valueOf(false), "??????" };
		}
	}
	
	
	
	
	public static boolean hasNet() {
		try {
			reqClient.execute(new HttpHead("https://www.google.com"), reqHandler);
			return true;
		} catch (IOException ex) {
			return false;
		}
	}
	
	
	
	public static boolean gdBrowserConnectivity() {
		return !handleGDBrowserRequest("").equals("-2");
	}
	
	
	
	
	
	// purely experimental and not working
	public static void setReqProxy(String host) throws MalformedURLException {
		
		URL proxyURL = new URL(host);
		HttpHost proxyHost = new HttpHost(proxyURL.getHost(), proxyURL.getPort());
		
		HttpRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxyHost);
		HttpClientBuilder clientBuilder = HttpClients.custom();

		clientBuilder = clientBuilder.setRoutePlanner(routePlanner);
		CloseableHttpClient httpClient = clientBuilder.build();
		LevelDownloader.setReqClient(httpClient);
	}
	
	public static void setReqClient(CloseableHttpClient client) {
		LevelDownloader.reqClient = client;
	}
	
	

}
