package pmmr;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import static pmmr.TestAssertions.check;
import static pmmr.TestAssertions.d;

/**
 * Multithreaded concurrency stress tests validating thread-safety of PMMR under high-load.
 */
public final class ConcurrencyTests {
    private ConcurrencyTests() {}

    public static void run() throws Exception {
        PMMR concurrentTree = new PMMR();
        for (int i = 0; i < 500; i++) concurrentTree.append(d(i));

        int threadCount = 8;
        ExecutorService exec = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Future<Boolean>> futures = new ArrayList<>();

        for (int tId = 0; tId < threadCount; tId++) {
            final int id = tId;
            futures.add(exec.submit(() -> {
                try {
                    for (int step = 0; step < 100; step++) {
                        if (id == 0) {
                            concurrentTree.append(d(500 + step));
                        } else if (id == 1) {
                            concurrentTree.prune(step * 2);
                        } else {
                            byte[] rt = concurrentTree.root();
                            check(rt != null && rt.length == 32, "concurrent root valid");
                            concurrentTree.isUnspent(10);
                            concurrentTree.stats();
                        }
                    }
                    return true;
                } finally {
                    latch.countDown();
                }
            }));
        }

        check(latch.await(10, TimeUnit.SECONDS), "concurrent test completed without deadlock");
        for (Future<Boolean> fut : futures) check(fut.get(), "thread completed cleanly");
        exec.shutdown();
        AuditReport postConcurrentAudit = concurrentTree.validateIntegrity();
        check(postConcurrentAudit.isValid(), "tree valid after concurrent access");
        System.out.println("[ok] multithreaded concurrency stress test");
    }
}
