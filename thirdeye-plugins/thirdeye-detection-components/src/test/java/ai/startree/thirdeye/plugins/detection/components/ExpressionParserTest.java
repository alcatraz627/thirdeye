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

import ai.startree.thirdeye.plugins.detection.components.ExpressionParser;
import ai.startree.thirdeye.spi.detection.ConfigUtils;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ExpressionParserTest {

  static final String PROP_AND = "and";
  static final String PROP_OR = "or";
  static final String PROP_OPERATOR = "operator";
  static final String PROP_LEFT_OP = "leftOp";
  static final String PROP_RIGHT_OP = "rightOp";
  static final String PROP_VALUE = "value";

  @Test
  public void testSingleExpression() {
    String expression = "entityA";
    Map<String, Object> operators = ExpressionParser.generateOperators(expression);
    Assert.assertNotNull(operators.get(PROP_VALUE));
    Assert.assertEquals(operators.get(PROP_VALUE), "entityA");
  }

  @Test
  public void testSimpleExpression() {
    String expression = "entityA && entityB";
    Map<String, Object> operators = ExpressionParser.generateOperators(expression);
    Assert.assertNotNull(operators.get(PROP_OPERATOR));
    Assert.assertEquals(operators.get(PROP_OPERATOR), PROP_AND);

    Assert.assertNotNull(operators.get(PROP_LEFT_OP));
    Map<String, Object> leftOperators = ConfigUtils.getMap(operators.get(PROP_LEFT_OP));
    Assert.assertNotNull(leftOperators.get(PROP_VALUE));
    Assert.assertEquals(leftOperators.get(PROP_VALUE), "entityA");

    Assert.assertNotNull(operators.get(PROP_RIGHT_OP));
    Map<String, Object> rightOperators = ConfigUtils.getMap(operators.get(PROP_RIGHT_OP));
    Assert.assertNotNull(rightOperators.get(PROP_VALUE));
    Assert.assertEquals(rightOperators.get(PROP_VALUE), "entityB");
  }

  @Test
  public void testComplexExpression() {
    String expression = "entityA && ( entityB || ( entityC && entityD))";
    Map<String, Object> operators = ExpressionParser.generateOperators(expression);
    Assert.assertNotNull(operators.get(PROP_OPERATOR));
    Assert.assertEquals(operators.get(PROP_OPERATOR), PROP_AND);

    Assert.assertNotNull(operators.get(PROP_LEFT_OP));
    Map<String, Object> leftOperators = ConfigUtils.getMap(operators.get(PROP_LEFT_OP));
    Assert.assertNotNull(leftOperators.get(PROP_VALUE));
    Assert.assertEquals(leftOperators.get(PROP_VALUE), "entityA");

    Assert.assertNotNull(operators.get(PROP_RIGHT_OP));
    Map<String, Object> rightOperators = ConfigUtils.getMap(operators.get(PROP_RIGHT_OP));
    Assert.assertEquals(rightOperators.get(PROP_OPERATOR), PROP_OR);
    Assert.assertNotNull(rightOperators.get(PROP_LEFT_OP));
    leftOperators = ConfigUtils.getMap(rightOperators.get(PROP_LEFT_OP));
    Assert.assertEquals(leftOperators.get(PROP_VALUE), "entityB");

    rightOperators = ConfigUtils.getMap(rightOperators.get(PROP_RIGHT_OP));
    Assert.assertNotNull(rightOperators.get(PROP_LEFT_OP));
    Assert.assertNotNull(rightOperators.get(PROP_RIGHT_OP));
    Assert.assertEquals(rightOperators.get(PROP_OPERATOR), PROP_AND);
    leftOperators = ConfigUtils.getMap(rightOperators.get(PROP_LEFT_OP));
    rightOperators = ConfigUtils.getMap(rightOperators.get(PROP_RIGHT_OP));
    Assert.assertEquals(leftOperators.get(PROP_VALUE), "entityC");
    Assert.assertEquals(rightOperators.get(PROP_VALUE), "entityD");
  }
}
