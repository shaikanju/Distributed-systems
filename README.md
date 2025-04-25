# Joke & Proverb Server System

## 📌 Description

This is a **Java-based multithreaded network application** that allows clients to receive either jokes or proverbs from a central server. The server can operate in two modes:

- **Joke Mode**: Sends random jokes to clients.
- **Proverb Mode**: Sends random proverbs to clients.

Clients receive content based on the current server mode, and the **Admin Client** can toggle the server mode in real time. Each client has their own independent session with a unique ID and personalized cycle tracking.

---

## 👨‍💻 Author

- **Name**: Anju Shaik  
- **Date**: October 8, 2023  
- **Java Version**: Java 20.0.2 (build 20.0.2+9-78)

---

## 🗂️ Project Structure

This project consists of the following components in a single Java file:

| Class Name           | Purpose                                                                 |
|----------------------|-------------------------------------------------------------------------|
| `JokeServer`         | Main server class that handles joke/proverb dispatch and listens for admin control. |
| `JokeClient`         | Client application that connects to the server to receive jokes/proverbs. |
| `JokeClientAdmin`    | Admin client to toggle the server mode.                                 |
| `ClientData`         | Serializable data structure that holds client ID and username.          |
| `AdminData`          | Serializable data structure holding the server mode toggle value.       |
| `JokeWorker`         | A thread that handles a single client connection.                       |
| `AdminWorker`        | A thread that handles a single admin connection.                        |
| `AL`                 | Runnable class that starts an admin port listener on the server.        |

---

## 🧰 Requirements

- Java JDK 20 or later
- Terminal/Command Prompt or any Java-compatible IDE

---

## ⚙️ Compilation

Open the terminal in your project folder and run:

```bash
javac JokeServer.java
```

> ⚠️ Ensure all classes are in one file named `JokeServer.java`.

---

## 🚀 How to Run

> Open three separate terminal windows (or tabs) for running the server, client, and admin client.

### 1. Run the Server

```bash
java JokeServer
```

- Listens on:
  - Port `4545` for clients
  - Port `5050` for admin clients

---

### 2. Run the Client

```bash
java JokeClient
```

- Prompts for a username.
- Sends requests to receive jokes or proverbs.
- Repeats until the user types `quit`.

---

### 3. Run the Admin Client

```bash
java JokeClientAdmin
```

- Toggles server mode between Joke and Proverb.
- Press `Enter` to toggle.
- Type `quit` to exit.

---

## 🎭 Behavior

### Client

- Each client gets a unique ID (via `UUID`).
- Tracks which jokes or proverbs have been sent (via string like `"NNNN"`).
- Once all 4 jokes/proverbs are sent, history resets and the cycle repeats.
- Communication uses `ObjectOutputStream` and `ObjectInputStream`.

### Server

- Handles multiple clients using threads (`JokeWorker`).
- Maintains individual history for each client in maps (`getJokeHistory`, `getProverbHistory`).
- Automatically resets history after a full cycle.

### Admin

- Connects to port `5050`.
- Sends a boolean flag to toggle server mode.

---

## 💬 Example Outputs

**Client Side (Joke Mode):**

```
please provide your name: Anju
Hit enter to begin, or type (quit) to exit
JA Anju: Why don't skeletons fight each other? Because they don't have the guts!
...
Joke cycle completed.
```

**Admin Side:**

```
Admin client connected to primary server: localhost
Server mode toggled to Proverb mode.
```

**Client Side (Proverb Mode):**

```
PA Anju: A kind word is like a spring day; it brings warmth to the heart.
...
Proverb cycle completed.
```

---

## ⚠️ Limitations

- ❌ Secondary server logic is not implemented.
- 🚫 No authentication or security layer.
- 🧪 Mixing object streams with other stream types (e.g., `BufferedReader`) can cause issues.

---

## 🧠 Learning Outcomes

- Hands-on experience with **Java socket programming**.
- Working with **multithreading**.
- Managing client-specific **state tracking**.
- Using **object serialization** for communication.
- Implementing a **console-based admin control system**.

---

## 📜 License

This project is for educational use. Feel free to modify and enhance it.
