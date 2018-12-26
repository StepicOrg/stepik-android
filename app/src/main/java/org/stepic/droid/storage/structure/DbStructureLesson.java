package org.stepic.droid.storage.structure;

@Deprecated
public final class DbStructureLesson {
    public static final String LESSONS = "lessons";

    public static final class Column {
        @Deprecated
        public static final String ID = "_id";
        public static final String LESSON_ID = "lesson_id";
        public static final String STEPS = "steps_arr";
        public static final String IS_FEATURED = "is_featured";
        public static final String IS_PRIME = "is_prime";
        public static final String PROGRESS = "progress";
        public static final String OWNER = "owner";
        public static final String SUBSCRIPTIONS = "subscriptions";
        public static final String VIEWED_BY = "viewed_by";
        public static final String PASSED_BY = "passed_by";
        public static final String DEPENDENCIES = "dependencies";
        public static final String IS_PUBLIC = "is_public";
        public static final String TITLE = "title";
        public static final String SLUG = "slug";
        public static final String CREATE_DATE = "create_date";
        public static final String UPDATE_DATE = "update_date";
        public static final String LEARNERS_GROUP = "learners_group";
        public static final String TEACHER_GROUP = "teacher_group";
        public static final String IS_CACHED = "is_cached";
        public static final String IS_LOADING = "is_loading";
        public static final String COVER_URL = "cover_url";
        public static final String VOTE_DELTA = "vote_delta";

    }

}
