package ru.nilsson03.library.bukkit.entity;

/** Immutable options controlling a {@link ManagedEntity}. */
public final class ManagedEntityOptions {
    public final boolean persistent;
    public final boolean removeWhenFarAway;
    public final boolean forceLoadChunk;
    public final boolean respawnIfMissing;
    public final long monitorInterval;
    public final boolean debug;

    public ManagedEntityOptions() {
        this(true, false, true, true, 100L, false);
    }

    public ManagedEntityOptions(boolean persistent, boolean removeWhenFarAway,
                                boolean forceLoadChunk, boolean respawnIfMissing,
                                long monitorInterval, boolean debug) {
        this.persistent = persistent;
        this.removeWhenFarAway = removeWhenFarAway;
        this.forceLoadChunk = forceLoadChunk;
        this.respawnIfMissing = respawnIfMissing;
        this.monitorInterval = monitorInterval;
        this.debug = debug;
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private boolean persistent = true;
        private boolean removeWhenFarAway = false;
        private boolean forceLoadChunk = true;
        private boolean respawnIfMissing = true;
        private long monitorInterval = 100L;
        private boolean debug = false;

        public Builder persistent(boolean value) { persistent = value; return this; }
        public Builder removeWhenFarAway(boolean value) { removeWhenFarAway = value; return this; }
        public Builder forceLoadChunk(boolean value) { forceLoadChunk = value; return this; }
        public Builder respawnIfMissing(boolean value) { respawnIfMissing = value; return this; }
        public Builder monitorInterval(long value) { monitorInterval = value; return this; }
        public Builder debug(boolean value) { debug = value; return this; }

        public ManagedEntityOptions build() {
            return new ManagedEntityOptions(persistent, removeWhenFarAway, forceLoadChunk,
                    respawnIfMissing, monitorInterval, debug);
        }
    }
}
