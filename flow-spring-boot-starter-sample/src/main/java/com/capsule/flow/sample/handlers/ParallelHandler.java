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

package com.capsule.flow.sample.handlers;

import com.alibaba.fastjson.JSONArray;
import com.capsule.flow.entity.FlowBasic;
import com.capsule.flow.handler.AbstractFlowHandler;
import com.capsule.flow.sample.entity.Parallel;
import com.capsule.flow.sample.rbac.EnforcerFactory;
import com.capsule.flow.sample.service.ParallelService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class ParallelHandler extends AbstractFlowHandler<Parallel> {

    @Resource
    private ParallelService parallelService;

    /**
     * 自定义校验权限
     *
     * @param currentFlows 当前可以处理的所有环节
     * @param user         当前用户，可以依据他获取权限
     * @return 有权限处理的所有环节
     */
    @Override
    protected List<FlowBasic> authPermission(List<FlowBasic> currentFlows, String user) {
        List<String> userRoles = EnforcerFactory.getEnforcer().getRolesForUser(user);
        List<FlowBasic> canHandledFlows = new ArrayList<>();
        if (CollectionUtils.isEmpty(userRoles)) {
            return canHandledFlows;
        }
        for (FlowBasic flowBasic : currentFlows) {
            if (StringUtils.isBlank(flowBasic.getHandleRoles())) {
                continue;
            }
            JSONArray handleRoles = JSONArray.parseArray(flowBasic.getHandleRoles());
            for (int i = 0; i < handleRoles.size(); i++) {
                if (userRoles.indexOf(handleRoles.getString(i)) > -1) {
                    canHandledFlows.add(flowBasic);
                    break;
                }
            }
        }
        return canHandledFlows;
    }

    /**
     * 处理后保存单据对象到数据库，流程引擎内不负责持久化操作，只负责审批日志和Flow Around创建及保存
     *
     * @param parallel 处理后的单据对象
     */
    @Override
    public void afterProcess(Parallel parallel) {
        parallelService.updateById(parallel.setUpdateTime(LocalDateTime.now()));
    }

    /**
     * 定义一个特有的流程名称，用于数据隔离
     *
     * @return FlowName
     */
    @Override
    protected String getFlowName() {
        return "PARALLEL_FLOW";
    }

    /**
     * 当前流程作用于哪个实体对象，用于数据隔离
     *
     * @return EntityName
     */
    @Override
    protected String getEntityName() {
        return "PARALLEL";
    }
}
