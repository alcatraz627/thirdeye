import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageLoadingIndicator } from "../../components/page-loading-indicator/page-loading-indicator.component";
import { useApplicationBreadcrumbsStore } from "../../store/application-breadcrumbs/application-breadcrumbs.store";

export const AlertsUpdatePage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [push] = useApplicationBreadcrumbsStore((state) => [state.push]);
    const { t } = useTranslation();

    useEffect(() => {
        // Create page breadcrumb
        push([
            {
                text: "ALERT_NAME",
                path: "",
            },
            {
                text: t("label.update"),
                path: "",
            },
        ]);

        setLoading(false);
    }, [push, t]);

    if (loading) {
        return (
            <PageContainer>
                <PageLoadingIndicator />
            </PageContainer>
        );
    }

    return <></>;
};