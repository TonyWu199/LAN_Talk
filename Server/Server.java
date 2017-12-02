package Server;


import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Server {
    private static final int PORT = 6066;
    private static ServerSocket serverSocket = null;
    private static final String DataBase_path = "Data_Base.txt";   //用户数据库路径
    private static String Text_record_dir = "Text_recode";    //用户聊天记录存放文件夹
    private static String client_ip_port = null;
    private static final List<String> user_List = new ArrayList<>();  //全体用户列表
    private static final HashMap<String,String> online_user_ip_Map = new HashMap<>(); //在线用户列表
    private static final List<String> offline_user_list = new ArrayList<>();  //离线用户列表

    private static JFrame frame = null;
    private static JPanel northPanel = null;   //上方面板
    private static JPanel southPanle = null;   //下方面板
    private static JTextField textHost = null;
    private static JTextArea InfoArea = null;
    private static JTextField SoftwareInfo = null;
    private static JList online_user  = null;
    private static JList offline_user = null;
    private static DefaultListModel<String> online_dlm;
    private static DefaultListModel<String> offline_dlm;
    private static JScrollPane CenterScroll;
    private static JScrollPane leftScroll;
    private static JScrollPane rightScroll;


    public static void main(String[] args){
        System.out.println("服务器启动...\n");
        //加载用户数据
        try {
            File data_file = new File(DataBase_path);

            //文件不存在
            if(!data_file.exists()) {
                System.out.println("正在创建初始化数据库");
                data_file.createNewFile();
            }
            String file_str;
            FileReader file_reader = new FileReader(data_file);
            BufferedReader file_br = new BufferedReader(file_reader);

            System.out.println("正在同步数据库信息");

            new DESCryptography();
            while((file_str = file_br.readLine()) != null){
                file_str = DESCryptography.DES_CBC_Decrypt(DESCryptography.hexStr2Bytes(file_str));    //读取数据库信息并解密
                //System.out.println(file_str.split("\\|")[0]);
                user_List.add(file_str.split("\\|")[0]);
                offline_user_list.add(file_str.split("\\|")[0]);
            }
            file_br.close();
            file_reader.close();

            //创建聊天记录文件夹
            File recodedir = new File(Text_record_dir);
            if(!recodedir.exists())
                recodedir.mkdir();
        } catch (Exception e) {
            e.printStackTrace();
        }


        Server server = new Server();
        server.init();
    }

    public void init(){
        try {
            //建立一个socket端口进行客户端消息监听
            serverSocket = new ServerSocket(PORT);
            Server_panel();
            while (true){
                //阻塞循环，表示服务器与客户端取得了连接
                Socket client = serverSocket.accept();
                //处理此次连接,每次收发消息为一次连接。
                new HandlerThread(client);
            }
        }catch (Exception e){
            System.out.println("服务器异常：" + e.getMessage());
        }
    }

    private class HandlerThread implements Runnable{
        private Socket socket;

        public HandlerThread(Socket client){
            socket = client;
            new Thread(this).start();
        }

        public void run(){
            client_ip_port = socket.getRemoteSocketAddress().toString();
            System.out.println("client" + client_ip_port);

            String username = null;
            try{
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                //这里要注意和客户端输出流的写方法对应
                String clientInputStr;
                while ((clientInputStr = input.readLine()) != null){
                    //取出用户的通信请求：登录/注册/上线声明/获得用户列表/退出通知
                    String category = clientInputStr.split("\\|")[0];
                    System.out.println("请求信息为 ：" + clientInputStr);
                    switch (category) {
                        case "login": {   //登陆功能
                            //调用登录方法
                            String s = checkin(clientInputStr);
                            out.write(s);
                            out.newLine();
                            out.flush();
                            break;
                        }

                        case "register": {  //注册
                            String s = register(clientInputStr);
                            out.write(s);
                            out.newLine();
                            out.flush();
                            break;
                        }

                        case "findpass":{
                            System.out.println(clientInputStr);
                            String s = getPass(clientInputStr);

                            out.write(s);
                            out.newLine();
                            out.flush();
                            break;
                        }

                        case "online": {  //申请在线
                            //添加在线用户列表（ip,用户名）,删除离线用户列表
                            online_user_ip_Map.put(clientInputStr.split("\\|")[1], clientInputStr.split("\\|")[2] + ":" + clientInputStr.split("\\|")[3]);
                            offline_user_list.remove(clientInputStr.split("\\|")[1]);
                            out.write("状态：在线");
                            out.newLine();
                            out.flush();
                            break;
                        }

                        case "get_online_user":{
                            StringBuilder user_list_str = new StringBuilder("----在线用户---- ");
                            if(!online_user_ip_Map.isEmpty()) {
                                for (String key : online_user_ip_Map.keySet()) {
                                    user_list_str.append(key).append(":").append(online_user_ip_Map.get(key)).append(" ");
                                }
                            }

                            user_list_str.append("----离线用户---- ");
                            if(!offline_user_list.isEmpty()) {
                                for (String temp : offline_user_list) {
                                    if(!(temp == null)) {
                                        user_list_str.append(temp).append(" ");
                                    }
                                }
                            }

                            System.out.println("用户列表为 :" + user_list_str);  //显示拉取的用户列表
                            out.write(user_list_str.toString());
                            out.newLine();
                            out.flush();
                            break;
                        }

                        case "save_recode":{
                            System.out.println("save_recode: " + clientInputStr.split("\\|")[1] + "&" + clientInputStr.split("\\|")[2] + "说了" + clientInputStr.split("\\|")[3]);

                            String p2p_username = clientInputStr.split("\\|")[1];
                            String p2p_target = clientInputStr.split("\\|")[2];
                            String save_content = clientInputStr.split("\\|")[3];

                            try {
                                File user_recode_dir = new File(Text_record_dir + "\\" + p2p_username);

                                //user数据库聊天记录保存
                                if (!user_recode_dir.exists())
                                    user_recode_dir.mkdir();

                                File user_recode_text = new File(Text_record_dir + "\\" + p2p_username + "\\" + p2p_target + ".txt");
                                System.out.println(user_recode_text.getName());
                                if (!user_recode_text.exists())
                                    user_recode_text.createNewFile();

                                FileWriter user_writer = new FileWriter(user_recode_text, false);  //覆盖写入
                                new DESCryptography();
                                user_writer.write(DESCryptography.byteToHexString(DESCryptography.DES_CBC_Encrypt(save_content)));   //写入服务器数据库
                                user_writer.close();


                                //target数据库聊天记录储存
                                File target_recode_dir = new File(Text_record_dir + "\\" + p2p_target);

                                //创建单个用户的记录文件
                                if (!target_recode_dir.exists())
                                    target_recode_dir.mkdir();

                                File target_recode_text = new File(Text_record_dir + "\\" + p2p_target + "\\" + p2p_username + ".txt");

                                if (!target_recode_text.exists())
                                    target_recode_text.createNewFile();

                                FileWriter target_writer = new FileWriter(target_recode_text, false);  //覆盖写入
                                new DESCryptography();
                                target_writer.write(DESCryptography.byteToHexString(DESCryptography.DES_CBC_Encrypt(save_content)));   //加密写入服务器数据库
                                target_writer.close();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            out.write("记录保存完毕");
                            out.newLine();
                            out.flush();
                            break;
                        }

                        case "get_recode":{
                            System.out.println("get recode: " + clientInputStr.split("\\|")[1] + " to " + clientInputStr.split("\\|")[2]);

                            String p2p_username = clientInputStr.split("\\|")[1];
                            String p2p_target = clientInputStr.split("\\|")[2];
                            String get_content;

                            try{
                                File user_recode_text = new File(Text_record_dir + "\\" + p2p_username + "\\" + p2p_target + ".txt");

                                //聊天记录不存在，返回提示字符串
                                if(!user_recode_text.exists()){
                                    out.write("无聊天内容");
                                    out.newLine();
                                    out.flush();
                                    break;
                                }
                                else{
                                    BufferedReader reader = new BufferedReader(new FileReader(user_recode_text));
                                    get_content = reader.readLine();
                                    System.out.println("get_content:" + get_content);
                                    new DESCryptography();
                                    out.write(DESCryptography.DES_CBC_Decrypt(DESCryptography.hexStr2Bytes(get_content)));
                                    out.newLine();
                                    out.flush();
                                    break;
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }


                        }

                        case "close": {   //关闭窗口，退出登录
                            System.out.println("closing");

                            //删除用户在线记录,将用户添加至离线列表
                            username = clientInputStr.split("\\|")[1];
                            System.out.println(username);
                            online_user_ip_Map.remove(username);
                            offline_user_list.add(username);
                            out.write("状态：离线");
                            out.newLine();
                            out.flush();

                            //关闭通道
                            input.close();
                            out.close();
                            break;
                        }
                    }
                }
            }catch (Exception e){
                if(username != null){
                    InfoArea.append(">>>用户---"+ username + "---已退出\n");
                }
            }finally {
                if(socket != null){
                    try{
                        socket.close();
                    }catch (Exception e){
                        socket = null;
                        System.out.println("服务器端finally异常:" + e.getMessage());
                    }
                }
            }
        }
    }

    private String register(String str) {
        String username;
        String password;
        String mail;

        username = str.split("\\|")[1];
        password = str.split("\\|")[2];
        mail = str.split("\\|")[3];
        //System.out.println("账号为：" + username + "密码为：" + password);
        try {
            for(String username_str : user_List) {
                if(username_str.equals(username)){
                    return "用户已存在";
                }
            }
            FileWriter writer = new FileWriter(DataBase_path, true);

            new DESCryptography();
            String user_info = username + "|" + password + "|" + mail;
            writer.write( DESCryptography.byteToHexString(DESCryptography.DES_CBC_Encrypt(user_info)) +"\r\n");   //加密并写入服务器数据库
            user_List.add(username);
            writer.close();
            InfoArea.append(">>>" + username + "已成功注册" + '\n');
            return "注册成功";
        } catch (Exception e) {
            System.out.println("写入数据库异常：" + e.getMessage());
            return "数据库异常，注册失败";
        }
    }

    private String checkin(String str){
        String username;
        String password;

        username = str.split("\\|")[1];
        password = str.split("\\|")[2];

        //这里需要读取密码，所以重新读取一遍文件
        try{
            String file_str = null;
            FileReader file_reader = new FileReader(DataBase_path);
            BufferedReader file_br = new BufferedReader(file_reader);
            new DESCryptography();
            while ((file_str = file_br.readLine()) != null){
                file_str = DESCryptography.DES_CBC_Decrypt(DESCryptography.hexStr2Bytes(file_str));    //读取数据库信息并解密
                if(file_str.contains(username + "|" + password) && !online_user_ip_Map.containsKey(username)){
                    InfoArea.append(">>>" + username + "已成功登录" + '\n');
                    return "登录成功";
                }
                else if(file_str.contains(username + "|" + password)){
                    return "此用户已在线";
                }
            }
            file_reader.close();
            file_br.close();
            return "账号密码错误。";
        }catch (Exception e){
            System.out.println(e.getMessage());
            return "数据库异常，登录失败。";
        }
    }

    private String getPass(String findpassinfo){
        String username = findpassinfo.split("\\|")[1];
        String mail = findpassinfo.split("\\|")[2];
        try {
            String file_str = null;
            FileReader file_reader = new FileReader(DataBase_path);
            BufferedReader file_br = new BufferedReader(file_reader);
            new DESCryptography();
            while ((file_str = file_br.readLine()) != null){
                file_str = DESCryptography.DES_CBC_Decrypt(DESCryptography.hexStr2Bytes(file_str));    //读取数据库信息并解密
                if(file_str.contains(username) && file_str.contains(mail))
                    return file_str.split("\\|")[2];
            }
            file_reader.close();
            file_br.close();
            return "不存在该用户";

        }catch (Exception e){
            e.printStackTrace();
            return "数据库异常，登录失败。";
        }
    }

    private static void Server_panel(){

        try {
            textHost = new JTextField(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        textHost.setEditable(false);
        //服务器上方基本信息
        JButton F5 = new JButton("刷新列表");
        northPanel = new JPanel();
        northPanel.setLayout(new GridLayout(1,3));
        northPanel.add(F5);
        northPanel.add(new JLabel("    "));
        northPanel.add(new JLabel("                                                            服务器ip"));
        northPanel.add(textHost);
        northPanel.setBorder(new TitledBorder("服务器基本信息"));

        //中间信息显示窗口
        InfoArea = new JTextArea();
        InfoArea.setEditable(false);
        InfoArea.setForeground(Color.BLACK);
        InfoArea.setLineWrap(true);
        InfoArea.setCaretPosition(InfoArea.getDocument().getLength());
        CenterScroll = new JScrollPane(InfoArea);
        CenterScroll.setBorder(new TitledBorder(">>>>>>>>>>>>>>>>>>>>>>>>>> 交 互 监 控 <<<<<<<<<<<<<<<<<<<<<<<<<<"));

        //下方服务器信息
        southPanle = new JPanel();
        southPanle.setLayout(new GridLayout(1,1));
        southPanle.add(new JLabel(""));

        //两侧用户信息
        online_dlm = new DefaultListModel<>();
        online_user = new JList<String>();
        online_user.setForeground(Color.BLACK);
        leftScroll = new JScrollPane(online_user);
        leftScroll.setBorder(new TitledBorder("在线用户"));

        offline_dlm = new DefaultListModel<>();
        offline_user =  new JList<String>();
        offline_user.setForeground(Color.black);
        //offline_dlm.addElement("a");
        //offline_user.setModel(offline_dlm);
        rightScroll = new JScrollPane(offline_user);
        rightScroll.setBorder(new TitledBorder("离线用户"));

        frame = new JFrame("Zz-ChatRoom !!Server!! v1.0");
        frame.setLayout(new BorderLayout());
        frame.setSize(1000,500);
        frame.add(northPanel,BorderLayout.NORTH);
        frame.add(leftScroll,"West");
        frame.add(rightScroll,"East");
        frame.add( southPanle,"South");
        frame.add(CenterScroll,"Center");

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        F5.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                offline_dlm.removeAllElements();
                if(!offline_user_list.isEmpty()) {
                    for (String temp : offline_user_list) {
                        if(!(temp == null)) {
                            offline_dlm.addElement(temp);
                        }
                    }
                }
                offline_user.setModel(offline_dlm);

                online_dlm.removeAllElements();
                if(!online_user_ip_Map.isEmpty()) {
                    for (String key : online_user_ip_Map.keySet()) {
                        online_dlm.addElement(key);
                    }
                }
                online_user.setModel(online_dlm);
            }
        });
    }
}
