package org.syndiate.GJDownloader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import com.google.gson.Gson;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;


@Command(name = "GDLevelDownloader", mixinStandardHelpOptions = true, version = "1.0.4",
description = "This is a utility designed to download the metadata, comments, and contents of any Geometry Dash level. Visit https://github.com/syndiate/GDLevelDownloader for more information.")
public class Main implements Runnable {
	
	
	
	@Option(names = {"-p", "--export-path"}, description = "Path to download level to.")
    private String exportPathStr;
	
	@Option(names = {"-d", "--level"}, description = "Download the level itself.")
    private boolean downloadLevel;
	
	@Option(names = {"-m", "--metadata"}, description = "Download the level's metadata.")
    private boolean metadata;
	
	@Option(names = {"-l", "--leaderboards"}, description = "Download the level's regular leaderboards.")
    private boolean leaderboards;
	
	@Option(names = {"--wl", "--weekly-leaderboards"}, description = "Download the level's weekly leaderboards.")
    private boolean weeklyLeaderboards;
	
	@Option(names = {"-c", "--comments"}, description = "Download the level's comments. Downloads the latest comments (first page) by default unless otherwise specified.")
    private boolean comments;
	
	@Option(names = {"-cp", "--comments-page"}, description = "Specifies which page of the level's comments to download to. Specify a non-zero integer (1+) to download all comments until that page number, or 0 to download all pages.")
	private Integer commentPage;
	
	@Option(names = {"--level-id"}, description = "The ID of the level you wish to download.")
    private int levelId;
	
	@Option(names = {"--level-ids"}, description = "Level IDs [start-end] to download.")
    private String levelIds;
	
	@Option(names = {"--nonexistent"}, description = "Download non-existent levels.")
	private boolean nonexistent;
	
	@Option(names = {"-r", "--rate-limit"}, description = "Set a custom rate limit wait time for downloading multiple levels in a single operation (default is 5 seconds). It is NOT recommended to wait for less than 4 seconds in between downloading multiple/several levels, as you will eventually be blocked by the GD servers.")
	private Integer customRateLim;
	
	@Option(names = {"-cr", "--comment-rate-limit"}, description = "Set a custom rate limit wait time for downloading multiple comment pages of a level in a single operation (default is 1 second).")
	private Integer customCommentRateLim;
	
	
	
	
	
	private final int levelDownloadWait = 30000;
	private final int netWait = 10000;
	private final int defRateLim = 5000;
	private final int defCommentRateLim = 1000;
	
	private final int maxDownloadRetries = 3;
	private final int maxNetRetries = 100;
	
	private int rateLim = defRateLim;
	private int commentRateLim = defCommentRateLim;
	
	private int downloadAttempts = 0;
	private volatile int currLevelId = 0;
	
	private final String updateURL = "https://api.github.com/repos/syndiate/GDLevelDownloader/releases/latest";
	private final String currVersion = "v1.0.5";
	
	
	
	
	
	
	
	
	
	
	public static void main(String[] args) {
        new Main(args);
    }
	
