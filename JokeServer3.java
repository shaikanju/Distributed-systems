/*

1. Name: Anju Shaik

2. Date: 2023-10-08

3. Java version: 20.0.2 (build 20.0.2+9-78)

4. Precise command-line compilation examples / instructions:

> javac JokeServer.Java

5. Precise examples / instructions to run this program:

In separate shell windows:

> java JokeServer
> java JokeClient
> java JokeClientAdmin



6. Full list of files needed for running the program:

 JokeServer.java

7. Notes:
---
Secondary server is not implemented other than that everything seems fine.


------------------
*/



import java.io.*;
import java.net.*;
import java.util.*;

//Admin data that can be serialized for communication between adminclient and server
class AdminData implements Serializable {

	private boolean serverMode;


	public AdminData(boolean serverMode) {
		this.serverMode = serverMode;

	}

	// Getter methods for retrieving data
	public boolean getServerMode() {
		return serverMode;
	}


}
//following is the JokeClientadmin class which toggles the server modeeither to joke or proverb mode
class JokeClientAdmin {
	public static void main(String argv[]) {
		String primaryServerName = "localhost";

		if (argv.length == 1) {
			primaryServerName = argv[0];
		}
		JokeClientAdmin admin = new JokeClientAdmin(primaryServerName);
		admin.run();
	}
	private String primaryServerName;


	private boolean serverMode = true; // Initialize server mode as true (joke mode)

