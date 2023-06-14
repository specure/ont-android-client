package at.specure.androidX.test.test_service_connector;

public interface TestServiceInterface {

    void locationChanged(Float distance);

    void newTimeToNextTest(Long time);

    void testStarted();

    void testCancelled();

    void loopModeStopped(boolean cancelledByUser);


}
