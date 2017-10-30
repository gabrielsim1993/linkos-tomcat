import java.io.*;
import java.util.*;
import java.lang.*;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.net.ssl.HttpsURLConnection;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.net.URL;

import com.zebra.sdk.comm.*;
import com.zebra.sdk.printer.*;

public class HomeServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException {
      PrintWriter out = response.getWriter();
      String output = new String();
      Gson gson = new Gson();
      response.setContentType("application/json");
      try {
        output = gson.toJson(new ZebraPrinterStatus(null, false, "200 OK"));
      } catch (Exception e) {
        output = gson.toJson(new ZebraPrinterStatus(null, false, e.toString()));
      } finally {
        out.println(output);
        out.close();
      }
  }
}
