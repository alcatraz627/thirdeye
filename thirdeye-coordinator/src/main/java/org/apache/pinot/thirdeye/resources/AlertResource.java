package org.apache.pinot.thirdeye.resources;

import static org.apache.pinot.thirdeye.resources.ResourceUtils.ensure;
import static org.apache.pinot.thirdeye.resources.ResourceUtils.ensureExists;
import static org.apache.pinot.thirdeye.util.ApiBeanMapper.toApi;

import io.swagger.annotations.Api;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.pinot.thirdeye.alert.AlertCreater;
import org.apache.pinot.thirdeye.alert.AlertPreviewGenerator;
import org.apache.pinot.thirdeye.api.AlertApi;
import org.apache.pinot.thirdeye.api.AlertEvaluationApi;
import org.apache.pinot.thirdeye.api.ApplicationApi;
import org.apache.pinot.thirdeye.api.UserApi;
import org.apache.pinot.thirdeye.auth.AuthService;
import org.apache.pinot.thirdeye.auth.ThirdEyePrincipal;
import org.apache.pinot.thirdeye.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.detection.DetectionPipelineResult;
import org.apache.pinot.thirdeye.util.ApiBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api(tags = "Alert")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class AlertResource {

  private static final Logger log = LoggerFactory.getLogger(AlertResource.class);

  private final AlertManager alertManager;
  private final AlertCreater alertCreater;
  private final AuthService authService;
  private final AlertPreviewGenerator alertPreviewGenerator;

  @Inject
  public AlertResource(
      final AlertManager alertManager,
      final AlertCreater alertCreater,
      final AuthService authService,
      final AlertPreviewGenerator alertPreviewGenerator) {
    this.alertManager = alertManager;
    this.alertCreater = alertCreater;
    this.authService = authService;
    this.alertPreviewGenerator = alertPreviewGenerator;
  }

  @GET
  public Response getAll(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader
  ) {
    final ThirdEyePrincipal principal = authService.authenticate(authHeader);

    final List<AlertDTO> all = alertManager.findAll();
    return Response
        .ok(all.stream().map(ApiBeanMapper::toApi))
        .build();
  }

  @POST
  public Response createMultiple(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader,
      List<AlertApi> list) {
    final ThirdEyePrincipal principal = authService.authenticate(authHeader);

    ensureExists(list, "Invalid request");
    ensure(list.size() == 1, "Only 1 insert supported at this time.");

    final AlertApi alertApi = list.get(0);
    ensureExists(alertApi.getName(), "Name must be present");
    ensureExists(alertApi.getDetections(), "Exactly 1 detection must be present");
    ensure(alertApi.getDetections().size() == 1, "Exactly 1 detection must be present");

    return Response
        .ok(toApi(alertCreater.create(alertApi
            .setOwner(new UserApi().setPrincipal(principal.getName()))
        )))
        .build();
  }

  @Path("preview")
  @POST
  public Response preview(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader,
      AlertEvaluationApi request)
      throws InterruptedException, ExecutionException, TimeoutException {
    final ThirdEyePrincipal principal = authService.authenticate(authHeader);

    ensureExists(request.getStart());
    ensureExists(request.getEnd());

    ensureExists(request.getAlert())
        .setOwner(new UserApi()
            .setPrincipal(principal.getName()));

    final DetectionPipelineResult pipelineResult = alertPreviewGenerator.runPreview(request);
    return Response
        .ok(pipelineResult)
        .build();
  }

  @PUT
  public Response editMultiple(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader,
      List<ApplicationApi> applicationApiList) {
    final ThirdEyePrincipal principal = authService.authenticate(authHeader);
    ensure(false, "Unsupported Operation.");
    return Response
        .ok()
        .build();
  }

  @GET
  @Path("{id}")
  public Response get(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader,
      @PathParam("id") Long id) {
    final ThirdEyePrincipal principal = authService.authenticate(authHeader);
    final AlertDTO dto = alertManager.findById(id);
    ensureExists(dto, "Invalid id");

    return Response
        .ok(toApi(dto))
        .build();
  }

  @DELETE
  @Path("{id}")
  public Response delete(
      @HeaderParam(HttpHeaders.AUTHORIZATION) String authHeader,
      @PathParam("id") Long id) {
    final ThirdEyePrincipal principal = authService.authenticate(authHeader);
    final AlertDTO dto = alertManager.findById(id);
    if (dto != null) {
      alertManager.delete(dto);
      return Response.ok(toApi(dto)).build();
    }

    return Response.ok("Not Found").build();
  }
}
