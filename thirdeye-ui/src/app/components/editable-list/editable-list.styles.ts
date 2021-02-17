import { makeStyles } from "@material-ui/core";
import { Dimension } from "../../utils/material-ui/dimension.util";

export const useEditableListStyles = makeStyles({
    list: {
        height: "250px",
        overflowX: "hidden",
        overflowY: "auto",
        padding: "0px",
    },
    addButton: {
        height: `${Dimension.HEIGHT_INPUT_DEFAULT}px`,
    },
});
