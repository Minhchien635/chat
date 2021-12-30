package com.chat.socket.Client;

import lombok.Data;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Data
public class Client {
    String host;
    int port;
    Socket socket;
    BufferedWriter out;
    BufferedReader in;
    SendMessage send;
    ReceiveMessage rev;

    public Client(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        socket = new Socket(host, port);
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        send = new SendMessage(socket, out);
        rev = new ReceiveMessage(socket, in);
    }

    public void run() throws IOException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.execute(send);
        executor.execute(rev);
    }
}
