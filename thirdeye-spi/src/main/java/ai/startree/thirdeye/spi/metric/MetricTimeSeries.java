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
package ai.startree.thirdeye.spi.metric;

import java.util.Set;
import org.joda.time.Interval;

public interface MetricTimeSeries {

  /**
   * Get data value for a given timestamp
   *
   * @return the corresponding value
   */
  Double get(long timestamp);

  /**
   * Remove anomalies data if needed
   */
  void remove(long timeStamp);

  /**
   * Contain timestamp or not
   *
   * @return true or false
   */
  boolean hasTimestamp(long timestamp);

  /**
   * Get timestamp set
   *
   * @return set
   */
  Set<Long> timestampSet();

  /**
   * Returns the interval of the time series, which provides the max and min timestamps (inclusive).
   */
  Interval getTimeSeriesInterval();

  /**
   * Get the size of the timestamp set
   *
   * @return the size of the number of timestamps in the series
   */
  int size();
}
