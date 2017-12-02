package Lab;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Client_chat_ui{
    private static final String IP_ADDR = "10.11.62.13";
    private static final int PORT = 6066;
    private static String username;
    private static String input_words = "";
    private static String last_target = "";
    private static String target = "";
    private static Socket socket;
    private static ServerSocket file_serverSocket;
    private static File file_choosed;
    private static Integer offline_talk;
    private static ServerSocket serverSocket;
    private static BufferedReader input;
    private static BufferedWriter out;
    private static BufferedReader p2p_input;
    private static BufferedWriter p2p_out;
    private static final HashMap<String,String> online_user_ip_Map = new HashMap<>(); //在线用户列表
    private static final List<String> offline_user_list = new ArrayList<>();
    private static Font a = new Font("宋体",1,16);  //消息区字体显示
    private static Font b = new Font("宋体",0,15);  //用户列表字体
    private static Font c = new Font("宋体",1,15);  //输入框字体

    private static JFrame frame;           //界面框架
    private static JTextArea textArea;     //右侧对话内容区域
    private static JList userList;    //用户列表刷新区
    private static DefaultListModel<String> dlm;   //用户列表刷新内容保存
    private static JTextArea input_textArea;   //输入文本
    private static JTextField textHostIp;    //服务器端口
    private static JTextField textClientIp;  //服务器IP
    private static JTextField textName;    //当前用户名
    private static JButton user_list_button;
    private static JButton text_send_button;   //信息发送按钮
    private static JButton file_send_button;   //文件发送按钮
    private static JPanel northPanel;      //上方面板
    private static JPanel southPanel;      //下方面板
    private static JScrollPane rightScroll;   //右侧滑动对话框
    private static JScrollPane leftScroll;    //左侧滑动对话框
    private static JSplitPane centerSplit;    //左右对话框分割


    public static void main(String[] args) {
        userPanel();   //test
    }

    public static void user_UI(String current_username){
        username = current_username;
        userPanel();   //test
        //p2p_Server();

        words_receiver t1 = new words_receiver();
        file_receiver t2 = new file_receiver();
        t1.start();
        t2.start();
    }

    private static class words_receiver extends Thread{
        public void run(){
            System.out.println("文本接收服务器开启");
            p2p_Server();
        }
    }

    private static class file_receiver extends Thread{
        public void run(){
            System.out.println("文件接收服务器开启");
            new ServerTcpListener();

            //接收到的文件存放位置
            File file = new File("E:\\receive.mp4");
            if(!file.exists())
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            ServerTcpListener.tcp_receive(file_serverSocket, file);
        }
    }

    private static void userPanel(){
        //建立socket
        try {
            socket = new Socket(IP_ADDR, PORT);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        tell_ip(username);

        //消息区显示设置
        textArea = new JTextArea();
        textArea.setFont(a);
        textArea.setEditable(false);
        textArea.setForeground(Color.black);
        textArea.setLineWrap(true);
        textArea.setCaretPosition(textArea.getDocument().getLength()); //跟踪最低端

        //用户列表
        dlm = new DefaultListModel<>();
        userList = new JList<String>();
        userList.setFont(b);
        userList.setForeground(Color.black);

        //上侧的连接信息
        user_list_button = new JButton("刷新用户");
        textHostIp = new JTextField("127.0.0.1:6066");
        textClientIp= new JTextField(socket.getLocalSocketAddress().toString().substring(1));
        textName = new JTextField(username );
        textHostIp.setEditable(false);
        textClientIp.setEditable(false);
        textName.setEditable(false);
        northPanel = new JPanel();
        northPanel.setLayout(new GridLayout(1, 6));    //第一行采用格式布局
        northPanel.add(user_list_button);
        northPanel.add(new JLabel("               服务器IP "));
        northPanel.add(textHostIp);
        northPanel.add(new JLabel("               客户端IP "));
        northPanel.add(textClientIp);
        northPanel.add(new JLabel("                当前用户"));
        northPanel.add(textName);
        northPanel.setBorder(new TitledBorder("连接信息"));

        //中部左侧在线用户区和右侧的聊天区
        rightScroll = new JScrollPane(textArea);
        rightScroll.setBorder(new TitledBorder(">>>>>>>>>>>>>>>>>>>>>>>>> Welcome to Zz ChatRoom v1.0 <<<<<<<<<<<<<<<<<<<<<<<<<"));
        leftScroll = new JScrollPane(userList);
        leftScroll.setBorder(new TitledBorder(">>>>>> 用 户 列 表 <<<<<<"));

        //下侧的用户输入信息框
        southPanel = new JPanel(new BorderLayout(5,5));    //下方布局管理设置为边界布局
        text_send_button = new JButton("发送");
        file_send_button = new JButton(" >>>> 传 输 文 件 <<<< ");
        input_textArea = new JTextArea();
        input_textArea.setLineWrap(true);
        input_textArea.setFont(c);
        JScrollPane input_textArea_scroll = new JScrollPane(input_textArea);
        southPanel.add(input_textArea_scroll, "Center");
        southPanel.add(text_send_button, "East");
        southPanel.add(file_send_button, "West");
        southPanel.setBorder(new TitledBorder("发送消息                                 Alt+Entet换行  '$'为非法字符"));
        centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, rightScroll);
        centerSplit.setDividerLocation(170);
        centerSplit.setBackground(Color.cyan);

        //整体框架
        frame = new JFrame("Zz-ChatRoom v1.0");
        frame.setLayout(new BorderLayout());
        frame.setSize(750, 600);
        frame.add(northPanel, "North");
        frame.add(centerSplit, "Center");
        frame.add(southPanel, "South");
        //frame.setResizable(false);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        //事件监听
        function_listener();
    }

    //向服务器发送客户端关闭信息
    private static void sendinfo_close(String username){
        try {
            System.out.println(socket.getLocalSocketAddress());
            out.write("close" + "|" + username);
            out.newLine();
            out.flush();
            String ServerStr = input.readLine();
            System.out.println(ServerStr);

            //关闭通道
            out.close();
            input.close();
        } catch (Exception e) {
            System.out.println("客户端异常：" + e.getMessage());
            e.printStackTrace();
        }
    }

    //向服务器发送已登录信息，并告知服务器ip地址
    private static void tell_ip(String username){
        try{
            new get_ip();
            String local_ip = get_ip.get_juyuwang_ip();
            //向服务器登记一个空闲端口
            serverSocket = new ServerSocket(9001);
            file_serverSocket = new ServerSocket(9002);
            out.write("online" + "|" + username + "|" + local_ip  + ":" + String.valueOf(serverSocket.getLocalPort()) + "|" + local_ip + ":" + String.valueOf(file_serverSocket.getLocalPort()));
            out.newLine();
            out.flush();
            String ServerStr = input.readLine();
            System.out.println(ServerStr);
        } catch (Exception e) {
            System.out.println("客户端异常：" + e.getMessage());
            e.printStackTrace();
        }
    }

    //拉取用户列表
    private static void get_user_list(JList<String> userlist, DefaultListModel dlm){
        try{
            out.write("get_online_user");
            out.newLine();
            out.flush();
            dlm.removeAllElements();   //清空用户列表，等待下次读入
            String ServerStr = input.readLine();
            System.out.println(ServerStr);
            for (String s: ServerStr.split(" ")){  //根据空格切割,仅显示用户名
                if(s.contains(":")) {
                    dlm.addElement(s.split(":")[0]);
                } else {
                    dlm.addElement(s);
                }
            }
            userlist.setModel(dlm);


            //由服务器获取的数据构建在线用户，离线用户列表。
            String[] temp_list = ServerStr.split("----");   //1,3存放数据

            online_user_ip_Map.clear();
            offline_user_list.clear();

            for(String tmp : temp_list[2].split(" ")) {
                if(tmp.equals(""))
                    continue;
                online_user_ip_Map.put(tmp.split(":")[0], tmp.split(":")[1] + ":" + tmp.split(":")[2] + ":" + tmp.split(":")[3] + ":" + tmp.split(":")[4]);
            }

            for(String tmp : temp_list[4].split(" ")){
                if(tmp.equals(""))
                    continue;
                offline_user_list.add(tmp);
            }
        }catch (NullPointerException e){
            System.out.println("客户端异常：" + e.getMessage() + "可能由于版本问题造成，可忽略");
            //e.printStackTrace();
        }catch (Exception e){
            System.out.println("客户端异常" + e.getMessage() + "可能由于版本问题造成，可忽略");
            //e.printStackTrace();
        }
    }

    //获取聊天记录
    private static void get_recode(String get_recode_info, JTextArea textArea){
        try {
            out.write(get_recode_info);
            out.newLine();
            out.flush();
            String ServerStr = input.readLine();
            System.out.println("接收到聊天记录 ：" + ServerStr);
            if(!ServerStr.equals("无聊天内容")) {
                for (String tmp : ServerStr.split("\\$")) {
                    if (!tmp.equals(""))
                        textArea.append(tmp + "\n");
                }
            }
        }catch (Exception e){
            System.out.println("客户端异常： " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void save_recode(String save_recode_info,String text){
        try {
            String one_line = "";
            System.out.println("textArea:" + text);
            for(String tmp : text.split("\n"))
                if(!(tmp == null))
                    one_line += (tmp + "$");     //用~作为回车的标记
            out.write(save_recode_info + "|" + one_line);
            out.newLine();
            out.flush();
            String ServerStr = input.readLine();
            System.out.println(ServerStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //可以接收多个用户发来的信息
    private static void p2p_Server(){
        try {

            while(true) {
                Socket p2p_client = serverSocket.accept();
                System.out.println(p2p_client.getRemoteSocketAddress());
                p2p_input = new BufferedReader(new InputStreamReader(p2p_client.getInputStream()));
                p2p_out = new BufferedWriter(new OutputStreamWriter(p2p_client.getOutputStream()));
                new HandlerThread(p2p_client);
            }
        } catch  (IOException e) {
            e.printStackTrace();
        }
    }

    //服务器多线程处理
    private static class HandlerThread implements Runnable{
        private Socket socket;

        public HandlerThread(Socket client){
            socket = client;
            new Thread(this).start();
        }

        @Override
        public void run() {
            input_listener();
            System.out.println("已完成p2p连接");
            Thread msg_receiver = new MSG_receiver(p2p_input);
            msg_receiver.start();

        }
    }

    private static void p2p_client(String target_ip){
        try {
            //System.out.println(target_ip);
            //从服务器的登记列表中获取需要通信用户ip信息
            System.out.println(target_ip);
            Socket p2p_socket = new Socket(target_ip.split(":")[0], Integer.parseInt(target_ip.split(":")[1]));    //与服务器文本信息接收端口建立socket连接
            textClientIp.setText(p2p_socket.getLocalSocketAddress().toString());
            p2p_input = new BufferedReader(new InputStreamReader(p2p_socket.getInputStream()));
            p2p_out = new BufferedWriter(new OutputStreamWriter(p2p_socket.getOutputStream()));

            System.out.println("申请p2p通信");
            input_listener();

            //多线程收
            Thread msg_receiver = new MSG_receiver(p2p_input);
            msg_receiver.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //多线程接收类
    public static class MSG_receiver extends Thread{
        private BufferedReader in;

        MSG_receiver(BufferedReader in) {
            this.in = in;
        }

        public void run() {
            StringBuilder msg = new StringBuilder();
            String tmp_str;
            try {
                System.out.println("Recevier");
                new DESCryptography();    //DES解密
                while (true) {
                    msg.delete(0,msg.length());
                    while(!(tmp_str = in.readLine()).equals("")) {
                        textArea.append(DESCryptography.DES_CBC_Decrypt(DESCryptography.hexStr2Bytes(tmp_str)));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //输入监听
    private static void input_listener(){
        /*-----发送消息区事件 start
        */
        //鼠标点击发送文本信息
        text_send_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SimpleDateFormat format_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String time = format_time.format(new Date()) ;
                input_words = input_textArea.getText();
                input_textArea.setText("");
                if(!input_words.equals("")) {
                    //本地更新消息，传送消息
                    try {
                        //本地更新
                        textArea.append("~" + time + " ※[" + username + "]※\n" + input_words + '\n');
                        input_words = "~" + time + " ※[" + username + "]※\n" + input_words + "\n";
                        System.out.println(input_words);
                        new DESCryptography();    //DES加密
                        if(offline_talk == 0) {   //在线聊天
                            byte[] write_byte = DESCryptography.DES_CBC_Encrypt(input_words);
                            p2p_out.write(DESCryptography.byteToHexString(write_byte));
                            p2p_out.newLine();
                            p2p_out.flush();
                        }
                        offline_talk = 0;
                        //输入栏置空
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                else
                    textArea.append("");
            }
        });
        /*-----发送消息事件 end
        */
    }


    //用户界面基本功能监听
    private static void function_listener(){

        //用户列表选择事件 单击触发
        userList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!userList.getValueIsAdjusting()){

                    target = userList.getSelectedValue().toString();   //选中聊天目标
                    offline_talk = 0;    //非离线聊天

                    if (!target.equals(username)) {   //不能选中自己通信

                        //存储调取聊天记录
                        if((!target.equals(last_target)) && (!last_target.equals(""))) {                  //更换聊天目标
                            if(!textArea.getText().equals(""))   //文本框不为空
                                 save_recode("save_recode" + "|" + username + "|" + last_target, textArea.getText());    //存储上一个窗口的聊天记录
                            textArea.setText("");
                            get_recode("get_recode" + "|" + username + "|" + target, textArea);          //获取现在窗口的聊天记录
                        }
                        else if(last_target.equals("")){
                            textArea.setText("");
                            get_recode("get_recode" + "|" + username + "|" + target, textArea);
                        }

                        //在线，离线聊天
                        if(online_user_ip_Map.keySet().contains(target)) {   //选中用户在线
                            p2p_client(online_user_ip_Map.get(target));
                        }
                        else{
                            System.out.println("离线聊天");
                            offline_talk = 1;
                            input_listener();
                        }

                        last_target = target;     //记录聊天对象
                    }

                    else{
                        System.out.println("不能选择自己");
                    }
                }
            }
        });

        //关闭窗口事件
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("close");
                save_recode("save_recode" + "|" + username + "|" + target, textArea.getText());
                sendinfo_close(username);
                super.windowClosing(e);
            }
        });

        //刷新用户列表
        user_list_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                get_user_list(userList, dlm);
            }
        });


        /*输入区便捷设置
         start*/

        //取消输入区中Enter键换行,按下Enter即发送信息
        input_textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && !e.isAltDown()) {
                    text_send_button.requestFocus();
                    text_send_button.doClick();
                    e.consume(); //加一句这个就行
                    input_textArea.requestFocus();
                }
            }
        });

        //输入区采用 Alt + Enter 的方式换行
        input_textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER && e.isAltDown()){
                    input_textArea.append("\n");
                }
            }
        });
        /*输入区便捷设置
         end*/

        file_send_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                file_transform_panel();
            }
        });
    }

    //以下是文件传输
    private static void file_transform_panel(){

        //选择传输用户
        online_user_ip_Map.remove(username);
        Object[] possibleValues = online_user_ip_Map.keySet().toArray();
        Object selectedValue = JOptionPane.showInputDialog(null, "Choose one", "Input",
                JOptionPane.INFORMATION_MESSAGE, null,
                possibleValues, possibleValues[0]);
        System.out.println(selectedValue);

        //选择传输的文件
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        Integer returnVal = fc.showOpenDialog(null);
        file_choosed = fc.getSelectedFile();

        File temp = new File("E:\\encrytemp.mp4");
        if(!temp.exists())
            try {
                temp.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

        try {
            Encry_file(file_choosed,temp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        new ClientTcpSend();
        ClientTcpSend.tcp_send(online_user_ip_Map.get(selectedValue).split(":")[2],9002,temp);
        temp.delete();
    }

    private static void Encry_file(File plaintext, File encryfile) throws Exception{
        System.out.println("开始加密");
        InputStream in  = new FileInputStream(plaintext);
        OutputStream out = new FileOutputStream(encryfile);

        int index;
        byte[] buff_temp = new byte[1024*1024];
        byte[] buff = new byte[1024 * 1024];

        new DESCryptography();
        while (( index = in.read(buff_temp)) != -1) {
            if(index == 1024)
                buff = DESCryptography.DES_CBC_Encrypt(buff_temp);
            else
                buff = buff_temp;
            out.write(buff, 0 ,index);
            out.flush();
        }
        System.out.println("加密完成");

        in.close();
        out.flush();
        out.close();
    }

    public static void Decry_file(File encryfile, File plaintext) throws Exception{
        System.out.println("开始解密");
        InputStream in = new FileInputStream(encryfile);
        OutputStream out = new FileOutputStream(plaintext);

        int index;
        byte[] buff_temp = new byte[1024*1024];
        byte[] buff = new byte[1024 * 1024];

        new DESCryptography();
        while (( index = in.read(buff_temp)) != -1) {
            if(index == 1024)
                buff = DESCryptography.DES_CBC_Decrypt(buff_temp,"file");
            else
                buff = buff_temp;
            out.write(buff, 0 ,index);
            out.flush();
        }
        System.out.println("解密完成");
        in.close();
        out.flush();
        out.close();
    }
}

