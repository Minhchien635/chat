package com.chat.socket.Server;

import com.chat.socket.DTO;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;

public class ServerThread implements Runnable {
    BufferedReader in;
    BufferedWriter out;
    private final Socket socket;
    private final DTO dataThread = new DTO();

    public ServerThread(Socket s, String name) throws IOException {
        this.socket = s;
        this.dataThread.myName = name;
        this.in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        this.out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
    }

    public void run() {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject;
        System.out.println("Client " + socket.toString() + " accepted");
        try {
            String input;
            while (true) {
                input = in.readLine();

                if (input.isEmpty()) {
                    // 1 client dừng kết nối dừng kết nối
                    // Thông báo đến client kia
                    // Trả client kia vào danh sách đợi kết nối
                    for (ServerThread worker : Server.workers) {
                        if (dataThread.clientName.equals(worker.dataThread.myName)) {

                            DTO data = new DTO();
                            data.clientName = "";
                            data.myName = dataThread.clientName;
                            data.clientNickname = "";
                            data.myNickname = dataThread.clientNickname;
                            data.status = "no connected";
                            data.message = "";
                            data.arrRefuse = new ArrayList<>();
                            JSONObject jo = null;
                            sendClient(jo, worker, dataThread, data);

                            break;
                        }
                    }
                    in.close();
                    out.close();
                    socket.close();
                    Server.workers.remove(this);
                    System.out.println("ServerThead removed");
                    break;
                }

                // Parser string về json
                jsonObject = (JSONObject) parser.parse(input);

                // Lấy thông tin trong json bỏ vô DTO
                DTO data = convertJsonToDTO(jsonObject);

                // Client mới kết nối đến server
                if (data.myNickname != "" && data.myName == ""
                        && data.clientNickname == "" && data.clientName == ""
                        && data.message == "" && data.status == "") {

                    dataThread.clientNickname = null;

                    dataThread.clientName = null;

                    dataThread.myNickname = data.myNickname;

                    ServerThread threadMaxRefuse = null;
                    for (ServerThread worker : Server.workers) {
                        int size = worker.dataThread.arrRefuse.size();
                        if (size == Server.workers.size()) {
                            threadMaxRefuse = worker;
                            break;
                        }
                    }

                    // Ưu tiên chọn client đã từ chối tất cả các client khác có sẵn để gửi về client vừa kết nối đến server
                    if (threadMaxRefuse != null) {
                        dataThread.clientNickname = threadMaxRefuse.dataThread.myName;

                        threadMaxRefuse.dataThread.clientNickname = dataThread.myNickname;

                        data.myName = dataThread.myName;

                        sendClientCurr(jsonObject, threadMaxRefuse, dataThread, data);

                        continue;
                    }

                    //  Chọn 1 client chưa kết nối đến client nào để gửi về client mới
                    for (ServerThread worker : Server.workers) {
                        if (!dataThread.myName.equals(worker.dataThread.myName) &&
                                worker.dataThread.clientNickname == null &&
                                !dataThread.arrRefuse.contains(worker.dataThread.myName)) {

                            dataThread.clientNickname = worker.dataThread.myName;

                            worker.dataThread.clientNickname = dataThread.myNickname;

                            data.myName = dataThread.myName;

                            // Gửi về client hiện tại
                            sendClientCurr(jsonObject, worker, dataThread, data);

                            break;
                        }
                    }
                    continue;
                }

                // Client mới ok
                // Gửi đến client kia
                if (data.myNickname != "" && data.myName != ""
                        && data.clientNickname != "" && data.clientName != ""
                        && data.message == "" && Objects.equals(data.status, "ok")) {
                    for (ServerThread worker : Server.workers) {
                        if (data.clientName.equals(worker.dataThread.myName)) {
                            data.status = "client ok";

                            sendClient(jsonObject, worker, dataThread, data);

                            break;
                        }
                    }
                    continue;
                }

                // Client kia chấp nhận
                // Gửi lại cho client mới
                if (data.myNickname != "" && data.myName != ""
                        && data.clientNickname != "" && data.clientName != ""
                        && data.message == "" && Objects.equals(data.status, "client ok")) {
                    for (ServerThread worker : Server.workers) {
                        if (data.clientName.equals(worker.dataThread.myName)) {
                            data.status = "accepted";

                            sendClient(jsonObject, worker, dataThread, data);

                            break;
                        }
                    }
                    continue;
                }

                // Hoàn tất kết nối và gửi message
                // Giữ status="accepted" để giữ kết nối giữa 2 client
                if (data.myNickname != "" && data.myName != ""
                        && data.clientNickname != "" && data.clientName != ""
                        && data.message != "" && Objects.equals(data.status, "accepted")) {
                    for (ServerThread worker : Server.workers) {
                        if (data.clientName.equals(worker.dataThread.myName)) {

                            sendClient(jsonObject, worker, dataThread, data);

                            break;
                        }
                    }
                    continue;
                }

                // Client không chấp nhận
                // Chọn client khác chưa kết nối với client nào để gửi qua client không chấp nhận kết nối
                if (data.myNickname != "" && data.myName != ""
                        && data.clientNickname != "" && data.clientName != ""
                        && data.message == "" && Objects.equals(data.status, "no accepted")) {

                    // Gửi client khác về cho client đã ok nhưng client kia không chấp nhận và thêm vào danh sách từ chối
                    for (ServerThread worker : Server.workers) {
                        if (data.clientName.equals(worker.dataThread.myName)) {
                            worker.dataThread.clientNickname = null;
                            dataThread.clientNickname = null;
                            dataThread.arrRefuse.add(data.clientName);
                            worker.dataThread.arrRefuse.add(dataThread.myName);
                            for (ServerThread worker1 : Server.workers) {
                                if (!worker.dataThread.myName.equals(worker1.dataThread.myName) &&
                                        worker1.dataThread.clientNickname == null &&
                                        !worker.dataThread.arrRefuse.contains(worker1.dataThread.myName)) {

                                    worker.dataThread.clientNickname = worker1.dataThread.myName;

                                    worker1.dataThread.clientNickname = worker.dataThread.myNickname;

                                    sendClient(jsonObject, worker, dataThread, data);

                                    break;
                                }
                            }
                            break;
                        }
                    }

                    // Gửi client khác về client đã không chấp nhận và thêm vào danh sách từ chối
                    for (ServerThread worker : Server.workers) {
                        if (dataThread.arrRefuse.contains(worker.dataThread.myName) ||
                                dataThread.myName.equals(worker.dataThread.myName) ||
                                worker.dataThread.clientNickname != null) {
                            continue;
                        }
                        dataThread.clientNickname = worker.dataThread.myNickname;

                        worker.dataThread.clientNickname = dataThread.myNickname;

                        sendClientCurr(jsonObject, worker, dataThread, data);

                        break;
                    }
                }
            }
            in.close();
            out.close();
            socket.close();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public JSONObject convertStringToJson(String myNickname, String clientNickname, String myName, String clientName,
                                          String status, String message, ArrayList<String> arrRefuse) {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("myNickname", myNickname);
        jsonObject.put("clientNickname", clientNickname);
        jsonObject.put("myName", myName);
        jsonObject.put("clientName", clientName);
        jsonObject.put("status", status);
        jsonObject.put("message", message);
        jsonObject.put("arrRefuse", arrRefuse);

        return jsonObject;
    }

    public DTO convertJsonToDTO(JSONObject jsonObject) {
        DTO data = new DTO();

        data.myNickname = jsonObject.get("myNickname").toString();
        data.clientNickname = jsonObject.get("clientNickname").toString();
        data.myName = jsonObject.get("myName").toString();
        data.clientName = jsonObject.get("clientName").toString();
        data.status = jsonObject.get("status").toString();
        data.message = jsonObject.get("message").toString();
        data.arrRefuse = new ArrayList<>();

        return data;
    }

    // Gửi về client kia
    public void sendClient(JSONObject jsonObject, ServerThread worker, DTO dataThread, DTO data) throws IOException {
        jsonObject.clear();
        jsonObject = convertStringToJson(worker.dataThread.myNickname, dataThread.myNickname,
                worker.dataThread.myName, dataThread.myName, data.status, data.message, dataThread.arrRefuse);

        worker.out.write(jsonObject.toJSONString());
        worker.out.newLine();
        worker.out.flush();
    }

    // Gửi về client hiện tại
    public void sendClientCurr(JSONObject jsonObject, ServerThread worker, DTO dataThread, DTO data) throws IOException {
        jsonObject.clear();
        jsonObject = convertStringToJson(dataThread.myNickname, worker.dataThread.myNickname,
                dataThread.myName, worker.dataThread.myName, data.status, data.message, dataThread.arrRefuse);

        this.out.write(jsonObject.toJSONString());
        this.out.newLine();
        this.out.flush();
    }

    /*public static void main(String[] agrs) {
        String a = "[dfds,dfsdf,34234,5645]";
        System.out.println(convertStringToArr(a));
    }*/

    /*public ArrayList<String> convertStringToArr(String str) {
        int index = str.length();
        String[] arrStr = str.substring(1, index - 2).split(",");
        return new ArrayList<>(List.of(arrStr));
    }*/
}
