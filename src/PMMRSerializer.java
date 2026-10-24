package pmmr;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.BitSet;

/**
 * Handles high-performance binary serialization and deserialization for PMMR storage files.
 * Format:
 *   MAGIC (4 bytes): 0x504D4D52 ("PMMR")
 *   size (8 bytes)
 *   prunedLeaves (8 bytes)
 *   bitsetLength (4 bytes) + bitset bytes
 *   nodeCount (4 bytes)
 *   for each node: pos (8 bytes) + hash (32 bytes)
 */
public final class PMMRSerializer {
    public static final int MAGIC = 0x504D4D52; // "PMMR"

    private PMMRSerializer() {}

    public static void save(Path file, long size, long prunedLeaves, BitSet dead, INodeStore store) throws IOException {
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(file)))) {
            out.writeInt(MAGIC);
            out.writeLong(size);
            out.writeLong(prunedLeaves);
            byte[] bits = dead.toByteArray();
            out.writeInt(bits.length);
            out.write(bits);
            out.writeInt(store.count());
            for (int i = 0; i < store.count(); i++) {
                out.writeLong(store.posAt(i));
                out.write(store.hashAt(i));
            }
        }
    }

    public static void load(Path file, PMMR target) throws IOException {
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(file)))) {
            if (in.readInt() != MAGIC) throw new IOException("not a PMMR file");
            long size = in.readLong();
            long prunedLeaves = in.readLong();
            byte[] bits = new byte[in.readInt()];
            in.readFully(bits);
            BitSet dead = BitSet.valueOf(bits);
            int n = in.readInt();
            NodeStore store = new NodeStore();
            for (int i = 0; i < n; i++) {
                long p = in.readLong();
                byte[] h = new byte[Hash.SIZE];
                in.readFully(h);
                store.append(p, h);
            }
            target.restoreState(size, prunedLeaves, dead, store);
        }
    }
}
