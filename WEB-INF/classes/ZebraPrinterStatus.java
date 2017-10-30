public class ZebraPrinterStatus {
  private String ipAddress;
  private boolean hasError;
  private String message;

  public ZebraPrinterStatus(String ipAddress, boolean hasError, String message) {
    this.ipAddress = ipAddress;
    this.hasError = hasError;
    this.message = message;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public boolean hasError() {
    return hasError;
  }

  public String getMessage() {
    return message;
  }
}
