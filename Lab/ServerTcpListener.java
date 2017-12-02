package Lab;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static Lab.Client_chat_ui.Decry_file;

public class ServerTcpListener implements Runnable {
//    public static void main(String[] args) {
//        ServerSocket server = null;
//        try {
//            server = new ServerSocket(33456);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        tcp_receive(server,file);
//    }

    public static void tcp_receive(ServerSocket file_serversocket, File file) {

        try {
            Thread th = new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        try {
                            System.out.println("开始监听...");
                            Socket socket = file_serversocket.accept();
                            System.out.println("有链接");
                            receiveFile(socket,file);
                        } catch (Exception e) {
                        }
                    }
                }

            });

            th.run(); //启动线程运行
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
    }

    public static void receiveFile(Socket socket, File receive_file) {

        byte[] inputByte = null;
        int length = 0;
        DataInputStream dis = null;
        FileOutputStream fos = null;

        File file = new File("E:\\receive_temp.mp4");
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            try {
                dis = new DataInputStream(socket.getInputStream());
                fos = new FileOutputStream(file);
                inputByte = new byte[1024];
                System.out.println("开始接收数据...");
                long startMili = System.currentTimeMillis();
                while ((length = dis.read(inputByte, 0, inputByte.length)) > 0) {
                    fos.write(inputByte, 0, length);
                    fos.flush();
                }
                long endMili = System.currentTimeMillis();
                System.out.println("耗时：" + (endMili - startMili) + "毫秒");
                System.out.println("完成接收,准备解密");
                Decry_file(file,receive_file);
                fos.close();
                file.delete();
            } finally {
                if (fos != null)
                    fos.close();
                if (dis != null)
                    dis.close();
                if (socket != null)
                    socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
