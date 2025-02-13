package or.kr.formulate.korail.util;

import or.kr.formulate.korail.dummy.timeout.CallableList;
import or.kr.formulate.korail.dummy.timeout.CallableTimeout;
import or.kr.formulate.korail.dummy.timeout.RunnableTimeout;
import or.kr.formulate.korail.dummy.timeout.TimeoutExam;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class TimeoutExamTest {
    @Test
    public void test() {
        CallableTimeout task = new CallableTimeout();
        TimeoutExam<CallableTimeout> exam = new TimeoutExam<>(task);
        exam.invoke();

        CallableList list = new CallableList();
        TimeoutExam<CallableList> exam2 = new TimeoutExam<>(list);
        exam2.invoke();

        RunnableTimeout r1 = new RunnableTimeout();
        TimeoutExam<RunnableTimeout> exam3 = new TimeoutExam<>(r1);
        exam3.invoke();

    }

    @Test
    public void test2() {

        CallableTimeout task = new CallableTimeout();
        CallableList list = new CallableList();
        RunnableTimeout r1 = new RunnableTimeout();



    }

    @Test
    void runAsync() throws ExecutionException, InterruptedException {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            System.out.println("1Thread: " + Thread.currentThread().getName());
        });

        future.get();
        System.out.println("2Thread: " + Thread.currentThread().getName());
    }

    @Test
    void supplyAsync() throws ExecutionException, InterruptedException {

        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            return "1Thread: " + Thread.currentThread().getName();
        });

        System.out.println(future.get());
        System.out.println("2Thread: " + Thread.currentThread().getName());
    }

    @Test
    void supplyAsyncAnotherThreadPool() throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            return "Thread: " + Thread.currentThread().getName();
        }, executorService);

        System.out.println(future.get());
        System.out.println("Thread: " + Thread.currentThread().getName());

        executorService.shutdown();
    }

    @Test
    void thenCompose() throws ExecutionException, InterruptedException {
        CompletableFuture<String> hello = CompletableFuture.supplyAsync(() -> {
            return "Hello";
        });

        // Future 간에 연관 관계가 있는 경우
        CompletableFuture<String> future = hello.thenCompose(this::mangKyu);
        System.out.println(future.get());
    }

    private CompletableFuture<String> mangKyu(String message) {
        return CompletableFuture.supplyAsync(() -> {
            return message + " " + "MangKyu";
        });
    }

    @Test
    void thenCombine() throws ExecutionException, InterruptedException {
        CompletableFuture<String> hello = CompletableFuture.supplyAsync(() -> {
            return "Hello";
        });

        CompletableFuture<String> mangKyu = CompletableFuture.supplyAsync(() -> {
            return "MangKyu";
        });

        CompletableFuture<String> future = hello.thenCombine(mangKyu, (h, w) -> h + " " + w);
        System.out.println(future.get());
    }

    @Test
    void allOf() throws ExecutionException, InterruptedException {
        CompletableFuture<String> hello = CompletableFuture.supplyAsync(() -> {
            return "Hello";
        });

        CompletableFuture<String> mangKyu = CompletableFuture.supplyAsync(() -> {
            return "MangKyu";
        });

//        List<CompletableFuture<String>> futures = List.of(hello, mangKyu);
        List<CompletableFuture<String>> futures = Arrays.asList(hello, mangKyu);

        CompletableFuture<List<String>> result = CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]))
                .thenApply(v -> futures.stream().
                        map(CompletableFuture::join).
                        collect(Collectors.toList()));

        List<String> stringList = result.get();
        stringList.forEach(System.out::println);

    }

    @Test
    void anyOf() throws ExecutionException, InterruptedException {
        CompletableFuture<String> hello = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            return "Hello";
        });

        CompletableFuture<String> mangKyu = CompletableFuture.supplyAsync(() -> {
            return "MangKyu";
        });

        CompletableFuture<Void> future = CompletableFuture.anyOf(hello, mangKyu).thenAccept(System.out::println);
        future.get();
    }

    @ParameterizedTest
    @ValueSource(booleans =  {true, false})
    void exceptionally(boolean doThrow) throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            if (doThrow) {
                throw new IllegalArgumentException("Invalid Argument");
            }

            return "Thread: " + Thread.currentThread().getName();
        }).exceptionally(e -> {
            return e.getMessage();
        });

        System.out.println(future.get());
    }

    @ParameterizedTest
    @ValueSource(booleans =  {true, false})
    void handle(boolean doThrow) throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            if (doThrow) {
                throw new IllegalArgumentException("Invalid Argument");
            }

            return "Thread: " + Thread.currentThread().getName();
        }).handle((result, e) -> {
            return e == null
                    ? result
                    : e.getMessage();
        });

        System.out.println(future.get());
    }

    @Test
    void thenApply() throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            return "Thread: " + Thread.currentThread().getName();
        }).thenApply(String::toUpperCase);

        System.out.println(future.get());
    }

    @Test
    void thenAccept() throws ExecutionException, InterruptedException {
        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
            return "Thread: " + Thread.currentThread().getName();
        }).thenAccept(s -> {
            System.out.println(s.toUpperCase());
        });

        future.get();
    }


    @Test
    void thenRun() throws ExecutionException, InterruptedException {
        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
            return "Thread: " + Thread.currentThread().getName();
        }).thenRun(() -> {
            System.out.println("Thread: " + Thread.currentThread().getName());
        });

        future.get();
    }
}
