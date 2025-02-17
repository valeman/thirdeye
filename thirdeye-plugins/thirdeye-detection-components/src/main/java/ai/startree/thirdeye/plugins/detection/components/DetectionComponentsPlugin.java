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
package ai.startree.thirdeye.plugins.detection.components;

import ai.startree.thirdeye.plugins.detection.components.triggers.ConsoleOutputTrigger;
import ai.startree.thirdeye.plugins.detection.components.triggers.ConsoleOutputTriggerSpec;
import ai.startree.thirdeye.plugins.detection.components.triggers.GenericEventTriggerFactory;
import ai.startree.thirdeye.spi.Plugin;
import ai.startree.thirdeye.spi.detection.EventTriggerFactory;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;

@AutoService(Plugin.class)
public class DetectionComponentsPlugin implements Plugin {

  @Override
  public Iterable<EventTriggerFactory> getEventTriggerFactories() {
    return ImmutableList.of(
        new GenericEventTriggerFactory<>(
            "CONSOLE_OUTPUT",
            ConsoleOutputTriggerSpec.class,
            ConsoleOutputTrigger.class
        )
    );
  }
}
