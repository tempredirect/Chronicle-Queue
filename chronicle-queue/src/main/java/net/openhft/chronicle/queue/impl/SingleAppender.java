package net.openhft.chronicle.queue.impl;

import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.wire.BinaryWire;
import net.openhft.chronicle.wire.Wire;
import net.openhft.chronicle.wire.WireOut;
import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.DirectStore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Created by peter.lawrey on 30/01/15.
 */
public class SingleAppender implements ExcerptAppender {

    @NotNull
    private final DirectChronicleQueue chronicle;
    @Nullable
    private final ChronicleWireOut wireOut;
    private final Bytes buffer = DirectStore.allocateLazy(128 * 1024).bytes();
    private final Wire wire = new BinaryWire(buffer);

    private long lastWrittenIndex = -1;

    public SingleAppender(ChronicleQueue chronicle) {
        this.chronicle = (DirectChronicleQueue) chronicle;
        wireOut = new ChronicleWireOut(null);
    }

    @Nullable
    @Override
    public WireOut wire() {
        return wireOut;
    }

    @Override
    public void writeDocument(@NotNull Consumer<WireOut> writer) {
        buffer.clear();
        writer.accept(wire);
        buffer.flip();
        lastWrittenIndex = chronicle.appendDocument(buffer);
    }

    /**
     * @return the last index generated by this appender
     * @throws IllegalStateException if the last index has not been set
     */
    @Override
    public long lastWrittenIndex() {
        if (lastWrittenIndex == -1) {
            String message = "No document has been written using this appender, so the " +
                    "lastWrittenIndex() is not available.";
            throw new IllegalStateException(message);
        }
        return lastWrittenIndex;
    }

    @NotNull
    @Override
    public ChronicleQueue chronicle() {
        return chronicle;
    }
}
