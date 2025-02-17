/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.plugins.detectors;

import static ai.startree.thirdeye.spi.Constants.COL_VALUE;
import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.dataframe.BooleanSeries;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.DoubleSeries;
import ai.startree.thirdeye.spi.dataframe.LongSeries;
import ai.startree.thirdeye.spi.detection.AnomalyDetector;
import ai.startree.thirdeye.spi.detection.AnomalyDetectorResult;
import ai.startree.thirdeye.spi.detection.DetectorException;
import ai.startree.thirdeye.spi.detection.v2.DataTable;
import ai.startree.thirdeye.spi.detection.v2.SimpleDataTable;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.testng.annotations.Test;

public class PercentageChangeRuleDetectorTest {

  private static final long JANUARY_1_2021 = 1609459200000L;
  private static final long JANUARY_2_2021 = 1609545600000L;
  private static final long JANUARY_3_2021 = 1609632000000L;
  private static final long JANUARY_4_2021 = 1609718400000L;
  private static final long JANUARY_5_2021 = 1609804800000L;

  @Test
  public void testNoAnomalies() throws DetectorException {
    // test all dataframes columns expected in a AnomalyDetectorResult dataframe
    Interval interval = new Interval(JANUARY_1_2021, JANUARY_5_2021, DateTimeZone.UTC);
    Map<String, DataTable> timeSeriesMap = new HashMap<>();
    DataFrame currentDf = new DataFrame()
        .addSeries(Constants.COL_TIME,
            JANUARY_1_2021,
            JANUARY_2_2021,
            JANUARY_3_2021,
            JANUARY_4_2021,
            JANUARY_5_2021)
        .addSeries(Constants.COL_VALUE, 100., 200., 300., 400., 500.);
    DataFrame baselineDf = new DataFrame()
        .addSeries(Constants.COL_VALUE, 99., 199., 299., 399., 499.);
    timeSeriesMap.put(AnomalyDetector.KEY_CURRENT, SimpleDataTable.fromDataFrame(currentDf));
    timeSeriesMap.put(AnomalyDetector.KEY_BASELINE, SimpleDataTable.fromDataFrame(baselineDf));

    PercentageChangeRuleDetectorSpec spec = new PercentageChangeRuleDetectorSpec();
    spec.setMonitoringGranularity("P1D");
    double percentageChange = 0.2;
    spec.setPercentageChange(percentageChange);
    PercentageChangeRuleDetector detector = new PercentageChangeRuleDetector();
    detector.init(spec);

    AnomalyDetectorResult output = detector.runDetection(interval, timeSeriesMap);
    // check everything in the dataframe
    DataFrame outputDf = output.getDataFrame();

    LongSeries outputTimeSeries = outputDf.getLongs(Constants.COL_TIME);
    LongSeries expectedTimeSeries = currentDf.getLongs(Constants.COL_TIME);
    assertThat(outputTimeSeries).isEqualTo(expectedTimeSeries);

    DoubleSeries outputValueSeries = outputDf.getDoubles(Constants.COL_VALUE);
    DoubleSeries expectedValueSeries = baselineDf.getDoubles(Constants.COL_VALUE);
    assertThat(outputValueSeries).isEqualTo(expectedValueSeries);

    DoubleSeries outputCurrentSeries = outputDf.getDoubles(Constants.COL_CURRENT);
    DoubleSeries expectedCurrentSeries = currentDf.getDoubles(Constants.COL_VALUE);
    assertThat(outputCurrentSeries).isEqualTo(expectedCurrentSeries);

    DoubleSeries outputUpperBoundSeries = outputDf.getDoubles(Constants.COL_UPPER_BOUND);
    DoubleSeries expectedUpperBoundSeries = baselineDf.getDoubles(COL_VALUE)
        .multiply(1 + percentageChange);
    assertThat(outputUpperBoundSeries).isEqualTo(expectedUpperBoundSeries);

    DoubleSeries outputLowerBoundSeries = outputDf.getDoubles(Constants.COL_LOWER_BOUND);
    DoubleSeries expectedLowerBoundSeries = baselineDf.getDoubles(COL_VALUE)
        .multiply(1 - percentageChange);
    assertThat(outputLowerBoundSeries).isEqualTo(expectedLowerBoundSeries);

    BooleanSeries outputAnomalySeries = outputDf.getBooleans(Constants.COL_ANOMALY);
    BooleanSeries expectedAnomalySeries = BooleanSeries.fillValues(currentDf.size(), false);
    assertThat(outputAnomalySeries).isEqualTo(expectedAnomalySeries);
  }

