/*
 * Copyright 2019-2029 DiGuoZhiMeng(https://github.com/DiGuoZhiMeng)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.capsule.flow.utils;

import com.capsule.flow.exception.FlowException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

/**
 * Class工具类
 *
 * @author DiGuoZhiMeng
 * @since 2020-06-14
 */
@Slf4j
public class ClassUtils {

    public static void setFieldValue(Object target, String fieldName, Object value) {
        if (target == null || StringUtils.isBlank(fieldName)) {
            throw new FlowException(10012, "Target object or field name can not be empty when set field value.");
        }
        try {
            FieldUtils.writeField(target, fieldName, value, true);
        } catch (IllegalAccessException e) {
            throw new FlowException(10011, "Field name " + fieldName + "  may not exist in object " + target.getClass().getCanonicalName());
        }
    }

    public static Long getLongValue(Object target, String fieldName) {
        if (target == null || StringUtils.isBlank(fieldName)) {
            throw new FlowException(10013, "Target object or field name can not be empty when get [Long] filed value.");
        }
        try {
            Object longValue = FieldUtils.readField(target, fieldName, true);
            if (longValue == null) {
                return null;
            }
            try {
                return Long.parseLong(longValue.toString());
            } catch (NumberFormatException e) {
                throw new FlowException(10015, "Value " + longValue + " can not be parsed to Long type.");
            }
        } catch (IllegalAccessException e) {
            throw new FlowException(10011, "Field name " + fieldName + "  may not exist in object " + target.getClass().getCanonicalName());
        }
    }

    public static String getStringValue(Object target, String fieldName) {
        if (target == null || StringUtils.isBlank(fieldName)) {
            throw new FlowException(10014, "Target object or field name can not be empty when get [String] filed value.");
        }
        try {
            Object longValue = FieldUtils.readField(target, fieldName, true);
            if (longValue == null) {
                return null;
            }
            return String.valueOf(longValue);
        } catch (IllegalAccessException e) {
            throw new FlowException(10011, "Field name " + fieldName + "  may not exist in object " + target.getClass().getCanonicalName());
        }
    }

}