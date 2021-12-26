package com.chat.socket.Client;

import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class SendMessage implements Runnable {
    private BufferedWriter out;
    private Socket socket;

    public SendMessage(Socket s, BufferedWriter o) {
        this.socket = s;
        this.out = o;
    }

    public void run() {
        JSONObject jsonObject = new JSONObject();
        try {
            while (true) {
                BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
                System.out.println("Nhập tin nhắn: ");
                String data = stdIn.readLine();
                System.out.println("Input from client: " + data);
                jsonObject.clear();
                //jsonObject.put("message", data);
                out.write(data );
                out.newLine();
                out.flush();
                if (data.equals("bye"))
                    break;
            }
            System.out.println("Client closed connection");
            out.close();
            socket.close();
        } catch (IOException e) {
        }
    }
}
