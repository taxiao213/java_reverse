package com.taxiao.reverse.util;

import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * radare2 使用
 *
 * CSDN:http://blog.csdn.net/yin13753884368/article
 * Github:https://github.com/taxiao213
 */
@Log4j2
public class Radare2Job5 {

    private Process process;
    private final String R2_EXIT = "exit";
    private LinkedBlockingQueue<String> linkedBlockingQueue;
    StringBuilder stringBuilder;
    private String path;
    private final long TIME = 1000 * 60 * 1;
    private boolean isClose = false;

    public Radare2Job5(String filePath) {
        linkedBlockingQueue = new LinkedBlockingQueue<>();
        stringBuilder = new StringBuilder();
        path = filePath;
    }

    public void startRadare2() {
        initRadareProcess(path);

        Thread threadRead = new Thread(this::readCmd);
        threadRead.setName("readCmdRs");
        threadRead.start();

        Thread threadError = new Thread(this::readErrorCmd);
        threadError.setName("readErrorCmd");
        threadError.start();

        Thread threadWrite = new Thread(this::writeCmd);
        threadWrite.setName("writeCmd");
        threadWrite.start();

        Thread threadClose = new Thread(this::closeProcess);
        threadClose.setName("closeProcess");
        threadClose.start();

        Thread threadPut = new Thread(this::putCmd);
        threadPut.setName("putCmd");
        threadPut.start();

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("main thread stop");
    }

    public void inputCmd(String cmd) {
        try {
            if (linkedBlockingQueue != null) {
                linkedBlockingQueue.put(cmd);
                linkedBlockingQueue.put(R2_EXIT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void inputCmd(String... cmd) {
        try {
            if (linkedBlockingQueue != null) {
                for (String st : cmd) {
                    linkedBlockingQueue.put(st);
                }
                linkedBlockingQueue.put(R2_EXIT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getRadare2Result() {
        if (stringBuilder != null) {
            return stringBuilder.toString();
        }
        return "";
    }

    public void writeCmd() {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        try {
            while (true) {
                try {
                    String cmdSt = linkedBlockingQueue.take();
                    log.debug("writeCmd cmdSt: {} ", cmdSt);
                    System.out.println("writeCmd: " + cmdSt);
                    bw.write(cmdSt);
                    bw.newLine();
                    bw.flush();
                    if (cmdSt.equals("exit")) {
                        log.debug("writeCmd r2 stop ");
                        System.out.println("r2 stop ");
                        break;
                    }
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("writeCmd Exception : {} ", e.getMessage());
                }
            }
        } finally {
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
                log.error("writeCmd BufferedWriter Exception : {} ", e.getMessage());
            }
            System.out.println(Thread.currentThread().getName() + "writeCmd stop");
            log.debug("Thread.name:{} , writeCmd r2 stop  ", Thread.currentThread().getName());
        }
    }

    private void readCmd() {
        InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream(), Charset.forName("GBK"));
        try {
            //用缓冲器读行
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = null;
            //结果输出流 直到读完为止
            System.out.println("start");
            log.debug("readCmd start ");
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
                log.debug("print: {} ", line);
                if (!line.startsWith("[0x") && !line.contains("[0x")) {
                    stringBuilder.append(line);
                }
            }
            System.out.println("end");
            log.debug("readCmd end ");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("readCmd : {} ", e.getMessage());
        } finally {
            try {
                inputStreamReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            log.debug("Thread.name:{} , readCmd finally end ", Thread.currentThread().getName());
            System.out.println("结束");
            isClose = true;
        }
    }

    private void readErrorCmd() {
        InputStreamReader inputStreamReader = new InputStreamReader(process.getErrorStream(), Charset.forName("GBK"));
        try {
            //用缓冲器读行
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = null;
            //结果输出流 直到读完为止
            System.out.println("readErrorCmd start");
            log.debug("readErrorCmd start ");
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
                log.debug("print: {} ", line);
            }
            System.out.println("readErrorCmd end");
            log.debug("readErrorCmd end ");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("readErrorCmd Exception : {} ", e.getMessage());
        } finally {
            try {
                inputStreamReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            log.debug("Thread.name:{} , readErrorCmd finally end ", Thread.currentThread().getName());
            System.out.println("结束");
            isClose = true;
        }
    }

    public void putCmd() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("please input your cmd:");
            String cmd = scanner.next();
            try {
                if (linkedBlockingQueue != null) {
                    linkedBlockingQueue.put(cmd);
                    log.debug("putCmd cmdSt: {} ", cmd);
                }
                if (cmd.equals("exit")) {
                    System.out.println("now, input cmd stop yet");
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 初始化 radare2 进程
     */
    private void initRadareProcess(String filePath) {
        String cmd = "r2 ";
        try {
            log.debug("initRadareProcess filePath: {} ", filePath);
            process = Runtime.getRuntime().exec(cmd + filePath);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("initRadareProcess Exception : {} ", e.getMessage());
        }
    }

    public void closeProcess() {
        long startTime = System.currentTimeMillis();
        while (true) {
            try {
                Thread.sleep(1000 * 1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (isClose) {
                break;
            }
            if (System.currentTimeMillis() - startTime > TIME) {
                break;
            }
        }
        if (process != null) {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            try {
                bw.write("exit");
                bw.newLine();
                bw.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                ProcessHandle processHandle = process.toHandle();
                if (processHandle != null) {
                    processHandle.destroy();
                    processHandle.descendants().forEach(ProcessHandle::destroy);
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("closeProcess Exception : {} ", e.getMessage());
            }
            process.destroy();
            log.debug("Thread.name:{} , closeProcess  ", Thread.currentThread().getName());
        }
    }
}
