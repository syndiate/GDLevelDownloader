package org.syndiate.GJDownloader;

public class MainTest {
	
	/*
	private final String updateURL = "https://api.github.com/repos/syndiate/GDLevelDownloader/releases/latest";
	private final String currVersion = "v1.0.4";
	
	// just update these methods every time you run a test, i dont feel like changing the methods to static in main
	// very gross i know
	@SuppressWarnings("unchecked")
	public boolean checkForUpdates() {
		try {
			String releaseData = HttpClients.createDefault().execute(new HttpGet(this.updateURL), new BasicHttpClientResponseHandler());
			return !((Map<String, String>) new Gson().fromJson(releaseData, Object.class)).get("tag_name").equals(this.currVersion);
		} catch (IOException e) {
			return false;
		}
	}
	
	

	@Test
	void updateTest() {
		assertFalse(checkForUpdates());
	}*/

}
