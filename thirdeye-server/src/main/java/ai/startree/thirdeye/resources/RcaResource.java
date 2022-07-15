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
package ai.startree.thirdeye.resources;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.spi.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.api.RootCauseEntity;
import ai.startree.thirdeye.spi.datalayer.Templatable;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiKeyAuthDefinition;
import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = "Root Cause Analysis", authorizations = {@Authorization(value = "oauth")})
@SwaggerDefinition(securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = @ApiKeyAuthDefinition(name = HttpHeaders.AUTHORIZATION, in = ApiKeyLocation.HEADER, key = "oauth")))
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class RcaResource {

  private static final Logger LOG = LoggerFactory.getLogger(RcaResource.class);

  private final RcaInvestigationResource rcaInvestigationResource;
  private final RcaMetricResource rcaMetricResource;
  private final RcaDimensionAnalysisResource rcaDimensionAnalysisResource;
  private final RcaRelatedResource rcaRelatedResource;

  @Inject
  public RcaResource(
      final RcaInvestigationResource rcaInvestigationResource,
      final RcaMetricResource rcaMetricResource,
      final RcaDimensionAnalysisResource rcaDimensionAnalysisResource,
      final RcaRelatedResource rcaRelatedResource) {
    this.rcaInvestigationResource = rcaInvestigationResource;
    this.rcaMetricResource = rcaMetricResource;
    this.rcaDimensionAnalysisResource = rcaDimensionAnalysisResource;
    this.rcaRelatedResource = rcaRelatedResource;
  }

  @Path(value = "/dim-analysis")
  public RcaDimensionAnalysisResource getDimensionAnalysisResource() {
    return rcaDimensionAnalysisResource;
  }

  @Path(value = "/investigations")
  public RcaInvestigationResource getRcaInvestigationResource() {
    return rcaInvestigationResource;
  }

  @Path(value = "/metrics")
  public RcaMetricResource getRcaMetricResource() {
    return rcaMetricResource;
  }

  @Path(value = "/related")
  public RcaRelatedResource getRcaRelatedResource() {
    return rcaRelatedResource;
  }

  @GET
  @Path("/query")
  @ApiOperation(value = "Send query")
  @Deprecated
  public List<RootCauseEntity> query(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @ApiParam(value = "framework name")
      @QueryParam("framework") String framework,
      @ApiParam(value = "start overall time window to consider for events")
      @QueryParam("analysisStart") Long analysisStart,
      @ApiParam(value = "end of overall time window to consider for events")
      @QueryParam("analysisEnd") Long analysisEnd,
      @ApiParam(value = "start time of the anomalous time period for the metric under analysis")
      @QueryParam("anomalyStart") Long anomalyStart,
      @ApiParam(value = "end time of the anomalous time period for the metric under analysis")
      @QueryParam("anomalyEnd") Long anomalyEnd,
      @ApiParam(value = "baseline start time, e.g. anomaly start time offset by 1 week")
      @QueryParam("baselineStart") Long baselineStart,
      @ApiParam(value = "baseline end time, e.g. typically anomaly start time offset by 1 week")
      @QueryParam("baselineEnd") Long baselineEnd,
      @QueryParam("formatterDepth") Integer formatterDepth,
      @ApiParam(value = "URNs of metrics to analyze")
      @QueryParam("urns") List<String> urns) throws Exception {

    throw new UnsupportedOperationException("Deprecated route");
  }

  @GET
  @Path("/raw")
  @ApiOperation(value = "Raw")
  @Deprecated
  public List<RootCauseEntity> raw(
      @ApiParam(hidden = true) @Auth ThirdEyePrincipal principal,
      @QueryParam("framework") String framework,
      @QueryParam("formatterDepth") Integer formatterDepth,
      @QueryParam("urns") List<String> urns) throws Exception {
    throw new UnsupportedOperationException("Deprecated route");
  }

  /**
   * Apply inclusion and exclusion lists
   */
  @NonNull
  protected static List<String> getRcaDimensions(final @NonNull List<String> includedDimensions,
      final @NonNull List<String> excludedDimensions,
      final DatasetConfigDTO datasetConfigDTO) {
    final boolean oneListIsEmpty = includedDimensions.isEmpty() || excludedDimensions.isEmpty();
    if (!oneListIsEmpty) {
      throw new IllegalArgumentException(
          "includedDimensions and excludedDimensions are both not empty. Cannot use an inclusion list and an exclusion list at the same time");
    } else if (!includedDimensions.isEmpty()) {
      // use inclusion list - takes precedence other every other lists
      return cleanDimensionStrings(includedDimensions);
    } else if (datasetConfigDTO.getDimensions() == null ) {
      // no known dimensions - no need to apply exclusion list
      return List.of();
    } else {
      final List<String> excludedDimensionsToUse;
      if (!excludedDimensions.isEmpty()) {
        // use argument exclusion list
        excludedDimensionsToUse = excludedDimensions;
      } else {
        // use default exclusion list
        excludedDimensionsToUse = optional(datasetConfigDTO.getRcaExcludedDimensions()).map(Templatable::value).orElse(List.of());
      }
      final List<String> rcaDimensions = new ArrayList<>(datasetConfigDTO.getDimensions());
      rcaDimensions.removeAll(cleanDimensionStrings(excludedDimensionsToUse));
      return rcaDimensions;
    }
  }

  private static @NonNull List<String> cleanDimensionStrings(
      @NonNull final List<String> dimensions) {
    return dimensions.stream().map(String::trim).collect(Collectors.toList());
  }
}
