package or.kr.formulate.korail.dummy;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class WhoisClient {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Argumnets is null....");
            return;
        }

        String domainName = args[0];

        String hostname = "whois.internic.net";
        int port = 43;

        try (Socket socket = new Socket(hostname, port)) {

            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            writer.println(domainName);

            InputStream input = socket.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            System.out.println("----------------------[result.start]----------------------------------");
            String line;

            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            System.out.println("----------------------[result.end]----------------------------------");
        } catch (UnknownHostException ex) {

            System.out.println("Server not found: " + ex.getMessage());

        } catch (IOException ex) {

            System.out.println("I/O error: " + ex.getMessage());
        }
    }
}
