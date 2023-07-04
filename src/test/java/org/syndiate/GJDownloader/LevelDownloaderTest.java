package org.syndiate.GJDownloader;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LevelDownloaderTest {

	// run only when you're actually rate limited
	@Test
	void rateLimTest() {
		assertTrue(((Boolean) LevelDownloader.checkRateLim()[0]) == true);
		System.out.println(LevelDownloader.checkRateLim()[1]);
	}

}
