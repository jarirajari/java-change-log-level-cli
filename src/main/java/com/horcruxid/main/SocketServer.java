package com.horcruxid.main;

import org.newsclub.net.unix.AFUNIXServerSocket;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketServer {

    private static boolean DEBUG = false;
    private ExecutorService clientProcessingPool = Executors.newFixedThreadPool(10);
    private String allowedUserUid = "";
    private static AFUNIXServerSocket server;
    private static Thread serverThread;

    public void start(String allowedUserUid) {
        final File socketFile = new File(new File(System.getProperty("java.io.tmpdir")), "change-log.sock");
        this.allowedUserUid = allowedUserUid;
        Runnable serverTask = () -> {
            try {
                server = AFUNIXServerSocket.newInstance();
                server.setReuseAddress(false);
                server.bind(AFUNIXSocketAddress.of(socketFile));
                while (true) {
                    if (DEBUG) System.out.println("Waiting for connection...");
                    try {
                        Socket sock = server.accept();
                        clientProcessingPool.submit(new ClientTask(sock));

                    } catch (IOException e) {
                        if (server.isClosed()) {
                            e.printStackTrace();
                        } else {
                            e.printStackTrace();
                        }
                    } finally {
                        if (DEBUG) System.out.println("exited...");
                    }


                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        serverThread = new Thread(serverTask);
        serverThread.start();

    }

    public void stop() {
        try {
            clientProcessingPool.shutdownNow();
            serverThread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ClientTask implements Runnable {
        private final Socket clientSocket;

        private ClientTask(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            if (DEBUG) System.out.println("Got a client !");
            // begin process the client's request
            try {
                // in terminal equivalent is "id -u"
                final String credsUid = String.valueOf(((AFUNIXSocket) clientSocket).getPeerCredentials().getUid());
                if (DEBUG) System.out.println("Connected: user uuid=" + credsUid);

                try (InputStream is = clientSocket.getInputStream();
                     OutputStream os = clientSocket.getOutputStream()) {
                    // TODO: new Application().interactiveShell(is, os);

                    {
                        if (DEBUG) System.out.println("Saying hello to client...");
                        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
                        if ((allowedUserUid.isEmpty()) || (!allowedUserUid.equals(credsUid))) {
                            if (DEBUG) System.out.println("Only one user is allowed to connect: " + allowedUserUid);
                            writer.write("Connection refused! \n");
                            writer.flush();
                            this.clientSocket.close();
                        }
                        writer.write("Let's play a game. Give a number! \n");
                        writer.flush();
                        String number = reader.readLine();
                        int num = Integer.parseInt(number);
                        int won = num + 1;
                        writer.write(String.format("I have number %d. I won!\n", won));
                        writer.flush();

                        writer.close();
                        reader.close();
                    }
                }
            } catch (IOException e) {
                if (DEBUG) System.out.println("main loop");
            }
            // end

            try {
                clientSocket.close();
            } catch (IOException e) {
                if (DEBUG) System.out.println("client loop");
                e.printStackTrace();
            }
        }
    }
}
