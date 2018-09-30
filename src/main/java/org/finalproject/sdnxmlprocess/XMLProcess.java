package org.finalproject.sdnxmlprocess;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.jayway.jsonpath.JsonPath;
import sun.misc.BASE64Encoder;

@SuppressWarnings("restriction")
public class XMLProcess {
	public static void main(String[] args) {
		
		final XMLProcess xmlProcess = new XMLProcess();
		
		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		service.scheduleAtFixedRate(new Runnable() {
		    public void run() {
		    	xmlProcess.get_response();
		    }
		}, 0, 5, TimeUnit.MINUTES);
		
	}

	public void get_response() {

		String username = "admin";
		String password = "admin";
		String url = "http://192.168.8.111:8181/restconf/operational/opendaylight-inventory:nodes";
		System.out.println(url);

		try {
			URL obj = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
			connection.setRequestMethod("GET");

			// Basic authorization
			BASE64Encoder enc = new sun.misc.BASE64Encoder();
			String userpassword = username + ":" + password;
			String encodedAuthorization = enc.encode(userpassword.getBytes());
			connection.setRequestProperty("Authorization", "Basic " + encodedAuthorization);

			int responseCode = connection.getResponseCode();
			System.out.println("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			ArrayList<String> switchCount = JsonPath.read(response.toString(), "$..node.length()");

			int switchCountInt = Integer.parseInt(trim_string(switchCount));

			for (int i = 0; i < switchCountInt; i++) {

				ArrayList<String> portCount = JsonPath.read(response.toString(),
						"$..node[" + i + "].node-connector.length()");
				int portCountInt = Integer.parseInt(trim_string(portCount));

				for (int j = 0; j < portCountInt; j++) {
					ArrayList<String> port_id = JsonPath.read(response.toString(),
							"$..node[" + i + "].node-connector[" + j + "].id");
					ArrayList<String> mac = JsonPath.read(response.toString(),
							"$..node[" + i + "].node-connector[" + j + "].flow-node-inventory:hardware-address");
					ArrayList<String> transmittedPackets = JsonPath.read(response.toString(),
							"$..node[" + i + "].node-connector[" + j + "]..packets.transmitted");
					ArrayList<String> receivedPackets = JsonPath.read(response.toString(),
							"$..node[" + i + "].node-connector[" + j + "]..packets.received");
					ArrayList<String> transmittedBytes = JsonPath.read(response.toString(),
							"$..node[" + i + "].node-connector[" + j + "]..bytes.transmitted");
					ArrayList<String> receivedBytes = JsonPath.read(response.toString(),
							"$..node[" + i + "].node-connector[" + j + "]..bytes.received");
					ArrayList<String> transmitDrops = JsonPath.read(response.toString(),
							"$..node[" + i + "].node-connector[" + j + "]..transmit-drops");
					ArrayList<String> receiveDrops = JsonPath.read(response.toString(),
							"$..node[" + i + "].node-connector[" + j + "]..receive-drops");
					ArrayList<String> transmitErrors = JsonPath.read(response.toString(),
							"$..node[" + i + "].node-connector[" + j + "]..transmit-errors");
					ArrayList<String> receiveErrors = JsonPath.read(response.toString(),
							"$..node[" + i + "].node-connector[" + j + "]..receive-errors");

					insertToPackets(trim_string(mac), trim_string(port_id),
							Integer.parseInt(trim_string(receivedPackets)),
							Integer.parseInt(trim_string(transmittedPackets)));
					insertToBytes(trim_string(mac), trim_string(port_id), Integer.parseInt(trim_string(receivedBytes)),
							Integer.parseInt(trim_string(transmittedBytes)));
					insertToErrDetails(trim_string(mac), trim_string(port_id),
							Integer.parseInt(trim_string(transmitDrops)), Integer.parseInt(trim_string(receiveDrops)),
							Integer.parseInt(trim_string(transmitErrors)),
							Integer.parseInt(trim_string(receiveErrors)));

//					System.out.println("port_id -->> " + port_id.toString());
//					System.out.println("mac -->> " + mac.toString());
//					System.out.println("transmittedPackets -->> " + transmittedPackets.toString());
//					System.out.println("receivedPackets -->> " + receivedPackets.toString());
//					System.out.println("transmittedBytes -->> " + transmittedBytes.toString());
//					System.out.println("receivedBytes -->> " + receivedBytes.toString());
//					System.out.println("transmitDrops -->> " + transmitDrops.toString());
//					System.out.println("receiveDrops -->> " + receiveDrops.toString());
//					System.out.println("transmitErrors -->> " + transmitErrors.toString());
//					System.out.println("receiveErrors -->> " + receiveErrors.toString());
//					System.out.println("");
//					System.out.println("");
				}
			}

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public String trim_string(ArrayList<String> str) {
		return str.toString().replace("[", "").replace("]", "");
	}

	public void insertToPackets(String mac, String portId, int receivedPackets, int transmittedPackets) {
		System.out.println("insertToPackets");
		try {
			// create a mysql database connection
			String myDriver = "com.mysql.jdbc.Driver";
			String myUrl = "jdbc:mysql://localhost/sdn";
			Class.forName(myDriver);
			Connection conn = DriverManager.getConnection(myUrl, "root", "");

			// the mysql insert statement
			String query = " insert into packets (mac, port_id, received_packets, transmitted_packets, date_time)"
					+ " values (?, ?, ?, ?, ?)";

			// create the mysql insert prepared statement
			PreparedStatement preparedStmt = conn.prepareStatement(query);
			preparedStmt.setString(1, mac);
			preparedStmt.setString(2, portId);
			preparedStmt.setInt(3, receivedPackets);
			preparedStmt.setInt(4, transmittedPackets);
			preparedStmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));

			// execute the prepared statement
			preparedStmt.execute();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void insertToBytes(String mac, String portId, int receivedBytes, int transmittedBytes) {
		System.out.println("insertToBytes");
		try {
			// create a mysql database connection
			String myDriver = "com.mysql.jdbc.Driver";
			String myUrl = "jdbc:mysql://localhost/sdn";
			Class.forName(myDriver);
			Connection conn = DriverManager.getConnection(myUrl, "root", "");

			// the mysql insert statement
			String query = "insert into bytes (mac, port_id, received_bytes, transmitted_bytes, date_time)"
					+ " values (?, ?, ?, ?, ?)";

			// create the mysql insert prepared statement
			PreparedStatement preparedStmt = conn.prepareStatement(query);
			preparedStmt.setString(1, mac);
			preparedStmt.setString(2, portId);
			preparedStmt.setInt(3, receivedBytes);
			preparedStmt.setInt(4, transmittedBytes);
			preparedStmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));

			// execute the prepared statement
			preparedStmt.execute();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void insertToErrDetails(String mac, String portId, int transmitted_drops, int received_drops,
			int transmitted_errors, int received_errors) {
		System.out.println("insertToErrDetails");
		try {
			// create a mysql database connection
			String myDriver = "com.mysql.jdbc.Driver";
			String myUrl = "jdbc:mysql://localhost/sdn";
			Class.forName(myDriver);
			Connection conn = DriverManager.getConnection(myUrl, "root", "");

			// the mysql insert statement
			String query = "insert into error_details (mac, port_id, transmitted_drops, received_drops,	transmitted_errors,	received_errors, date_time"
					+ ")" + " values (?, ?, ?, ?, ?, ?, ?)";

			// create the mysql insert prepared statement
			PreparedStatement preparedStmt = conn.prepareStatement(query);
			preparedStmt.setString(1, mac);
			preparedStmt.setString(2, portId);
			preparedStmt.setInt(3, transmitted_drops);
			preparedStmt.setInt(4, received_drops);
			preparedStmt.setInt(5, transmitted_errors);
			preparedStmt.setInt(6, received_errors);
			preparedStmt.setTimestamp(7, new Timestamp(System.currentTimeMillis()));

			// execute the prepared statement
			preparedStmt.execute();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}