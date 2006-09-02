#!/usr/bin/perl
use OsmXML;
use Data::Dumper;

# Definitions
$OsmFile = "../Data/bedford.osm"; # Source OSM file
$Filename = "osm.def"; # Temporary mapyrus file
$MapFile = "output.eps"; # Output map file
$OutputFormat = substr($MapFile, -3); # e.g. "eps","svg","png"
$Java = "/usr/java/jre1.5.0_06/bin/java"; # Location of java 5
$Mapyrus = "mapyrus.jar"; # Location of mapyrus

print "Hello, world!\n";
unlink $MapFile;

open(STDOUT, ">", $Filename) || die($!);
CreateMap($MapFile, $OutputFormat, $OsmFile);

# Alternative: create a pipe to mapyrus, and use the -e command line options

# render the file
$RenderCommand = "$Java -classpath $Mapyrus org.mapyrus.Mapyrus $Filename";
`$RenderCommand`;

sub DrawUnclassified(){
  print "linestyle .1, \"round\", \"round\"\n";
  print "color '#666666'\nstroke\n";
}
sub DrawMotorway(){
  print "begin mwayname name\n";
  print "rotate -Mapyrus.rotation\n"; # Back to "upright" mode
  print "box -3,-2,3,2\ncolor 'blue'\nfill\nclearpath\n";
  print "color 'white'\njustify 'center'\nmove 0,-1\nlabel name\n";
  print "end\n";
  
  print "linestyle .5, \"round\", \"round\"\n";
  print "color '#44CCFF'\nstroke\n";
  print "parallelpath -1, 1\n";
  print "color '#44CCFF'\n";
  print "linestyle 0.1, \"round\", \"round\"\n";
  print "stroke\n";
  print "samplepath 30, 9\n";
  print "mwayname 'MW'\n";
}
sub DrawPrimary(){
  print "linestyle .45, \"round\", \"round\"\n";
  print "color 'black'\nstroke\n";
  
  print "linestyle .4, \"round\", \"round\"\n";
  print "color '#FF4400'\nstroke\n";
  
}
sub DrawSecondary(){
  print "linestyle .3, \"round\", \"round\"\n";
  print "color 'black'\nstroke\n";
  
  print "linestyle .25, \"round\", \"round\"\n";
  print "color 'orange'\nstroke\n";

}
sub DrawRiver(){
  print "linestyle 1.6, \"round\", \"round\"\n";
  print "color '#8888FF'\nstroke\n";
  
  print "parallelpath -0.8, 0.8\n";
  print "color '#0000CC'\n";
  print "linestyle 0.1, \"round\", \"round\"\n";
  print "stroke\n";

}
sub DrawPath(){
  print "linestyle 0.1, \"butt\", \"bevel\", 0, 1, 1\n";
  print "color '#008800'\nstroke\n";
  
  #print "begin arrow\nmove -1,-1\ndraw -1,1,2,0\nfill\nend\n";
  #print "samplepath 20, 2.5\narrow\n";
}
sub DrawBike(){
  print "linestyle 0.1, \"butt\", \"bevel\", 0, 1.5, 2\n";
  print "color '#008800'\nstroke\n";
  
  #print "begin arrow\nmove -1,-1\ndraw -1,1,2,0\nfill\nend\n";
  #print "samplepath 20, 2.5\narrow\n";
}
sub DrawCoast(){
  print "linestyle 0.1, \"round\", \"round\"\n";
  print "color 'blue'\nstroke\n";
}
sub DrawRail(){
  # Solid line with white dashes on top
  print "linestyle 0.5, \"round\", \"round\"\n";
  print "color '#880088'\nstroke\n";
  print "linestyle 0.4, \"round\", \"round\",0,2,2\n";
  print "color '#FFFFFF'\nstroke\n";
}
sub CreateMap(){
  my($MapFile, $OutputFormat, $OsmFile)  = @_;
  my %Functions = (
    "debug" => \&DrawUnclassified,
    "unclassified" => \&DrawUnclassified,
    "residential" => \&DrawUnclassified,
    "minor" => \&DrawUnclassified,
    "secondary" => \&DrawSecondary,
    "primary" => \&DrawPrimary,
    "motorway" => \&DrawMotorway,
    "motorway_link" => \&DrawMotorway,
    "river"=>\&DrawRiver,
    "cycleway"=>\&DrawBike,
    "bridleway"=>\&DrawBike,
    "path"=>\&DrawPath,
    "footpath"=>\&DrawPath,
    "footway"=>\&DrawPath,
    "coastline"=>\&DrawCoast,
    "railway"=>\&DrawRail,
    "rail"=>\&DrawRail,
    );
  

  $OSM = new OsmXML();
  $OSM->load($OsmFile);
  ($S,$W,$N,$E) = OsmXML::bounds();

  # Header
  print "#" x 80 . "\n";
  print "# This is map data from the OpenStreetMap project\n";
  print "# Generated by $0, from $OsmFile\n";
  print "# This data is licensed as Creative Commons CC-BY-SA 2.0\n";
  print "# Render this file using Mapyrus (mapyrus.sourceforge.net)\n";
  print "#" x 80 . "\n\n";
  
  # Output format
  print "newpage \"$OutputFormat\", \"$MapFile\", 800, 800\n";
  
  # Drawing area
  print "worlds $W, $S, $E, $N\n";  
  print "include symbols.mapyrus\n";
  
  #---------------- Ways --------------------
  foreach $Way(%OsmXML::Ways){
    
     printf "clearpath\n";
     
    $Type =  
      $Way->{"highway"} ||
      $Way->{"railway"} ||
      $Way->{"waterway"} ||
      $Way->{"natural"} ||
      $Way->{"class"} ||
      "debug";
      
    @Segments = split(/,/,$Way->{"segments"});
    
    $LastId = -1;
    foreach $SegmentID(@Segments){
      $Segment = $OsmXML::Segments{$SegmentID};
    
      $Node1 = $OsmXML::Nodes{$Segment->{"from"}};
      $Node2 = $OsmXML::Nodes{$Segment->{"to"}};
    
      if($Segment->{"from"} == $LastId){
        printf ",%f,%f",
          $Node2->{"lon"},
          $Node2->{"lat"};
      }
      else
      {
        printf "\nmove %f,%f\ndraw %f,%f",
          $Node1->{"lon"},
          $Node1->{"lat"},
          $Node2->{"lon"},
          $Node2->{"lat"};
      }
      $LastId = $Segment->{"to"};
        
      } # Next segment
    print "\n"; # End the list of vertices
    
    
    $Functions{$Type}() if(exists $Functions{$Type});

  } # Next way
  
  #---------------- Nodes --------------------
  print "font 'Arial', 4\n";
  foreach $Node(values %OsmXML::Nodes){
    $Type = $Node->{"amenity"} || 
      $Node->{"place"} || 
      $Node->{"railway"} || 
      $Node->{"place_of_worship"} || 
      "";
    if($Type =~ /(hospital|parking|church|school|library|lighthouse)/){
      printf "clearpath\nmove %f,%f\n%s\n",
        $Node->{"lon"},
        $Node->{"lat"},
        $Type;
    }
    if($Type && 0){
      
      printf "clearpath\ncolor 'black'\nmove %f,%f\nlabel '%s'\n",
        $Node->{"lon"},
        $Node->{"lat"},
        $Type;
      }
  }
  
  
  # Anything else
  print "include scalebar.mapyrus\n";
  printf "clearpath\nscalebar Mapyrus.worlds.scale, 'm', %f, %f\n",
    0,
    0;
}


=head1 NAME

Rendering program for OpenStreetMap files, which uses Mapyrus

=head1 AUTHOR

Oliver White (oliver.white@blibbleblobble.co.uk)

=head1 DESCRIPTION

See http://wiki.openstreetmap.org/index.php/Mapyrus

=head1 COPYRIGHT

Copyright 2006, Oliver White

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

=cut