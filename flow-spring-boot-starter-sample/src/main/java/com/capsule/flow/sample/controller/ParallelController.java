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

package com.capsule.flow.sample.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.capsule.flow.entity.FlowLog;
import com.capsule.flow.enums.TodoEnum;
import com.capsule.flow.exception.FlowException;
import com.capsule.flow.sample.entity.Parallel;
import com.capsule.flow.sample.handlers.ParallelHandler;
import com.capsule.flow.sample.rbac.EnforcerFactory;
import com.capsule.flow.sample.service.ParallelService;
import com.capsule.flow.vo.FlowRootVo;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

@RestController
@Slf4j
@RequestMapping("/parallel")
public class ParallelController {

    @Resource
    private ParallelService parallelService;

    @Resource
    private ParallelHandler parallelHandler;

    /**
     * 获取当前单据实体的流程可视化信息
     * http://localhost:8080/parallel/getFlowMetaInfoV2/20
     *
     * @return FlowRootVo
     */
    @GetMapping("/getFlowMetaInfoV2/{id}")
    public FlowRootVo getFlowMetaInfoV2(@PathVariable Integer id) {
        Parallel parallel = parallelService.getById(id);
        return parallelHandler.getFlowMetaInfoV2(parallel);
    }

    /**
     * 获取当前单据实体的流程可视化信息
     * http://localhost:8080/parallel/getFlowMetaInfoV1/20
     *
     * @return FlowRootVo
     */
    @GetMapping("/getFlowMetaInfoV1/{id}")
    public FlowRootVo getFlowMetaInfoV1(@PathVariable Integer id) {
        Parallel parallel = parallelService.getById(id);
        return parallelHandler.getFlowMetaInfoV1(parallel);
    }

    /**
     * 获取所有待我处理的数据：包括待提交的、待处理的
     * http://localhost:8080/parallel/myTodo/user1
     *
     * @return List<Parallel>
     */
    @GetMapping("/myTodo/{user}")
    public List<Parallel> myTodo(@PathVariable String user) {
        List<String> userRoles = EnforcerFactory.getEnforcer().getRolesForUser(user);
        Set<String> status = parallelHandler.getTodoStatus(TodoEnum.ALL, Sets.newHashSet(userRoles));
        String sql = parallelHandler.todoSql(status, null);
        if (CollectionUtils.isEmpty(status) || StringUtils.isBlank(sql)) {
            return null;
        }
        return parallelService.list(new LambdaQueryWrapper<Parallel>().apply(sql));
    }

    /**
     * 获取所有待我处理的数据：不包括待提交的、只包括待处理的
     * http://localhost:8080/parallel/myApproval/user1
     *
     * @return List<Parallel>
     */
    @GetMapping("/myApproval/{user}")
    public List<Parallel> myApproval(@PathVariable String user) {
        List<String> userRoles = EnforcerFactory.getEnforcer().getRolesForUser(user);
        Set<String> status = parallelHandler.getTodoStatus(TodoEnum.ONLY_PENDING_APPROVAL, Sets.newHashSet(userRoles));
        String sql = parallelHandler.todoSql(status, null);
        if (CollectionUtils.isEmpty(status) || StringUtils.isBlank(sql)) {
            return null;
        }
        return parallelService.list(new LambdaQueryWrapper<Parallel>().apply(sql));
    }

    /**
     * 获取所有审批中的数据
     * http://localhost:8080/parallel/pendingHandle
     *
     * @return List<Parallel>
     */
    @GetMapping("/pendingHandle")
    public List<Parallel> pendingHandle() {
        Set<String> status = parallelHandler.getTodoStatus(TodoEnum.ONLY_PENDING_APPROVAL, null);
        String sql = parallelHandler.todoSql(status, "parallel.");
        if (CollectionUtils.isEmpty(status) || StringUtils.isBlank(sql)) {
            return null;
        }
        return parallelService.list(new LambdaQueryWrapper<Parallel>().apply(sql));
    }

    /**
     * 获取所有待提交的数据
     * http://localhost:8080/parallel/pendingSubmit
     *
     * @return List<Parallel>
     */
    @GetMapping("/pendingSubmit")
    public List<Parallel> pendingSubmit() {
        Set<String> status = parallelHandler.getTodoStatus(TodoEnum.ONLY_PENDING_SUBMIT, null);
        String sql = parallelHandler.todoSql(status, "parallel.");
        if (CollectionUtils.isEmpty(status) || StringUtils.isBlank(sql)) {
            return null;
        }
        return parallelService.list(new LambdaQueryWrapper<Parallel>().apply(sql));
    }

    /**
     * 判断是否有提交、审批等处理权限
     * http://localhost:8080/parallel/verifyHandleAccess/8/user1
     *
     * @param id   单据主键id
     * @param user 当前用户
     * @return true有权限，false无权限
     */
    @GetMapping("/verifyHandleAccess/{id}/{user}")
    public boolean verifyHandleAccess(@PathVariable Integer id, @PathVariable String user) {
        Parallel parallel = parallelService.getById(id);
        return parallelHandler.verifyHandleAccess(parallel, user);
    }

