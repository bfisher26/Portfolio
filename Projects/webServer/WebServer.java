/**
 *
 * @author Bridger Fisher HW4.
 */

import java.net.*;
import java.util.concurrent.*;

public class WebServer
{
	public static final int DEFAULT_PORT = 8080;

	private static final Executor exec = Executors.newCachedThreadPool();

	public static void main(String[] args) throws java.io.IOException {
		ServerSocket server = null;
		System.out.println("Waiting for connections ....");
    Configuration config = null;

    try {
      config = new Configuration(args[0]);
    }
    catch(ConfigurationException configExc) {
      System.out.println("Invalid Configuration: " + args[0]);
    }

		try {
			// establish the socket
			server = new ServerSocket(DEFAULT_PORT);

			while (true) {
				// we block here until there is a client connection
				Socket client = server.accept();

	      Runnable connection = new Connection(client, config);
				exec.execute(connection);
				// close the socket

			}
		}
		catch (Exception e) {
			System.err.println(e);
		}
		finally {
			// close streams and socket
			if (server != null){
				server.close();
			}
		}
	}
}
