import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.List;

import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.TcpConnection;
import com.zebra.sdk.printer.*;
import com.zebra.sdk.device.ZebraIllegalArgumentException;
import com.zebra.sdk.settings.AlertCondition;
import com.zebra.sdk.settings.AlertDestination;


public class PrinterAlertServlet extends HttpServlet {

  /** Input printer IP details **/
  private static final String PRINTSERVER_IP = "10.10.8.204";
  private static final AlertDestination ALERT_DESTINATION = AlertDestination.HTTP;
  private static final String DESTINATION = "http://10.10.8.205:8080/workato/status";

  public String configPrinterAllMessagesAlert() throws Exception {
    String result = "";
    TcpConnection connection = new TcpConnection(PRINTSERVER_IP, TcpConnection.DEFAULT_ZPL_TCP_PORT);
    try {
      connection.open();
      ZebraPrinter genericPrinter = ZebraPrinterFactory.getInstance(connection);
      if (genericPrinter.equals(null)) {
        result += "Failed to get printer instance wrapper.\n\n";
      }
      // ZebraPrinterLinkOs linkOsPrinter = ZebraPrinterFactory.createLinkOsPrinter(genericPrinter);
      ZebraPrinterLinkOs linkOsPrinter = ZebraPrinterFactory.getLinkOsPrinter(connection);
      if (linkOsPrinter != null) {
        if (linkOsPrinter.getConfiguredAlerts().size() == 0) {
          PrinterAlert setAlerts = new PrinterAlert(AlertCondition.ALL_MESSAGES, ALERT_DESTINATION, true, false, DESTINATION, 0, false);
          linkOsPrinter.configureAlert(setAlerts);
          List<PrinterAlert> alerts = linkOsPrinter.getConfiguredAlerts();
          result += alerts.size() + " alerts set.\n";
          for (PrinterAlert thisAlert : alerts) {
            result += (thisAlert.getCondition() + " " + thisAlert.getDestinationAsSgdString() + "\n");
          }
        } else {
          linkOsPrinter.removeAllAlerts();
          result = "All alerts deconfigured.";
        }
      } else {
        result = "Printer down.";
      }
    } catch (ConnectionException e) {
      result = "Connection exception.";
    } catch (ZebraPrinterLanguageUnknownException e) {
      result = "Zebra printer language unknown exception.";
    } catch (ZebraIllegalArgumentException e) {
      result = "Zebra illegal argument exception.";
    } catch (Exception e) {
      result = e.toString();
    } finally {
      connection.close();
    }
    return result;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException {
      // Set the response MIME type of the response message
      response.setContentType("text/html");
      // Allocate an output writer to write the response message into the network socket
      PrintWriter out = response.getWriter();

      // Write the response message, in an HTML page
      try {
        String status = configPrinterAllMessagesAlert();
        out.println("<html>");
        out.println("<head><title>Hello, World</title></head>");
        out.println("<p>Response: <strong>" + status + "</strong></p>");
        out.println("</body></html>");
      } catch (Exception e) {
        out.println("<p>Error: " + e.toString() + "</p>");
      } finally {
        out.close();
      }
    }
}
