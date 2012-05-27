package chat;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Client {
	BufferedReader in;
	PrintWriter out;
	JFrame frame = new JFrame("Chatter");
	JTextField textField = new JTextField(40);
	JTextArea messageArea = new JTextArea(8, 40);

	public Client() {
		textField.setEditable(false);
		messageArea.setEditable(true);
		frame.getContentPane().add(textField, "North");
		frame.getContentPane().add(new JScrollPane(messageArea), "Center");
		frame.pack();

		textField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				out.println(textField.getText());
				textField.setText("");
			}
		});

	}

	private String getServerAddress() {
		return JOptionPane.showInputDialog(frame,
				"Enter the IP address of the Server:",
				"Welcome to the Chatter", JOptionPane.QUESTION_MESSAGE);
	}

	private String getName() {
		return JOptionPane.showInputDialog(frame, "Choose a screen name: ",
				"Screen name Selection", JOptionPane.PLAIN_MESSAGE);
	}

	private void run() throws UnknownHostException, IOException  {
		String serverAddress = getServerAddress();
		Socket socket = new Socket(serverAddress, 1337);
		in = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);
		
		while (true) {
			String line = in.readLine();
			if (line.startsWith("SUBMITNAME")) {
				out.println(getName());
			} else if (line.startsWith("NAMEACCEPTED")) {
				textField.setEditable(true);
			} else if (line.startsWith("MESSAGE")) {
				messageArea.append(line.substring(7) + "\n");
			} else if (line.startsWith("KILL")) {
				socket.close();
				System.exit(0);
			} else {
				messageArea.append(line + "\n");
			}
		}
	}

	public static void main(String[] args) throws UnknownHostException,
			IOException {
		Client client = new Client();
		client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		client.frame.setVisible(true);
		client.run();
	}

}
