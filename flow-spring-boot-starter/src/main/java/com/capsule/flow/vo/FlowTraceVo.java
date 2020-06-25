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

package com.capsule.flow.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 单据实体的历史审批路径
 *
 * @author DiGuoZhiMeng
 * @since 2019/06/17
 */
@Data
@Accessors(chain = true)
public class FlowTraceVo {

    /**
     * 上一个状态
     */
    private String prevStatus;

    /**
     * 动作
     */
    private String action;

    /**
     * 下一个状态
     */
    private String nextStatus;

}
