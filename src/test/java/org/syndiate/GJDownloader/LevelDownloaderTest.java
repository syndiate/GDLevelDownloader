package org.syndiate.GJDownloader;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class LevelDownloaderTest {

	// run only when you're actually rate limited
	@Test
	void rateLimTest() {
		if (((Boolean) LevelDownloader.checkRateLim()[0])) {
			System.out.println("HEY\n\\n\\n\\n\\nRATE LIMITED");
		}
		System.out.println(LevelDownloader.checkRateLim()[1]);
	}
	
	@Test
	void levelExistsTest() {
		assertTrue(LevelDownloader.levelExists("128"));
		assertFalse(LevelDownloader.levelExists("129"));
	}

}
