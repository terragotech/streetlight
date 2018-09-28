package com.terragoedge;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Test {
    private static ExecutorService emailExecutor = Executors.newFixedThreadPool(3);

    public static void main(String[] args) {
        getData();
    }

    private static void getData() {
        List<Future<String>> futures = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            final Future<String> future = emailExecutor.submit(new MyInfoCallable(i));
            futures.add(future);
        }
        for (Future<String> f : futures) {
            try {
                System.out.println(f.get());
            } catch (InterruptedException | ExecutionException ex) {
            }
        }
    }

    public static String getMYInfo(int i) {
        String somevav = "success";
        if (i == 0) {
            somevav = "success";
        } else {
            somevav = "fail";
        }
        return somevav;
    }

    private static class MyInfoCallable implements Callable<String> {

        int i;

        public MyInfoCallable(int i) {
            this.i = i;
        }

        @Override
        public String call() throws Exception {
            return getMYInfo(i);
        }

    }
}
