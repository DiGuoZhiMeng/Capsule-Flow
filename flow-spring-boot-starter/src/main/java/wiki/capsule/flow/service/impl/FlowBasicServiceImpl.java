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

package wiki.capsule.flow.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import wiki.capsule.flow.entity.FlowBasic;
import wiki.capsule.flow.exception.FlowException;
import wiki.capsule.flow.mapper.FlowBasicMapper;
import wiki.capsule.flow.service.FlowBasicService;
import wiki.capsule.flow.utils.RuleUtils;
import wiki.capsule.flow.vo.FlowMetaVo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 * 流程基础信息表 服务实现类
 * </pre>
 *
 * @author DiGuoZhiMeng
 * @since 2020-06-14
 */
@Slf4j
@Service
public class FlowBasicServiceImpl extends ServiceImpl<FlowBasicMapper, FlowBasic> implements FlowBasicService {

    @Override
    public List<FlowMetaVo> getFlowMetaInfoV1(String flowName) {
        List<FlowBasic> flowBasics = list(new LambdaQueryWrapper<FlowBasic>().eq(FlowBasic::getFlowName, flowName)
                .ge(FlowBasic::getTaskOrder, 1));
        List<FlowMetaVo> flowMetaVoList = new ArrayList<>();
        for (FlowBasic flowBasic : flowBasics) {
            if (flowBasic.getNextStatus().startsWith("<%") && flowBasic.getNextStatus().endsWith("%>")) {
                if (StringUtils.isBlank(flowBasic.getNextStatusAlias())) {
                    throw new FlowException(10024, "Next status alias can not be empty.");
                }
                JSONArray array = JSON.parseArray(flowBasic.getNextStatusAlias());
                for (int i = 0; i < array.size(); i++) {
                    JSONObject item = array.getJSONObject(i);
                    FlowMetaVo metaVo = new FlowMetaVo().setAction(flowBasic.getAction())
                            .setPrevStatus(flowBasic.getPrevStatus()).setNextStatus(item.getString("next_step"))
                            .setLabel(item.getString("label"));
                    flowMetaVoList.add(metaVo);
                }
            } else {
                FlowMetaVo metaVo = new FlowMetaVo().setAction(flowBasic.getAction()).setNextStatus(flowBasic.getNextStatus())
                        .setPrevStatus(flowBasic.getPrevStatus());
                flowMetaVoList.add(metaVo);
            }
        }
        return flowMetaVoList;
    }

    @Override
    public List<FlowMetaVo> getFlowMetaInfoV2(String flowName, Object bizEntity) {
        List<FlowBasic> flowBasics = list(new LambdaQueryWrapper<FlowBasic>().eq(FlowBasic::getFlowName, flowName)
                .ge(FlowBasic::getTaskOrder, 1));
        List<FlowMetaVo> flowMetaVoList = new ArrayList<>();
        Map<String, FlowBasic> flowBasicMap = new HashMap<>();
        FlowBasic firstFlow = null;
        for (FlowBasic flowBasic : flowBasics) {
            flowBasicMap.put(flowBasic.getPrevStatus(), flowBasic);
            if (flowBasic.getTaskOrder() == 1) {
                if (firstFlow != null) {
                    throw new FlowException(10025, "Only one flow's task order can be 1.");
                }
                firstFlow = flowBasic;
            }
        }
        if (firstFlow == null) {
            throw new FlowException(10025, "There must be one flow's task order is 1.");
        }
        FlowBasic flowBasic = firstFlow;
        while (flowBasic != null) {
            String nextStatus;
            if (flowBasic.getNextStatus().startsWith("<%") && flowBasic.getNextStatus().endsWith("%>")) {
                nextStatus = RuleUtils.render(flowBasic.getNextStatus(), "vo", bizEntity);
                FlowMetaVo metaVo = new FlowMetaVo().setPrevStatus(flowBasic.getPrevStatus())
                        .setNextStatus(nextStatus).setAction(flowBasic.getAction());
                if (StringUtils.isNotBlank(flowBasic.getNextStatusAlias())) {
                    JSONArray array = JSON.parseArray(flowBasic.getNextStatusAlias());
                    for (int i = 0; i < array.size(); i++) {
                        JSONObject item = array.getJSONObject(i);
                        if (nextStatus.equalsIgnoreCase(item.getString("next_status"))) {
                            metaVo.setLabel(item.getString("label"));
                            break;
                        }
                    }
                }
                flowMetaVoList.add(metaVo);
            } else {
                FlowMetaVo metaVo = new FlowMetaVo().setPrevStatus(flowBasic.getPrevStatus())
                        .setNextStatus(flowBasic.getNextStatus()).setAction(flowBasic.getAction());
                flowMetaVoList.add(metaVo);
                nextStatus = flowBasic.getNextStatus();
            }
            flowBasic = flowBasicMap.get(nextStatus);
        }
        return flowMetaVoList;
    }
}
