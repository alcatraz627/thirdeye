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
package ai.startree.thirdeye.spi.detection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ConfigUtilsTest {

  @Test
  public void testGetListNull() {
    Assert.assertTrue(ConfigUtils.getList(null).isEmpty());
  }

  @Test
  public void testGetListPartialNull() {
    Assert.assertEquals(ConfigUtils.getList(Arrays.asList("a", null)).size(), 2);
  }

  @Test
  public void testGetMapNull() {
    Assert.assertTrue(ConfigUtils.getMap(null).isEmpty());
  }

  @Test
  public void testGetMapPartialNull() {
    Assert.assertEquals(ConfigUtils.getMap(Collections.singletonMap("a", null)).size(), 1);
  }

  @Test
  public void testGetLongsNull() {
    Assert.assertTrue(ConfigUtils.getLongs(null).isEmpty());
  }

  @Test
  public void testGetLongsPartialNull() {
    Assert.assertEquals(ConfigUtils.getLongs(Arrays.asList(1L, null, 2L)).size(), 2);
  }

  @Test
  public void testGetMultimapNull() {
    Assert.assertTrue(ConfigUtils.getMultimap(null).isEmpty());
  }

  @Test
  public void testGetMultimapPartialNull() {
    Assert.assertEquals(
        ConfigUtils.getMultimap(Collections.singletonMap("a", Arrays.asList("A", null))).size(), 2);
  }

  @Test
  public void testGetListModification() {
    List<String> defaultList = new ArrayList<>();
    List<String> list = ConfigUtils.getList(null, defaultList);
    list.add("value");
    Assert.assertNotEquals(list, defaultList);
  }

  @Test
  public void testGetMapModification() {
    Map<String, String> defaultMap = new HashMap<>();
    Map<String, String> map = ConfigUtils.getMap(null, defaultMap);
    map.put("key", "value");
    Assert.assertNotEquals(map, defaultMap);
  }
}
