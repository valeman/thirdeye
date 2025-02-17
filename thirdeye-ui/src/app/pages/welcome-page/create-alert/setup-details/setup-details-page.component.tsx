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
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import {
    Box,
    Button,
    Grid,
    Switch,
    TextField,
    Typography,
} from "@material-ui/core";
import {
    default as React,
    FunctionComponent,
    useEffect,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import {
    Link as RouterLink,
    useNavigate,
    useOutletContext,
} from "react-router-dom";
import { AlertFrequency } from "../../../../components/alert-wizard-v2/alert-details/alert-frequency/alert-frequency.component";
import { AvailableAlgorithmOption } from "../../../../components/alert-wizard-v3/algorithm-selection/algorithm-selection.interfaces";
import { EmailListInput } from "../../../../components/form-basics/email-list-input/email-list-input.component";
import { InputSection } from "../../../../components/form-basics/input-section/input-section.component";
import {
    PageContentsCardV1,
    PageContentsGridV1,
} from "../../../../platform/components";
import { EditableAlert } from "../../../../rest/dto/alert.interfaces";
import { generateGenericNameForAlert } from "../../../../utils/alerts/alerts.util";
import { AppRouteRelative } from "../../../../utils/routes/routes.util";

export const SetupDetailsPage: FunctionComponent = () => {
    const navigate = useNavigate();
    const { t } = useTranslation();

    const {
        alert,
        handleAlertPropertyChange,
        selectedAlgorithmOption,
        emails,
        setEmails,
        handleCreateAlertClick,
        isCreatingAlert,
    } = useOutletContext<{
        alert: EditableAlert;
        handleAlertPropertyChange: (contents: Partial<EditableAlert>) => void;
        selectedAlgorithmOption: AvailableAlgorithmOption;
        emails: string[];
        setEmails: (emails: string[]) => void;
        handleCreateAlertClick: () => void;
        isCreatingAlert: boolean;
    }>();

    const [
        shouldDisplayScheduleConfiguration,
        setShouldDisplayScheduleConfiguration,
    ] = useState(false);
    const [isNotificationsOn, setIsNotificationsOn] = useState(false);

    useEffect(() => {
        // On initial render, ensure a metric, dataset, and dataSource are set
        if (
            !alert.templateProperties.aggregationColumn ||
            !alert.templateProperties.dataset ||
            !alert.templateProperties.dataSource
        ) {
            navigate(
                `../${AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_MONITORING}`
            );
        }
    }, []);

    return (
        <>
            <PageContentsGridV1>
                <Grid item xs={12}>
                    <Typography variant="h5">
                        {t("message.setup-alert-details")}
                    </Typography>
                    <Typography variant="body1">
                        {t("message.add-the-final-details-for-your-alert")}
                    </Typography>
                </Grid>

                <Grid item xs={12}>
                    <PageContentsCardV1>
                        <Box marginBottom={2}>
                            <Typography variant="h5">
                                {t("label.alert-details")}
                            </Typography>
                        </Box>
                        <Grid container>
                            <InputSection
                                inputComponent={
                                    <TextField
                                        fullWidth
                                        defaultValue={
                                            alert.name ||
                                            generateGenericNameForAlert(
                                                alert.templateProperties
                                                    .aggregationColumn as string,
                                                alert.templateProperties
                                                    .aggregationFunction as string,
                                                selectedAlgorithmOption
                                                    .algorithmOption
                                                    .title as string,
                                                selectedAlgorithmOption
                                                    ?.algorithmOption
                                                    .alertTemplateForMultidimension ===
                                                    alert?.template?.name
                                            )
                                        }
                                        onChange={(e) =>
                                            handleAlertPropertyChange({
                                                name: e.currentTarget.value,
                                            })
                                        }
                                    />
                                }
                                label={t("label.name")}
                            />
                        </Grid>
                    </PageContentsCardV1>
                </Grid>

                <Grid item xs={12}>
                    <PageContentsCardV1>
                        <Grid container>
                            <Grid item lg={3} md={5} sm={10} xs={10}>
                                <Box marginBottom={2}>
                                    <Typography variant="h5">
                                        {t("label.alert-schedule")}
                                    </Typography>
                                    <Typography variant="body2">
                                        {t("message.how-often-pipeline-checks")}
                                    </Typography>
                                </Box>
                            </Grid>
                            <Grid item lg={9} md={7} sm={2} xs={2}>
                                <Switch
                                    checked={shouldDisplayScheduleConfiguration}
                                    color="primary"
                                    name="checked"
                                    onChange={() =>
                                        setShouldDisplayScheduleConfiguration(
                                            !shouldDisplayScheduleConfiguration
                                        )
                                    }
                                />
                            </Grid>

                            {shouldDisplayScheduleConfiguration && (
                                <AlertFrequency
                                    alert={alert}
                                    onAlertPropertyChange={
                                        handleAlertPropertyChange
                                    }
                                />
                            )}
                        </Grid>
                    </PageContentsCardV1>
                </Grid>

                <Grid item xs={12}>
                    <PageContentsCardV1>
                        <Grid container>
                            <Grid item lg={3} md={5} sm={10} xs={10}>
                                <Box marginBottom={2}>
                                    <Typography variant="h5">
                                        {t("label.configure-notifications")}
                                    </Typography>
                                    <Typography variant="body2">
                                        {t(
                                            "message.select-who-to-notify-when-finding-anomalies"
                                        )}
                                    </Typography>
                                </Box>
                            </Grid>
                            <Grid item lg={9} md={7} sm={2} xs={2}>
                                <Switch
                                    checked={isNotificationsOn}
                                    color="primary"
                                    name="checked"
                                    onChange={() =>
                                        setIsNotificationsOn(!isNotificationsOn)
                                    }
                                />
                            </Grid>

                            {isNotificationsOn && (
                                <InputSection
                                    inputComponent={
                                        <EmailListInput
                                            emails={emails}
                                            onChange={setEmails}
                                        />
                                    }
                                    label={t("label.add-email")}
                                />
                            )}
                        </Grid>
                    </PageContentsCardV1>
                </Grid>
            </PageContentsGridV1>

            <Box width="100%">
                <PageContentsCardV1>
                    <Grid container justifyContent="flex-end">
                        <Grid item>
                            <Button
                                color="secondary"
                                component={RouterLink}
                                to={`../${AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_MONITORING}`}
                            >
                                {t("label.back")}
                            </Button>
                        </Grid>
                        <Grid item>
                            <Button
                                color="primary"
                                disabled={isCreatingAlert}
                                onClick={handleCreateAlertClick}
                            >
                                {isCreatingAlert
                                    ? t("label.creating")
                                    : t("label.next")}
                            </Button>
                        </Grid>
                    </Grid>
                </PageContentsCardV1>
            </Box>
        </>
    );
};
