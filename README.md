# Zebra LinkOS via Tomcat
Interface with LinkOS-enabled Zebra printers via your local Tomcat server.

## Background
We are attempting to connect to Zebra printers using a Tomcat server. Features at present include:

1. **Query status**: Query of printer status using SDK method `getPrinterStatus()` and supplied TCP/IP addresses.
2. **Send alert to HTTP webhook** Servlet POST endpoint to allow printer to send alerts using SDK method `configureAlerts()`.
3. **TODO** Multithread / promise queries for the above.

### 1. Query status
By entering `http://localhost:port/servlet/get?ip=10.10.0.1,10.10.0.2` you should be returned with a JSON object with the following information:

```JSON
[
  {
    "ipAddress": "10.10.0.1",
    "hasError": true,
    "message": "Cannot Print because the printer is paused."
  },
  {
    "ipAddress": "10.10.0.2",
    "hasError": false,
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

### 2. Send alert to HTTP webhook
Opening POST endpoint in servlet method `doPost()` will allow it to handle incoming POST requests from the Zebra printer. Data is sent over as a HTTP `multipart-form`

```
POST /http_post/alert.php HTTP/1.1
Host: 10.3.4.58
Accept: */*
Connection: close
Content-Length: 281
Expect: 100-continue
Content-Type: multipart/form-data; boundary=------------------
----------350c75835f46
------------------------------350c75835f46
Content-Disposition: form-data; name="alertMsg"
ALERT%3A%20PRINTER%20PAUSED
------------------------------350c75835f46
Content-Disposition: form-data; name="uniqueId"
XXQLJ120900310
------------------------------350c75835f46--
```

This is translated into a JSON object with structure

```json
{
  "ipAddress": "1J384DHF843",
  "hasError": true,
  "message": "ALERT: PRINTER PAUSED"
}
```

and sent to the webhook. The constant parameter `WEBHOOK_URL` in `PrinterStatusServlet.java` has been omitted, please furnish your own.

#### Notes
- Note that the IP address of the printer cannot be sent over in this iteration, since the `POST` request does not make this available.
- You could configure the 8th parameter in `PrinterAlert` to customize this message. Refer to [Zebra Techdocs](http://techdocs.zebra.com/link-os/latest/webservices/).

## Setup
- Install the following
```
tomcat-8.5.23
gson-2.1
```

- Ensure you have opened up a `GET` servlet endpoint at `http://localhost:8080/servlet/get` and configured the necessary files. You can refer to several tutorials online to see how this is done.
- Do the same for the `POST` endpoint.
- Download the LinkOS SDK from Zebra, or import the file that is in `lib/ZSDK_API.jar`. Remember that you would need to include this file in your compile path.
- To configure printer alerts, open a servlet that uses SDK method `configureAlerts()`. I have implemented a simple on/off `GET` switch at endpoint `http://localhost:8080/servlet/config`.

### Compilation
Use the following script or code to compile the relevant class files.

```
javac -classpath .:/usr/local/Cellar/tomcat/8.5.23/libexec/lib/*:/usr/local/Cellar/tomcat/8.5.23/libexec/webapps/workato/WEB-INF/lib/ZSDK_API.jar *.java
```

Or you could run `./compile.sh`.
