# JMS Chat System

A distributed chat application built using **Java Message Service (JMS)** supporting both **broadcast messaging (Topic)** and **private messaging (Queue)**.

The system includes a simple graphical interface where users can view messages, see connected users, and send messages either to all participants or to a selected user.

This project was developed to demonstrate the use of **message-oriented middleware** and asynchronous communication using JMS.

---

## Features

- Broadcast messaging using **JMS Topics**
- Private messaging using **JMS Queues**
- Graphical user interface for chat interaction
- List of connected users
- Message display panel
- Option to send messages to all users or to a selected user
- Configurable message persistence and delivery settings

---

## Technologies

- **Java**
- **Java Message Service (JMS)**
- **Swing**
- Message broker (e.g. ActiveMQ)

---

## Architecture

The application uses a message-oriented architecture based on JMS.

Two messaging patterns are implemented:

### Topic (Broadcast)

Messages are published to a **Topic**, allowing all subscribed users to receive the message.

```
User → Topic → All connected users
```

### Queue (Private Message)

Private messages are sent through **Queues**, ensuring that the message is delivered to a specific user.

```
User A → Queue → User B
```

---

## Interface

The graphical interface includes:

- Message display area
- Input field for writing messages
- List of active users
- Option to choose:
  - Broadcast message
  - Private message

---

## Running the Project

1. Start the JMS broker (e.g. ActiveMQ)
2. Run the chat server (if applicable)
3. Launch multiple chat clients

Each client will connect to the messaging broker and participate in the chat.

---

## Educational Purpose

This project was developed as part of a university assignment to explore:

- Message-oriented middleware
- Asynchronous communication
- Publish/Subscribe architecture
- Distributed systems design

---

## Future Improvements

- User authentication
- Message history persistence
- Improved UI
- Encryption for private messages
- WebSocket or web-based interface

---
