package org.stepic.droid.core.presenters;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.stepic.droid.core.presenters.contracts.UnitsLearningProgressView;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class UnitsLearningProgressPresenterTest {

    private UnitsLearningProgressPresenter unitsLearningProgressPresenter;

    @Mock
    UnitsLearningProgressView unitsView;


    @Before
    public void beforeEachTest() {
        MockitoAnnotations.initMocks(this);

        unitsLearningProgressPresenter = new UnitsLearningProgressPresenter();
    }

    @Test
    public void onProgressUpdated_attached_post() {
        long unitId = 1L;
        double newScore = 2.0;

        unitsLearningProgressPresenter.attachView(unitsView);
        unitsLearningProgressPresenter.onScoreUpdated(unitId, newScore);
        unitsLearningProgressPresenter.detachView(unitsView);

        verify(unitsView).setNewScore(unitId, newScore);
    }

    @Test
    public void onProgressUpdated_afterDetaching_notPost() {
        long unitId = 1L;
        double newScore = 2.0;

        unitsLearningProgressPresenter.attachView(unitsView);
        unitsLearningProgressPresenter.detachView(unitsView);
        unitsLearningProgressPresenter.onScoreUpdated(unitId, newScore);

        verify(unitsView, never()).setNewScore(any(Long.class), any(Double.class));
    }


    @Test
    public void onProgressUpdated_beforeAttaching_notPost() {
        long unitId = 1L;
        double newScore = 2.0;

        unitsLearningProgressPresenter.onScoreUpdated(unitId, newScore);
        unitsLearningProgressPresenter.attachView(unitsView);
        unitsLearningProgressPresenter.detachView(unitsView);

        verify(unitsView, never()).setNewScore(any(Long.class), any(Double.class));
    }

    @Test
    public void onUnitPassed_attached_post() {
        long unitId = 1L;

        unitsLearningProgressPresenter.attachView(unitsView);
        unitsLearningProgressPresenter.onUnitPassed(unitId);
        unitsLearningProgressPresenter.detachView(unitsView);

        verify(unitsView).setUnitPassed(unitId);
    }

    @Test
    public void onUnitPassed_afterDetaching_notPost() {
        long unitId = 1L;

        unitsLearningProgressPresenter.attachView(unitsView);
        unitsLearningProgressPresenter.detachView(unitsView);
        unitsLearningProgressPresenter.onUnitPassed(unitId);

        verify(unitsView, never()).setUnitPassed(any(Long.class));
    }


    @Test
    public void onUnitPassed_beforeAttaching_notPost() {
        long unitId = 1L;

        unitsLearningProgressPresenter.onUnitPassed(unitId);
        unitsLearningProgressPresenter.attachView(unitsView);
        unitsLearningProgressPresenter.detachView(unitsView);

        verify(unitsView, never()).setUnitPassed(any(Long.class));
    }

}
