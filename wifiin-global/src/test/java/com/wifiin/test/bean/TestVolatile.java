package com.wifiin.test.bean;

import java.util.concurrent.CountDownLatch;

public class TestVolatile extends Thread {
    /*A*/
//    public volatile boolean runFlag = true;
    public boolean runFlag = true;

    public boolean isRunFlag() {
        return runFlag;
    }

    public void setRunFlag(boolean runFlag) {
        this.runFlag = runFlag;
    }

    @Override
    public void run() {
        System.out.println("进入run");
        while (isRunFlag()) {
            /*B*/
//            System.out.println("running");
        }
        System.out.println("退出run");
    }

    public static void main(String[] args) throws InterruptedException {
        TestVolatile testVolatile = new TestVolatile();
        testVolatile.start();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        testVolatile.setRunFlag(false);
        System.out.println("main already set runflag to false");
        new CountDownLatch(1).await();
    }
}

