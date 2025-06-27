// NetworkUtils.java - Network connectivity utilities
package main.utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class NetworkUtils {

    /**
     * Check if a host and port are reachable
     * @param host Hostname or IP address
     * @param port Port number
     * @param timeout Timeout in milliseconds
     * @return true if reachable, false otherwise
     */
    public static boolean isHostReachable(String host, int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            return true;
        } catch (SocketTimeoutException e) {
            System.err.println("Timeout connecting to " + host + ":" + port);
            return false;
        } catch (IOException e) {
            System.err.println("Cannot connect to " + host + ":" + port + " - " + e.getMessage());
            return false;
        }
    }

    /**
     * Get local IP address for network diagnostics
     * @return Local IP address
     */
    public static String getLocalIPAddress() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("google.com", 80));
            return socket.getLocalAddress().getHostAddress();
        } catch (IOException e) {
            return "Unable to determine local IP";
        }
    }

    /**
     * Ping a host to check connectivity
     * @param host Hostname or IP address
     * @return true if pingable, false otherwise
     */
    public static boolean pingHost(String host) {
        try {
            Process process = Runtime.getRuntime().exec("ping -c 1 " + host);
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            System.err.println("Error pinging host: " + e.getMessage());
            return false;
        }
    }
}