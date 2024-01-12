/*
        java-change-log-level-cli
        Copyright (C) 2022 Jari Kuusisto Mäkelä

        This library is free software; you can redistribute it and/or
        modify it under the terms of the GNU Lesser General Public
        License as published by the Free Software Foundation; either
        version 2.1 of the License, or (at your option) any later version.

        This library is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
        Lesser General Public License for more details.

        You should have received a copy of the GNU Lesser General Public
        License along with this library; if not, write to the Free Software
        Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.horcruxid.main;

import org.jvnet.libpam.PAMException;
import org.jvnet.libpam.UnixUser;
import org.newsclub.net.unix.AFUNIXServerSocket;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

import java.io.*;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketServer {

    private static Logger log = Logger.getLogger(SocketServer.class.getName());
    private static final boolean DEBUG = false;
    private ExecutorService clientProcessingPool = Executors.newFixedThreadPool(1);
    private String allowedUserUid = "";
    private static AFUNIXServerSocket server;
    private static Thread serverThread;
    private static SocketServer singleton = null;
    private final Object lock = new Object();
    private final AtomicBoolean notInterrupted = new AtomicBoolean(true);

    public static SocketServer socketServer() {

        if (singleton == null) {
            singleton = new SocketServer();
        }

        return singleton;
    }

    private void configureAllowedUser(String allowedUserName) {
        try {
            UnixUser current = new UnixUser(allowedUserName);
            int unixUserUID = current.getUID();
            this.allowedUserUid = String.valueOf(unixUserUID);
        } catch (PAMException e) {
            this.allowedUserUid = "";
        }
    }

    private Optional<File> generateSocketFile() {
        final String filename = "change-log.sock";
        File socketFile = new File(new File(System.getProperty("java.io.tmpdir")), filename);
        socketFile.deleteOnExit();

        return Optional.ofNullable(socketFile);
    }

    public void start(String allowedUserName) {
        synchronized (lock) {
            if (DEBUG) System.out.println("Started... ");
            try {
                File socketFile = this.generateSocketFile().get();
                configureAllowedUser(allowedUserName);
                server = AFUNIXServerSocket.newInstance();
                server.setReuseAddress(false);
                server.setDeleteOnClose(true);
                server.setSoTimeout(15*1000);
                server.bind(AFUNIXSocketAddress.of(socketFile));

                Runnable serverTask = () -> {
                    while (notInterrupted.get()) {
                        if (DEBUG) System.out.println("Waiting for connection... ");
                        try {
                            Socket sock = server.accept();
                            if (DEBUG) System.out.println("Accepted a client connection...");
                            clientProcessingPool.submit(new ClientTask(sock, this.allowedUserUid));

                        } catch (IOException e) {
                            // Timeout works as circuit breaker for the server thread
                        } finally {
                            if (DEBUG) System.out.println("running...");
                        }
                    }
                };

                serverThread = new Thread(serverTask);
                serverThread.start();
            } catch (NullPointerException | IOException e) {
                log.log(Level.SEVERE, e.getMessage());
            }
        }
    }

    public void stop() {
        synchronized (lock) {
            try {
                notInterrupted.set(false);
                clientProcessingPool.shutdownNow();
                serverThread.join();
            } catch (NullPointerException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (DEBUG) System.out.println("Stopped... ");
            }
        }
    }

    private class ClientTask implements Runnable {
        private final Socket clientSocket;
        private final String allowedUserId;

        private ClientTask(Socket clientSocket, String allowedUserId) {
            this.clientSocket = clientSocket;
            this.allowedUserId = allowedUserId;
        }

        @Override
        public void run() {
            if (DEBUG) System.out.println("Got a client !");
            // begin process the client's request
            try {
                // in terminal equivalent is "id -u"
                final String connectingUserId = String.valueOf(((AFUNIXSocket) clientSocket).getPeerCredentials().getUid());
                if (DEBUG) System.out.println("Connected: user uuid=" + connectingUserId);

                if ((!this.allowedUserId.isEmpty()) && (connectingUserId.equals(this.allowedUserId))) {
                    try (InputStream is = clientSocket.getInputStream();
                         OutputStream os = clientSocket.getOutputStream()) {
                        new Application().interactiveShell(is, os);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            // end

            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
