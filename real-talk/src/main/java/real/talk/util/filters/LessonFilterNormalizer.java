package real.talk.util.filters;


import real.talk.model.dto.lesson.LessonFilter;

public final class LessonFilterNormalizer {
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 50;
    private static final String DEFAULT_SORT = "-createdAt";

    public static LessonFilter normalize(LessonFilter f) {
        if (f == null) return null;

        Integer page = (f.getPage() == null || f.getPage() < 0) ? DEFAULT_PAGE : f.getPage();

        Integer size;
        if (f.getSize() == null) size = DEFAULT_SIZE;
        else if (f.getSize() < MIN_SIZE) size = DEFAULT_SIZE;
        else size = Math.min(f.getSize(), MAX_SIZE);

        String sort = (f.getSort() == null || f.getSort().isBlank()) ? DEFAULT_SORT : f.getSort().trim();

        // language=ALL трактуем как отсутствие фильтра
        String language = normalizeAllToNull(f.getLanguage());
        String email = normalizeAllToNull(f.getEmail());

        // whitelist сортировки: language | lesson_topic | createdAt (+ опц. префикс '-')
        if (!isAllowedSort(sort)) {
            sort = DEFAULT_SORT;
        }

        return f.toBuilder()
                .page(page)
                .size(size)
                .sort(sort)
                .language(language)
                .email(email)
                .build();
    }

    private static String normalizeAllToNull(String s) {
        if (s == null) return null;
        String v = s.trim();
        if (v.isEmpty()) return null;
        return "ALL".equalsIgnoreCase(v) ? null : v;
    }

    private static boolean isAllowedSort(String sort) {
        String token = sort.startsWith("-") || sort.startsWith("+") ? sort.substring(1) : sort;
        return token.equals("language") || token.equals("lesson_topic") || token.equals("createdAt");
    }
}