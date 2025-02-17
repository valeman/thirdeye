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
package ai.startree.thirdeye.subscriptiongroup.filter;

import static ai.startree.thirdeye.spi.util.AnomalyUtils.isIgnore;

import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EmailSchemeDto;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.NotificationSchemesDto;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.detection.AnomalyResultSource;
import com.google.common.collect.Collections2;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.MapUtils;


public abstract class StatefulDetectionAlertFilter extends DetectionAlertFilter {

  public static final String PROP_TO = "to";
  public static final String PROP_CC = "cc";
  public static final String PROP_BCC = "bcc";
  public static final String PROP_RECIPIENTS = "recipients";

  private final MergedAnomalyResultManager mergedAnomalyResultManager;
  private final AlertManager detectionConfigManager;

  public StatefulDetectionAlertFilter(SubscriptionGroupDTO config,
      long endTime, final MergedAnomalyResultManager mergedAnomalyResultManager,
      final AlertManager detectionConfigManager) {
    super(config, endTime);
    this.mergedAnomalyResultManager = mergedAnomalyResultManager;
    this.detectionConfigManager = detectionConfigManager;
  }

  /**
   * Helper to determine presence of user-feedback for an anomaly
   *
   * @param anomaly anomaly
   * @return {@code true} if feedback exists and is TRUE or FALSE, {@code false} otherwise
   */
  private static boolean hasFeedback(MergedAnomalyResultDTO anomaly) {
    return anomaly.getFeedback() != null
        && !anomaly.getFeedback().getFeedbackType().isUnresolved();
  }

  protected final Set<MergedAnomalyResultDTO> filter(Map<Long, Long> vectorClocks) {
    // retrieve all candidate anomalies
    Set<MergedAnomalyResultDTO> allAnomalies = new HashSet<>();
    for (Long detectionId : vectorClocks.keySet()) {
      // Ignore disabled detections
      AlertDTO detection = detectionConfigManager
          .findById(detectionId);
      if (detection == null || !detection.isActive()) {
        continue;
      }

      // No point in fetching anomalies older than MAX_ANOMALY_NOTIFICATION_LOOKBACK
      long startTime = vectorClocks.get(detectionId);
      if (startTime < this.endTime - Constants.ANOMALY_NOTIFICATION_LOOKBACK_TIME) {
        startTime = this.endTime - Constants.ANOMALY_NOTIFICATION_LOOKBACK_TIME;
      }

      Collection<MergedAnomalyResultDTO> candidates = mergedAnomalyResultManager
          .findByCreatedTimeInRangeAndDetectionConfigId(startTime + 1, this.endTime, detectionId);

      long finalStartTime = startTime;
      Collection<MergedAnomalyResultDTO> anomalies =
          Collections2.filter(candidates, anomaly -> anomaly != null && !anomaly.isChild()
              && !hasFeedback(anomaly)
              && anomaly.getCreatedTime() > finalStartTime
              && !isIgnore(anomaly)
              && (anomaly.getAnomalyResultSource()
              .equals(AnomalyResultSource.DEFAULT_ANOMALY_DETECTION) ||
              anomaly.getAnomalyResultSource()
                  .equals(AnomalyResultSource.ANOMALY_REPLAY)));

      allAnomalies.addAll(anomalies);
    }
    return allAnomalies;
  }

  protected final Map<Long, Long> makeVectorClocks(Collection<Long> detectionConfigIds) {
    Map<Long, Long> clocks = new HashMap<>();

    for (Long id : detectionConfigIds) {
      clocks.put(id, MapUtils.getLong(this.config.getVectorClocks(), id, 0L));
    }

    return clocks;
  }

  protected Set<String> cleanupRecipients(Set<String> recipient) {
    Set<String> filteredRecipients = new HashSet<>();
    if (recipient != null) {
      filteredRecipients.addAll(recipient);
      filteredRecipients = filteredRecipients.stream().map(String::trim)
          .collect(Collectors.toSet());
      filteredRecipients.removeIf(rec -> rec == null || "".equals(rec));
    }
    return filteredRecipients;
  }

  /**
   * Extracts the alert schemes from config and also merges (overrides)
   * recipients explicitly defined outside the scope of alert schemes.
   */
  protected NotificationSchemesDto generateNotificationSchemeProps(SubscriptionGroupDTO config,
      List<String> to, List<String> cc, List<String> bcc) {
    NotificationSchemesDto notificationSchemeProps = new NotificationSchemesDto();
    notificationSchemeProps.setWebhookScheme(config.getNotificationSchemes().getWebhookScheme());
    // Override the email alert scheme
    EmailSchemeDto emailScheme = new EmailSchemeDto()
        .setCc(cc)
        .setTo(to)
        .setBcc(bcc);
    notificationSchemeProps.setEmailScheme(emailScheme);
    return notificationSchemeProps;
  }
}
