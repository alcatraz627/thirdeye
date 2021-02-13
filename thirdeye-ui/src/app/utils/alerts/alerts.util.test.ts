import { cloneDeep } from "lodash";
import { AlertCardData } from "../../components/entity-cards/alert-card/alert-card.interfaces";
import {
    Alert,
    AlertNode,
    AlertNodeType,
} from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import {
    createAlertEvaluation,
    createDefaultAlert,
    createEmptyAlertCardData,
    createEmptyAlertDatasetAndMetric,
    createEmptyAlertSubscriptionGroup,
    filterAlerts,
    getAlertCardData,
    getAlertCardDatas,
} from "./alerts.util";

jest.mock("i18next", () => ({
    t: jest.fn().mockImplementation((key) => key),
}));

describe("Alerts Util", () => {
    test("createDefaultAlert should create appropriate alert", () => {
        expect(createDefaultAlert()).toEqual(mockDefaultAlert);
    });

    test("createEmptyAlertCardData should create appropriate alert card data", () => {
        expect(createEmptyAlertCardData()).toEqual(mockEmptyAlertCardData);
    });

    test("createEmptyAlertDatasetAndMetric should create appropriate alert dataset and metric", () => {
        expect(createEmptyAlertDatasetAndMetric()).toEqual(
            mockEmptyAlertDatasetAndMetric
        );
    });

    test("createEmptyAlertSubscriptionGroup should create appropriate alert subscription group", () => {
        expect(createEmptyAlertSubscriptionGroup()).toEqual(
            mockEmptyAlertSubscriptionGroup
        );
    });

    test("createAlertEvaluation should create appropriate alert evaluation", () => {
        expect(
            createAlertEvaluation(
                {
                    id: 1,
                } as Alert,
                2,
                3
            )
        ).toEqual({
            alert: {
                id: 1,
            },
            start: 2,
            end: 3,
        });
    });

    test("getAlertCardData should return empty alert card data for invalid alert", () => {
        expect(
            getAlertCardData((null as unknown) as Alert, mockSubscriptionGroups)
        ).toEqual(mockEmptyAlertCardData);
    });

    test("getAlertCardData should return appropriate alert card data for alert and invalid subscription groups", () => {
        const expectedAlertCardData = cloneDeep(mockAlertCardData1);
        expectedAlertCardData.subscriptionGroups = [];

        expect(
            getAlertCardData(
                mockAlert1,
                (null as unknown) as SubscriptionGroup[]
            )
        ).toEqual(expectedAlertCardData);
    });

    test("getAlertCardData should return appropriate alert card data for alert and empty subscription groups", () => {
        const expectedAlertCardData = cloneDeep(mockAlertCardData1);
        expectedAlertCardData.subscriptionGroups = [];

        expect(getAlertCardData(mockAlert1, [])).toEqual(expectedAlertCardData);
    });

    test("getAlertCardData should return appropriate alert card data for alert and subscription groups", () => {
        expect(getAlertCardData(mockAlert1, mockSubscriptionGroups)).toEqual(
            mockAlertCardData1
        );
    });

    test("getAlertCardDatas should return empty array for invalid alerts", () => {
        expect(
            getAlertCardDatas(
                (null as unknown) as Alert[],
                mockSubscriptionGroups
            )
        ).toEqual([]);
    });

    test("getAlertCardDatas should return empty array for empty alerts", () => {
        expect(getAlertCardDatas([], mockSubscriptionGroups)).toEqual([]);
    });

    test("getAlertCardDatas should return appropriate alert card data array for alerts and invalid subscription groups", () => {
        const expectedAlertCardData1 = cloneDeep(mockAlertCardData1);
        expectedAlertCardData1.subscriptionGroups = [];
        const expectedAlertCardData2 = cloneDeep(mockAlertCardData2);
        expectedAlertCardData2.subscriptionGroups = [];

        expect(
            getAlertCardDatas(
                mockAlerts,
                (null as unknown) as SubscriptionGroup[]
            )
        ).toEqual([expectedAlertCardData1, expectedAlertCardData2]);
    });

    test("getAlertCardDatas should return appropriate alert card data array for alerts and empty subscription groups", () => {
        const expectedAlertCardData1 = cloneDeep(mockAlertCardData1);
        expectedAlertCardData1.subscriptionGroups = [];
        const expectedAlertCardData2 = cloneDeep(mockAlertCardData2);
        expectedAlertCardData2.subscriptionGroups = [];

        expect(getAlertCardDatas(mockAlerts, [])).toEqual([
            expectedAlertCardData1,
            expectedAlertCardData2,
        ]);
    });

    test("getAlertCardDatas should return appropriate alert card data array for alerts and subscription groups", () => {
        expect(getAlertCardDatas(mockAlerts, mockSubscriptionGroups)).toEqual(
            mockAlertCardDatas
        );
    });

    test("filterAlerts should return empty array for invalid alert card data array", () => {
        expect(
            filterAlerts((null as unknown) as AlertCardData[], mockSearchWords)
        ).toEqual([]);
    });

    test("filterAlerts should return empty array for empty alert card data array", () => {
        expect(filterAlerts([], mockSearchWords)).toEqual([]);
    });

    test("filterAlerts should return appropriate alert card data array for alert card data array and invalid search words", () => {
        expect(
            filterAlerts(mockAlertCardDatas, (null as unknown) as string[])
        ).toEqual(mockAlertCardDatas);
    });

    test("filterAlerts should return appropriate alert card data array for alert card data array and empty search words", () => {
        expect(filterAlerts(mockAlertCardDatas, [])).toEqual(
            mockAlertCardDatas
        );
    });

    test("filterAlerts should return appropriate alert card data array for alert card data array and search words", () => {
        expect(filterAlerts(mockAlertCardDatas, mockSearchWords)).toEqual([
            mockAlertCardData1,
        ]);
    });
});

