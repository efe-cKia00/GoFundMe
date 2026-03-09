# GoFundMe — TCP Fundraising Application

A multithreaded client-server fundraising application built in Java using TCP sockets. Multiple clients can connect to a single server simultaneously and interact with a shared list of fundraising events in real time.

---

## Architecture

```
Server
 ├── owns one EventManager (shared, thread-safe)
 └── spawns one ClientHandler thread per connected client
      └── each ClientHandler reads/writes to EventManager (synchronized)

Client  <──── TCP Socket ────>  ClientHandler
```

### Classes

| Class | Role |
|---|---|
| `FundraisingEvent` | Data model representing a single fundraising event (name, target amount, raised amount, deadline) |
| `EventManager` | Thread-safe manager for the shared event list; handles creating, listing, and donating |
| `Server` | Entry point for the server; accepts incoming connections and spawns a `ClientHandler` thread per client |
| `ClientHandler` | Runs on its own thread; handles all communication with one connected client |
| `Client` | Entry point for the client; presents an interactive menu and communicates with the server over a TCP socket |

---

## Requirements

- Java 25+
- Apache Maven

---

## Build

From the project root directory:

```bash
mvn compile
```

---

## How to Run

**1. Start the server** (must be started first):

```bash
mvn exec:java -Dexec.mainClass=org.eproject.gofundme.Server
```

**2. Start one or more clients** (each in a separate terminal):

```bash
mvn exec:java -Dexec.mainClass=org.eproject.gofundme.Client
```

The server listens on **port 12345** by default. The client connects to `localhost:12345`.

---

## Features

- **Create Event** — provide an event name, target amount, and deadline (YYYY-MM-DD); the server registers the event
- **List Events** — displays all current events (deadline not yet passed) and past events (deadline has passed), each sorted by deadline in ascending order
- **Donate** — select a current event by its account ID and enter a donation amount
- **Quit** — gracefully disconnects the client from the server

All user input is validated on the client side before being sent to the server. The server additionally validates all incoming data and logs every request and connection event to the server console.

---

## Communication Protocol

All communication between `Client` and `ClientHandler` is plain-text over a TCP socket. Each value is sent on its own line.

| Client sends | Server reads | Server responds |
|---|---|---|
| `CREATE` → name → targetAmount → deadline | Validates and creates the event | One-line success or error message |
| `LIST` | Fetches current and past events | Lines of event data, terminated by `END_OF_LIST` |
| `DONATE` → index → amount | Validates and applies the donation | One-line success or error message |
| `QUIT` | Closes the client's thread | *(no response; connection closed)* |

---

## Author(s)
- [Efe Awo-Osagie](https://www.linkedin.com/in/efe-awo/)

---

## Future Development

- **File Persistence** — a planned future enhancement would serialize all `FundraisingEvent` data to a file (e.g., CSV or JSON) whenever the event list is modified. On startup, the server would load this file and restore all previously created events, allowing data to survive server restarts.

---

## Notice:
This application served as the Author's Java TCP socket programming project in CSCI455 (Networking and Parallel Computation) course. It is for educational purposes only. See your institution's guidelines for code sharing and reuse.
