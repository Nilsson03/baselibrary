package ru.nilsson03.library.bukkit.util;

/**
 * Перечисление поддерживаемых версий Minecraft.
 */
public enum ServerVersion {
    // Legacy versions
    v1_7(0, "1.7"),
    v1_8(1, "1.8"),
    v1_9(2, "1.9"),
    v1_10(3, "1.10"),
    v1_11(4, "1.11"),
    v1_12(5, "1.12"),

    // Modern versions
    v1_13(6, "1.13"),
    v1_14(7, "1.14"),
    v1_15(8, "1.15"),
    v1_16(9, "1.16"),
    v1_17(10, "1.17"),
    v1_18(11, "1.18"),
    v1_19(12, "1.19"),
    v1_20(13, "1.20"),
    v1_21(14, "1.21"),
    v1_22(15, "1.22"),
    v1_23(16, "1.23"),

    // Special cases
    UNKNOWN(-1, "UNKNOWN");

    private final int weight;
    private final String versionString;

    ServerVersion(int weight, String versionString) {
        this.weight = weight;
        this.versionString = versionString;
    }

    public int getWeight() {
        return weight;
    }

    public String getVersionString() {
        return versionString;
    }

    /**
     * Сравнивает текущую версию сервера с указанной.
     *
     * @param other версия для сравнения
     * @return true если текущая версия старше указанной
     * @throws IllegalArgumentException если параметр other равен null
     */
    public boolean isOlderThan(ServerVersion other) {
        return this.weight < other.weight;
    }

    /**
     * Сравнивает текущую версию сервера с указанной.
     *
     * @param other версия для сравнения
     * @return true если текущая версия старше или равна указанной
     * @throws IllegalArgumentException если параметр other равен null
     */
    public boolean isOlderOrEqual(ServerVersion other) {
        return this.weight <= other.weight;
    }

    /**
     * Сравнивает текущую версию сервера с указанной.
     *
     * @param other версия для сравнения
     * @return true если текущая версия новее указанной
     * @throws IllegalArgumentException если параметр other равен null
     */
    public boolean isNewerThan(ServerVersion other) {
        return this.weight > other.weight;
    }

    /**
     * Сравнивает текущую версию сервера с указанной.
     *
     * @param other версия для сравнения
     * @return true если текущая версия новее или равна указанной
     * @throws IllegalArgumentException если параметр other равен null
     */
    public boolean isNewerOrEqual(ServerVersion other) {
        return this.weight >= other.weight;
    }

    public boolean isEquels(ServerVersion other) {
        return this.weight == other.weight;
    }
}