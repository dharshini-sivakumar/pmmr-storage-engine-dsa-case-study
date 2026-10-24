package pmmr;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import static pmmr.TestAssertions.check;
import static pmmr.TestAssertions.d;

/**
 * Tests for binary serialization and deserialization of PMMR storage files.
 */
public final class PersistenceTests {
    private PersistenceTests() {}

    public static void run() throws Exception {
        PMMR s = new PMMR();
        for (int i = 0; i < 500; i++) s.append(d(i));
        for (int i = 0; i < 500; i += 2) s.prune(i);
        s.compact();
        Path f = Files.createTempFile("pmmr", ".bin");
        s.save(f);
        PMMR l = PMMR.load(f);
        Files.delete(f);
        check(Arrays.equals(s.root(), l.root()) && s.storedNodes() == l.storedNodes(), "save/load");
        check(MerkleProof.verify(l.proof(1), d(1), l.root()), "proof after load");
        l.append(d(500));
        s.append(d(500));
        check(Arrays.equals(s.root(), l.root()), "append after load");
        System.out.println("[ok] persistence");
    }
}
