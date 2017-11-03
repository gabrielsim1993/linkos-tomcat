import java.io.*;
import java.util.*;
import java.lang.*;

import javax.servlet.*;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.*;
import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.net.URLDecoder;

import com.google.gson.Gson;
import org.apache.catalina.core.ApplicationPart;

import com.zebra.sdk.comm.*;
import com.zebra.sdk.printer.*;

@MultipartConfig
public class PrinterStatusServlet extends HttpServlet {

  private List<String> getIpAddressList(String ipAddresses) {
    return new ArrayList<String>(Arrays.asList(ipAddresses.split(",")));
  }

  private List<Connection> initiateConnections(List<String> ipAddressList) {
    List<Connection> connections = new ArrayList<Connection>();
    for (String ip : ipAddressList) {
      Connection connection = new TcpConnection(ip, TcpConnection.DEFAULT_ZPL_TCP_PORT);
      connections.add(connection);
    }
    return connections;
  }

  private List<ZebraPrinterStatus> getPrinterStatus(String ipAddresses) throws Exception {
    String result = "";
    String message = "";
    boolean hasError = true;

    List<String> ipAddressList = getIpAddressList(ipAddresses);
    List<Connection> connections = initiateConnections(ipAddressList);
    List<ZebraPrinterStatus> statuses = new ArrayList<ZebraPrinterStatus>();

    ZebraPrinterStatus zbps;

    for (Connection connection : connections) {
      try {
        connection.open();
        ZebraPrinter printer = ZebraPrinterFactory.getInstance(connection);
        PrinterStatus printerStatus = printer.getCurrentStatus();
        if (printerStatus.isReadyToPrint) {
          message = "Ready To Print";
          hasError = false;
        } else if (printerStatus.isPaused) {
          message = "Cannot Print because the printer is paused.";
        } else if (printerStatus.isHeadOpen) {
          message = "Cannot Print because the printer head is open.";
        } else if (printerStatus.isPaperOut) {
          message = "Cannot Print because the paper is out.";
        } else {
          message = "Cannot Print.";
        }
      } catch (Exception e) {
        message = e.toString();
      } finally {
        zbps = new ZebraPrinterStatus(((TcpConnection)connection).getAddress(), hasError, message);
        connection.close();
      }
      statuses.add(zbps);
    }
    return statuses;
  }

  public ZebraPrinterStatus getDefaultStatus() {
    return new ZebraPrinterStatus(null, true, "Please give me a few IP addresses to work with.");
  }

  public ZebraPrinterStatus getErrorStatus(String errorMessage) {
    return new ZebraPrinterStatus(null, true, errorMessage);
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException {
      String ipAddresses = request.getParameter("ip");
      String output = "";
      List<ZebraPrinterStatus> statuses;
      PrintWriter out = response.getWriter();
      Gson gson = new Gson();
      response.setContentType("application/json");

      try {
        statuses = getPrinterStatus(ipAddresses);
        output = gson.toJson(statuses);
      } catch (NullPointerException npe) {
        output = gson.toJson(getDefaultStatus());
      } catch (Exception e) {
        output = gson.toJson(getErrorStatus(e.toString()));
      } finally {
        out.println(output);
        out.close();
      }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException {
      String printerIpAddress = request.getHeader("Host");

      ApplicationPart alertMsgPart = (ApplicationPart)request.getPart("alertMsg");
      String alertMsg = URLDecoder.decode(alertMsgPart.getString("UTF-8"), "UTF-8");

      ZebraPrinterStatus zbps = new ZebraPrinterStatus(printerIpAddress, true, alertMsg);
      List<ZebraPrinterStatus> zbpsArray = new ArrayList<ZebraPrinterStatus>();
      zbpsArray.add(zbps);
      Gson gson = new Gson();
      String output = gson.toJson(zbpsArray);

      URL url = new URL(WEBHOOK_URL);
      HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
      con.setRequestMethod("POST");
      con.setDoOutput(true);
      DataOutputStream wr = new DataOutputStream(con.getOutputStream());
      wr.writeBytes(output);
      wr.flush();
      wr.close();
      con.getResponseCode();
    }
}
