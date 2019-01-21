/*
 * Copyright (c) 2017.
 *
 * This file is part of QA App.
 *
 *  Health Network QIS App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Health Network QIS App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.domain.usecase;

import static org.eyeseetea.malariacare.domain.usecase.CallbackInvoked.invokedInProgressCallback;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.eyeseetea.malariacare.data.database.iomodules.dhis.exporter.PushController;
import org.eyeseetea.malariacare.data.remote.api.ServerInfoDataSource;
import org.eyeseetea.malariacare.data.repositories.ServerInfoRepository;
import org.eyeseetea.malariacare.domain.boundary.IPushController;
import org.eyeseetea.malariacare.domain.boundary.executors.IAsyncExecutor;
import org.eyeseetea.malariacare.domain.boundary.executors.IMainExecutor;
import org.eyeseetea.malariacare.domain.entity.Credentials;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.MockitoRule;

public class PushUseCaseTest {
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    IPushController mPushController;

    @Test
    public void should_invoke_in_progress_error_callback_when_is_in_progress() {
        givenThereIsAInProgressPushController();

        IMainExecutor mainExecutor = new IMainExecutor() {
            @Override
            public void run(Runnable runnable) {
                runnable.run();
            }
        };
        IAsyncExecutor asyncExecutor = new IAsyncExecutor() {
            @Override
            public void run(Runnable runnable) {
                runnable.run();
            }
        };
        Credentials credentials = new Credentials("", "", "");
        ServerInfoRepository serverInfoRepository = new ServerInfoRepository(new ServerInfoDataSource(credentials));
        PushUseCase pushUseCase = new PushUseCase(mPushController, mainExecutor, asyncExecutor, serverInfoRepository);

        pushUseCase.execute(credentials, 0, new PushUseCase.Callback() {

            @Override
            public void onComplete(PushController.Kind kind) {
                callbackInvoked(false);
            }

            @Override
            public void onPushError() {
                callbackInvoked(false);
            }

            @Override
            public void onPushInProgressError() {
                callbackInvoked(true);
            }

            @Override
            public void onSurveysNotFoundError() {
                callbackInvoked(false);
            }

            @Override
            public void onInformativeError(String message) {
                callbackInvoked(false);
            }

            @Override
            public void onConversionError() {
                callbackInvoked(false);
            }

            @Override
            public void onNetworkError() {
                callbackInvoked(false);
            }

            @Override
            public void onServerVersionError() {
                callbackInvoked(false);
            }
        });

        assertThat(invokedInProgressCallback, is(true));
    }

    private void callbackInvoked(boolean inProgressCallback) {
        invokedInProgressCallback = inProgressCallback;
    }

    private void givenThereIsAInProgressPushController() {
        when(mPushController.isPushInProgress()).thenReturn(true);
    }


}
class CallbackInvoked{
    public static boolean invokedInProgressCallback;
}

