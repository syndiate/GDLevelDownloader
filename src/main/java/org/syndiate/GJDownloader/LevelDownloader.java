package org.syndiate.GJDownloader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;



public class LevelDownloader {
	
	
	private static final String levelEndpoint = "http://www.boomlings.com/database/downloadGJLevel22.php";
	private static final String metadataEndpoint = "https://gdbrowser.com/api/level/";
	private static final String commentsEndpoint = "https://gdbrowser.com/api/comments/";
	private static final String leaderboardEndpoint = "https://gdbrowser.com/api/leaderboardLevel/";
	private static final String leaderboardEntryCount = "200";
	
	
	private static final String secret = "Wmfd2893gb7";
	private static final String gameVersion = "21";
	private static final String binaryVersion = "35";
	
	
	private static final CloseableHttpClient reqClient = HttpClients.createDefault();
	private static final BasicHttpClientResponseHandler reqHandler = new BasicHttpClientResponseHandler();
	
	
	
		
	
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
	
	
	// sorry gd cologne, ill eventually migrate to using boomlings.com itself, but as it stands I truly don't have the time to program that in (because RobTop decided to make it as painful as possible to grab any sort of any information from
	
	// thanks gd cologne for having the server return 500 when a level doesn't exist (????)
	private static String handleGDBrowserRequest(String URL) {
		try {
			return reqClient.execute(new HttpGet(URL), reqHandler);
		} catch (IOException ex) {
			return "-1";
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
	

}
