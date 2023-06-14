package at.specure.android.test;

import java.util.List;

import at.specure.androidX.data.test.TestResultView;

public interface TestResultFetcher {

    void sendTestResults(List<TestResultView> results);
}