  @Test
  public void testDetectionRunsOnIntervalOnly() throws DetectorException {
    // test anomaly analysis is only conducted on the interval
    // notice the interval is smaller than the dataframe data
    Interval interval = new Interval(JANUARY_3_2021, JANUARY_5_2021, DateTimeZone.UTC);
    Map<String, DataTable> timeSeriesMap = new HashMap<>();
    DataFrame currentDf = new DataFrame()
        .addSeries(Constants.COL_TIME,
            JANUARY_1_2021,
            JANUARY_2_2021,
            JANUARY_3_2021,
            JANUARY_4_2021,
            JANUARY_5_2021)
        .addSeries(Constants.COL_VALUE, 100., 200., 300., 400., 500.);
    DataFrame baselineDf = new DataFrame()
        .addSeries(Constants.COL_VALUE, 10., 10., 10., 10., 10.);
    timeSeriesMap.put(AnomalyDetector.KEY_CURRENT, SimpleDataTable.fromDataFrame(currentDf));
    timeSeriesMap.put(AnomalyDetector.KEY_BASELINE, SimpleDataTable.fromDataFrame(baselineDf));

    PercentageChangeRuleDetectorSpec spec = new PercentageChangeRuleDetectorSpec();
    spec.setMonitoringGranularity("P1D");
    double percentageChange = 0.01;
    spec.setPercentageChange(percentageChange);
    PercentageChangeRuleDetector detector = new PercentageChangeRuleDetector();
    detector.init(spec);

    AnomalyDetectorResult output = detector.runDetection(interval, timeSeriesMap);
    DataFrame outputDf = output.getDataFrame();
    BooleanSeries outputTimeSeries = outputDf.getBooleans(Constants.COL_ANOMALY);
    assertThat(outputTimeSeries.sliceTo(2)).isEqualTo(BooleanSeries.fillValues( 2, BooleanSeries.FALSE));
    assertThat(outputTimeSeries.sliceFrom(2)).isEqualTo(BooleanSeries.fillValues(3, BooleanSeries.TRUE));
  }

  @Test
  public void testAnomaliesUpAndDown() throws DetectorException {
    Interval interval = new Interval(JANUARY_1_2021, JANUARY_5_2021, DateTimeZone.UTC);
    Map<String, DataTable> timeSeriesMap = new HashMap<>();
    DataFrame currentDf = new DataFrame()
        .addSeries(Constants.COL_TIME,
            JANUARY_1_2021,
            JANUARY_2_2021,
            JANUARY_3_2021,
            JANUARY_4_2021,
            JANUARY_5_2021)
        .addSeries(Constants.COL_VALUE, 100., 200., 300., 400., 500.);
    DataFrame baselineDf = new DataFrame()
        .addSeries(Constants.COL_VALUE, 130, 199., 299., 310., 390);
    timeSeriesMap.put(AnomalyDetector.KEY_CURRENT, SimpleDataTable.fromDataFrame(currentDf));
    timeSeriesMap.put(AnomalyDetector.KEY_BASELINE, SimpleDataTable.fromDataFrame(baselineDf));

    PercentageChangeRuleDetectorSpec spec = new PercentageChangeRuleDetectorSpec();
    spec.setMonitoringGranularity("P1D");
    double percentageChange = 0.2;
    spec.setPercentageChange(percentageChange);
    PercentageChangeRuleDetector detector = new PercentageChangeRuleDetector();
    detector.init(spec);

    AnomalyDetectorResult output = detector.runDetection(interval, timeSeriesMap);
    // check everything in the dataframe
    DataFrame outputDf = output.getDataFrame();

    BooleanSeries outputAnomalySeries = outputDf.getBooleans(Constants.COL_ANOMALY);
    BooleanSeries expectedAnomalySeries = BooleanSeries.buildFrom(
        BooleanSeries.TRUE,
        BooleanSeries.FALSE,
        BooleanSeries.FALSE,
        BooleanSeries.TRUE,
        BooleanSeries.TRUE);
    assertThat(outputAnomalySeries).isEqualTo(expectedAnomalySeries);
  }

