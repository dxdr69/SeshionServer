# SeshionServer

Seshion is a client-server based meetup app for Android that allows users to create and join public events (sessions) based on their geographical location. In addition to this, 
users can also be invited to or invite others to events, add friends, and view currently open events around them in real-time via a map interface.

[Client repository can be found here](https://github.com/dxdr69/SeshionClient)

## Documentation

[Full documentation can be found here](https://drive.google.com/drive/folders/1WrRzsw0rnPyAzixODXPZbLkHebdfItTs?usp=sharing)

## Getting Started

This repository contains the back-end to the Seshion app, which consists of a Java program that runs an encrypted, multi-threaded WebSocket server to handle incoming client connections and requests. It also contains a .sql script to create the necessary tables for a PostgreSQL database that the server interacts with for storage and retrieval of user information.

### Prerequisites

The following programs are required if you want to host an instance of the application: 

```
Java 8 (Oracle or OpenJDK)
Gson 2.8.6 as a .jar file
PostgreSQL 12
```

### Build Instructions

**Server**
```
1. Download the .jar for Gson 2.8.6, place it inside the project root directory, and add it as an entry to the .classpath
2. Compile the Seshion server as a .jar using the repository source code
```

**Database**
```
1. Install PostgreSQL 12 and create a new database instance
2. Change the default password for the instance to "Lgn@Psql"
3. Create a new database called "seshiondb" 
4. Run seshion_create.sql to create the necessary tables
```

**Deployment**
```
With the database active, run the Seshion server .jar with Java to start the back-end
```

## Built With

* [Java 8](https://java.com/en/download/faq/java8.xml)
* [OpenJDK 8](https://wiki.openjdk.java.net/display/jdk8u/Main)
* [Gson 2.8.6](https://search.maven.org/artifact/com.google.code.gson/gson/2.8.6/jar)
* [PostgreSQL 12](https://www.postgresql.org/docs/12/index.html)
* [Android Studio](https://developer.android.com/studio?hl=it)

## Authors

* **Adrian Brocke** - *Android client* - [TwizzyBomb](https://github.com/TwizzyBomb)
* **Nicholas Martin** - *Back-end database management* - [dxdr69](https://github.com/dxdr69)
* **Shenghui Wu** - *Back-end web server* - [ShenghuiW](https://github.com/ShenghuiW)
* **David Toth** - *Documentation and system modeling* - IonizedSilver@gmail.com
