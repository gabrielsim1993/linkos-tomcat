# Zebra LinkOS via Tomcat
Interface with LinkOS-enabled Zebra printers via your local Tomcat server.

## Background
We are attempting to connect to Zebra printers using a Tomcat server. Features at present include:

- **Query status**: Query of printer status using SDK method `getPrinterStatus()` and supplied TCP/IP addresses.
- **TODO** Multithread / promise query for the above.
- **TODO** Open servlet POST endpoint to allow printer to communicate with server directly.

### Query status
By entering `http://localhost:port/servlet/get?ip=10.10.0.1,10.10.0.2` you should be returned with a JSON object with the following information:

```JSON
[
  {
    "ipAddress": "10.10.0.1",
    "hasError": false,
    "message": "Cannot Print because the printer is paused."
  },
  {
    "ipAddress": "10.10.0.2",
    "hasError": true,
    "message": "Ready To Print."
  }
]
```

#### Notes
1. The query string is parsed using delimiter `,`. Thereafter each IP address string is passed into an opened Zebra `connection` and iterated through to obtain each print status.

2. The JSON object is built by GSON factory taking into account the object structure of [`ZebraPrinterStatus`](/WEB-INF/classes/ZebraPrinterStatus.java)
```java
public class ZebraPrinterStatus {
  private String ipAddress;
  private boolean hasError;
  private String message;
  ...
}
```

3. The messages can be configured according to the real statuses returned in [`PrinterStatusServlet.java`](/WEB-INF/classes/PrinterStatusServlet.java).
```java
if (printerStatus.isReadyToPrint) {
  message = "Ready To Print";
  hasError = false;
} else if (printerStatus.isPaused) {
  message = "Cannot Print because the printer is paused.";
} ...
```

## Setup
- Install the following
```
tomcat-8.5.23
```

- Ensure you have opened up a `GET` servlet endpoint at `http://localhost:8080/servlet/get` and configured the necessary files. You can refer to several tutorials online to see how this is done.
- Download the LinkOS SDK from Zebra, or import the file that is in `lib/ZSDK_API.jar`. Remember that you would need to include this file in your compile path.

### Compilation
Use the following script or code to compile the relevant class files.

```
javac -classpath .:/usr/local/Cellar/tomcat/8.5.23/libexec/lib/servlet-api.jar:/usr/local/Cellar/tomcat/8.5.23/libexec/webapps/workato/WEB-INF/lib/ZSDK_API.jar:/usr/local/Cellar/tomcat/8.5.23/libexec/lib/gson-2.6.2.jar HomeServlet.java
```

We have included the following paths into the compile path

- PWD: `.`
- Tomcat: `/usr/local/Cellar/tomcat/8.5.23/libexec/lib/servlet-api.jar`
- LinkOS SDK: `/usr/local/Cellar/tomcat/8.5.23/libexec/webapps/workato/WEB-INF/lib/ZSDK_API.jar`
- Google GSON: `/usr/local/Cellar/tomcat/8.5.23/libexec/lib/gson-2.6.2.jar`
