package com.chat.socket.Client;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;

public class ReceiveMessage implements Runnable {
    private final BufferedReader in;
    private final Socket socket;

    public ReceiveMessage(Socket s, BufferedReader i) {
        this.socket = s;
        this.in = i;
    }

    public JSONObject receive() throws IOException {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject;
        try {
            if (socket.isClosed()) {
                return null;
            }
            String data = in.readLine();
            jsonObject = (JSONObject) parser.parse(data);
            System.out.println("Receive: " + data);

            return jsonObject;
        } catch (ParseException ex) {
            return null;
        }
    }

    public void close() throws IOException {
        System.out.println("Client closed receive");
        in.close();
        socket.close();
    }

    @Override
    public void run() {

    }
}
