package com.chat.socket.Client;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;

public class ReceiveMessage implements Runnable {
    private BufferedReader in;
    private Socket socket;

    public ReceiveMessage(Socket s, BufferedReader i) {
        this.socket = s;
        this.in = i;
    }

    public void run() {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject;
        String clientName;
        String message;

        try {
            while (true) {
                String data = in.readLine();
                if( data == null){
                    break;
                }
                jsonObject = (JSONObject) parser.parse(data);
                clientName = (String) jsonObject.get("clientName");
                message = (String) jsonObject.get("message");

                System.out.println("Receive: " + data);
            }
            in.close();
            socket.close();
        } catch (IOException | ParseException e) {
            System.out.println(e);
        }
    }
}
