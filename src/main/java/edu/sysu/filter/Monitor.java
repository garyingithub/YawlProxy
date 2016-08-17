package edu.sysu.filter;

import edu.sysu.monitor.CaseMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by gary on 16-8-17.
 */

public class Monitor extends Thread {


    private Logger logger= LoggerFactory.getLogger(this.getClass());
    Object hashLock = new Object();

    //当前的连接数和工作线程数
    static int workThreadNum = 0;
    static int socketConnect = 0;

    private ServerSocket serverSocket;
    //服务器IP
    private String host = "192.168.199.175";

    //服务器端口
    private int stateReportPort = 9897;

    //扫描间隔
    private int scanTime = 1800;


    private CaseMonitor caseMonitor;

    public Monitor(CaseMonitor caseMonitor){
        this.caseMonitor=caseMonitor;
    }
    @Override
    public void run() {
        //绑定端口,并开始侦听用户的心跳包
        serverSocket = startListenUserReport(stateReportPort);
        if (serverSocket == null) {
            System.out.println("【创建ServerSocket失败！】");
            return;
        }
        //启动扫描线程
        //Thread scanThread = new Thread(new scan());
        //scanThread.start();
        //等待用户心跳包请求
        while (true) {
            Socket socket =
                    null;
            try {
                socketConnect = socketConnect + 1;
                //接收客户端的连接
                socket = serverSocket.accept();
                //为该连接创建一个工作线程
                Thread workThread = new Thread(new Handler(socket));
                //启动工作线程
                workThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 创建一个ServerSocket来侦听用户心跳包请求
     *
     * @param port 指定的服务器端的端口
     * @return 返回ServerSocket
     * @author dream
     */
    public ServerSocket startListenUserReport(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket();
            if (!serverSocket.getReuseAddress()) {
                serverSocket.setReuseAddress(true);
            }
            serverSocket.bind(new InetSocketAddress(host, port));
            System.out.println("【开始在" + serverSocket.getLocalSocketAddress() + "上侦听用户的心跳包请求！】");
            return serverSocket;
        } catch (IOException e) {
            System.out.println("【端口" + port + "已经被占用！】");
            if (serverSocket != null) {
                if (!serverSocket.isClosed()) {
                    try {
                        serverSocket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
        return serverSocket;
    }


    class Handler implements Runnable {
        private Socket socket;

        /**
         * 构造函数，从调用者那里取得socket
         *
         * @param socket 指定的socket
         * @author dream
         */
        public Handler(Socket socket) {
            this.socket = socket;
        }

        /**
         * 从指定的socket中得到输入流
         *
         * @param socket 指定的socket
         * @return 返回BufferedReader
         * @author dream
         */
        private BufferedReader getReader(Socket socket) {
            InputStream is = null;
            BufferedReader br = null;

            try {
                is = socket.getInputStream();
                br = new BufferedReader(new InputStreamReader(is));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return br;
        }

        private Map parseString(String input){
            String[] kvs=input.split(" ");
            Map<String,Integer> result=new HashMap<>();
            for(String s:kvs){
                String[] temp=s.split(",");
                result.put(temp[0],Integer.valueOf(temp[1]));
            }
            return result;
        }
        public void run() {
            try {
                workThreadNum = workThreadNum + 1;
                BufferedReader br = getReader(socket);
                String meg = null;
                StringBuffer report = new StringBuffer();
                while ((meg = br.readLine()) != null) {
                    if(meg.length()>1&&!meg.contains("hehe"))
                        caseMonitor.updateMap(parseString(meg));
                }


            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    try {
                        //断开连接
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}