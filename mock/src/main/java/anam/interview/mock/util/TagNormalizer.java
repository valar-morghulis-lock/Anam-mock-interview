package anam.interview.mock.util;

public final class TagNormalizer {

    private TagNormalizer() {}

    public static String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed.toLowerCase();
    }
}