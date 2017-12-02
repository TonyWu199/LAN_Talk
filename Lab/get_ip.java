package Lab;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class get_ip {
    public static void main(String[] args) {
        System.out.println(get_juyuwang_ip());
    }

    public static String get_juyuwang_ip(){
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            while(nis.hasMoreElements())
            {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration ias = ni.getInetAddresses();
                while (ias.hasMoreElements())
                {
                    InetAddress ia = (InetAddress) ias.nextElement();
                    if(ia.getHostAddress().contains("10.11"))
                        return ia.getHostAddress();
                }

            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