const mockDefaultAlert = {
    name: "new-alert",
    description: "This is the detection used by online service",
    nodes: {
        "detection-1": {
            type: AlertNodeType.DETECTION,
            subType: "PERCENTAGE_RULE",
            metric: {
                name: "views",
                dataset: {
                    name: "pageviews",
                },
            },
            params: {
                offset: "wo1w",
                percentageChange: 0.2,
            },
        },
    },
};

const mockEmptyAlertCardData = {
    id: -1,
    name: "label.no-data-marker",
    active: false,
    activeText: "label.no-data-marker",
    userId: -1,
    createdBy: "label.no-data-marker",
    detectionTypes: [],
    filteredBy: [],
    datasetAndMetrics: [],
    subscriptionGroups: [],
    alert: null,
};

const mockEmptyAlertDatasetAndMetric = {
    datasetId: -1,
    datasetName: "label.no-data-marker",
    metricId: -1,
    metricName: "label.no-data-marker",
};

const mockEmptyAlertSubscriptionGroup = {
    id: -1,
    name: "label.no-data-marker",
};

const mockAlert1 = {
    id: 1,
    name: "testName1",
    active: true,
    owner: {
        id: 2,
        principal: "testPrincipal2",
    },
    nodes: {
        alertNode1: {
            type: AlertNodeType.DETECTION,
            subType: "testSubType1",
            metric: {
                id: 3,
                name: "testName3",
                dataset: {
                    id: 4,
                    name: "testName4",
                },
            },
        } as AlertNode,
        alertNode2: {
            type: AlertNodeType.FILTER,
            subType: "testSubType2",
            metric: {
                id: 5,
                name: "testName5",
            },
        } as AlertNode,
        alertNode3: {
            type: "testType3" as AlertNodeType,
            subType: "testSubType3",
        } as AlertNode,
    } as { [index: string]: AlertNode },
} as Alert;

const mockAlert2 = {
    id: 6,
    name: "testName6",
    active: false,
} as Alert;

const mockAlerts = [mockAlert1, mockAlert2];

const mockSubscriptionGroup1 = {
    id: 7,
    name: "testName7",
    alerts: [
        {
            id: 1,
        },
        {
            id: 6,
        },
    ],
} as SubscriptionGroup;

const mockSubscriptionGroup2 = {
    id: 8,
    name: "testName8",
    alerts: [
        {
            id: 1,
        },
    ],
} as SubscriptionGroup;

const mockSubscriptionGroup3 = {
    id: 9,
    name: "testName9",
} as SubscriptionGroup;

const mockSubscriptionGroups = [
    mockSubscriptionGroup1,
    mockSubscriptionGroup2,
    mockSubscriptionGroup3,
];

const mockAlertCardData1 = {
    id: 1,
    name: "testName1",
    active: true,
    activeText: "label.active",
    userId: 2,
    createdBy: "testPrincipal2",
    detectionTypes: ["testSubType1"],
    filteredBy: ["testSubType2"],
    datasetAndMetrics: [
        {
            datasetId: 4,
            datasetName: "testName4",
            metricId: 3,
            metricName: "testName3",
        },
        {
            datasetId: -1,
            datasetName: "label.no-data-marker",
            metricId: 5,
            metricName: "testName5",
        },
    ],
    subscriptionGroups: [
        {
            id: 7,
            name: "testName7",
        },
        {
            id: 8,
            name: "testName8",
        },
    ],
    alert: mockAlert1,
};

const mockAlertCardData2 = {
    id: 6,
    name: "testName6",
    active: false,
    activeText: "label.inactive",
    userId: -1,
    createdBy: "label.no-data-marker",
    detectionTypes: [],
    filteredBy: [],
    datasetAndMetrics: [],
    subscriptionGroups: [
        {
            id: 7,
            name: "testName7",
        },
    ],
    alert: mockAlert2,
};

const mockAlertCardDatas = [mockAlertCardData1, mockAlertCardData2];

const mockSearchWords = ["name1", "name9"];
