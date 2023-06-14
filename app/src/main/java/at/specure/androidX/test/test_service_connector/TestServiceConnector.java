package at.specure.androidX.test.test_service_connector;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import at.specure.android.test.TestService;
import timber.log.Timber;

/**
 * Communication from service to implementator of testServiceInterface - in this case to controller
 */
public class TestServiceConnector implements ServiceConnection, TestServiceInterface {

    private TestServiceConnectorInterface testServiceConnectorInterface;
    private TestService testService;

    public TestServiceConnector(TestServiceConnectorInterface testServiceConnectorInterface) {
        this.testServiceConnectorInterface = testServiceConnectorInterface;
    }

    public void stopTest() {
        if (testService != null) {
            testService.stopTest();
        }
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Timber.e("TEST SERVICE CONNECTED");
        final TestService.RMBTBinder binder = (TestService.RMBTBinder) service;
        binder.setTestServiceInterface(this);
        if (testServiceConnectorInterface != null) {
            testService = binder.getService();
            if (testService != null) {
                testServiceConnectorInterface.onTestServiceBound(testService);
                if (testService.isLoopModeRunning() || testService.isTestRunning()) {
                    testServiceConnectorInterface.testStarted();
                }
            }
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Timber.e("TEST SERVICE DISCONNECTED");
    }

    @Override
    public void locationChanged(Float distance) {
        Timber.e("Next test location changed in connector");
        this.testServiceConnectorInterface.onDistanceFromPreviousTestChanged(distance);
    }

    @Override
    public void newTimeToNextTest(Long time) {

    }

    @Override
    public void testStarted() {
        Timber.e("TESST STARTED");
        this.testServiceConnectorInterface.testStarted();
    }

    @Override
    public void testCancelled() {
        this.testServiceConnectorInterface.testCancelled();
    }

    @Override
    public void loopModeStopped(boolean cancelledByUser) {
        this.testServiceConnectorInterface.loopModeStopped(cancelledByUser);
    }
}
