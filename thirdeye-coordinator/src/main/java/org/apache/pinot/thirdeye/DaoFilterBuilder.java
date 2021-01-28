package org.apache.pinot.thirdeye;

import static org.apache.pinot.thirdeye.ThirdEyeStatus.ERR_INVALID_QUERY_PARAM_OPERATOR;
import static org.apache.pinot.thirdeye.ThirdEyeStatus.ERR_UNEXPECTED_QUERY_PARAM;
import static org.apache.pinot.thirdeye.datalayer.util.ThirdEyeSpiUtils.optional;
import static org.apache.pinot.thirdeye.resources.ResourceUtils.ensureExists;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.pinot.thirdeye.datalayer.DaoFilter;
import org.apache.pinot.thirdeye.datalayer.util.Predicate;
import org.apache.pinot.thirdeye.datalayer.util.Predicate.OPER;
import org.apache.pinot.thirdeye.util.Pair;

public class DaoFilterBuilder {

  private static final ImmutableSet<String> KEYWORDS = ImmutableSet.of("limit");
  private static final ImmutableMap<String, OPER> OPERATOR_MAP = ImmutableMap.<String, OPER>builder()
      .put("eq", OPER.EQ)
      .put("gt", OPER.GT)
      .put("gte", OPER.GE)
      .put("lt", OPER.LT)
      .put("lte", OPER.LE)
      .put("neq", OPER.NEQ)
      .build();
  private static final Pattern PATTERN = Pattern.compile("\\[(\\w+)\\](\\S+)");
  private final ImmutableMap<String, String> apiToBeanMap;

  public DaoFilterBuilder(final ImmutableMap<String, String> apiToBeanMap) {
    this.apiToBeanMap = apiToBeanMap;
  }

  static Pair<OPER, String> toPair(final Object o) {
    final String s = o.toString();

    final Matcher m = PATTERN.matcher(s);
    if (m.matches()) {
      final OPER operator = OPERATOR_MAP.get(m.group(1));
      if (operator == null) {
        throw new ThirdEyeException(ERR_INVALID_QUERY_PARAM_OPERATOR, OPERATOR_MAP.keySet());
      }
      return Pair.createPair(operator, m.group(2));
    }
    return Pair.createPair(OPER.EQ, s);
  }

  static Predicate toPredicate(final String columnName, final Object[] objects) {
    final List<Predicate> predicates = Arrays.stream(objects)
        .map(DaoFilterBuilder::toPair)
        .map(p -> new Predicate(columnName, p.get0(), p.get1()))
        .collect(Collectors.toList());

    return Predicate.AND(predicates.toArray(new Predicate[]{}));
  }

  public DaoFilter buildFilter(final MultivaluedMap<String, String> queryParameters) {
    final DaoFilter daoFilter = new DaoFilter();
    optional(queryParameters.getFirst("limit"))
        .map(Integer::valueOf)
        .ifPresent(daoFilter::setLimit);

    return daoFilter.setPredicate(buildPredicate(queryParameters));
  }

  private Predicate buildPredicate(final MultivaluedMap<String, String> queryParameters) {
    final List<Predicate> predicates = new ArrayList<>();
    for (Map.Entry<String, List<String>> e : queryParameters.entrySet()) {
      final String qParam = e.getKey();
      if (KEYWORDS.contains(qParam)) {
        continue;
      }
      final String columnName = ensureExists(
          apiToBeanMap.get(qParam),
          ERR_UNEXPECTED_QUERY_PARAM,
          apiToBeanMap.keySet());
      final Object[] objects = e.getValue().toArray();
      predicates.add(toPredicate(columnName, objects));
    }
    return Predicate.AND(predicates.toArray(new Predicate[]{}));
  }
}