	public Main(String[] args) {
		new CommandLine(this).execute(args);
	}
	
	
	public void run() {

		
		
		
		if (levelId != 0 && levelIds != null) {
			System.out.println("You cannot specify both a single level ID and a range of level IDs. Check --help for more information.");
			return;
		}
		if (levelId == 0 && levelIds == null) {
			System.out.println("You must specify either a single level ID or a range of level IDs to download. Check --help for more information.");
			return;
		}
		if (!downloadLevel && !metadata && !leaderboards && !comments && !weeklyLeaderboards) {
			System.out.println("Please specify what you would like to retrieve from the level. Check --help for more information.");
			return;
		}
		if (exportPathStr == null) {
			System.out.println("You must specify an absolute path where GDLevelDownloader can download the level(s) to. Check --help for more information.");
			return;
		}
		if (!isValidFilePath(exportPathStr)) {
			System.out.println("The file path specified is invalid.");
			return;
		}
		if (!canUseFilePath(exportPathStr)) {
			System.out.println("GDLevelDownloader cannot access the file path specified. You may need to run it as an administrator.");
			return;
		}
		if (comments && commentPage == null) {
			System.out.println("You must specify a page at which GDLevelDownloader will stop downloading the level(s)'s comments. Check --help for more information.");
			return;
		}
		
		
		
		if (customRateLim != null) {
			rateLim = customRateLim;
		}
		if (customCommentRateLim != null) {
			commentRateLim = customCommentRateLim;
		}
		if (commentPage != null && commentPage == 0) {
			commentPage = Integer.MAX_VALUE;
		}
		
		
		
		
		System.out.println("\n\n");
		if (checkForUpdates()) {
			System.out.println("There's a new update available! Go to https://github.com/syndiate/GDLevelDownloader/releases/latest to download it.");
		}
		
		System.out.println("\n\n");
		if (levelIds == null) {
			writeLevelData(levelId);
			return;
		}
		
		
		
		
		
		
			
			
		
		String[] suppliedLevelIDs = levelIds.replaceAll("[\\[\\]]", "").split("-");
		
		if (suppliedLevelIDs.length != 2) {
			System.out.println("You did not specify 2 level IDs correctly using the [(id1)-(id2)] format. Check --help for more information.");
			return;
		}
		
		
		String id1Str = suppliedLevelIDs[0];
		String id2Str = suppliedLevelIDs[1];
		
		
		if (!id1Str.matches("^-?\\d+$") || !id2Str.matches("^-?\\d+$")) {
			System.out.println("One or both of the level IDs specified are not valid integers.");
			return;
		}

		
		
		int id1 = Integer.parseInt(id1Str);
		int id2 = Integer.parseInt(id2Str);
		
		
		
		HashMap<Integer, Integer> retryData = new HashMap<>();
		for (int i = id1; i <= id2; i++) {

			
			this.currLevelId = i;
			
			System.out.println("\n\n");
			System.out.println("Level ID: " + String.valueOf(i));
			System.out.println("=============================");
			String result = writeLevelData(i);
			
			
			switch (result) {
			
				// check connectivity
				case "downloadFailed": {
					
					
					if (retryData.get(i) >= maxDownloadRetries) {
						System.out.println("Either GDBrowser or the GD servers are down, or your Internet connection is currently extremely unstable. Shutting down.");
						cancel();
						return;
					}
					
					
					System.out.println("Testing and/or waiting for an Internet connection...");
					if (!waitForNet()) {
						System.out.println("After waiting and retrying for " + (netWait * maxNetRetries) + " seconds, it can be said that your Internet connection appears to be completely disabled. Shutting down.");
						cancel();
						return;
					}
					
					
					retryData.put(i, retryData.get(i).intValue() + 1); 		i--;
					System.out.println("Internet connection found. Attempting the download process for this level again.");
					break;
					
					
				}
				case "rateLimited":
					return;
				case "levelDownloaded": {
					// wait between downloading a level since robtop's servers are rate limited
					sleep(rateLim);
					break;
				}
				
			}
			

		}
		
		

		
    }
	
	
	
	
	private void cancel() {
		
		File exportPath = new File(exportPathStr + "/" + currLevelId);
		
		File[] contents = exportPath.listFiles();
        if (contents == null) {
        	exportPath.delete();
        	return;
        }
        for (File file : contents) {
        	file.delete();
        }
        
        exportPath.delete();
        
	}
	
	
	
	
	
