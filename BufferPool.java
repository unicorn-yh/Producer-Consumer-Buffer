import java.lang.Thread;
import java.lang.String;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.management.ManagementFactory;

public class BufferPool {
    long startTime = System.currentTimeMillis();
    public int nElem = 0;
    final int DATAMAXLEN = 10;        //字符串限长
    static final int BUFFERCOUNT = 6;        //缓冲区个数
    public int[] status = new int[BUFFERCOUNT]; //记录缓冲区状态的数组
    public int totalelement = 0;
    public int producerCount = 12;  //生产者进程需重复12次
    public int consumerCount = 8;   //消费者进程需重复8次
    public String[] content = new String[BUFFERCOUNT]; //存储字符串数据的数组

    
    public static void main(String[] args) {
        
        BufferPool bp = new BufferPool();
        int cores = Runtime.getRuntime().availableProcessors();

        System.out.println("------------- A Producer-Consumer solution using threading in Java -------------");
        System.out.println("Buffer Count : " + BUFFERCOUNT);
        System.out.println("Total Number of Threads : " + ManagementFactory.getThreadMXBean().getThreadCount());
        System.out.println("Starting Simulation with : " + cores + " CPUs");
        System.out.println("2 PRODUCER and 3 CONSUMER\n");
        

        //定义两个生产者
        Producer p1 = new Producer(bp,1);
        Producer p2 = new Producer(bp,2);
        Thread pThd1 = new Thread(p1);
        Thread pThd2 = new Thread(p2);
      
        //定义三个消费者
        Consumer c1 = new Consumer(bp,1);
        Consumer c2 = new Consumer(bp,2);
        Consumer c3 = new Consumer(bp,3);
        Thread cThd1 = new Thread(c1);
        Thread cThd2 = new Thread(c2);
        Thread cThd3 = new Thread(c3);
      
        //开始进程
        pThd1.start();
        pThd2.start();
        cThd1.start();
        cThd2.start();
        cThd3.start();
    }
    BufferPool(){
        for(int i=0;i<BUFFERCOUNT;i++){
            content[i] = "";
            content[i] = String.format("%1$10s", content[i]);  //设置字符串限长
        }
    }
}

class Producer implements Runnable {  //生产者
    int i;
    int index;
    BufferPool bp;
    
    public Producer(BufferPool bp,int index){
        i = 0;
        this.bp = bp;
        this.index = index;
    }
    
    @Override
    public void run(){
        while(bp.producerCount > 0){
            try{
                Thread.sleep((long)(Math.random() * 10000));  //随机等待一段时间
        
                //若缓冲区已满，等待消费者取走数据后再添加
                synchronized(bp){
                    while(bp.nElem == bp.status.length && bp.producerCount > 0){
                        bp.producerCount--;
                        if(bp.producerCount <= 2){
                            System.out.print("P"+(12-bp.producerCount)+" : ");
                        }
                        else{
                            System.out.print("P"+(12-bp.producerCount)+"  : ");
                        }
                        System.out.println("Array is full, producer "+index+" will wait --> number of elements: " + bp.nElem+"       | "+(System.currentTimeMillis()-bp.startTime)/1000+" s");
                        bp.wait();
                    }//while
                }//synchcontent
                
                //若缓冲区不满，则往缓冲区添加数据
                synchronized(bp){
                    if(bp.status[i%6] == 0 && bp.producerCount > 0){
                        String data = "DataStr" + (++bp.totalelement);
                        if(data.length() > bp.DATAMAXLEN){
                            System.out.println("Data too large to put into storage. (Max String Length = 10)");
                            continue;
                        }
                        bp.producerCount--;
                        bp.status[i%6] = 1;
                        if(bp.producerCount <= 2){
                            System.out.print("P"+(12-bp.producerCount)+" : ");
                        }
                        else{
                            System.out.print("P"+(12-bp.producerCount)+"  : ");
                        }
                        System.out.print("Producer "+index+" set buffer[ " + (i%6) + " ] to FULL  | ");
                        if(bp.content[i%6] == "" || bp.content[i%6].equals(String.format("%1$10s", ""))){
                            bp.content[i%6] = data;
                            if(bp.totalelement >= 10){
                                System.out.println("Push \"" + bp.content[i%6] +"\" into storage | "+(System.currentTimeMillis()-bp.startTime)/1000+" s");
                            }
                            else{
                                System.out.println("Push \"" + bp.content[i%6] +"\" into storage  | "+(System.currentTimeMillis()-bp.startTime)/1000+" s");
                            } 
                        }
                        else{
                            System.out.println("Error putting data into storage.");
                        }
                        
                        i++;
                        bp.nElem++;
                    }
                    bp.notify();
                }//synch
            } catch (InterruptedException ex) {
                Logger.getLogger(Producer.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }
    
}

class Consumer implements Runnable{
    int[] status;
    int i = 0;
    int index;
    BufferPool bp;
    
    public Consumer(BufferPool bp,int index){
        this.bp = bp;
        this.index = index;
    }
    
    @Override
    public void run(){
        while(bp.consumerCount > 0){
            try{
                
                Thread.sleep((long)(Math.random() * 10000));   //随机等待一段时间
                
                //若缓冲区为空，等待生产者添加数据后再读取
                synchronized(bp){  
                    while(bp.nElem == 0 && bp.consumerCount > 0){
                        bp.consumerCount--;
                        System.out.print("C"+(8-bp.consumerCount)+"  : ");
                        System.out.println("Array is empty, consumer "+index+" will wait --> number of elements: " + bp.nElem+"      | "+(System.currentTimeMillis()-bp.startTime)/1000+" s");
                        bp.wait();
                    }//while
                }//synch
                
                //若缓冲区为非空，则从缓冲区获取数据
                synchronized(bp){
                    if(bp.status[i%6] == 1 && bp.consumerCount > 0){
                        bp.status[i%6] = 0;
                        bp.consumerCount--;
                        System.out.print("C"+(8-bp.consumerCount)+"  : ");
                        System.out.print("Consumer "+index+" set buffer[ " + (i%6) + " ] to EMPTY | ");
                        if(bp.totalelement >= 10){
                            System.out.println("Pop  \""+ bp.content[i%6]+ "\" from storage | "+(System.currentTimeMillis()-bp.startTime)/1000+" s");
                        }
                        else{
                            System.out.println("Pop  \""+ bp.content[i%6]+ "\" from storage  | "+(System.currentTimeMillis()-bp.startTime)/1000+" s");
                        }
                        bp.content[i%6] = "";
                        i++;
                        bp.nElem--;
                    }
                   
                    bp.notify();
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Producer.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }
    
}

