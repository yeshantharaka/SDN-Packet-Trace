package org.finalproject.sdnxmlprocess;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.jayway.jsonpath.JsonPath;

import sun.misc.BASE64Encoder;

public class XMLProcess {
	public static void main(String[] args) {
		XMLProcess xmlProcess = new XMLProcess();
		xmlProcess.get_response();
		xmlProcess.get_response_data();
	}

	public void get_response() {
		try {
			
			String username = "admin";
			String password = "admin";
			
			String newurl = "http://192.168.8.109:8181/restconf/operational/network-topology:network-topology/topology/flow%3A1";
			System.out.println(newurl);
			
			URL obj = new URL(newurl);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			
//			connection.setRequestMethod("POST");
		    @SuppressWarnings("restriction")
			BASE64Encoder enc = new sun.misc.BASE64Encoder();
		    String userpassword = username + ":" + password;
		    @SuppressWarnings("restriction")
			String encodedAuthorization = enc.encode( userpassword.getBytes() );
		    con.setRequestProperty("Authorization", "Basic "+ encodedAuthorization);
			
			int responseCode = con.getResponseCode();
			System.out.println("Response Code : " + responseCode);
			
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			
			// print in String
//			 System.out.println(response.toString());
			
//			 ArrayList<String> authors = JsonPath.read(response.toString(), "$.topology[0].node[*]");
			 ArrayList<String> authors = JsonPath.read(response.toString(), "$..host-tracker-service:addresses");
			 ArrayList<String> author = JsonPath.read(response.toString(), "$..mac");
			 
			 System.out.println("------topology data-----");
			 System.out.println(authors);
			 System.out.println(author);
			 
			 
			 
		} catch (Exception e) {
			System.out.println(e);
		}
	}
        
        public void get_response_data() {
		try {
			
			String username = "admin";
			String password = "admin";
			
			String newurl = "http://192.168.8.109:8181/restconf/operational/opendaylight-inventory:nodes/node/openflow%3A1";
			System.out.println(newurl);
			
			URL obj = new URL(newurl);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			
//			connection.setRequestMethod("POST");
		    @SuppressWarnings("restriction")
			BASE64Encoder enc = new sun.misc.BASE64Encoder();
		    String userpassword = username + ":" + password;
		    @SuppressWarnings("restriction")
			String encodedAuthorization = enc.encode( userpassword.getBytes() );
		    con.setRequestProperty("Authorization", "Basic "+ encodedAuthorization);
			
			int responseCode = con.getResponseCode();
			System.out.println("Response Code : " + responseCode);
			
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			
			// print in String
//			 System.out.println(response.toString());
			
//			 ArrayList<String> authors = JsonPath.read(response.toString(), "$.topology[0].node[*]");
			 ArrayList<String> authors = JsonPath.read(response.toString(), "$..node-connector");
			 ArrayList<String> author = JsonPath.read(response.toString(), "$..packets");
			 ArrayList<String> authorMac = JsonPath.read(response.toString(), "$..receive-drops");
			 
			 System.out.println("---transmit data---");
			 System.out.println(authors);
			 System.out.println(author);
			 System.out.println(authorMac);
			 
			 
			 
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}