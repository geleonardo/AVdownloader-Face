package com.company;
import java.io.*;
import java.util.Properties;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



public class Main {
    public static double getDiskInfo() {
        StringBuffer sb=new StringBuffer();
        File[] roots = File.listRoots();// 获取磁盘分区列表
        for (File file : roots) {
            long totalSpace=file.getTotalSpace();
            long freeSpace=file.getFreeSpace();
            long usableSpace=file.getUsableSpace();
            double size=0,free=0;
            if(totalSpace>0){
                sb.append(file.getPath() + "(总计：");
                size=(((double)totalSpace/ (1024*1024*1024))*100/100.0);
                free=((((double)usableSpace/ (1024*1024*1024))*100)/100.0);
                return free;
            }
        }return 0;
    }




    public static void main (String args[]) throws Exception{
//        Properties prop = System.getProperties();
//        prop.setProperty("http.proxyHost", "192.168.137.1");
//        prop.setProperty("http.proxyPort", "1080");
//        prop.setProperty("https.proxyHost", "192.168.137.1");
//        prop.setProperty("https.proxyPort", "1080");
        //String encoding = "UTF-8";
        File file = new File(System.getProperty("user.dir")+"/Downloaded.log");//读取已下载影片
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        String magnet,size,cache,downloaded="";
        double Size,disk;
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
            downloaded=new String(filecontent);
            Document doc = null,Doc;
            Document doc1 = null;
            System.out.println(downloaded);
            for(int i=1;i<=999999999;i++){//循环监测首页内容
                doc = Jsoup.connect("https://www.javbus.com").get();//读取首页
                Elements links = ((Element) doc).select("a[href]");
                int b=0,ID=1234;
                for(Element link : links){
                    String linkHref =(link.attr("href"));
                    String a=OpenUrl(linkHref);
                    if((a!=null)&&(a.indexOf("javascript")==-1)&&(a.indexOf("page")==-1)&&(a.indexOf("-")!=-1)){//选择链接
                        if (b>=17&&(b<=46)){
                            //System.out.println(a);
                            if(downloaded.indexOf(a) == -1){
                                Doc = Jsoup.connect(a).get();
                                boolean beauty=false;//创建颜值阈值布尔变量
                                Elements Images = ((Element) Doc).select("a[href]");
                                 //System.out.println(Images);
                                for(Element Image: Images) {
                                    String ImageHref = (Image.attr("href"));
                                    String x = OpenUrl(ImageHref);
                                    if((x!=null)&&x.indexOf("cover")!=-1){//下载首页图片
                                        System.out.println(x);
                                        com.company.Images.Download(x);
                                        Thread.sleep(1500);
                                    }
                                }
                                String bool="error",line=null;
                                 while ((bool.indexOf("True")==-1)&&(bool.indexOf("False")==-1)){
                                    String[] args1 = new String[] { "python3", System.getProperty("user.dir")+"/main_facepp.py", "1.jpg","65","40"};//调用Python脚本完成人脸识别
                                    Process pr=Runtime.getRuntime().exec(args1);
                                    StreamCaptureThread errorStream = new StreamCaptureThread(pr.getErrorStream());
                                    StreamCaptureThread outputStream = new StreamCaptureThread(pr.getInputStream());
                                    new Thread(errorStream).start();
                                    new Thread(outputStream).start();
                                    pr.waitFor();
                                    in.close();
                                    line=outputStream.output.toString();
                                    bool=line;
                                    System.out.println(line);
                                    if (line.indexOf("True")!=-1)
                                        beauty=true;
                                    //System.out.println(beauty);
                                    //System.out.println(line);

                                    }
                                System.out.println(beauty);
                                if(beauty) {//如果颜值过关则加入下载
                                    System.out.println(a);
                                    cache=Magnet.Magnet(a);//使用Magnet方法下载磁力链接和大小
                                    int intIndex = cache.indexOf("~");//分离磁力链接和大小
                                    size=cache.substring(0,intIndex-2);
                                    magnet=cache.substring(intIndex+1,cache.length());
                                    Size=Double.parseDouble(size);
                                    disk=getDiskInfo();//读取磁盘大小
                                    while((Size+1)>=disk){
                                        Thread.sleep(100000);
                                        //System.out.println(1000);
                                        disk=getDiskInfo();//等待下载完成磁盘清空
                                    }
                                    RPC.addrpc(magnet,ID);//发送rpc命令到aria2端口
                                    RPC.tellrpc(ID);
                                    ID++;
                                    downloaded=a+downloaded;
                                    FileWriter fw = new FileWriter(System.getProperty("user.dir")+"/Downloaded.log", true);//写入已下载列表
                                    BufferedWriter bw = new BufferedWriter(fw);
                                    bw.write(a);
                                    bw.close();
                                    fw.close();
                                }
                            }
                        }
                        b++;
                    }
                }
                Thread.sleep(100000);
            }

        } catch (IOException e) {
            System.out.println("以上地址未获取到页面");
            e.printStackTrace();
        }

    }
    public static String OpenUrl(String str){
        if ((str.length()>=24)){
            return str;
        }
        else return null;

    }

}
