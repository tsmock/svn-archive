<?php
################################################################################
# This file forms part of the Freemap source code.                             #
# (c) 2004-06 Nick Whitelegg (Hogweed Software)                                #
# Licenced under the Lesser GNU General Public Licence; see COPYING            #
# for details.                                                                 #
################################################################################
//header("Content-type: text/xml");


#globals
$inLat = $inLong =  false;
$lat = $long = 0;
#end globals

$pos = geocoder($_REQUEST['place'],$_REQUEST['country']);
header("Location: index.php?lat=$pos[lat]&lon=$pos[long]");

function geocoder($place, $country)
{
	global $lat, $long;
	$url = "http://brainoff.com/geocoder/rest/?city=$place,$country";
	$ch=curl_init ($url);
	curl_setopt($ch,CURLOPT_RETURNTRANSFER,true);
	curl_setopt($ch,CURLOPT_HEADER,false);
	$resp=curl_exec($ch);
	curl_close($ch);

	$parser = xml_parser_create();
	xml_set_element_handler($parser,"on_start_element","on_end_element");
	xml_set_character_data_handler($parser,"on_characters");

	$resp2 = explode("\n",$resp);
	foreach($resp2 as $line)	
	{
		if (!xml_parse($parser,$line))
			return false;	
	}

	return array("lat"=>$lat,"long"=>$long);
}


#NB the PHP expat library reads in all tags as capitals - even if they're
#lower case!!!
function on_start_element($parser,$element,$attrs)
{
	global $inLat, $inLong;

	if($element=="GEO:LAT") 
	{
		$inLat = true;
	}
	elseif($element=="GEO:LONG")
	{
		$inLong = true;
	}
}

function on_end_element($parser, $element)
{
	global $inLat, $inLong;

	if($element=="GEO:LAT") 
	{
		$inLat = false;
	}
	elseif($element=="GEO:LONG")
	{
		$inLong = false;
	}
}

function on_characters ($parser, $characters)
{
	global $inLat, $inLong;
	global $lat, $long;

	if($inLat)
	{
		$lat=$characters;
	}
	else if ($inLong)
	{
		$long=$characters;
	}
}
?>
