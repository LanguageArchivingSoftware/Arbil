<?php

function getSortedDirectoryListing() {
    $directoryListing = array();
    if (false !== ($handle = opendir('/data/extweb1/docs/TG/j2se/jnlp/arbil/'))) {

        while (false !== ($file = readdir($handle))) {
            $fileMTime = filemtime($file);
            $directoryListing[] = array(date("Y-m-d H:i:s", $fileMTime), $file, false);
        }
        closedir($handle);
        sort($directoryListing);
        $directoryListing = array_reverse($directoryListing);
    }
    return $directoryListing;
}

function outputTable(&$directoryListing, $filterString, $tableTitle) {
    echo "<table>\n";
    echo "<tr><td colspan=2><b>$tableTitle</b></td></tr>\n";
    foreach ($directoryListing as $index => $fileData) {
        if (preg_match($filterString, $fileData[1])) {
            echo "<tr><td>" . $fileData[0] . "</td><td><a href=\"http://www.mpi.nl/tg/j2se/jnlp/arbil/" . $fileData[1] . "\">" . $fileData[1] . "</a></td></tr>\n";
            $directoryListing[$index][2] = true; // set this flag to indicate that the entry has been shown
        }
    }
    echo "<table><br>\n";
}

function listUnusedEntries($directoryListing, $tableTitle) {
    echo "<table>\n";
    echo "<tr><td colspan=2><b>$tableTitle</b></td></tr>\n";
    foreach ($directoryListing as $index => $fileData) {
        if ($fileData[2] != true) {
            echo "<tr><td>" . $fileData[0] . "</td><td><a href=\"http://www.mpi.nl/tg/j2se/jnlp/arbil/" . $fileData[1] . "\">" . $fileData[1] . "</a></td></tr>\n";
        }
    }
    echo "<table><br>\n";
}

$directoryListing = getSortedDirectoryListing();
outputTable($directoryListing, "/^arbil-stable.*deb$/", "Stable Debian");
outputTable($directoryListing, "/^arbil-testing.*deb$/", "Testing Debian");
outputTable($directoryListing, "/^arbil-clarin.*deb$/", "Clarin Debian (merged to the current stable)");
outputTable($directoryListing, "/^arbil.*exe$/", "Windows Installers");
outputTable($directoryListing, "/^arbil.*tar$/", "Mac Installers");
outputTable($directoryListing, "/^arbil.*jar$/", "Raw Jar File (requires the lib directory from an existing installation)");
//listUnusedEntries($directoryListing, "Otherwise Unlisted Versions")
?>

