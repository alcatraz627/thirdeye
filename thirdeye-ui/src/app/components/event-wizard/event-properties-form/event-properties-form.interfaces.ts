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
import { FieldErrors } from "react-hook-form/dist/types/errors";
import { FieldName } from "react-hook-form/dist/types/fields";
import { Control } from "react-hook-form/dist/types/form";
import { RegisterOptions } from "react-hook-form/dist/types/validator";
import { Event } from "../../../rest/dto/event.interfaces";

export interface EventPropertiesFormProps {
    formRegister: (name: FieldName<Event>, rules?: RegisterOptions) => void;
    formErrors: FieldErrors<Event>;
    formControl: Control<Event>;
    onSubmit?: (event: Event) => void;
    fullWidth?: boolean;
}

export interface DynamicFormType {
    key: string;
    propertyName: string;
    propertyValue: string[];
}
