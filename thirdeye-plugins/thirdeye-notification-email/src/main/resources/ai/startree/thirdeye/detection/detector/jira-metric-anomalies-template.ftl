<#--

    Copyright 2022 StarTree Inc

    Licensed under the StarTree Community License (the "License"); you may not use
    this file except in compliance with the License. You may obtain a copy of the
    License at http://www.startree.ai/legal/startree-community-license

    Unless required by applicable law or agreed to in writing, software distributed under the
    License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
    either express or implied.
    See the License for the specific language governing permissions and limitations under
    the License.

-->
<#if anomalyCount == 1>
  ThirdEye has detected *[an anomaly|${dashboardHost}/app/#/anomalies?anomalyIds=${anomalyIds}]* on the metric <#list metricsMap?keys as id>*${metricsMap[id].name}*</#list> between *${startTime}* and *${endTime}* (${timeZone})
<#else>
  ThirdEye has detected [*${anomalyCount} anomalies*|${dashboardHost}/app/#/anomalies?anomalyIds=${anomalyIds}] on the metrics listed below between *${startTime}* and *${endTime}* (${timeZone})
</#if>
<#list metricToAnomalyDetailsMap?keys as metric>

------------------------------------------------------------------------------------------------------------------------------------------------------------
  *Metric:*&nbsp;_${metric}_
  <#list detectionToAnomalyDetailsMap?keys as detectionName>
    <#assign newTable = false>
    <#list detectionToAnomalyDetailsMap[detectionName] as anomaly>
      <#if anomaly.metric==metric>
        <#assign newTable=true>
        <#assign description=anomaly.funcDescription>
      </#if>
    </#list>

    <#if newTable>

      *Alert Name:*&nbsp;_${detectionName}_ ([edit|${dashboardHost}/app/#/manage/explore/${functionToId[detectionName]?string.computer}])
      *Description:* ${description}
    </#if>

    <#list detectionToAnomalyDetailsMap[detectionName] as anomaly>
      <#if anomaly.metric==metric>
        <#if newTable>
          ||Start||Duration||Type||Dimensions||Current||Predicted||Change||
        </#if>
        <#assign newTable = false>
        |[${anomaly.startDateTime} ${anomaly.timezone}|${anomaly.anomalyURL}${anomaly.anomalyId}]|${anomaly.duration}|${anomaly.anomalyType}|<#if anomaly.dimensions?has_content><#list anomaly.dimensions as dimension>${dimension}\\ </#list><#else>-</#if>|_${anomaly.currentVal}_|_${anomaly.baselineVal}_|_${anomaly.positiveLift?string('+','')}${anomaly.lift}_|
      </#if>
    </#list>
  </#list>
</#list>

=======================================================================================

*Reference Links:*
<#if referenceLinks?has_content>
  <#list referenceLinks?keys as referenceLinkKey>
    - [${referenceLinkKey}|${referenceLinks[referenceLinkKey]}]
  </#list>
</#if>

=======================================================================================

_You are receiving this alert because you have subscribed to ThirdEye Alert Service for *${alertConfigName}*. If you have any questions regarding this report, please email thirdeye-support@startree.ai_
