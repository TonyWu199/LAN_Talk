package Lab;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;

public class ClientTcpSend {
    public static void main(String[] args) {
        File file = new File("E:\\send.mp4");
        tcp_send("127.0.0.1",33456, file);
    }

    //封装实现
    public static void tcp_send(String ip, int port, File file_sent) {
        int length = 0;
        byte[] sendBytes = null;
        Socket socket = null;
        DataOutputStream dos = null;
        FileInputStream fis = null;
        try {
            try {
                socket = new Socket(ip,port);
                dos = new DataOutputStream(socket.getOutputStream());
                fis = new FileInputStream(file_sent);
                sendBytes = new byte[1024 * 1024 * 10];
                while ((length = fis.read(sendBytes, 0, sendBytes.length)) > 0) {
                    dos.write(sendBytes, 0, length);
                    dos.flush();
                }
            } finally {
                if (dos != null)
                    dos.close();
                if (fis != null)
                    fis.close();
                if (socket != null)
                    socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}