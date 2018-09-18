package org.stepic.droid.features.stories.repository

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.features.stories.model.ViewedStoryTemplate
import org.stepic.droid.storage.dao.IDao
import org.stepic.droid.web.Api
import org.stepic.droid.web.model.story_templates.StoryTemplatesResponse
import org.stepik.android.model.StoryTemplate
import javax.inject.Inject

@AppSingleton
class StoryTemplatesRepositoryImpl
@Inject
constructor(
        private val api: Api,
        private val viewedStoryTemplateDao: IDao<ViewedStoryTemplate>
) : StoryTemplatesRepository {
    override fun getStoryTemplates(): Single<List<StoryTemplate>> =
            getStoryTemplatesByPage(1)
                    .concatMap { it.storyTemplates.toObservable() }
                    .toList()
                    .map { it.sortedBy(StoryTemplate::position) }

    private fun getStoryTemplatesByPage(page: Int): Observable<StoryTemplatesResponse> =
            api.getStoryTemplates(page)
                    .concatMap {
                        val templatesObservable = Observable.just(it)
                        if (it.meta.hasNext) {
                            templatesObservable.concatWith(getStoryTemplatesByPage(it.meta.page + 1))
                        } else {
                            templatesObservable
                        }
                    }

    override fun getViewedStoriesIds(): Single<Set<Long>> = Single.create { emitter ->
        emitter.onSuccess(
                viewedStoryTemplateDao
                        .getAll()
                        .asSequence()
                        .map(ViewedStoryTemplate::storyTemplateId)
                        .toSet()
        )
    }

    override fun markStoryAsViewed(storyTemplateId: Long): Completable = Completable.fromAction {
        viewedStoryTemplateDao.insertOrReplace(ViewedStoryTemplate(storyTemplateId))
    }
}