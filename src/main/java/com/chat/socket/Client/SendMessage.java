package com.chat.socket.Client;

import org.json.simple.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;

public class SendMessage implements Runnable {
    private BufferedWriter out;
    private Socket socket;

    public SendMessage(Socket s, BufferedWriter o) {
        this.socket = s;
        this.out = o;
    }

    public void sendData(JSONObject data) {
        try {
            out.write(data.toJSONString());
            out.newLine();
            out.flush();
        } catch (IOException e) {
        }
    }

    public void close() throws IOException {
        System.out.println("Client closed send");
        out.close();
        socket.close();
    }

    @Override
    public void run() {

    }
}
