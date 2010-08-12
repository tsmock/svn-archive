<?php
session_start();

function psbmt()
{
    if(isset($_SESSION['gatekeeper']))
    {
        //$_SESSION['photosession'] = newsession();
        ?>
        <div id='pansubmit_all'>
        <?php
        //display_map(320,320);
        ?>
        <div id='psbmt0'>
        <h2>Submit a series of photos</h2>
        <p>
        Upload each photo or panorama in your trip (you can now select
		more than one at once, thanks to Andrew Valums' multi-file
		uploader, see <a href='http://valums.com/ajax-upload'>here</a>).
        Then, if you also recorded a GPX trace
        while out, you can select "Auto-position with GPX", below, to 
        automatically position each photo or panorama. </p>
		<p>You can then:
			<ul>
			<li>use the <a href='photomgr.php'>photo management
			page</a> to manage your photos (position any not auto-located
			using your GPX trace, group photos taken from the same point in
			different directions, or specify which photos are panoramas);</li>
			<li>orientate a photo on the main page by rotating its camera
			icon.</li>
			</ul>
		</p>
		<p>Note that photos will need to be authorised before they become
		visible to the public; however you can still manage them on the
		<a href='photomgr.php'>photo management page</a> and orientate
		them on the main page.</p>
		


		<div id='div1'>this is div1</div>

		<script type='text/javascript'>
		function uploadFiles()
		{
			var uploader = new qq.FileUploader( {
			element: document.getElementById('div1'),
			action: '/otv/photoupload.php',
			sizeLimit: 3*1024*1024,
			onComplete: function(id,fileName,responseJSON)
			{
				var str="";
				for(x in responseJSON)
					str +=  x+"="+responseJSON[x]+":";
			
			}
			} );
			init();
		}

		window.onload = uploadFiles;
		</script>
		<p>
		<a href='gpxupload.php'>
		Auto-position uploaded photos with GPX
		</a> |
		<a href='index.php'>Back to map</a>
		</p>
		<?php
    }
    else
    {
        echo "<div id='pansubmit_all'>Need to be logged in to upload photos".
        " <a href='user.php?action=login&redirect=index.php".
        "'>Login now!</a></div>";
    }
}

function newsession()
{
    $q= "SELECT MAX(photosession) AS pssn FROM panoramas ".
                            "WHERE user=$_SESSION[gatekeeper]";
    $result=mysql_query($q) or die(mysql_error());
    $row=mysql_fetch_assoc($result);
    return ($row['pssn']==null) ? 1:$row['pssn']+1;
}

function display_map($w,$h)
{
    echo "<div style='width:${w}px; height:${h}px' id='map'></div>";
}

?>
