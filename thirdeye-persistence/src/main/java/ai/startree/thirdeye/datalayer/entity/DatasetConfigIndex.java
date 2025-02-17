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
package ai.startree.thirdeye.datalayer.entity;

public class DatasetConfigIndex extends AbstractIndexEntity {

  String dataset;
  String displayName;
  boolean active;
  boolean requiresCompletenessCheck;
  long lastRefreshTime;

  public String getDataset() {
    return dataset;
  }

  public void setDataset(String dataset) {
    this.dataset = dataset;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public boolean isRequiresCompletenessCheck() {
    return requiresCompletenessCheck;
  }

  public void setRequiresCompletenessCheck(boolean requiresCompletenessCheck) {
    this.requiresCompletenessCheck = requiresCompletenessCheck;
  }

  public long getLastRefreshTime() {
    return lastRefreshTime;
  }

  public void setLastRefreshTime(long lastRefreshTime) {
    this.lastRefreshTime = lastRefreshTime;
  }
}