	public JokeClientAdmin(String primaryServerName) {
		this.primaryServerName = primaryServerName;

	}
//the run method
	public void run() {
		Scanner consoleIn = new Scanner(System.in);
		System.out.println("Admin client connected to primary server: " + primaryServerName);

		try {
			while (true) {
				String input = consoleIn.nextLine();



				// Toggle server mode and send it to the server upon each connection
				toggleServerMode();
				AdminData adminData = new AdminData(serverMode);

				// Determine which server to connect to based on usingPrimaryServer flag
				//however here the seconday server case is not implemented
				String serverName = primaryServerName;
				int serverPort =  5050 ;

				// Connect to the selected server
				//in this case only the primary server
				Socket socket = new Socket(serverName, serverPort);
				ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
				oos.writeObject(adminData);
				oos.flush();

				System.out.println("Server mode toggled to " + (serverMode ? "Joke" : "Proverb") + " mode.");
				System.out.println("Press Enter to toggle again , enter 's' to toggle servers and  'quit' to exit.");

				if ("quit".equalsIgnoreCase(input)) {
					break; // Exit the admin client if 'quit' is entered
				}

				// Close the socket connection
				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
//print this message when disconnected
		System.out.println("Admin client disconnected.");
	}
	//the toggle method when server has to be toggled from joke to proverb or proverb to joke
	private void toggleServerMode() {
		serverMode = !serverMode;
	}
}
//the following is the clientdata ,it is an object that can be used to communicate between jokeClient and server
class ClientData implements Serializable {
	private String clientID;
	private String userName;
	int randomVal;

	public ClientData(String clientID, String userName) {
		this.clientID = clientID;
		this.userName = userName;
		int randomVal;
	}

	public String getClientID() {//get method to retrieve clientID
		return clientID;
	}

	public String getName() {//get method to retrieve username of client that is connecting
		return userName;
	}
}
//the Client class that displays joke/proverb based on the server mode
class JokeClient {


	static String getName() {
		Scanner enter = new Scanner(System.in);
		System.out.print("please provide your name: ");
		return enter.nextLine();
	}

	static String getClientID() {
		UUID uuid = UUID.randomUUID();
		// Convert UUID to a string without hyphens
		String clientID = uuid.toString().replace("-", "");
		return clientID;
	}

	//connect to server
	static void connectToServer(ClientData clientData, String serverName, int port) {
		try {
			Socket sock = new Socket(serverName, port);

			ObjectOutputStream writer = new ObjectOutputStream(sock.getOutputStream());
			writer.writeObject(clientData);
			writer.flush();
			ObjectInputStream reader = new ObjectInputStream(sock.getInputStream());
			Object receivedfromserver = reader.readObject();

			if (receivedfromserver != null) {

				System.out.println(receivedfromserver);

				sock.close();
			}
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Socket error.");
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		int port = 4545;
		String serverName;

		System.out.print("Client is starting:");

		ClientData clientData = new ClientData(getClientID(), getName());

		if (args.length < 1) {
			serverName = "localhost";
		} else {
			serverName = args[0];
		}

		Scanner scanner = new Scanner(System.in);

		try {
			String line;
			do {
				System.out.println("Hit enter to begin, or type (quit) to exit");
				System.out.flush();
				line = scanner.nextLine();

				if (!line.contains("quit")) {
					connectToServer(clientData, serverName, port);
				}
			} while (!line.contains("quit"));

			System.out.println("Cancelled by User Request");
		} catch (Exception x) {
			System.out.println("Error");
			x.printStackTrace();
		} finally {
			scanner.close();
		}
	}

}
	public class JokeServer {

		//serverMode is DEFAULTED to joke.
		public static String serverMode = "Joke";
		public static Map<String, String> getJokeHistory = new HashMap<String, String>();
		public static Map<String, String> getProverbHistory = new HashMap<String, String>();
		public static int jr = 0;
		public static int provreset = 0;

		public static void main(String a[]) throws IOException {


			//Maximum requests a server can queue for processing by the OS
			int q_len = 6;
			// Default server port
			int servport = 4545;
			Socket sock;

			System.out.println("anju's JokeServer at" + servport);

			//Start the AdminLooper thread to handle administration input

			AL adminLooper = new AL();

			Thread adminThread = new Thread(adminLooper);

			adminThread.start();


			ServerSocket servsock = new ServerSocket(servport, q_len);


			while (true) {
				//accept the connection
				sock = servsock.accept();
				//after connection starts start the thread
				new JokeWorker(sock).start();

			}
		}
	}


	class AL implements Runnable {

		private static boolean switche = true;

		public void run() {
			int q_len = 6;
			int adminPort = 5050;

			Socket sock;

			try {
				//adminclient trying to connect to server
				ServerSocket servsock = new ServerSocket(adminPort, q_len);
				System.out.println("AdminLooper thread listening at port " + adminPort);
				while (switche) {

					sock = servsock.accept();
					// adminworker thread created
					new AdminWorker(sock).start();
				}
				//to handle excdptions place catch outside try blocks
			} catch (IOException f) {
				System.out.println(f);
			}
		}

	}


	class JokeWorker extends Thread {

		Socket sock;

		JokeWorker(Socket s) {
			sock = s;
		}

		public void run() {
			//using the objectoutputstreams and objectinputstreams to send and receive data
			//initializing them to null
			ObjectOutputStream objectOutputStream = null;

			ObjectInputStream objectInputStream = null;


			try {

				objectOutputStream = new ObjectOutputStream(sock.getOutputStream());
				objectOutputStream.flush();
				objectInputStream = new ObjectInputStream(sock.getInputStream());
				try {
					//deserialize the data from clientdata
					ClientData clientData = (ClientData) objectInputStream.readObject();

					System.out.println("Client ID: " + clientData.getClientID());

					//if client id that is extracted from clientdata is not present in hostory map it is better to make it to default 'NNNN'

					if (JokeServer.getJokeHistory.get(clientData.getClientID()) == null) {
						JokeServer.getJokeHistory.put(clientData.getClientID(), "NNNN");
					}
					if (JokeServer.getProverbHistory.get(clientData.getClientID()) == null) {
						JokeServer.getProverbHistory.put(clientData.getClientID(), "NNNN");
					}

					//randomizing the data
					randomizing(clientData, objectOutputStream);

					// Reset flags after 1 cycle
					if (JokeServer.jr == 1) {
						JokeServer.jr = 0;
					}

					if (JokeServer.provreset == 1) {
						JokeServer.provreset = 0;
					}
				} catch (ClassNotFoundException f) {
					//catch exceptions if any during deserialization
					System.out.println("error");
					f.printStackTrace();
				}
				// socket closed
				sock.close();
			} catch (IOException x) {
				System.out.println(x);
			}
		}

		static void randomizing(ClientData clientData, ObjectOutputStream toClient) {
			String clientretrieve;
			if (JokeServer.serverMode.equals("Joke")) {
				// get the already maintained history
				clientretrieve = JokeServer.getJokeHistory.get(clientData.getClientID());
				// Randomly select a joke to send to the client
				randomHelper(clientData, clientretrieve, toClient);
				if (JokeServer.jr == 0) {
					// only generate joke when above is zero
					jokeGenerator(clientData, toClient);
				}
			} else {
				// Retrieve the proverb history for the client
				clientretrieve = JokeServer.getProverbHistory.get(clientData.getClientID());
				// Randomly select a proverb to send to the client
				randomHelper(clientData, clientretrieve, toClient);
				if (JokeServer.provreset == 0) {
					// only generate new proverb if above is zero
					proverbGenerator(clientData, toClient);
				}
			}
		}

		static void randomHelper(ClientData clientData, String clientretrieve, ObjectOutputStream objectOutputStream) {

			Random generator = new Random();

			List<Integer> setofseen = new ArrayList<Integer>();

			System.out.println("assigning Id to  " + clientData.getName());

			for (int f = 0; f < clientretrieve.length(); f++) {
				if (clientretrieve.charAt(f) == 'N') {
					setofseen.add(f);
				}
			}
			if (!setofseen.isEmpty()) {
				// If there are items yet to be seen, select a random one
				int indrand = generator.nextInt(setofseen.size());
				//id generated for client

				clientData.randomVal = setofseen.get(indrand);
			} else if (setofseen.isEmpty()) {
				// Incase all jokes/proverbs have been seen, inform the client and reset history
				try {
					objectOutputStream.writeObject(JokeServer.serverMode + " cycle completed.");
				} catch (IOException e) {
					e.printStackTrace();
				}
				//reset the history when the cycle is complete
				historytobereset(clientData);
			}
		}

		static void jokeGenerator(ClientData clientData, ObjectOutputStream objectOutputStream) {
			// Generate and send a joke based on the random value
			try {
				if (clientData.randomVal == 0) {
					objectOutputStream.writeObject("JA " + clientData.getName() + ": Why don't skeletons fight each other? Because they don't have the guts!");
				} else if (clientData.randomVal == 1) {
					objectOutputStream.writeObject("JB " + clientData.getName() + ": How does a penguin build its house? Igloos it together!");
				} else if (clientData.randomVal == 2) {
					objectOutputStream.writeObject("JC " + clientData.getName() + ": How do you organize a space party? You planet!");
				} else if (clientData.randomVal == 3) {
					objectOutputStream.writeObject("JD " + clientData.getName() + ": Why did the bicycle fall over? Because it was two-tired!");
				} else {
					objectOutputStream.writeObject("error detected. " + clientData.randomVal);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			// // Update the client's joke history
			historyListUpdate(clientData.getClientID(), clientData.randomVal);
		}

		static void proverbGenerator(ClientData clientData, ObjectOutputStream objectOutputStream) {
			// Generate and send a proverb based on the random value
			try {
				if (clientData.randomVal == 0) {
					objectOutputStream.writeObject("PA " + clientData.getName() + ": A kind word is like a spring day; it brings warmth to the heart.");
				} else if (clientData.randomVal == 1) {
					objectOutputStream.writeObject("PB " + clientData.getName() + ": The wisest person in the room is often the one who listens the most.");
				} else if (clientData.randomVal == 2) {
					objectOutputStream.writeObject("PC " + clientData.getName() + ": Like a river flowing, life's journey carries us to unexpected destinations.");
				} else if (clientData.randomVal == 3) {
					objectOutputStream.writeObject("PD " + clientData.getName() + ": Patience is the key that unlocks the door to many beautiful possibilities.");
				} else {
					objectOutputStream.writeObject("error detected.");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			// Update the client's proverb history
			historyListUpdate(clientData.getClientID(), clientData.randomVal);
		}

		static void historyListUpdate(String clientID, int index) {
			ArrayList<Character> characterList = new ArrayList<Character>();
			String updatedHistory = "";

			String history;
			HashMap<String, String> historyMap;

			if (JokeServer.serverMode.equals("Joke")) {
				historyMap = JokeServer.getJokeHistory;
			} else {
				historyMap = JokeServer.getProverbHistory;
			}

			history = historyMap.get(clientID);

			for (int i = 0; i < history.length(); i++) {
				characterList.add(history.charAt(i));
			}

			characterList.set(index, 'Y');

			updatedHistory = listToString(characterList);

			historyMap.put(clientID, updatedHistory);
		}

		

		static String ListToString(ArrayList<Character> characterList) {
			StringBuilder resultBuilder = new StringBuilder(characterList.size());
			;

			for (Character ch : characterList) {
				resultBuilder.append(ch);
			}

			return resultBuilder.toString();
		}


		static void historytobereset(ClientData clientData) {
			if (JokeServer.serverMode.equals("Joke")) {
				// Reset the joke history, random value, and set the reset flag
				JokeServer.getJokeHistory.put(clientData.getClientID(), "NNNN");

				clientData.randomVal = 0;

				JokeServer.jr = 1;
			} else {
				// Reset the proverb history, random value, and set the reset flag
				JokeServer.getProverbHistory.put(clientData.getClientID(), "NNNN");

				clientData.randomVal = 0;

				JokeServer.provreset = 1;
			}
		}

		static String stringHelper(ArrayList<Character> charHolder) {
			// Create a StringBuilder to build the final string
			StringBuilder stringBuilder = new StringBuilder(charHolder.size());

			// Iterate through the list of characters and append each one to the StringBuilder
			for (Character character : charHolder) {
				stringBuilder.append(character);
			}

			// Convert the StringBuilder to a string and return it
			return stringBuilder.toString();
		}
	}

	class AdminWorker extends Thread {


		Socket s;

		AdminWorker(Socket s) {
			this.s = s;
		}

		public void run() {
			//printstream and reader initialized

			try {
				ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
				//connecting the input and output streams from the client.
				AdminData adminData = (AdminData) ois.readObject();
				boolean serverMode = adminData.getServerMode();
				JokeServer.serverMode = serverMode ? "Joke" : "Proverb";
				System.out.println("Received administration data from client.");
				System.out.println("Server Mode: " + (serverMode ? "Joke" : "Proverb"));
				s.close();
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}

		}
	}



	/*
	OUTPUTS:
	from single client when connected to server (one joke cycle)
	SERVER SIDE:
	java JokeServer
anju's JokeServer at4545
AdminLooper thread listening at port 5050
Client ID: 4ca9753be70a46cea72b922c7028eefc
assigning Id to  anju
Client ID: 4ca9753be70a46cea72b922c7028eefc
assigning Id to  anju
Client ID: 4ca9753be70a46cea72b922c7028eefc
assigning Id to  anju
Client ID: 4ca9753be70a46cea72b922c7028eefc
assigning Id to  anju
Client ID: 4ca9753be70a46cea72b922c7028eefc
assigning Id to  anju
Client ID: 4ca9753be70a46cea72b922c7028eefc
assigning Id to  anju
Client ID: 4ca9753be70a46cea72b922c7028eefc
assigning Id to  anju
Client ID: 4ca9753be70a46cea72b922c7028eefc
assigning Id to  anju
Client ID: 4ca9753be70a46cea72b922c7028eefc
assigning Id to  anju

CLIENT SIDE
Client is starting:please provide your name: anju
Hit enter to begin, or type (quit) to exit

JC anju: How do you organize a space party? You planet!
Hit enter to begin, or type (quit) to exit

JD anju: Why did the bicycle fall over? Because it was two-tired!
Hit enter to begin, or type (quit) to exit

JA anju: Why don't skeletons fight each other? Because they don't have the guts!
Hit enter to begin, or type (quit) to exit

JB anju: How does a penguin build its house? Igloos it together!
Hit enter to begin, or type (quit) to exit

Joke cycle completed.
Hit enter to begin, or type (quit) to exit

JA anju: Why don't skeletons fight each other? Because they don't have the guts!
Hit enter to begin, or type (quit) to exit

JB anju: How does a penguin build its house? Igloos it together!
Hit enter to begin, or type (quit) to exit

JC anju: How do you organize a space party? You planet!
Hit enter to begin, or type (quit) to exit

JD anju: Why did the bicycle fall over? Because it was two-tired!
Hit enter to begin, or type (quit) to exit

NOW IN PROVERB MODE:
SERVER SIDE
assigning Id to  anju
Received administration data from client.
Server Mode: Proverb
ADMIN CLIENT SIDE:
java JokeClientAdmin
Admin client connected to primary server: localhost

Server mode toggled to Proverb mode.
Press Enter to toggle again , enter 's' to toggle servers and  'quit' to exit.

CLIENT SIDE:

PA anju: A kind word is like a spring day; it brings warmth to the heart.
Hit enter to begin, or type (quit) to exit

PD anju: Patience is the key that unlocks the door to many beautiful possibilities.
Hit enter to begin, or type (quit) to exit

PB anju: The wisest person in the room is often the one who listens the most.
Hit enter to begin, or type (quit) to exit

PC anju: Like a river flowing, life's journey carries us to unexpected destinations.
Hit enter to begin, or type (quit) to exit

Proverb cycle completed.
Hit enter to begin, or type (quit) to exit

PB anju: The wisest person in the room is often the one who listens the most.
Hit enter to begin, or type (quit) to exit

PC anju: Like a river flowing, life's journey carries us to unexpected destinations.
Hit enter to begin, or type (quit) to exit

PA anju: A kind word is like a spring day; it brings warmth to the heart.
Hit enter to begin, or type (quit) to exit

PD anju: Patience is the key that unlocks the door to many beautiful possibilities.
Hit enter to begin, or type (quit) to exit

Proverb cycle completed.

INTERLEAVING BETWEEN CLIENTS AND ADMIN CLIENT TOGGLING THE MODE IN BETWEEN:
CLIENT 1:
Proverb cycle completed.
Hit enter to begin, or type (quit) to exit

PD anju: Patience is the key that unlocks the door to many beautiful possibilities.
Hit enter to begin, or type (quit) to exit

PC anju: Like a river flowing, life's journey carries us to unexpected destinations.
Hit enter to begin, or type (quit) to exit

PA anju: A kind word is like a spring day; it brings warmth to the heart.
Hit enter to begin, or type (quit) to exit

Joke cycle completed.
Hit enter to begin, or type (quit) to exit

JC anju: How do you organize a space party? You planet!
Hit enter to begin, or type (quit) to exit

CLIENT 2:
Client is starting:please provide your name: shaik
Hit enter to begin, or type (quit) to exit

PD shaik: Patience is the key that unlocks the door to many beautiful possibilities.
Hit enter to begin, or type (quit) to exit

PC shaik: Like a river flowing, life's journey carries us to unexpected destinations.
Hit enter to begin, or type (quit) to exit

PB shaik: The wisest person in the room is often the one who listens the most.
Hit enter to begin, or type (quit) to exit

JB shaik: How does a penguin build its house? Igloos it together!
Hit enter to begin, or type (quit) to exit

JD shaik: Why did the bicycle fall over? Because it was two-tired!
Hit enter to begin, or type (quit) to exit

JA shaik: Why don't skeletons fight each other? Because they don't have the guts!
Hit enter to begin, or type (quit) to exit

SERVER SIDE:
assigning Id to  anju
Client ID: 613bb92ebd434fcb9b91a3a269204a8e
assigning Id to  shaik
Client ID: 613bb92ebd434fcb9b91a3a269204a8e
assigning Id to  shaik
Client ID: 4ca9753be70a46cea72b922c7028eefc
assigning Id to  anju
Client ID: 4ca9753be70a46cea72b922c7028eefc
assigning Id to  anju
Client ID: 613bb92ebd434fcb9b91a3a269204a8e
assigning Id to  shaik
Client ID: 4ca9753be70a46cea72b922c7028eefc
assigning Id to  anju
Received administration data from client.
Server Mode: Joke
Client ID: 4ca9753be70a46cea72b922c7028eefc
assigning Id to  anju
Client ID: 4ca9753be70a46cea72b922c7028eefc
assigning Id to  anju
Client ID: 613bb92ebd434fcb9b91a3a269204a8e
assigning Id to  shaik
Client ID: 613bb92ebd434fcb9b91a3a269204a8e
assigning Id to  shaik
Client ID: 613bb92ebd434fcb9b91a3a269204a8e
assigning Id to  shaik


MY POSTINGS IN D2L:
1)It is imperative to exercise caution and maintain consistency when employing data transmission methods in network communication. In a specific scenario, i  utilized both ObjectOutputStream and ObjectInputStream for data exchange by the server and was combined with the use of PrintStream and buffer reader by client for output. This  inadvertently resulted in the appearance of encrypted characters at the outset of each printed joke. so its better to stick to one method for data exchange
Also
I encountered challenges while attempting to extend the existing codebase to incorporate secondary servers alongside their corresponding client and client admin components. This expansion process proved to be complex and presented difficulties in achieving the desired functionality.

2)	Doubt about Log files



3 hours ago
Hi paavani these links were helpful!

	 */

	

	
