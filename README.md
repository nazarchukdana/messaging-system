# Client-Server Messaging System

## Project Goal
The Client-Server Messaging System is designed to facilitate real-time communication between multiple users through a server-managed infrastructure. This project emphasizes input-output (I/O) streams, focusing on socket-based communication to ensure seamless message delivery and efficient client-server interactions. Additionally, the system enforces message filtering to maintain a clean communication environment.

## Project Overview
This project consists of **two applications**:
1. **Server**: Manages client connections, relays messages, and enforces filtering rules.
2. **Client**: Connects to the server and enables users to send and receive messages.

## Features
### Server
- Loads **configuration settings** from a file, specifying:
  - Server port
  - Server name
  - List of **banned phrases** (to filter messages).
- Supports **multiple clients** connecting simultaneously.
- Maintains a **list of active clients** (username and port).
- Broadcasts messages **to recipients specified by the sender**.
- **Filters messages** containing banned phrases and notifies the sender.
- Notifies clients when a user **disconnects**.

### Client
- Connects to the server.
- Displays **error messages** if unable to connect.
- Receives the **list of connected users** and **instructions** on messaging features.
- Messaging capabilities:
  - **Broadcast messages** to all clients.
  - **Send private messages** to specific clients.
  - **Send group messages** to multiple clients.
  - **Exclude certain users** from receiving a message.
  - **Query the server** for the list of banned phrases.
- Receives **notifications** when a client disconnects.

## Configuration File
The **server configuration file** should include:
```
ChatServer
12345
spam
offensive_word
```

## Contributors
- **Author:** Dana Nazarchuk
- Created as a school project for Polish Japanese Academy of Information technologies

