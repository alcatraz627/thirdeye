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
package ai.startree.thirdeye.plugins.notification.webhook;

import ai.startree.thirdeye.spi.api.NotificationPayloadApi;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface WebhookService {

  @POST
  Call<Void> sendWebhook(@Url String url, @Header("X-Signature") String signature, @Body NotificationPayloadApi entity);

  @POST
  Call<Void> sendWebhook(@Url String url, @Body NotificationPayloadApi entity);
}
