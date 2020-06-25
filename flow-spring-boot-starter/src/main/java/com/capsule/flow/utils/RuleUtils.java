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

import lombok.extern.slf4j.Slf4j;
import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.resource.StringTemplateResourceLoader;

import java.io.IOException;

/**
 * 基于Beetls的规则引擎工具类
 *
 * @author DiGuoZhiMeng
 * @since 2020-06-14
 */
@Slf4j
public class RuleUtils {

    private static GroupTemplate instance = null;

    private static GroupTemplate getInstance() {
        if (instance == null) {
            synchronized (GroupTemplate.class) {
                if (instance == null) {
                    StringTemplateResourceLoader resourceLoader = new StringTemplateResourceLoader();
                    Configuration cfg = null;
                    try {
                        cfg = Configuration.defaultConfiguration();
                    } catch (IOException e) {
                        log.error("RuleUtils.getInstance error:", e);
                    }
                    instance = new GroupTemplate(resourceLoader, cfg);
                }
            }
        }
        return instance;
    }

    public static String render(String el, String key, Object value) {
        Template template = getInstance().getTemplate(el);
        template.binding(key, value);
        return template.render();
    }
}