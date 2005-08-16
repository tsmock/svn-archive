#!/usr/bin/ruby -w


require 'cgi'
load 'osm/dao.rb'
require 'osm/gpx'

include Apache


r = Apache.request
cgi = CGI.new

page = cgi['page'].to_i

bbox = cgi['bbox'].split(',')

bllat = bbox[0].to_f
bllon = bbox[1].to_f
trlat = bbox[2].to_f
trlon = bbox[3].to_f

if bllat > trlat || bllon > trlon
  exit BAD_REQUEST
end

dao = OSM::Dao.instance

points = dao.get_track_points(bllat, bllon, trlat, trlon, page)

gpx = OSM::Gpx.new

gpx.addtrk points

puts gpx.to_s_pretty

