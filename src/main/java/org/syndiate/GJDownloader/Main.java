package org.syndiate.GJDownloader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;


@Command(name = "GDLevelDownloader", mixinStandardHelpOptions = true, version = "1.0",
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
	private int commentPage;
	
	@Option(names = {"--level-id"}, description = "The ID of the level you wish to download.")
    private int levelId;
	
	@Option(names = {"--level-ids"}, description = "Level IDs [start-end] to download.")
    private String levelIds;
	
	@Option(names = {"--nonexistent"}, description = "Download non-existent levels.")
	private boolean nonexistent;
	
	
	
	
	
	
	
	
	
	
	
	public static void main(String[] args) {
        new CommandLine(new Main()).execute(args);
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
		System.out.println("\n\n");
		
		
		
		
		
		
		if (levelIds == null) {
			writeLevelData(levelId);
			return;
		}
			
			
		
		int[] levelIDRange = new int[2];
		String[] suppliedLevelIDs = levelIds.replaceAll("[\\[\\]]", "").split("-");

		
		if (suppliedLevelIDs.length != 2) {
			System.out.println("You did not specify 2 level IDs correctly using the [(id1)-(id2)] format. Check --help for more information.");
			return;
		}

		
		String id1 = suppliedLevelIDs[0];
		String id2 = suppliedLevelIDs[1];

		
		if (!id1.matches("^-?\\d+$") || !id2.matches("^-?\\d+$")) {
			System.out.println("One or both of the level IDs specified are not valid integers.");
			return;
		}

		
		levelIDRange[0] = Integer.parseInt(id1);
		levelIDRange[1] = Integer.parseInt(id2);

		
		for (int i = levelIDRange[0]; i <= levelIDRange[1]; i++) {

			System.out.println("\n\n");
			System.out.println("Level ID: " + String.valueOf(i));
			System.out.println("=============================");
			writeLevelData(i);

		}
		
		
		
		
    }
	
	
	
	
	
	
	
	
	
	
	
	
	public void writeLevelData(int lvlID) {
		
		
		String levelID = String.valueOf(lvlID);
		String exportPath = exportPathStr + "/" + levelID;
		File exportPathFile = new File(exportPath);
		exportPathFile.mkdirs();
		
		

		if (downloadLevel) {

			
			String levelData = "";
			try {
				levelData = LevelDownloader.getRawLevel(levelID);
			} catch (IOException e) {
				System.out.println("An error occurred while retrieving the raw level data. Details:\n" + e.getMessage());
			}
			
			
			if ((!nonexistent && levelData.equals("-1")) || levelData.equals("")) {
				exportPathFile.delete();
				return;
			}
			
			writeFile(exportPath + "/level.txt", levelData, "contents (the true, raw data)");
			
			if (levelData.equals("-1")) {
				System.out.println("Not downloading anything else from the level since it doesn't exist.");
				return;
			}
			

		}
		
		
		
		if (metadata) {
			writeFile(exportPath + "/metadata.json", LevelDownloader.getMetadata(levelID), "metadata");
		}
		
		if (leaderboards) {
			writeFile(exportPath + "/leaderboards.json", LevelDownloader.getMetadata(levelID), "leaderboards");
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
				
				if (comments.equals("")) {
					break;
				}
				writeFile(commentPgPath, comments, "comments (page " + practicalPgNum + ")");
				
			}
			
		}
		
		
		
		
		
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
	
	
}