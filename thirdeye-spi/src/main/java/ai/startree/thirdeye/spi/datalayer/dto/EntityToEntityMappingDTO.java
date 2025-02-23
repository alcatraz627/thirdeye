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
package ai.startree.thirdeye.spi.datalayer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Objects;

/**
 * This class holds the mapping between entities
 * We have predefined some valid relations
 * Each mapping can maintain a score, to say whay it the degree of correlation between the entities
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntityToEntityMappingDTO extends AbstractDTO {

  String fromURN;
  String toURN;
  String mappingType;
  double score;

  public String getFromURN() {
    return fromURN;
  }

  public void setFromURN(String fromURN) {
    this.fromURN = fromURN;
  }

  public String getToURN() {
    return toURN;
  }

  public void setToURN(String toURN) {
    this.toURN = toURN;
  }

  public String getMappingType() {
    return mappingType;
  }

  public void setMappingType(String mappingType) {
    this.mappingType = mappingType;
  }

  public double getScore() {
    return score;
  }

  public void setScore(double score) {
    this.score = score;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof EntityToEntityMappingDTO)) {
      return false;
    }
    EntityToEntityMappingDTO em = (EntityToEntityMappingDTO) o;
    return Objects.equals(fromURN, em.getFromURN())
        && Objects.equals(toURN, em.getToURN())
        && Objects.equals(mappingType, em.getMappingType())
        && Objects.equals(score, em.getScore());
  }

  @Override
  public int hashCode() {
    return Objects.hash(fromURN, toURN, mappingType, score);
  }
}
