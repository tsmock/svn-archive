#!/usr/bin/env python

# NOTE: Don't run these tests too often. They always download some amount of data.

import unittest
from srtm_loader import *
import os
import hashlib

#file://localhost/usr/share/doc/python2.5-doc/html/lib/minimal-example.html

class DownloaderTest(unittest.TestCase):
	def setUp(self):
		#try:
			#os.remove("testcache/N00E010.hgt.zip")
		#except:
			#pass
		#try:
			#os.remove("testcache/filelist")
		#except:
			#pass
		#try:
			#os.rmdir("testcache")
		#except Exception, e:
			#print e
		self.downloader = SRTMDownloader(cachedir="testcache")
	
	def testFilenameParser(self):
		self.assertEqual(self.downloader.parseFilename("S01W002.hgt.zip"), ( -1,   -2))
		self.assertEqual(self.downloader.parseFilename("S10E020.hgt.zip"), (-10,   20))
		self.assertEqual(self.downloader.parseFilename("N10W150.hgt.zip"), ( 10, -150))
		self.assertEqual(self.downloader.parseFilename("N89E179.hgt.zip"), ( 89,  179))
		self.assertEqual(self.downloader.parseFilename("S010W020.hgt.zip"), None)
		self.assertEqual(self.downloader.parseFilename("S10W20.hgt.zip"),  None)
		self.assertEqual(self.downloader.parseFilename("X10W020.hgt.zip"), None)
		self.assertEqual(self.downloader.parseFilename("S10Y020.hgt.zip"), None)

	def testFindTile(self):
		self.seq = range(10)
		#Start with no tile list
		self.assertRaises(NoSuchTileError, self.downloader.getTile, 0, 0)
		self.assertRaises(NoSuchTileError, self.downloader.getTile, 0, 10)
		#Load tile list
		self.downloader.loadFileList()
		# Tile for 0 0 still does not exist
		self.assertRaises(NoSuchTileError, self.downloader.getTile, 0, 0)
		# Tile for 0 10 should be availabe now
		self.downloader.getTile(0, 10)

	def testDownload(self):
		self.downloader.loadFileList()
		self.downloader.downloadTile("Africa", "N00E010.hgt.zip")
		self.assert_(os.path.exists("testcache/N00E010.hgt.zip"))
		self.assert_(hashlib.sha1(open("testcache/N00E010.hgt.zip", 'rb').read()).hexdigest() ==
			"168b1fddb4f22b8cdc6523ff0207e0eb6be314af")

if __name__ == '__main__':
    unittest.main()
