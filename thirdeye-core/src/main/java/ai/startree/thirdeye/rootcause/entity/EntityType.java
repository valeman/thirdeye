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
package ai.startree.thirdeye.rootcause.entity;

import ai.startree.thirdeye.rootcause.Entity;
import ai.startree.thirdeye.util.ParsedUrn;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 * Wrapper class for URN prefix based typing of Entity.
 */
public final class EntityType {

  private final String prefix;

  public String getPrefix() {
    return prefix;
  }

  public EntityType(String prefix) {
    if (!prefix.endsWith(":")) {
      throw new IllegalArgumentException("Prefix must end with ':'");
    }
    this.prefix = prefix;
  }

  /**
   * Returns the parameterized type as string urn. Attaches values in order. Also unwraps elements
   * if provided as a Collection.
   *
   * @param values parameters, in order
   * @return formatted urn
   */
  public String formatURN(Object... values) {
    List<String> tailValues = new ArrayList<>();
    for (Object value : values) {

      // unwrap collection
      if (value instanceof Collection) {
        for (Object v : (Collection<String>) value) {
          tailValues.add(v.toString());
        }

        // single item
      } else {
        tailValues.add(value.toString());
      }
    }

    return this.prefix + StringUtils.join(tailValues, ":");
  }

  public boolean isType(String urn) {
    return urn.startsWith(this.prefix);
  }

  public boolean isType(Entity e) {
    return e.getUrn().startsWith(this.prefix);
  }

  public boolean isType(ParsedUrn parsedUrn) {
    final int prefixSize = this.getPrefixSize();
    List<String> parts = Arrays.asList(this.prefix.split(":"));
    return Objects.equals(parts, parsedUrn.getPrefixes().subList(0, prefixSize));
  }

  public int getPrefixSize() {
    return this.prefix.split(":").length;
  }
}
