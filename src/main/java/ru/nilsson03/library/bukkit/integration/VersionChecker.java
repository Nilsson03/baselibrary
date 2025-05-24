package ru.nilsson03.library.bukkit.integration;

import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

/**
 * Вспомогательный класс для сравнения версий с поддержкой семантического версионирования.
 * Обрабатывает версии в форматах типа "1.2.3", "2.5.0-beta" и т.д.
 */
public final class VersionChecker {

    /**
     * Сравнивает две версии, проверяя соответствует ли текущая версия минимальным требованиям.
     *
     * @param version проверяемая версия (например "1.5.2-release")
     * @param minVersion минимально требуемая версия (например "1.4.0")
     * @return true если текущая версия больше или равна минимальной, false в противном случае
     * @throws IllegalArgumentException если версии имеют неверный формат
     */
    public static boolean isCompatible(String version, String minVersion) {
        try {
            VersionInfo versionInfo = parseVersion(version);
            VersionInfo minVersionInfo = parseVersion(minVersion);

            int comparison = compareVersionParts(versionInfo.versionParts(), minVersionInfo.versionParts());
            if (comparison != 0) {
                return comparison > 0;
            }

            return versionInfo.keywordWeight() >= minVersionInfo.keywordWeight();
        } catch (Exception e) {
            ConsoleLogger.error("baselibrary", "Versions comparison error %s and %s", version, minVersion);
            return false;
        }
    }

    /**
     * Парсит строку версии в объект VersionInfo, содержащий числовые части и вес ключевого слова.
     *
     * @param version строка версии для парсинга
     * @return VersionInfo с распарсенными данными о версии
     */
    private static VersionInfo parseVersion(String version) {
        String[] parts = version.split("-", 2);
        String versionPart = parts[0];
        String keyword = parts.length > 1 ? parts[1] : "release";

        String[] versionParts = versionPart.split("\\.");
        long[] numericParts = new long[versionParts.length];
        for (int i = 0; i < versionParts.length; i++) {
            numericParts[i] = parseVersionPart(versionParts[i]);
        }

        int keywordWeight = getKeywordWeight(keyword);

        return new VersionInfo(numericParts, keywordWeight);
    }

    /**
     * Парсит часть версии, извлекая числовое значение.
     *
     * @param part часть версии (например "1", "2b" и т.д.)
     * @return числовое значение части версии
     */
    private static long parseVersionPart(String part) {
        try {
            String numericPart = part.replaceAll("\\D", "");
            if (numericPart.isEmpty()) {
                return 0;
            }
            return Long.parseLong(numericPart);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Определяет вес ключевого слова версии.
     *
     * @param keyword ключевое слово (alpha, beta, release и т.д.)
     * @return числовой вес ключевого слова
     */
    private static int getKeywordWeight(String keyword) {
        switch (keyword.toLowerCase()) {
            case "alpha":
                return 1;
            case "beta":
                return 2;
            case "pre-release":
                return 3;
            case "release":
                return 4;
            default:
                return 4;
        }
    }

    /**
     * Сравнивает числовые части версий.
     *
     * @param version1 числовые части первой версии
     * @param version2 числовые части второй версии
     * @return положительное число если version1 > version2,
     *         отрицательное если version1 < version2,
     *         0 если версии равны
     */
    private static int compareVersionParts(long[] version1, long[] version2) {
        int maxLength = Math.max(version1.length, version2.length);
        for (int i = 0; i < maxLength; i++) {
            long part1 = i < version1.length ? version1[i] : 0;
            long part2 = i < version2.length ? version2[i] : 0;
            if (part1 < part2) {
                return -1;
            }
            if (part1 > part2) {
                return 1;
            }
        }
        return 0;
    }

    /**
     * Запись для хранения информации о версии.
     *
     * @param versionParts числовые части версии
     * @param keywordWeight вес ключевого слова
     */
    private record VersionInfo(long[] versionParts, int keywordWeight) {
    }
}
