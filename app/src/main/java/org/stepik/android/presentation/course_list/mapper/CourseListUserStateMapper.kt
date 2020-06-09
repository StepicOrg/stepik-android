package org.stepik.android.presentation.course_list.mapper

import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.util.PagedList
import org.stepic.droid.util.plus
import org.stepic.droid.util.mapPaged
import org.stepic.droid.util.mapToLongArray
import org.stepic.droid.util.mutate
import org.stepic.droid.util.slice
import org.stepic.droid.util.filterNot
import org.stepic.droid.util.insert
import org.stepic.droid.util.transform
import org.stepik.android.domain.course_list.model.CourseListItem
import org.stepik.android.domain.course_list.model.UserCourseQuery
import org.stepik.android.domain.course_list.model.UserCoursesLoaded
import org.stepik.android.domain.user_courses.model.UserCourse
import org.stepik.android.presentation.course_list.CourseListUserView
import org.stepik.android.presentation.course_list.CourseListView
import java.util.Date
import javax.inject.Inject

class CourseListUserStateMapper
@Inject
constructor(
    private val courseListStateMapper: CourseListStateMapper
) {
    companion object {
        private const val PAGE_SIZE = 20
    }

    /**
     * Fetch courses
     */
    fun mapToFetchCoursesSuccess(state: CourseListUserView.State, items: PagedList<CourseListItem.Data>, isNewItems: Boolean): CourseListUserView.State {
        val courseListViewState = (state as? CourseListUserView.State.Data)
            ?.courseListViewState
            ?: return state

        val newCourseListViewState =
            // todo перенести в CourseListStateMapper и абстрагироваться от user courses
            when (courseListViewState) {
                /**
                 * Empty может быть еще когда пользователь отписался от курса или переместил в архив
                 */
                CourseListView.State.Loading,
                CourseListView.State.Empty,
                CourseListView.State.NetworkError ->
                    if (items.isNotEmpty() && state.userCourses.isNotEmpty()) {
                        val pagedItems = PagedList(items, page = items.page, hasNext = items.size < state.userCourses.size)
                        CourseListView.State.Content(pagedItems, pagedItems)
                    } else {
                        CourseListView.State.Empty
                    }

                is CourseListView.State.Content -> {
                    val itemsMap = items.associateBy { it.course.id }

                    /**
                     * в случае next page надо учесть
                     * перемещенные элементы
                     * load more placeholder
                     *
                     * как отличить next page от подгрузки элементов c remote? - нужен параметр для отличия этих кейсов - [isNewItems]
                     */
                    if (isNewItems) {
                        // [startIndex] - индекс последнего (для которого есть CourseListItem.Data) элемента в user courses
                        val startIndex = courseListViewState
                            .courseListItems
                            // проверяем, что последний элемент placeholder пагинации
                            .takeIf { (it.lastOrNull() as? CourseListItem.PlaceHolder)?.courseId == -1L }
                            ?.findLast { it is CourseListItem.Data || it is CourseListItem.PlaceHolder && it.courseId != -1L }
                            ?.let {
                                when (it) {
                                    is CourseListItem.Data ->
                                        it.course.id

                                    is CourseListItem.PlaceHolder ->
                                        it.courseId
                                }
                            }
                            ?.let { lastCourseId ->
                                state.userCourses.indexOfFirst { it.course == lastCourseId }
                            }
                            ?.takeIf { it >= 0 }

                        if (startIndex != null) {
                            // новый хвост из Data элементов в порядке id в user courses
                            val newDataItems: List<CourseListItem.Data> = state
                                .userCourses
                                .slice(from = startIndex + 1)
                                .mapNotNull { itemsMap[it.course] }

                            courseListViewState
                                .copy(
                                    courseListDataItems = courseListViewState.courseListDataItems + PagedList(newDataItems, hasNext = startIndex + 1 < state.userCourses.size),
                                    courseListItems = courseListViewState.courseListItems.slice(to = courseListViewState.courseListItems.size - 1) + newDataItems
                                )
                        } else {
                            courseListViewState
                        }
                    } else {
                        // тут мы просто обновляем элементы новыми с REMOTE порядок должен быть и так верным
                        courseListStateMapper.mergeWithUpdatedItems(courseListViewState, itemsMap)
                    }
                }

                else ->
                    courseListViewState
            }

        return state.copy(courseListViewState = newCourseListViewState)
    }

    fun mapToFetchCoursesError(state: CourseListUserView.State): CourseListUserView.State {
        val courseListViewState = (state as? CourseListUserView.State.Data)
            ?.courseListViewState
            ?: return state

        val newCourseListViewState =
            when (courseListViewState) {
                CourseListView.State.Loading,
                CourseListView.State.Empty,
                CourseListView.State.NetworkError ->
                    CourseListView.State.NetworkError

                is CourseListView.State.Content ->
                    courseListViewState
                        .copy(
                            courseListItems = courseListViewState.courseListItems.dropLastWhile { it is CourseListItem.PlaceHolder && it.courseId == -1L }
                        )

                else ->
                    courseListViewState
            }

        return state.copy(courseListViewState = newCourseListViewState)
    }

    fun getNextPageCourseIds(userCourses: List<UserCourse>, courseListViewState: CourseListView.State.Content): LongArray? {
        if (!courseListViewState.courseListDataItems.hasNext ||
            (courseListViewState.courseListItems.last() as? CourseListItem.PlaceHolder)?.courseId == -1L) {
            return null
        }

        val offset = courseListViewState.courseListItems.size

        return userCourses
            .slice(offset, offset + PAGE_SIZE)
            .mapToLongArray(UserCourse::course)
    }

    /**
     * Continue course
     */
    fun mergeWithCourseContinue(state: CourseListUserView.State, courseId: Long): Pair<CourseListUserView.State, Boolean> {
        val userCourses = (state as? CourseListUserView.State.Data)
            ?.userCourses
            ?: return state to false

        val index = userCourses.indexOfFirst { it.course == courseId }
        var isNeedLoadCourse = false
        return if (index > 0) { // if index == 0 we do not need to update state
            val newUserCourses = userCourses.mutate { add(0, removeAt(index).copy(lastViewed = Date(DateTimeHelper.nowUtc()))) }

            val newCourseListViewState =
                with(state.courseListViewState) {
                    if (this is CourseListView.State.Content) {
                        val courseListDataIndex =
                            courseListDataItems.indexOfFirst { it.course.id == courseId }

                        val newCourseListDataItems =
                            if (courseListDataIndex > 0) {
                                PagedList(courseListDataItems.mutate { add(0, removeAt(courseListDataIndex)) }, hasNext = courseListDataItems.hasNext)
                            } else {
                                courseListDataItems
                            }

                        val newCourseListItems =
                            if (index in courseListItems.indices) {
                                courseListItems.mutate { add(0, removeAt(index)) }
                            } else {
                                isNeedLoadCourse = true
                                listOf(CourseListItem.PlaceHolder(courseId)) + courseListItems
                            }

                        copy(newCourseListDataItems, newCourseListItems)
                    } else {
                        this
                    }
                }

            state.copy(userCourses = newUserCourses, courseListViewState = newCourseListViewState) to isNeedLoadCourse
        } else {
            state to isNeedLoadCourse
        }
    }

    /**
     * Enrollments
     */
    fun mergeWithEnrolledCourse(state: CourseListUserView.State, courseId: Long): Pair<CourseListUserView.State, Boolean> =
        if (state is CourseListUserView.State.Data &&
            with(state.userCourseQuery) { isFavorite != true && isArchived != true } // in this case course do not match userCourseQuery
        ) {
            addUserCourseToTop(state, UserCourse(user = 0, course = courseId, lastViewed = Date(DateTimeHelper.nowUtc())))
        } else {
            state to false
        }

    fun mergeWithRemovedCourse(state: CourseListUserView.State, courseId: Long): CourseListUserView.State =
        if (state is CourseListUserView.State.Data) {
            val userCourses = state.userCourses.filterNot { it.course == courseId }

            val courseListViewState =
                when (state.courseListViewState) {
                    is CourseListView.State.Content ->
                        if (userCourses.isEmpty()) {
                            CourseListView.State.Empty
                        } else {
                            state.courseListViewState.copy(
                                state.courseListViewState.courseListDataItems
                                    .filterNot { it.course.id == courseId },

                                state.courseListViewState.courseListItems
                                    .filterNot {
                                        it is CourseListItem.Data && it.course.id == courseId ||
                                        it is CourseListItem.PlaceHolder && it.courseId == courseId
                                    }
                            )
                        }

                    else ->
                        state.courseListViewState
                }

            state.copy(userCourses = userCourses, courseListViewState = courseListViewState)
        } else {
            state
        }

    /**
     * Course placeholders
     */
    fun mergeWithPlaceholderSuccess(state: CourseListUserView.State, courseListItem: CourseListItem.Data): CourseListUserView.State =
        if (state is CourseListUserView.State.Data &&
            state.courseListViewState is CourseListView.State.Content) {
            val listState = state.courseListViewState
            /**
             * Нам нужно вставить в courseListDataItems, но так как courseListDataItems не содержит плейсхолдеров
             * то нам нужно понять куда вставить в courseListDataItems.
             * Для этого мы находим предшесвующий вставляемуму элемент и вставляем после него.
             */
            var previousCourseId = -1L

            val courseListItems = listState
                .courseListItems
                .map { item ->
                    if (item is CourseListItem.PlaceHolder && item.courseId == courseListItem.course.id) {
                        courseListItem
                    } else {
                        if (item is CourseListItem.Data) {
                            previousCourseId = item.course.id
                        }
                        item
                    }
                }

            val indexOfPreviousCourseItem =
                if (previousCourseId == -1L) {
                    -1
                } else {
                    listState.courseListDataItems.indexOfFirst { it.course.id == previousCourseId }
                }

            val courseListDataItems =
                if (indexOfPreviousCourseItem == -1) {
                    listState.courseListDataItems
                } else {
                    listState.courseListDataItems.transform { insert(indexOfPreviousCourseItem + 1, courseListItem) }
                }

            state.copy(courseListViewState = listState.copy(courseListDataItems, courseListItems))
        } else {
            state
        }

    fun mergeWithUserCourse(state: CourseListUserView.State, userCourse: UserCourse): Pair<CourseListUserView.State, Boolean> =
        if (state is CourseListUserView.State.Data) {
            if (userCourse.isMatchQuery(state.userCourseQuery)) {
                /*
                - понять куда его вставить
                - обновить user courses


                Варианты:
                1) Помещать элементы в начало списка
                   - нужно при паблишинге сортировать по дате
                   - при обновлении списка элемент уедет вниз
                2) При вхождении в видимую область помещать элемент на свое место, иначе в конец списка
                   - проблемы аналогично верхним (но так как обычно не больше 2х страниц, то скорее всего у большинства всё будет ок)
                   - если мы помещаем элемент в конец списка, то нарушается порядок и потом непонятно куда класть следующий
                3) Помещать элемент на свое место и подгружать при попадании в область видимости
                   - элемент на своем месте
                   - нарушается предикат, что user courses 1 к 1 соотвествуют courseListItems, так как теперь могут быть разрывы

                   победил вариант 1, так как все варианты с trade off, но первый проще всего сделать
                 */

                if (state.userCourses.any { it.course == userCourse.course }) { // если такой уже есть, то ничего не трогаем
                    state to false
                } else {
                    addUserCourseToTop(state, userCourse)
                }
            } else {
                mergeWithRemovedCourse(state, userCourse.course) to false
            }
        } else {
            state to false
        }

    private fun addUserCourseToTop(state: CourseListUserView.State.Data, userCourse: UserCourse): Pair<CourseListUserView.State, Boolean> {
        val userCourseWrapper = listOf(userCourse)
        val coursePlaceholder = listOf(CourseListItem.PlaceHolder(userCourse.course))
        val isNeedLoadCourse =
            state.courseListViewState == CourseListView.State.Empty ||
                    state.courseListViewState is CourseListView.State.Content

        val (userCourses, courseListViewState) =
            when (state.courseListViewState) {
                CourseListView.State.Empty ->
                    userCourseWrapper to CourseListView.State.Content(PagedList(emptyList()), coursePlaceholder)

                is CourseListView.State.Content ->
                    Pair(
                        userCourseWrapper + state.userCourses,
                        state.courseListViewState.copy(courseListItems = coursePlaceholder + state.courseListViewState.courseListItems)
                    )

                else ->
                    state.userCourses to state.courseListViewState
            }

        return state.copy(userCourses = userCourses, courseListViewState = courseListViewState) to isNeedLoadCourse
    }

    fun getLatestCourseToPublish(state: CourseListUserView.State.Data): UserCoursesLoaded =
        if (state.courseListViewState is CourseListView.State.Content) {
            val userCoursesMap =
                state.userCourses.associateBy(UserCourse::course)

            state.courseListViewState.courseListDataItems
                .maxBy { userCoursesMap[it.course.id]?.time ?: 0L }
                ?.let(UserCoursesLoaded::FirstCourse)
                ?: UserCoursesLoaded.Empty
        } else {
            UserCoursesLoaded.Empty
        }

    private val UserCourse.time: Long
        get() = lastViewed?.time ?: 0

    private fun UserCourse.isMatchQuery(userCourseQuery: UserCourseQuery): Boolean =
        (userCourseQuery.isFavorite == null || userCourseQuery.isFavorite == this.isFavorite) &&
        (userCourseQuery.isArchived == null || userCourseQuery.isArchived == this.isArchived)
}