    /**
     * 根据单据id获取当前单据所有历史审批日志
     * http://localhost:8080/parallel/historyLogAll/8
     *
     * @param id 单据主键id
     * @return 日志列表
     */
    @GetMapping("/historyLogAll/{id}")
    public List<FlowLog> historyLogAll(@PathVariable Long id) {
        Parallel parallel = parallelService.getById(id);
        return parallelHandler.getHistoryFlowLog(parallel, false);
    }

    /**
     * 根据单据id获取当前单据当前审批回合内审批日志
     * http://localhost:8080/parallel/historyLog/8
     *
     * @param id 单据主键id
     * @return 日志列表
     */
    @GetMapping("/historyLog/{id}")
    public List<FlowLog> historyLog(@PathVariable Long id) {
        Parallel parallel = parallelService.getById(id);
        return parallelHandler.getHistoryFlowLog(parallel, true);
    }

    /**
     * 驳回测试
     * http://localhost:8080/parallel/reject/8/user2/驳回测试
     *
     * @param id      单据主键id
     * @param user    当前用户
     * @param comment 审批意见
     * @return 驳回后的实体
     */
    @GetMapping("/reject/{id}/{user}/{comment}")
    public Parallel reject(@PathVariable Integer id, @PathVariable String user, @PathVariable String comment) {
        Parallel parallel = parallelService.getById(id);
        // 未处于待提交状态无法驳回或审批通过
        if (parallelHandler.getInitialStatus().equalsIgnoreCase(parallel.getApprovalStatus())) {
            throw new FlowException("Current parallel is not submitted, can not be rejected.");
        }
        // 审批通过或者驳回了无法继续驳回
        if (parallelHandler.getApprovedStatus().contains(parallel.getApprovalStatus()) || parallelHandler.getRejectedStatus().contains(parallel.getApprovalStatus())) {
            throw new FlowException("Current parallel is approved or rejected, can not reject it.");
        }
        parallelHandler.process(parallel, "Reject", comment, user, false, false);
        return parallel;
    }

    /**
     * 审批通过测试
     * http://localhost:8080/parallel/approve/5/user2/审批通过测试
     *
     * @param id      单据主键id
     * @param user    当前用户
     * @param comment 审批意见
     * @return 审批后的实体
     */
    @GetMapping("/approve/{id}/{user}/{comment}")
    public Parallel approve(@PathVariable Integer id, @PathVariable String user, @PathVariable String comment) {
        Parallel parallel = parallelService.getById(id);
        // 未处于待提交状态无法驳回或审批通过
        if (parallelHandler.getInitialStatus().equalsIgnoreCase(parallel.getApprovalStatus())) {
            throw new FlowException("Current parallel is not submitted, can not be approved.");
        }
        // 审批通过或者驳回了无法继续审批通过
        if (parallelHandler.getApprovedStatus().contains(parallel.getApprovalStatus()) || parallelHandler.getRejectedStatus().contains(parallel.getApprovalStatus())) {
            throw new FlowException("Current parallel is approved or rejected, can not approve it.");
        }
        parallelHandler.process(parallel, "Approve", comment, user, false, false);
        return parallel;
    }

    /**
     * 测试驳回后再次提交
     * http://localhost:8080/parallel/submitAfterRejected/8/user1/测试驳回后再次提交
     *
     * @param id      单据主键id
     * @param user    当前用户
     * @param comment 提交说明
     * @return 提交后的实体
     */
    @GetMapping("/submitAfterRejected/{id}/{user}/{comment}")
    public Parallel submitAfterRejected(@PathVariable Long id, @PathVariable String user, @PathVariable String comment) {
        Parallel parallel = parallelService.getById(id);
        // 只有处于待提交状态的才可以提交
        if (!parallelHandler.getRejectedStatus().contains(parallel.getApprovalStatus())) {
            throw new FlowException("Current parallel not be rejected, can not submit again.");
        }
        parallelHandler.process(parallel, "Submit", comment, user, true, true);
        return parallel;
    }

    /**
     * 测试提交
     * http://localhost:8080/parallel/submit/5/user1/测试提交
     *
     * @param id      单据主键id
     * @param user    当前用户
     * @param comment 提交说明
     * @return 提交后的实体
     */
    @GetMapping("/submit/{id}/{user}/{comment}")
    public Parallel submit(@PathVariable Integer id, @PathVariable String user, @PathVariable String comment) {
        Parallel parallel = parallelService.getById(id);
        // 只有处于待提交状态的才可以提交
        if (!parallelHandler.getInitialStatus().equalsIgnoreCase(parallel.getApprovalStatus())) {
            throw new FlowException("Current parallel is not in initial status,  can not be submit.");
        }
        parallelHandler.process(parallel, "Submit", comment, user, true, true);
        return parallel;
    }

    /**
     * 测试保存
     * http://localhost:8080/parallel/save/lisi
     *
     * @param parallelName 名称
     * @return 新建的实体
     */
    @GetMapping("/save/{parallelName}")
    public Parallel save(@PathVariable String parallelName) {
        Parallel parallel = new Parallel().setName(parallelName);
        parallelHandler.initApprovalStatus(parallel);
        parallelService.save(parallel);
        return parallel;
    }

    /**
     * 列表查询
     * http://localhost:8080/parallel/list
     *
     * @return 实体列表
     */
    @GetMapping("/list")
    public List<Parallel> list() {
        return parallelService.list();
    }
}
