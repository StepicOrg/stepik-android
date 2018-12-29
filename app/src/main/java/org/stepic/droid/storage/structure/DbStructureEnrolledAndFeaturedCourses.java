package org.stepic.droid.storage.structure;

@Deprecated
public final class DbStructureEnrolledAndFeaturedCourses {
    public static final String ENROLLED_COURSES = "courses";
    public static final String FEATURED_COURSES = "featured_courses";

    public static final class Column {
        @Deprecated
        public static final String ID = "_id";
        public static final String COURSE_ID = "course_id";
        public static final String SUMMARY = "summary";
        public static final String WORKLOAD = "workload";
        public static final String COVER_LINK = "cover";
        public static final String INTRO_LINK_VIMEO = "intro";
        public static final String COURSE_FORMAT = "course_format";
        public static final String TARGET_AUDIENCE = "target_audience";
        public static final String INSTRUCTORS = "instructors";
        public static final String REQUIREMENTS = "requirements";
        public static final String DESCRIPTION = "description";
        public static final String SECTIONS = "sections"; //'модули' in Russian
        public static final String TOTAL_UNITS = "total_units"; //'уроки' in Russian
        public static final String ENROLLMENT = "enrollment"; //'количество зарегистрированных' in Russian
        public static final String IS_FEATURED = "is_featured";
        public static final String OWNER = "owner";
        public static final String IS_CONTEST = "is_contest";
        public static final String LANGUAGE = "language";
        public static final String IS_PUBLIC = "is_public";
        public static final String TITLE = "title";
        public static final String SLUG = "slug";
        @Deprecated
        public static final String BEGIN_DATE_SOURCE = "begin_date_source";
        public static final String BEGIN_DATE = "begin_date";
        public static final String LAST_DEADLINE = "last_deadline";
        public static final String IS_CACHED = "is_cached";
        public static final String IS_LOADING = "is_loading";
        public static final String CERTIFICATE = "certificate";
        public static final String INTRO_VIDEO_ID = "intro_video_id";
        public static final String SCHEDULE_LINK = "schedule_link";
        public static final String SCHEDULE_LONG_LINK = "schedule_long_link";
        public static final String END_DATE = "end_date";
        public static final String LAST_STEP_ID = "last_step";
        public static final String IS_ACTIVE = "is_active";
        public static final String LEARNERS_COUNT = "learners_count";
        public static final String PROGRESS = "progress";
        public static final String AVERAGE_RATING = "average_rating";
        public static final String REVIEW_SUMMARY = "review_summary";
    }
}
