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

package wiki.capsule.flow.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * 返回流程数据包括：当前流程的主流程数据，单据实体当前审批状态，历史审批路径，用于前端可视化流程展示
 *
 * @author DiGuoZhiMeng
 * @since 2019/06/17
 */
@Data
@Accessors(chain = true)
public class FlowRootVo {
    /**
     * 主流程元数据
     */
    private List<FlowMetaVo> flowMetaVoList;

    /**
     * 历史审批路径
     */
    private List<FlowTraceVo> flowTraceVoList;

    /**
     * 当前审批状态
     */
    private Map<String, Object> approvalStatus;

}