package start;

import com.wy313.entity.Page;
import com.wy313.service.iDownService;
import com.wy313.service.iProcessService;
import com.wy313.service.iStoreService;
import com.wy313.service.impl.BQGDownService;
import com.wy313.service.impl.BQGProcessService;
import com.wy313.service.impl.MysqlStoreService;
import com.wy313.tools.LoadProperters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class BQGMain {
    //页面下载
    private iDownService iDownService;
    //页面解析
    private com.wy313.service.iProcessService iProcessService;

    //开启线程
    private ExecutorService newfiexThree= Executors.newFixedThreadPool(5);

    //页面保存
    private iStoreService iStoreService;

    public com.wy313.service.iStoreService getiStoreService() {
        return iStoreService;
    }

    public void setiStoreService(com.wy313.service.iStoreService iStoreService) {
        this.iStoreService = iStoreService;
    }

    public com.wy313.service.iDownService getiDownService() {
        return iDownService;
    }

    public void setiDownService(com.wy313.service.iDownService iDownService) {
        this.iDownService = iDownService;
    }

    public com.wy313.service.iProcessService getiProcessService() {
        return iProcessService;
    }

    public void setiProcessService(com.wy313.service.iProcessService iProcessService) {
        this.iProcessService = iProcessService;
    }

    public static void main(String[] args) {
        BQGMain bqgMain=new BQGMain();
        bqgMain.setiDownService(new BQGDownService());
        bqgMain.setiProcessService(new BQGProcessService());
        bqgMain.setiStoreService(new MysqlStoreService());

        String url= LoadProperters.getConfig("bqgurl");
//       url="https://www.biqudao.com/bqge_1/";
//        url="https://www.biqudao.com/bqge31190/";
//        url="https://www.biqudao.com/bqge31190/1921984.html";


        Page page = bqgMain.getiDownService().down(url);
        bqgMain.getiProcessService().process(page);
        List<String> menuList = (List<String>) page.getMenuList();
        page=null;
        for(String  i:menuList){
                page=bqgMain.getiDownService().down(i);
            bqgMain.getiProcessService().process(page);
            List< HashMap<String, String>> pagelist =( ArrayList< HashMap<String,String>>)page.getPagelist();

            for( HashMap<String, String> j:pagelist){
                page.setTypeName(j.keySet().toArray()[0].toString());
                Page repage = bqgMain.splid(j.values().toArray()[0].toString());

                bqgMain.getiStoreService().store(repage);

            }

        }


    }
        public Page splid(String url){
            System.out.println("---------------splid start ---------------");
            Page   page = getiDownService().down(url);
            page.setMainurl(url);
            getiProcessService().process(page);
            while (true) {

                final String url_start=page.getStorelist().poll();
                if (url_start!=null) {
                    Future<?> submit = newfiexThree.submit(new Runnable() {
                                Page p1;
                                @Override
                                public void run() {
                                    System.out.println("线程运行中： " + (url_start));
                                    p1 = getiDownService().down(url_start);
                                    page.setUrl(p1.getUrl());
                                    page.setConetnt(p1.getConetnt());

                                    getiProcessService().process(page);
                                    //System.out.println(page.getChapts());
                            p1 = null;
                        }
                    });
//                    ThreadPoolExecutor tpe = ((ThreadPoolExecutor)newfiexThree);
//                    //判断已经执行完成线程数
//                    if(tpe.getActiveCount()>2){
//                        newfiexThree.shutdownNow();
//                        break;
//                    }

                }else{
                    ThreadPoolExecutor tpe = ((ThreadPoolExecutor)newfiexThree);

                    if(tpe.getActiveCount()==0&& tpe.getQueue().size()==0){
                        newfiexThree.shutdown();
                        break;
                    }
                }


                }

//            ThreadPoolExecutor tpe = ((ThreadPoolExecutor)newfiexThree);
//            while(true){
//                if(tpe.getActiveCount()==0){
//                   break;
//                }
//            }
            System.out.println("---------------splid end ---------------");
            return page;



        }

}
