import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Redirect, Route, Switch } from "react-router-dom";
import { PageContainer } from "../components/page-container/page-container.component";
import { PageLoadingIndicator } from "../components/page-loading-indicator/page-loading-indicator.component";
import { AnomaliesAllPage } from "../pages/anomalies-all-page/anomalies-all-page.component";
import { AnomaliesDetailPage } from "../pages/anomalies-detail-page/anomalies-detail-page.component";
import { useApplicationBreadcrumbsStore } from "../store/application-breadcrumbs/application-breadcrumbs.store";
import {
    AppRoute,
    getAnomaliesAllPath,
    getAnomaliesPath,
    getPageNotFoundPath,
} from "../utils/route/routes.util";

export const AnomaliesRouter: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [push] = useApplicationBreadcrumbsStore((state) => [state.push]);
    const { t } = useTranslation();

    useEffect(() => {
        // Create router breadcrumb
        push(
            [
                {
                    text: t("label.anomalies"),
                    path: getAnomaliesPath(),
                },
            ],
            true // Clear existing breadcrumbs
        );

        setLoading(false);
    }, [push, t]);

    if (loading) {
        return (
            <PageContainer>
                <PageLoadingIndicator />
            </PageContainer>
        );
    }

    return (
        <Switch>
            {/* Anomalies path */}
            <Route exact path={AppRoute.ANOMALIES}>
                {/* Redirect to anomalies all path */}
                <Redirect to={getAnomaliesAllPath()} />
            </Route>

            {/* Anomalies all path */}
            <Route
                exact
                component={AnomaliesAllPage}
                path={AppRoute.ANOMALIES_ALL}
            />

            {/* Anomalies detail path */}
            <Route
                exact
                component={AnomaliesDetailPage}
                path={AppRoute.ANOMALIES_DETAIL}
            />

            {/* No match found, redirect to page not found path */}
            <Redirect to={getPageNotFoundPath()} />
        </Switch>
    );
};