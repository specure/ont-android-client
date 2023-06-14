package at.specure.androidX.test.test_service_connector;

import at.specure.android.test.TestService;

public interface TestServiceConnectorInterface {

    void onTestServiceBound(TestService testService);

    void onTestServiceUnbound();

    void testStarted();

    void testCancelled();

    void loopModeStopped(boolean cancelledByUser);

    void onDistanceFromPreviousTestChanged(Float distance);
}
