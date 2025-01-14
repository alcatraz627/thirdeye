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

import ai.startree.thirdeye.spi.datalayer.dto.EmailSchemeDto;
import ai.startree.thirdeye.spi.datalayer.dto.NotificationSchemesDto;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import java.util.Map;

public class SubscriptionUtils {

  public static final String PROP_RECIPIENTS = "recipients";
  public static final String PROP_TO = "to";

  /**
   * Make a new(child) subscription group from given(parent) subscription group.
   *
   * This is used   preparing the notification for each dimensional recipients.
   */
  public static SubscriptionGroupDTO makeChildSubscriptionConfig(
      SubscriptionGroupDTO parentConfig,
      NotificationSchemesDto notificationSchemes,
      Map<String, String> refLinks) {
    // TODO: clone object using serialization rather than manual copy
    SubscriptionGroupDTO subsConfig = new SubscriptionGroupDTO();
    subsConfig.setId(parentConfig.getId());
    subsConfig.setFrom(parentConfig.getFrom());
    subsConfig.setActive(parentConfig.isActive());
    subsConfig.setOwners(parentConfig.getOwners());
    subsConfig.setYaml(parentConfig.getYaml());
    subsConfig.setName(parentConfig.getName());
    subsConfig.setSubjectType(parentConfig.getSubjectType());
    subsConfig.setProperties(parentConfig.getProperties());
    subsConfig.setVectorClocks(parentConfig.getVectorClocks());

    subsConfig.setNotificationSchemes(notificationSchemes);
    subsConfig.setRefLinks(refLinks);

    return subsConfig;
  }

  /**
   * Validates if the subscription config has email recipients configured or not.
   */
  public static boolean isEmptyEmailRecipients(SubscriptionGroupDTO subscriptionGroupDTO) {
    EmailSchemeDto emailProps = subscriptionGroupDTO.getNotificationSchemes().getEmailScheme();
    return emailProps==null || emailProps.getTo() == null || emailProps.getTo().isEmpty();
  }
}
