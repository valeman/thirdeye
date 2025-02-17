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
import { useTheme } from "@material-ui/core";
import { AxisBottom, TickRendererProps } from "@visx/axis";
import { Text } from "@visx/text";
import React, { FunctionComponent, ReactNode } from "react";
import {
    formatDateTimeForTimeAxis,
    getTickValuesForTimeAxis,
    SEPARATOR_DATE_TIME,
} from "../../../utils/visualization/visualization.util";
import { TimeAxisBottomProps } from "./time-axis-bottom.interfaces";
import { useTimeAxisBottomStyles } from "./time-axis-bottom.styles";

const NUM_TICKS_XS = 4;
const NUM_TICKS_SM = 6;
const NUM_TICKS_MD = 8;
const NUM_TICKS_LG = 10;

// Customization of visx time axis with formatted tick labels based on scale domain interval
export const TimeAxisBottom: FunctionComponent<TimeAxisBottomProps> = (
    props: TimeAxisBottomProps
) => {
    const timeAxisBottomClasses = useTimeAxisBottomStyles();
    const theme = useTheme();

    const tickFormatter = (date: number | { valueOf(): number }): string => {
        return formatDateTimeForTimeAxis(date, props.scale);
    };

    const getTickValues = (): number[] => {
        // Determine number of ticks to display based on input or parent container width
        // (in that order of priority)
        let numTicks;
        if (props.numTicks) {
            numTicks = props.numTicks;
        } else if (
            props.parentWidth &&
            props.parentWidth < theme.breakpoints.values.sm
        ) {
            // Parent container width roughly equal to screen width xs
            numTicks = NUM_TICKS_XS;
        } else if (
            props.parentWidth &&
            props.parentWidth < theme.breakpoints.values.md
        ) {
            // Parent container width roughly equal to screen width sm
            numTicks = NUM_TICKS_SM;
        } else if (
            props.parentWidth &&
            props.parentWidth < theme.breakpoints.values.lg
        ) {
            // Parent container width roughly equal to screen width md
            numTicks = NUM_TICKS_MD;
        } else {
            // Parent container width roughly equal to screen width lg
            numTicks = NUM_TICKS_LG;
        }

        return getTickValuesForTimeAxis(props.scale, numTicks);
    };

    // Renders formatted date from tick renderer props based on whether or not it contains
    // SEPARATOR_DATE_TIME
    const tickComponentRenderer = (
        tickRendererProps: TickRendererProps
    ): ReactNode => {
        let dateString = "";
        let timeString = "";

        if (tickRendererProps.formattedValue) {
            // To begin with, assume the string can be rendered as is
            dateString = tickRendererProps.formattedValue;
        }

        if (
            tickRendererProps.formattedValue &&
            tickRendererProps.formattedValue.includes(SEPARATOR_DATE_TIME)
        ) {
            // String contains both date and time which need to be rendered in two parts one below
            // the other
            const dateParts =
                tickRendererProps.formattedValue.split(SEPARATOR_DATE_TIME);
            dateString = dateParts && dateParts[0] && dateParts[0];
            timeString = dateParts && dateParts[1] && dateParts[1];
        }

        return (
            <>
                <Text
                    textAnchor={tickRendererProps.textAnchor}
                    x={tickRendererProps.x}
                    y={tickRendererProps.y + 2}
                >
                    {dateString}
                </Text>

                {timeString && (
                    <Text
                        textAnchor={tickRendererProps.textAnchor}
                        x={tickRendererProps.x}
                        y={tickRendererProps.y + 16}
                    >
                        {timeString}
                    </Text>
                )}
            </>
        );
    };

    return (
        <AxisBottom
            left={props.left}
            scale={props.scale}
            tickClassName={timeAxisBottomClasses.tick}
            tickComponent={tickComponentRenderer}
            tickFormat={tickFormatter}
            tickValues={getTickValues()}
            top={props.top}
        />
    );
};