  @Test
  public void testAnomaliesUpOnly() throws DetectorException {
    Interval interval = new Interval(JANUARY_1_2021, JANUARY_5_2021, DateTimeZone.UTC);
    Map<String, DataTable> timeSeriesMap = new HashMap<>();
    DataFrame currentDf = new DataFrame()
        .addSeries(Constants.COL_TIME,
            JANUARY_1_2021,
            JANUARY_2_2021,
            JANUARY_3_2021,
            JANUARY_4_2021,
            JANUARY_5_2021)
        .addSeries(Constants.COL_VALUE, 100., 200., 300., 400., 500.);
    DataFrame baselineDf = new DataFrame()
        .addSeries(Constants.COL_VALUE, 130, 199., 299., 320., 400);
    timeSeriesMap.put(AnomalyDetector.KEY_CURRENT, SimpleDataTable.fromDataFrame(currentDf));
    timeSeriesMap.put(AnomalyDetector.KEY_BASELINE, SimpleDataTable.fromDataFrame(baselineDf));

    PercentageChangeRuleDetectorSpec spec = new PercentageChangeRuleDetectorSpec();
    spec.setMonitoringGranularity("P1D");
    spec.setPattern("UP");
    double percentageChange = 0.2;
    spec.setPercentageChange(percentageChange);
    PercentageChangeRuleDetector detector = new PercentageChangeRuleDetector();
    detector.init(spec);

    AnomalyDetectorResult output = detector.runDetection(interval, timeSeriesMap);
    // check everything in the dataframe
    DataFrame outputDf = output.getDataFrame();

    BooleanSeries outputAnomalySeries = outputDf.getBooleans(Constants.COL_ANOMALY);
    BooleanSeries expectedAnomalySeries = BooleanSeries.buildFrom(
        BooleanSeries.FALSE, // change is down
        BooleanSeries.FALSE,
        BooleanSeries.FALSE,
        BooleanSeries.TRUE, // change is up
        BooleanSeries.TRUE); // change is up
    assertThat(outputAnomalySeries).isEqualTo(expectedAnomalySeries);
  }

  @Test
  public void testAnomaliesDownOnly() throws DetectorException {
    Interval interval = new Interval(JANUARY_1_2021, JANUARY_5_2021, DateTimeZone.UTC);
    Map<String, DataTable> timeSeriesMap = new HashMap<>();
    DataFrame currentDf = new DataFrame()
        .addSeries(Constants.COL_TIME,
            JANUARY_1_2021,
            JANUARY_2_2021,
            JANUARY_3_2021,
            JANUARY_4_2021,
            JANUARY_5_2021)
        .addSeries(Constants.COL_VALUE, 100., 200., 300., 400., 500.);
    DataFrame baselineDf = new DataFrame()
        .addSeries(Constants.COL_VALUE, 130, 199., 299., 320., 400);
    timeSeriesMap.put(AnomalyDetector.KEY_CURRENT, SimpleDataTable.fromDataFrame(currentDf));
    timeSeriesMap.put(AnomalyDetector.KEY_BASELINE, SimpleDataTable.fromDataFrame(baselineDf));

    PercentageChangeRuleDetectorSpec spec = new PercentageChangeRuleDetectorSpec();
    spec.setMonitoringGranularity("P1D");
    spec.setPattern("DOWN");
    double percentageChange = 0.2;
    spec.setPercentageChange(percentageChange);
    PercentageChangeRuleDetector detector = new PercentageChangeRuleDetector();
    detector.init(spec);

    AnomalyDetectorResult output = detector.runDetection(interval, timeSeriesMap);
    // check everything in the dataframe
    DataFrame outputDf = output.getDataFrame();

    BooleanSeries outputAnomalySeries = outputDf.getBooleans(Constants.COL_ANOMALY);
    BooleanSeries expectedAnomalySeries = BooleanSeries.buildFrom(
        BooleanSeries.TRUE, // change is down
        BooleanSeries.FALSE,
        BooleanSeries.FALSE,
        BooleanSeries.FALSE, // change is up
        BooleanSeries.FALSE); // change is up
    assertThat(outputAnomalySeries).isEqualTo(expectedAnomalySeries);
  }
}
