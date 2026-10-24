package pmmr;

/**
 * Common base interface for all cryptographic proofs in the PMMR system.
 */
public interface Proof {

    /**
     * The MMR total node size at which the proof was generated.
     *
     * @return 1-indexed MMR size
     */
    long getMmrSize();
}
