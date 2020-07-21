# SeshionServer

Seshion is a client-server based meetup app for Android that allows users to create and join public events based on their geographical location. In addition to this, 
users can also be invited to or invite others to events, add friends, and view currently open events around them in real-time via a map interface.

[Client repository can be found here](https://github.com/dxdr69/SeshionClient)

## Documentation

[Full documentation can be found here](https://drive.google.com/drive/folders/1WrRzsw0rnPyAzixODXPZbLkHebdfItTs?usp=sharing)

## Getting Started

This repository contains the back-end to the Seshion app, which consists of a Java program that runs a web server to handle incoming client connections, requests, and a PostgreSQL database.

### Prerequisites

The Seshion back-end is currently hosted on a VPS provided by DigitalOcean and rented by the creators of the project. The following programs are required if you want to host an instance of the application: 

```
Java 8 (Oracle or OpenJDK)
PostgreSQL 12
```

### Installing

1. Compile the .jar using the repository source code
2. Change the default password for the PostgreSQL instance to "Lgn@Psql"
3. Create a new database in the PostgreSQL instance called "seshiondb" 
4. Create the necessary tables using the provided .sql scripts
5. Run the .jar to start the back-end

## Built With

* [Java 8](https://java.com/en/download/faq/java8.xml)
* [OpenJDK 8](https://wiki.openjdk.java.net/display/jdk8u/Main)
* [PostgreSQL 12](https://www.postgresql.org/docs/12/index.html)

## Authors

* **Adrian Brocke** - *Android client* - [TwizzyBomb](https://github.com/TwizzyBomb)
* **Nicholas Martin** - *Back-end database management* - [dxdr69](https://github.com/dxdr69)
* **Shenghui Wu** - *Back-end web server* - [ShenghuiW](https://github.com/ShenghuiW)
* **David Toth** - *Documentation and system modeling* - IonizedSilver@gmail.com