	// the most beautifully horrible two lines you'll ever see
	@SuppressWarnings("unchecked")
	public boolean checkForUpdates() {
		try {
			String releaseData = HttpClients.createDefault().execute(new HttpGet(this.updateURL), new BasicHttpClientResponseHandler());
			return !((Map<String, String>) new Gson().fromJson(releaseData, Object.class)).get("tag_name").equals(this.currVersion);
		} catch (IOException e) {
			return false;
		}
	}
	
	
	
	
	
	
	
	
	
	
	// Returns a "status code" depending on the result.
	public String writeLevelData(int lvlID) {
		
		
		
		String levelID = String.valueOf(lvlID);
		String exportPath = exportPathStr + "/" + levelID;
		File exportPathFile = new File(exportPath);
		exportPathFile.mkdirs();
		
		
		
		if (!LevelDownloader.gdBrowserConnectivity()) {
			return "downloadFailed";
		}

		
		
		
		if (downloadLevel) {
			
			if (!LevelDownloader.levelExists(levelID)) {
				
				if (!nonexistent) {
					exportPathFile.delete();
					System.out.println("Skipping since the level doesn't exist and it wasn't specified to download nonexistent levels.");
					return "levelNotDirectlyDownloaded";
				}
				
				writeFile(exportPath + "/level.txt", "-1", "contents (the true, raw data)");
				System.out.println("Not downloading anything else from the level since it doesn't exist.");
				return "levelNotDirectlyDownloaded";
				
			}

			
			
			
			AtomicReference<String> levelDataRef = new AtomicReference<>("");
			
			ExecutorService executor = Executors.newSingleThreadExecutor();
			Future<?> future = executor.submit(() -> {
				
				try {
					levelDataRef.set(LevelDownloader.getRawLevel(levelID));
				} catch (IOException e) {
					System.out.println("An error occurred while retrieving the raw level data. Details:\n" + e.getMessage());
				}
				
			});
			
			

			
			try {
				future.get(levelDownloadWait, TimeUnit.MILLISECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				
				

				Object[] rateLimData = LevelDownloader.checkRateLim();
				boolean isRateLimited = (Boolean) rateLimData[0];
				String waitTime = rateLimData[1].toString();

				
				if (isRateLimited) {
					System.out.println(
							"You have been blocked by Geometry Dash's servers for sending too many requests/downloading too many levels in a short amount of time. Check back in "
									+ waitTime + " seconds.");
					future.cancel(true);
					executor.shutdownNow();
					return "rateLimited";
				}
				
				
				
				downloadAttempts++;
				if (downloadAttempts < maxDownloadRetries) {
					System.out.println("Request timed out. Redownloading level.");
					return writeLevelData(lvlID);
				}
				
				System.out.println("Attempted to download level 3 times to no avail.");
				return "downloadFailed";
				
				
			}
			executor.shutdown();
			writeFile(exportPath + "/level.txt", levelDataRef.get(), "contents (the true, raw data)");
			
		}
		
		
		
		
		
		
		
		if (metadata) {
			writeFile(exportPath + "/metadata.json", LevelDownloader.getMetadata(levelID), "metadata");
		}
		
		if (leaderboards) {
			writeFile(exportPath + "/leaderboards.json", LevelDownloader.getRegularLeaderboards(levelID), "leaderboards");
		}
		
		if (weeklyLeaderboards) {
			writeFile(exportPath + "/weekly-leaderboards.json", LevelDownloader.getWeeklyLeaderboards(levelID), "weekly leaderboards");
		}
		
		
		
		
		
		if (comments) {
			
			String commentExportPath = exportPath + "/comments/";
			new File(commentExportPath).mkdirs();
			
			for (int i = 0; i < commentPage; i++) {

				String practicalPgNum = String.valueOf(i + 1);
				String commentPgPath = commentExportPath + "/pg" + practicalPgNum + ".json";
				String comments = LevelDownloader.getComments(levelID, i);
				
				if (comments == null) {
					break;
				}
				if (comments.equals("") || comments.equals("-1")) {
					break;
				}
				
				writeFile(commentPgPath, comments, "comments (page " + practicalPgNum + ")");
				
				// i dont know if gdbrowser is rate limited or not, but nonetheless, id rather just not flood the server with comment reqs
				sleep(commentRateLim);
			}

		}
		
		
		
		return "levelDownloaded";
	}
	
	
	
	
	public boolean waitForNet() {
		
		for (int netRetries = 0; netRetries <= maxNetRetries; netRetries++) {
			
			if (LevelDownloader.hasNet()) {
				return true;
			}
			
			try {
				Thread.sleep(netWait);
			} catch (InterruptedException notGoingToHappenEx) {}
			
			continue;
			
		}
		return false;
		
	}
	
	
	

	
	
	
	
	
	
	private void writeFile(String filePath, String data, String writeComponent) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
	        writer.write(data);
	        System.out.println("Downloaded the level's " + writeComponent + ".");
	    } catch (IOException e) {
	        System.out.println("An error occurred while writing the level's " + writeComponent + ". Details: \n" + e.getMessage());
	    }
	}
	
	
	
	

	
	
	
	
	
	
	public boolean isValidFilePath(String filePath) {
		
		try {
           Paths.get(filePath);
        } catch (InvalidPathException ex) {
           return false;
        }

        return true;
    }
	
	
	public boolean canUseFilePath(String filePath) {
        return Files.isReadable(Paths.get(filePath)) && Files.isWritable(Paths.get(filePath));
    }
	
	
	
	
	public void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException theHolyException) {
			theHolyException.printStackTrace();
		}
	}
	
	
}
