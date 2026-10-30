package dev.conc.p2;

/** Thrown by EventPipeline.run when the consumer's sink failed; the cause is the sink's exception. */
public final class PipelineFailedException extends RuntimeException {
    public PipelineFailedException(Throwable cause) {
        super(cause);
    }
}
