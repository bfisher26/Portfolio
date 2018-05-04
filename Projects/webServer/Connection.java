/**
 * @author Bridger Fisher HW4
 */

import java.net.*;
import java.io.*;
import java.net.URL;
import java.util.*;

public class Connection implements Runnable
{
	private Socket client;
	private Configuration config;
	public static final int PORT = 8080;
	public static final int BUFFER_SIZE = 256;

	public Connection(Socket client, Configuration config) {
		this.client = client;
		this.config = config;
	}

	public void run() {
		BufferedReader in;
		OutputStream out;
		File fileRequested = null;

		try {
				// read what the client sent
				in = new BufferedReader(new InputStreamReader(client.getInputStream()));
				out = new BufferedOutputStream(client.getOutputStream());

				// we just want the first line
				String requestLine = in.readLine();

				/* If we don't read a GET, just ignore it and close the socket */
				if ( requestLine == null || !requestLine.substring(0,3).equals("GET") ) {
						client.close();
						return;
				}

				String originHost = client.getInetAddress().toString().replaceFirst("/","");
				String request_parsed = parseRequest(requestLine);
				String statusCode;
				String date;
				String fileType = null;
				long contentLength = 0;
				File requestedFile = null;
				Date dateObj = new Date();
				String contentResponse = null;
				String contentLengthResponse;
				String closedConnection;
				String logStatusCode;
				String serverName;

				if (request_parsed.equals("/")) {
						fileRequested = new File(config.getDefaultDocument());
						request_parsed = config.getDefaultDocument();
				}
				else {
						fileRequested = new File(config.getDocumentRoot() + "/" + request_parsed);
				}

				if(fileRequested.isFile()){
					statusCode = "HTTP/1.1 200 OK \r\n";
					contentLength = fileRequested.length();
					logStatusCode = "200";

					if(request_parsed.length() > 0){
						fileType = parseFileType(fileRequested);
					}
				}
				else{
					statusCode = "HTTP/1.1 404 NOT FOUND \r\n";
					fileRequested = new File(config.getFourOhFourDocument());
					contentLength = fileRequested.length();
					logStatusCode = "404";
					fileType = parseFileType(fileRequested);
				}

				date = "Date: " + dateObj + "\r\n";;
				serverName = "Server Name: " + config.getServerName() + "\r\n";;
				contentResponse = "Content-Type: " + fileType + "\r\n";;
				contentLengthResponse = "Content-Length: " + contentLength + "\r\n";;
				closedConnection = "Connection: closed" + "\r\n\r\n";

				String response = statusCode + date + serverName + contentResponse + contentLengthResponse + closedConnection;
				String logEntry = originHost + " [" + dateObj + "]" + " '" + requestLine + "' " + logStatusCode + " " + contentLength;

				System.out.println(response);

				// System.out.println("Log: " + logEntry);
				out.write(response.getBytes());
				out.flush();

				FileInputStream fileToClient = new FileInputStream(fileRequested);
				byte[] buffer = new byte[BUFFER_SIZE];
				int numberOfBytes;

				while((numberOfBytes = fileToClient.read(buffer)) != -1){
					out.write(buffer, 0, numberOfBytes);
					out.flush();
				}

				File logFile = new File(config.getLogFile());
				BufferedWriter writeToLog = new BufferedWriter(new FileWriter(logFile, true));
				writeToLog.write(logEntry+"\n");
				writeToLog.flush();
				writeToLog.close();

				fileToClient.close();
				out.close();
				in.close();
				client.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String parseRequest(String request){
		String[] split_request = request.split("\\s+");
		for(int i=0; i < split_request.length; i++){
		}
		String request_resource;
		if(split_request[1].equals("/")){
			request_resource = split_request[1];
		}
		else {
			request_resource = split_request[1];
		}
		return request_resource;
	}

	public String parseFileType(File fileRequested){
		String fileTypeString;
		if(fileRequested.toString().contains(".html")){
			fileTypeString = "text/html";
		}
		else if(fileRequested.toString().contains(".gif")){
			fileTypeString = "image/gif";
		}
		else if(fileRequested.toString().contains(".jpg")){
			fileTypeString = "image/jpeg";
		}
		else if(fileRequested.toString().contains(".png")){
			fileTypeString = "image/png";
		}
		else if(fileRequested.toString().contains(".txt")){
			fileTypeString = "text/plain";
		}
		else{
			fileTypeString = "other";
		}
		return fileTypeString;
	}
}
