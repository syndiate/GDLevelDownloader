**GDLevelDownloader** is a utility designed to download the metadata, comments, and contents of any Geometry Dash level.

[Download the latest release here.](https://github.com/syndiate/GDLevelDownloader/releases/latest/)


Usage
----------
```
java -jar [name_of_downloaded_file].jar [args]
```

List of arguments and flags:
```
  -c, --comments             Download the level's comments. Downloads the
                               latest comments (first page) by default unless
                               otherwise specified.
      -cp, --comments-page=<commentPage>
                             Specifies which page of the level's comments to
                               download to. Specify a non-zero integer (1+) to
                               download all comments until that page number, or
                               0 to download all pages.
  -d, --level                Download the level itself.
  -h, --help                 Show this help message and exit.
  -l, --leaderboards         Download the level's regular leaderboards.
      --level-id=<levelId>   The ID of the level you wish to download.
      --level-ids=<levelIds> Level IDs [start-end] to download.
  -m, --metadata             Download the level's metadata.
      --nonexistent          Download non-existent levels.
  -p, --export-path=<exportPathStr>
                             Path to download level to.
  -V, --version              Print version information and exit.
      --wl, --weekly-leaderboards
                             Download the level's weekly leaderboards.
```

#### Examples

Downloading "1st Level" (by RealStorm)'s raw level contents (nothing more, nothing less):
```
java -jar [name_of_downloaded_jar_file].jar --level --level-id 128
```

Downloading everything from "1st Level" (the level contents, metadata, all comments until page 3, and regular and weekly leaderboards):
```
java -jar [name_of_downloaded_jar_file].jar --level-id 128 --level --metadata --comments --comments-page 3 --leaderboards --weekly-leaderboards
```

Downloading the metadata and weekly leaderboards from levels 128 all the way through 136:
```
java -jar [name_of_downloaded_jar_file].jar --level-ids [128-136] --metadata --weekly-leaderboards
```