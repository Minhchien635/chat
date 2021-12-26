package com.chat.socket.Client;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    public static void main(String[] args) throws IOException {
        String host = "localhost";
        int port = 1234;
        Socket socket = new Socket(host, port);
        System.out.println("Client connected");
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        ExecutorService executor = Executors.newFixedThreadPool(2);

        SendMessage send = new SendMessage(socket, out);
        ReceiveMessage rev = new ReceiveMessage(socket, in);

        executor.execute(send);
        executor.execute(rev);
    }
}
