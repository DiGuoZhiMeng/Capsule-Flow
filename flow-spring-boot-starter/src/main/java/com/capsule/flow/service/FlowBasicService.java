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

package com.capsule.flow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.capsule.flow.entity.FlowBasic;
import com.capsule.flow.vo.FlowMetaVo;

import java.util.List;

/**
 * <pre>
 * 流程基础信息表 服务类
 * </pre>
 *
 * @author DiGuoZhiMeng
 * @since 2020-06-14
 */
public interface FlowBasicService extends IService<FlowBasic> {

    /**
     * 通过审批流名称获取审批流元数据信息，用于前端流程可视化展示
     * 查询出所有正向流程梳理，即taskOrder>0
     * 允许非有向无环图的场景
     * 如果是通过规则引擎判断分支的场景需要在nextStatusAlias中配置json字段
     *
     * @param flowName 流程名称
     * @return List<FlowMetaVo>
     */
    List<FlowMetaVo> getFlowMetaInfoV1(String flowName);


    /**
     * 通过审批流名称获取审批流元数据信息，用于前端流程可视化展示
     * 查询出所有正向流程梳理，即taskOrder>0，按先后排序后返回
     * 必须是有向无环图的场景
     * 如果是通过规则引擎判断分支的场景需要通过业务实体的实际值判断出分支走向，把实际要走过的最终线路返回
     *
     * @param flowName 流程名称
     * @param bizEntity 业务实体
     * @return List<FlowMetaVo>
     */
    List<FlowMetaVo> getFlowMetaInfoV2(String flowName, Object bizEntity);
}
