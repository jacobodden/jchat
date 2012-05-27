package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

/**
 * Multithreaded chat server. Client connects server requests screen name by
 * sending the client the text "USERNAME: ", keeps requesting until unique name
 * is found. Once found Server sends name + " accepted as username" Then all
 * messages submited by name are broadcasted to all others users
 * 
 */

public class Server {
	private static final int PORT = 1337; // port to listen for connects

	private static Vector<String> userNameList = new Vector<String>(); // store
	// usernames
	private static Vector<PrintWriter> otherUsers = new Vector<PrintWriter>(); // store

	// writters
	// for
	// all
	// other
	// users

	public static void main(String[] args) throws IOException {
		System.out.println("The chat server is up.");
		ServerSocket listener = new ServerSocket(PORT);
		try {
			while (true) {
				new chatProtocol(listener.accept()).start();
			}
		} catch (IOException e) {
			System.out.println("Error starting connection thread");
		} finally {
			listener.close();
		}
	}

	private static class chatProtocol extends Thread {
		private String userName;
		private Socket clientSocket;
		private BufferedReader in;
		private PrintWriter out;
		private boolean QUIT = false;

		public chatProtocol(Socket socket) {
			this.clientSocket = socket;
		}

		@Override
		public void run() {
			try {
				in = new BufferedReader(new InputStreamReader(clientSocket
						.getInputStream()));
				out = new PrintWriter(clientSocket.getOutputStream(), true);

				// Loop for prompting user for a unique username
				while (true) {
					out.println("SUBMITNAME");
					userName = in.readLine();
					if (userName == null)
						return;
					synchronized (userNameList) {
						if (!userNameList.contains(userName)) {
							userNameList.add(userName);
							break;
						}
					}
				}
				out.println("NAMEACCEPTED");
				otherUsers.add(out);
				for (PrintWriter others : otherUsers) {
					others.println(userName + " has joined the chat");
				}

				while (true && QUIT != true) {
					String input = in.readLine();
					if (input == null) {
						return;
					}
					if (input.equalsIgnoreCase("/quit")) {
						out.println("KILL");
						QUIT = true;
					} else {
						for (PrintWriter others : otherUsers) {
							others.println("MESSAGE" + userName + ": " + input);
						}
					}
				}
			} catch (IOException e) {
				System.out.println("Error in chatProtocol thread");
			} finally {
				if (userName != null) {
					for (PrintWriter others : otherUsers) {
						others.println(userName + " has left the chat");
					}
					userNameList.remove(userName);
				}
				if (out != null) {
					otherUsers.remove(out);
				}
				try {
					clientSocket.close();
				} catch (IOException e) {
					System.out.println("Error closing client socket");
				}
			}
		}
	}

}
