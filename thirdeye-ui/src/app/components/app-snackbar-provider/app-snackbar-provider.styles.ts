import { makeStyles } from "@material-ui/core";
import { Dimension } from "../../utils/material-ui-util/dimension-util";
import { Palette } from "../../utils/material-ui-util/palette-util";

export const useAppSnackbarProviderStyles = makeStyles({
    container: {
        width: Dimension.WIDTH_SNACKBAR_DEFAULT,
    },
    success: {
        color: `${Palette.COLOR_TEXT_DEFAULT} !important`,
        backgroundColor: `${Palette.COLOR_BACKGROUND_ALERT_SUCCESS} !important`,
    },
    error: {
        color: `${Palette.COLOR_TEXT_DEFAULT} !important`,
        backgroundColor: `${Palette.COLOR_BACKGROUND_ALERT_ERROR} !important`,
    },
    warning: {
        color: `${Palette.COLOR_TEXT_DEFAULT} !important`,
        backgroundColor: `${Palette.COLOR_BACKGROUND_ALERT_WARNING} !important`,
    },
    info: {
        color: `${Palette.COLOR_TEXT_DEFAULT} !important`,
        backgroundColor: `${Palette.COLOR_BACKGROUND_ALERT_INFO} !important`,
    },
});