package Lab;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.Socket;


public class Client_login_ui {
    private static String IP_ADDR;
    private static final int PORT = 6066;
    private static Socket socket;
    private static BufferedReader input;
    private static BufferedWriter out;
    private static Integer access = 0;
    private static String username;


    public static void main(String[] args) {
        LoginPanel();
    }

    //登录面板
    public static void LoginPanel() {
        String inputValue = JOptionPane.showInputDialog("Input server ip");
        IP_ADDR = inputValue;
        JPanel panel = new JPanel();
        panel.setLayout(null);
        // 账户名提示及输入账户名
        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(30, 200, 80, 30);
        panel.add(userLabel);
        JTextField userText = new JTextField(20);
        userText.setBounds(100, 200, 165, 30);
        panel.add(userText);

        // 创建注册按钮
        JButton registerButton = new JButton("Register");
        registerButton.setBounds(270, 200, 90, 25);
        panel.add(registerButton);

        // 输入密码,密码会以圆点的形式显示
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(30, 240, 80, 30);
        panel.add(passwordLabel);
        JPasswordField passwordText = new JPasswordField(20);
        passwordText.setBounds(100, 240, 165, 30);
        panel.add(passwordText);

        // 创建找回密码按钮
        JButton findpassButton = new JButton("FindPass");
        findpassButton.setBounds(270, 240, 90, 25);
        panel.add(findpassButton);

        //创建登陆提示信息
        JLabel errorInfo = new JLabel("");
        errorInfo.setBounds(100,270,165,40);
        panel.add(errorInfo);

        // 创建登录按钮
        JButton loginButton = new JButton("Login/Enter");
        loginButton.setBounds(100, 300, 165, 40);
        panel.add(loginButton);

        // 创建 JFrame 实例
        JFrame frame = new JFrame("Zz-Login v1.0");
        // Setting the width and height of frame
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.add(panel);
        frame.setVisible(true);

        //创建一个流套接字并将其连接到指定的主机上的指定端口号
        try {
            socket = new Socket(IP_ADDR, PORT);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*登录事件 start
         */
        //点击登录
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SendInfo_login("login" + "|" + userText.getText() + "|" + passwordText.getText(), errorInfo, frame);
            }
        });

        //回车登录
        loginButton.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loginButton.doClick();
            }
        },KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0),JComponent.WHEN_IN_FOCUSED_WINDOW);

        /*登录事件 end
         */

        //注册事件
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RegisterPanel();
            }
        });

        //找回密码时间
        findpassButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                findPassPanel();
            }
        });

        //循环监听登录事件，此处有一个坑
        while (access != 1)  {
            System.out.print("");
        }
        Client_chat_ui.user_UI(username);
    }

    //注册面板
    public static void RegisterPanel(){
        // 创建 JFrame 实例
        JFrame frame = new JFrame("Zz-Register v1.0");
        // Setting the width and height of frame
        frame.setSize(400, 450);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        /* 创建面板，这个类似于 HTML 的 div 标签
         * 我们可以创建多个面板并在 JFrame 中指定位置
         * 面板中我们可以添加文本字段，按钮及其他组件。
         */
        JPanel registerpanel = new JPanel();
        // 添加面板
        frame.add(registerpanel);
        frame.setVisible(true);
        registerpanel.setLayout(null);

        //欢迎信息
        JLabel welcomeLabel = new JLabel("Welcome to Register");
        welcomeLabel.setBounds(20,5,300,100);
        Font font = new Font("TimesRoman",Font.ITALIC,24);
        welcomeLabel.setFont(font);
        registerpanel.add(welcomeLabel);

        //输入账户名
        JLabel userLabel = new JLabel("Username");
        userLabel.setBounds(50,150,80,25);
        registerpanel.add(userLabel);
        JTextField userText = new JTextField(20);
        userText.setBounds(130,150,165,25);
        registerpanel.add(userText);

        //输入密码
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setBounds(50,200,80,25);
        registerpanel.add(passwordLabel);
        JPasswordField passwordText = new JPasswordField(20);
        passwordText.setBounds(130,200,165,25);
        registerpanel.add(passwordText);

        //输入邮箱
        JLabel mailLabel = new JLabel("Email");
        mailLabel.setBounds(50,250,80,25);
        registerpanel.add(mailLabel);
        JTextField mailText = new JTextField(20);
        mailText.setBounds(130,250,165,25);
        registerpanel.add(mailText);

        //errorInfo
        JLabel errorInfo = new JLabel("");
        errorInfo.setBounds(130,280,165,40);
        registerpanel.add(errorInfo);

        //确认
        JButton registerButton = new JButton("Register");
        registerButton.setBounds(130,320,165,40);
        registerpanel.add(registerButton);


        /*登录响应事件
         start*/

        //单击登录
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SendInfo_register("register" + "|" + userText.getText() + "|" + passwordText.getText() + "|" + mailText.getText(), errorInfo);
                if(errorInfo.getText().equals("注册成功")){
                    JOptionPane.showMessageDialog(frame, "注册成功");
                    frame.dispose();
                }
            }
        });

        //回车登录
        registerButton.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerButton.doClick();
            }
        },KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0),JComponent.WHEN_IN_FOCUSED_WINDOW);
        /*登录响应事件
         end*/
    }

    //向服务器发送登录信息
    private static void SendInfo_login(String loginInfo, JLabel errorInfo, JFrame frame) {
        //Socket socket = null;
        username = loginInfo.split("\\|")[1];
        try {
            System.out.println(loginInfo);
            out.write(loginInfo);
            out.newLine();
            out.flush();
            //服务器端发来的信息
            String ServerStr = input.readLine();
            errorInfo.setText(ServerStr);

            System.out.println(ServerStr);
            if(ServerStr.equals("登录成功")){
                frame.dispose();
                access = 1;
            }
        } catch (Exception e) {
            System.out.println("客户端异常：" + e.getMessage());
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    socket = null;
                    System.out.println("客户端finally异常：" + e.getMessage());
                }
            }
        }
    }

    //向服务器发送注册信息
    private static void SendInfo_register(String registerInfo, JLabel errorInfo) {
        //Socket socket = null;
        try {
            out.write(registerInfo);
            out.newLine();
            out.flush();
            //服务器端发来的信息
            String ServerStr = input.readLine();
            errorInfo.setText(ServerStr);
        } catch (Exception e) {
            System.out.println("客户端异常：" + e.getMessage());
        }
    }

    //找回密码
    private static void findPassPanel(){


        JPanel findPasspanel = new JPanel();
        findPasspanel.setLayout(null);

        //欢迎语句
        JLabel welcomeLabel = new JLabel("Welcome to FindPass");
        welcomeLabel.setBounds(20,5,300,100);
        Font font = new Font("TimesRoman",Font.ITALIC,24);
        welcomeLabel.setFont(font);
        findPasspanel.add(welcomeLabel);

        //输入用户名
        JLabel userLabel = new JLabel("Input your name");
        userLabel.setBounds(50,90,150,25);
        userLabel.setFont(new Font("TimesRoman",Font.BOLD,12));
        findPasspanel.add(userLabel);
        JTextField username = new JTextField(20);
        username.setBounds(150,90,165,25);
        findPasspanel.add(username);

        //请输入邮箱,以邮箱为依据找回密码
        JLabel mailLabel = new JLabel("Input your mail");
        mailLabel.setBounds(50,130,150,25);
        mailLabel.setFont(new Font("TimesRoman",Font.BOLD,12));
        findPasspanel.add(mailLabel);
        JTextField mail = new JTextField(20);
        mail.setBounds(150,130,165,25);
        findPasspanel.add(mail);

        JButton send = new JButton("Find");
        send.setBounds(125,200,150,30);
        findPasspanel.add(send);

        JFrame frame = new JFrame("Zz-FindPass v1.0");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.add(findPasspanel);
        frame.setVisible(true);

        send.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SendInfo_findPass("findpass"+ "|" + username.getText() + "|" + mail.getText());
            }
        });
    }

    private static void SendInfo_findPass(String findPass_info){
        try {

            out.write(findPass_info);
            out.newLine();
            out.flush();

            String ServerStr = input.readLine();
            JOptionPane.showMessageDialog(null,"密码为" + ServerStr,"找回密码",JOptionPane.WARNING_MESSAGE);


        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

