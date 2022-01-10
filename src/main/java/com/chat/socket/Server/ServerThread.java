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
    private final Socket socket;
    private final DTO dataThread = new DTO();
    BufferedReader in;
    BufferedWriter out;

    public ServerThread(Socket s, String name) throws IOException {
        this.socket = s;
        this.dataThread.myName = name;
        this.dataThread.arrRefuse = new ArrayList<>();
        this.in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        this.out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
    }

    public void run() {
        JSONParser parser = new JSONParser();
        System.out.println("Client " + socket.toString() + " accepted");
        try {
            String input;
            while (true) {
                input = in.readLine();
                System.out.println(input);

                System.out.println("Tất cả thread: " + Server.workers.size());

                // Client buộc tắt cửa sổ nickname ("force quit")
                if (input == null) {
                    System.out.println("numThreadCurr: " + Server.workers.size());
                    Server.workers.remove(this);
                    System.out.println("Removed thread " + this.dataThread.myName + ", numThread: " + Server.workers.size());
                    break;
                }

                // Parser string về json
                // Convert json về DTO
                JSONObject jsonObject = (JSONObject) parser.parse(input);
                DTO data = convertJsonToDTO(jsonObject);

                // Client dừng kết nối
                if (data.status.equals("no connected")) {

                    // Client không chấp nhận tạo kết nối mới
                    if (data.myName == "") {
                        System.out.println("numThreadCurr: " + Server.workers.size());
                        Server.workers.remove(this);
                        System.out.println("Removed thread " + this.dataThread.myName + ", numThread: " + Server.workers.size());
                        break;
                    }

                    // Gửi thông tin 1 client ko kết nối về client kia
                    for (ServerThread worker : Server.workers) {
                        if (dataThread.clientName.equals(worker.dataThread.myName)) {
                            DTO data1 = new DTO();
                            data1.myNickname = worker.dataThread.myNickname;
                            data1.myName = worker.dataThread.myName;
                            data1.clientNickname = data.myNickname;
                            data1.clientName = "";
                            data1.status = "no connected";
                            data1.message = "";

                            sendClient(worker, dataThread, data1);
                            break;
                        }
                    }

                    // Hủy thread hiện tại
                    System.out.println("numThreadCurr: " + Server.workers.size());
                    Server.workers.remove(this);
                    System.out.println("Removed thread " + this.dataThread.myName + ", numThread: " + Server.workers.size());
                    break;
                }

                // Client mới kết nối đến server
                if (data.myNickname != "" && data.status == "") {
                    if (checkExistedNickname(data.myNickname)) {
                        data.status = "nickname existed";
                        jsonObject = convertDtoToJson(data.myNickname, "", "", "", data.status, "");

                        this.out.write(jsonObject.toJSONString());
                        this.out.newLine();
                        this.out.flush();

                        System.out.println("numThreadCurr: " + Server.workers.size());
                        Server.workers.remove(this);
                        System.out.println("Removed thread " + this.dataThread.myName + ", numThread: " + Server.workers.size());
                        break;
                    }

                    dataThread.clientNickname = "";
                    dataThread.clientName = "";
                    dataThread.myNickname = data.myNickname;

                    //  Chọn 1 client chưa kết nối đến client nào để gửi về client mới
                    for (ServerThread worker : Server.workers) {
                        if (!dataThread.myName.equals(worker.dataThread.myName) && worker.dataThread.clientNickname == "" && !dataThread.arrRefuse.contains(worker.dataThread.myName)) {

                            dataThread.clientNickname = worker.dataThread.myName;
                            dataThread.clientName = worker.dataThread.myName;

                            worker.dataThread.clientNickname = dataThread.myNickname;
                            worker.dataThread.clientName = dataThread.myName;

                            data.myName = dataThread.myName;
                            // Gửi về client hiện tại
                            sendClientCurr(worker, dataThread, data);
                            break;
                        }
                    }
                    continue;
                }

                // Client mới ok
                // Gửi đến client kia (chờ kết nối)
                if (Objects.equals(data.status, "ok")) {
                    for (ServerThread worker : Server.workers) {
                        if (data.clientName.equals(worker.dataThread.myName)) {
                            data.status = "client ok";
                            sendClient(worker, dataThread, data);
                            break;
                        }
                    }
                    continue;
                }

                // Client kia (chờ kết nối) chấp nhận
                // Gửi lại cho client mới
                // Hoàn tất kết nối
                if (Objects.equals(data.status, "client ok")) {
                    for (ServerThread worker : Server.workers) {
                        if (data.clientName.equals(worker.dataThread.myName)) {
                            data.status = "accepted";
                            sendClient(worker, dataThread, data);
                            break;
                        }
                    }
                    continue;
                }

                // Truyền data
                // Giữ status="accepted" để giữ kết nối giữa 2 client
                if (Objects.equals(data.status, "accepted")) {
                    for (ServerThread worker : Server.workers) {
                        if (data.clientName.equals(worker.dataThread.myName)) {
                            sendClient(worker, dataThread, data);
                            break;
                        }
                    }
                    continue;
                }

                // Client không chấp nhận
                // Chọn client khác chưa kết nối với client nào để gửi qua client không chấp nhận kết nối
                // Chọn client khác chưa kết nối với client nào để gửi qua client đã không được chấp nhận kết nối
                if (Objects.equals(data.status, "no accepted")) {
                    // Gửi client khác về cho client đã ok nhưng client kia không chấp nhận và thêm vào danh sách từ chối
                    for (ServerThread worker : Server.workers) {
                        if (dataThread.clientName.equals(worker.dataThread.myName)) {

                            worker.dataThread.clientNickname = "";
                            worker.dataThread.clientName = "";
                            worker.dataThread.arrRefuse.add(dataThread.myName);

                            dataThread.clientNickname = "";
                            dataThread.clientName = "";
                            dataThread.arrRefuse.add(worker.dataThread.myName);

                            for (ServerThread worker1 : Server.workers) {
                                if (!worker.dataThread.myName.equals(worker1.dataThread.myName) && worker1.dataThread.clientName.equals("") && !worker.dataThread.arrRefuse.contains(worker1.dataThread.myName)) {
                                    worker.dataThread.clientNickname = worker1.dataThread.myNickname;
                                    worker.dataThread.clientName = worker1.dataThread.myName;
                                    worker1.dataThread.clientNickname = worker.dataThread.myNickname;
                                    worker1.dataThread.clientName = worker.dataThread.myName;

                                    data.clientNickname = worker1.dataThread.myNickname;
                                    data.clientName = worker1.dataThread.myName;
                                    data.status = "";

                                    sendClient(worker, worker1.dataThread, data);
                                    break;
                                }
                            }
                            break;
                        }
                    }

                    // Gửi client khác về client đã không chấp nhận và thêm vào danh sách từ chối
                    for (ServerThread worker : Server.workers) {
                        if (dataThread.arrRefuse.contains(worker.dataThread.myName) || dataThread.myName.equals(worker.dataThread.myName) || worker.dataThread.clientName != "") {
                            continue;
                        }

                        dataThread.clientNickname = worker.dataThread.myNickname;
                        dataThread.clientName = worker.dataThread.myName;
                        worker.dataThread.clientNickname = dataThread.myNickname;
                        worker.dataThread.clientName = dataThread.myName;

                        data.clientNickname = worker.dataThread.myNickname;
                        data.clientName = worker.dataThread.myName;
                        data.status = "";

                        sendClientCurr(worker, dataThread, data);
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

    public JSONObject convertDtoToJson(String myNickname, String clientNickname, String myName, String clientName, String status, String message) {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("myNickname", myNickname);
        jsonObject.put("clientNickname", clientNickname);
        jsonObject.put("myName", myName);
        jsonObject.put("clientName", clientName);
        jsonObject.put("status", status);
        jsonObject.put("message", message);

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

        return data;
    }

    // Gửi về client kia
    public void sendClient(ServerThread worker, DTO dataThread, DTO data) throws IOException {
        JSONObject jsonObject = convertDtoToJson(worker.dataThread.myNickname, dataThread.myNickname, worker.dataThread.myName, dataThread.myName, data.status, data.message);

        worker.out.write(jsonObject.toJSONString());
        worker.out.newLine();
        worker.out.flush();
    }

    // Gửi về client hiện tại
    public void sendClientCurr(ServerThread worker, DTO dataThread, DTO data) throws IOException {
        JSONObject jsonObject = convertDtoToJson(dataThread.myNickname, worker.dataThread.myNickname, dataThread.myName, worker.dataThread.myName, data.status, data.message);

        this.out.write(jsonObject.toJSONString());
        this.out.newLine();
        this.out.flush();
    }

    // Kiểm tra nickname tồn tại
    public boolean checkExistedNickname(String nickname) {
        for (ServerThread worker : Server.workers) {
            if (nickname.equals(worker.dataThread.myNickname) && !nickname.equals(this.dataThread.myNickname)) {
                return true;
            }
        }
        return false;
    }
}
