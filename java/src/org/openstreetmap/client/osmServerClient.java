/*
   Copyright (C) 2004 Stephen Coast (steve@fractalus.com)

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
   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

 */

package org.openstreetmap.client;

import java.util.*;
import java.net.*;
import java.lang.*;
import java.io.*;
import com.bbn.openmap.LatLonPoint;
import org.apache.xmlrpc.applet.*;

import org.openstreetmap.util.*;

public class osmServerClient
{
  private String sUsername = "";
  private String sPassword = "";
  private String sLoginToken = "";
  private long loginTime = 0;


  SimpleXmlRpcClient xmlrpc;

  public osmServerClient()
  {

    try
    {

      xmlrpc = new SimpleXmlRpcClient("http://www.openstreetmap.org/api/xml.jsp");


    }
    catch(Exception e)
    {
      System.out.println("oh de-ar " + e);
      e.printStackTrace();


      System.exit(-1);


    }

  } // osmServerClient


 
  public synchronized int addNewStreet(
      String sStreetName,
      double lat1,
      double lon1,
      double lat2,
      double lon2
      )
  {
    
    Vector params = new Vector();

    params.addElement(sLoginToken);
    params.addElement(sStreetName);
    params.addElement(new Double(lat1));
    params.addElement(new Double(lon1));
    params.addElement(new Double(lat2));
    params.addElement(new Double(lon2));


    Integer i =  new Integer(-1);
    
    try{

      i = (Integer)xmlrpc.execute("openstreetmap.addNewStreet", params);

    }
    catch(Exception e)
    {
      System.out.println("oh de-ar " + e);
      e.printStackTrace();

      return -1;

    }

    return  i.intValue();

  } // addStreetSegment




  public synchronized boolean addStreetSegment(
      int street_uid,
      double lat1,
      double lon1,
      double lat2,
      double lon2
      )
  {

    Vector params = new Vector();

    params.addElement(sLoginToken);
    params.addElement(new Integer(street_uid));
    params.addElement(new Double(lat1));
    params.addElement(new Double(lon1));
    params.addElement(new Double(lat2));
    params.addElement(new Double(lon2));

    Boolean bYesNo;

    try{

      bYesNo = (Boolean)xmlrpc.execute("openstreetmap.addStreetSegment", params);

    }
    catch(Exception e)
    {
      System.out.println("oh de-ar " + e);
      e.printStackTrace();

      return false;

    }

    return  bYesNo.booleanValue();



  } // addStreetSegment


  public synchronized boolean deletePoint(double lat, double lon)
  {
    Vector params = new Vector();

    params.addElement(sLoginToken);
    params.addElement(new Double(lon));
    params.addElement(new Double(lat));

    Boolean bYesNo;

    try{

      bYesNo = (Boolean)xmlrpc.execute("openstreetmap.dropPoint", params);

    }
    catch(Exception e)
    {
      System.out.println("oh de-ar " + e);
      e.printStackTrace();

      return false;

    }

    return  bYesNo.booleanValue();



  } // deletePoint


  public synchronized boolean deletePointsInArea(
      double lon1,
      double lat1,
      double lon2,
      double lat2
      )
  {
    Vector params = new Vector();

    params.addElement(sLoginToken);
    params.addElement(new Double(lon1));
    params.addElement(new Double(lat1));
    params.addElement(new Double(lon2));
    params.addElement(new Double(lat2));

    Boolean bYesNo;

    try{

      bYesNo = (Boolean)xmlrpc.execute("openstreetmap.dropPointsInArea", params);

    }
    catch(Exception e)
    {
      System.out.println("oh de-ar " + e);
      e.printStackTrace();

      return false;

    }

    return  bYesNo.booleanValue();



  } // deletePoint



  public synchronized boolean login(String user, String pass)
  {
    System.out.println("trying to login with '" + user + "' , '" + pass +"'...");
    Vector params = new Vector();

    params.addElement(user);

    params.addElement(pass);


    String token = "hum";
    try{

      token = (String)xmlrpc.execute("openstreetmap.login", params);

    }
    catch(Exception e)
    {
      System.out.println("oh de-ar " + e);
      e.printStackTrace();

      return false;

    }

    if( token.equals("ERROR"))
    {
      return false;
    }

    sUsername = user;
    sPassword = pass;
    sLoginToken = token;
    loginTime = System.currentTimeMillis() + (1000 * 60 * 9);
    // set logout time for 9 mins hence

    return true;


  } // login


  public synchronized boolean loggedIn()
  {
    if( loginTime > System.currentTimeMillis() )
    {
      return true;

    }

    return false;

  }




  public synchronized Vector getStreets(
      LatLonPoint llp1,
      LatLonPoint llp2)
  {

    System.out.println("getting streets...");

    Vector streets = new Vector();

    try{

      Vector params = new Vector();

      params.addElement( "applet" ); 
      params.addElement( new Double((double)llp1.getLatitude()) );
      params.addElement( new Double((double)llp1.getLongitude()) );
      params.addElement( new Double((double)llp2.getLatitude()) );
      params.addElement( new Double((double)llp2.getLongitude()) );

      streets = (Vector)xmlrpc.execute("openstreetmap.getStreets",params);


      System.out.println("done getting streets");


    }
    catch(Exception e)
    {
      System.out.println("oh de-ar " + e);
      e.printStackTrace();


      System.exit(-1);

    }

    return streets;

  } // getStreets


  public synchronized Vector getPoints(LatLonPoint llp1,
      LatLonPoint llp2)
  {
    Vector gpsPoints = new Vector();


    try{

      Vector params = new Vector();

      params.addElement( "applet" ); 
      params.addElement( new Double((double)llp1.getLatitude()) );
      params.addElement( new Double((double)llp1.getLongitude()) );
      params.addElement( new Double((double)llp2.getLatitude()) );
      params.addElement( new Double((double)llp2.getLongitude()) );

      Vector results = (Vector) xmlrpc.execute("openstreetmap.getPoints",params);

      System.out.println("reading points...");

      Enumeration e = results.elements();

      while(e.hasMoreElements())
      {

        //gpsPoints.add( 


        //new gpspoint( 
        float lat = (float)((Double)e.nextElement()).doubleValue();
        float lon = (float)((Double)e.nextElement()).doubleValue();


        gpsPoints.add( new gpspoint(lat,lon,0,0) );

      }

      System.out.println("done getting points");

    }
    catch(Exception e)
    {
      System.out.println("oh de-ar " + e);
      e.printStackTrace();


      System.exit(-1);

    }

    return gpsPoints;

  } // getPoints


} // osmServerClient
