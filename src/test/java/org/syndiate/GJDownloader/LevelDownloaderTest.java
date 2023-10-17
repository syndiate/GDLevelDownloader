package org.syndiate.GJDownloader;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
		if (!LevelDownloader.hasNet()) {
			System.out.println("no net");
		}
		assertTrue(LevelDownloader.levelExists("128"));
		assertFalse(LevelDownloader.levelExists("129"));
	}
	
	@Test
	void gdBrowserConnTest() {
		if (!LevelDownloader.hasNet()) {
			System.out.println("no net");
			return;
		}
		assertTrue(LevelDownloader.gdBrowserConnectivity());
	}
	
}
