import { makeStyles } from "@material-ui/core";
import { Palette } from "../../../../utils/material-ui-util/palette-util";

export const useAlertEvaluationTimeSeriesLegendStyles = makeStyles({
    legendContainer: {
        display: "flex",
        justifyContent: "space-between",
        marginLeft: "10px",
        marginRight: "10px",
    },
    legendItem: {
        cursor: "pointer",
    },
    legendItemDisabled: {
        color: Palette.COLOR_TEXT_GREY,
    },
    legendItemText: {
        paddingLeft: "10px",
        paddingTop: "1px",
    },
});