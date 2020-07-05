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

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import wiki.capsule.flow.entity.FlowRound;
import wiki.capsule.flow.mapper.FlowRoundMapper;
import wiki.capsule.flow.service.FlowRoundService;

/**
 * <pre>
 * 流程审批回合及数据变动记录表 服务实现类
 * </pre>
 *
 * @author DiGuoZhiMeng
 * @since 2020-06-14
 */
@Slf4j
@Service
public class FlowRoundServiceImpl extends ServiceImpl<FlowRoundMapper, FlowRound> implements FlowRoundService {

}
