package at.htlleonding.mutiny;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wildfly.common.Assert;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@QuarkusTest
public class MutinyScratchPad {

    Integer integerResult = -1;
    Boolean hasFailed = false;
    Boolean[] booleans = new Boolean[5];
    final Integer theAnswer = 42;

    void printWithThreadId(String msg) {
        System.out.println("Thread: " + Thread.currentThread().getName() + " " + msg);
    }

    @Test
    public void uniFromItem_subscribe_retrieveItem() {
        Uni<Integer> u = Uni.createFrom().item(() -> 1);
        var result = u.subscribe().with(item -> integerResult = item);

        Assertions.assertEquals(1, integerResult);
    }

    @Test
    public void uniFromItem_addEventReactions_retrieveItem() {
        Uni<Integer> u1 = Uni.createFrom().item(() -> 1);
        var u2 = u1.onItem().invoke(() -> booleans[0] = true);
        var u3 = u2.onSubscription().invoke(() -> booleans[1] = true);

        var result = u3.subscribe().with(item -> {
            booleans[2] = true;
            integerResult = item;
        });

        Assertions.assertEquals(1, integerResult);
        Assert.assertTrue(booleans[0]);
        Assert.assertTrue(booleans[1]);
        Assert.assertTrue(booleans[2]);
    }

    @Test
    public void uniFromItem_throwsException_onFailure() {
        Uni<Integer> u =
                Uni.createFrom().item(() -> {
                    throw new IllegalArgumentException("Boom");
                });

        var u2 = u.onFailure().recoverWithItem(f -> {
            hasFailed = true;
            return -2;
        });

        integerResult = u2.await().atMost(Duration.ofMinutes(1));

        Assert.assertTrue(hasFailed);
        Assertions.assertEquals(-2, integerResult);
    }

    private Integer produceInt() throws InterruptedException {
        printWithThreadId("produceInt");
        Thread.sleep(250);
        return 42;
    }

    @Test
    public void uniFromProducer_delayed_retrieveItem() {
        Uni<Integer> u1 = Uni.createFrom().emitter(em -> {
            try {
                em.complete(produceInt());
            } catch (InterruptedException e) {
                e.printStackTrace();
                em.fail(e);
            }
        });

        var u2 = u1.onItem().invoke(() -> booleans[0] = true);
        var u3 = u2.onSubscription().invoke(() -> booleans[1] = true);

        var startAt = System.currentTimeMillis();
        var result = u3.subscribe().with(item -> {
            booleans[2] = true;
            integerResult = item;
        });
        var finishAt = System.currentTimeMillis();

        Assertions.assertEquals(theAnswer, integerResult);
        Assert.assertTrue(booleans[0]);
        Assert.assertTrue(booleans[1]);
        Assert.assertTrue(booleans[2]);
        Assert.assertTrue((finishAt - startAt) >= 250);
    }

    @Test
    public void uniFromProducer_delayed_block_retrieveItem() {
        Uni<Integer> u1 = Uni.createFrom().emitter(em -> {
            try {
                em.complete(produceInt());
            } catch (InterruptedException e) {
                e.printStackTrace();
                em.fail(e);
            }
        });

        var u2 = u1.onItem().invoke(() -> booleans[0] = true);
        var u3 = u2.onSubscription().invoke(() -> booleans[1] = true);

        integerResult = u3.await().indefinitely();

        Assertions.assertEquals(theAnswer, integerResult);
        Assert.assertTrue(booleans[0]);
        Assert.assertTrue(booleans[1]);
    }

    ThreadFactory threadFactory = new NameableThreadFactory("EMIT_ON_THREAD");
    ExecutorService executor = Executors.newFixedThreadPool(10, threadFactory);

    @Test
    public void uniFromProducer_executeOnAnotherThread_delayed_retrieveItem() {
        //specify the work to do
        Uni<Integer> u1 = Uni.createFrom()
                .emitter(em -> {
                    try {
                        em.complete(produceInt());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        em.fail(e);
                    }
                });

        //tell it to run a subscription on another thread
        var u2 = u1
                .runSubscriptionOn(executor);

        //set up processing of item
        var u3 = u2
                .onItem().invoke(() -> {
                    booleans[0] = true;
                    printWithThreadId(".onItem()");
                })
                .onSubscription().invoke(() -> {
                    booleans[1] = true;
                    printWithThreadId(".onSubscription()");
                });

        //use it
        var startAt = System.currentTimeMillis();
        u3.subscribe().with(item -> {
            booleans[2] = true;
            integerResult = item;
            printWithThreadId(".subscribe()");
        });
        var finishAt = System.currentTimeMillis();

        //main thread is not blocked
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sum += i;
        }

        //subscription is now async!
        //main thread was not blocked
        Assert.assertTrue((finishAt - startAt) < 50);
        Assertions.assertEquals(45, sum);

        //work was done on another thread
        Assertions.assertEquals(theAnswer, integerResult);
        Assert.assertTrue(booleans[0]);
        Assert.assertTrue(booleans[1]);
        Assert.assertTrue(booleans[2]);


    }


}
