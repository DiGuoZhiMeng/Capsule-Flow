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
import com.capsule.flow.sample.entity.Branch;
import com.capsule.flow.sample.handlers.BranchHandler;
import com.capsule.flow.sample.rbac.EnforcerFactory;
import com.capsule.flow.sample.service.BranchService;
import com.capsule.flow.vo.FlowRootVo;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/branch")
public class BranchController {

    @Resource
    private BranchService branchService;

    @Resource
    private BranchHandler branchHandler;

    /**
     * 获取当前单据实体的流程可视化信息
     * http://localhost:8080/branch/getFlowMetaInfoV2/20
     *
     * @return FlowRootVo
     */
    @GetMapping("/getFlowMetaInfoV2/{id}")
    public FlowRootVo getFlowMetaInfoV2(@PathVariable Integer id) {
        Branch branch = branchService.getById(id);
        return branchHandler.getFlowMetaInfoV2(branch);
    }

    /**
     * 获取当前单据实体的流程可视化信息
     * http://localhost:8080/branch/getFlowMetaInfoV1/20
     *
     * @return FlowRootVo
     */
    @GetMapping("/getFlowMetaInfoV1/{id}")
    public FlowRootVo getFlowMetaInfoV1(@PathVariable Integer id) {
        Branch branch = branchService.getById(id);
        return branchHandler.getFlowMetaInfoV1(branch);
    }

    /**
     * 获取所有待我处理的数据：包括待提交的、待处理的
     * http://localhost:8080/branch/myTodo/user1
     *
     * @return List<Foo>
     */
    @GetMapping("/myTodo/{user}")
    public List<Branch> myTodo(@PathVariable String user) {
        List<String> userRoles = EnforcerFactory.getEnforcer().getRolesForUser(user);
        Set<String> status = branchHandler.getTodoStatus(TodoEnum.ALL, Sets.newHashSet(userRoles));
        String sql = branchHandler.todoSql(status, "branch.");
        if (StringUtils.isBlank(sql)) {
            return null;
        }
        return branchService.list(new LambdaQueryWrapper<Branch>().apply(sql));
    }

    /**
     * 获取所有待我处理的数据：不包括待提交的、只包括待处理的
     * http://localhost:8080/branch/myApproval/user1
     *
     * @return List<Parallel>
     */
    @GetMapping("/myApproval/{user}")
    public List<Branch> myApproval(@PathVariable String user) {
        List<String> userRoles = EnforcerFactory.getEnforcer().getRolesForUser(user);
        Set<String> status = branchHandler.getTodoStatus(TodoEnum.ONLY_PENDING_APPROVAL, Sets.newHashSet(userRoles));
        String sql = branchHandler.todoSql(status, "branch.");
        if (StringUtils.isBlank(sql)) {
            return null;
        }
        return branchService.list(new LambdaQueryWrapper<Branch>().apply(sql));
    }

    /**
     * 获取所有审批中的数据
     * http://localhost:8080/branch/pendingHandle
     *
     * @return List<Foo>
     */
    @GetMapping("/pendingHandle")
    public List<Branch> pendingHandle() {
        Set<String> status = branchHandler.getTodoStatus(TodoEnum.ONLY_PENDING_APPROVAL, null);
        String sql = branchHandler.todoSql(status, null);
        if (StringUtils.isBlank(sql)) {
            return null;
        }
        return branchService.list(new LambdaQueryWrapper<Branch>().apply(sql));
    }

    /**
     * 获取所有待提交的数据
     * http://localhost:8080/branch/pendingSubmit
     *
     * @return List<Foo>
     */
    @GetMapping("/pendingSubmit")
    public List<Branch> pendingSubmit() {
        Set<String> status = branchHandler.getTodoStatus(TodoEnum.ONLY_PENDING_SUBMIT, null);
        String sql = branchHandler.todoSql(status, null);
        if (StringUtils.isBlank(sql)) {
            return null;
        }
        return branchService.list(new LambdaQueryWrapper<Branch>().apply(sql));
    }

    /**
     * 判断是否有提交、审批等处理权限
     * http://localhost:8080/branch/verifyHandleAccess/8/user1
     *
     * @param id   单据主键id
     * @param user 当前用户
     * @return true有权限，false无权限
     */
    @GetMapping("/verifyHandleAccess/{id}/{user}")
    public boolean verifyHandleAccess(@PathVariable Integer id, @PathVariable String user) {
        Branch branch = branchService.getById(id);
        return branchHandler.verifyHandleAccess(branch, user);
    }

    /**
     * 根据单据id获取当前单据所有历史审批日志
     * http://localhost:8080/branch/historyLogAll/8
     *
     * @param id 单据主键id
     * @return 日志列表
     */
    @GetMapping("/historyLogAll/{id}")
    public List<FlowLog> historyLogAll(@PathVariable Long id) {
        Branch branch = branchService.getById(id);
        return branchHandler.getHistoryFlowLog(branch, false);
    }

    /**
     * 根据单据id获取当前单据当前审批回合内审批日志
     * http://localhost:8080/branch/historyLog/8
     *
     * @param id 单据主键id
     * @return 日志列表
     */
    @GetMapping("/historyLog/{id}")
    public List<FlowLog> historyLog(@PathVariable Long id) {
        Branch branch = branchService.getById(id);
        return branchHandler.getHistoryFlowLog(branch, true);
    }

    /**
     * 驳回测试
     * http://localhost:8080/branch/reject/8/user2/驳回测试
     *
     * @param id      单据主键id
     * @param user    当前用户
     * @param comment 审批意见
     * @return 驳回后的实体
     */
    @GetMapping("/reject/{id}/{user}/{comment}")
    public Branch reject(@PathVariable Integer id, @PathVariable String user, @PathVariable String comment) {
        Branch branch = branchService.getById(id);
        // 未处于待提交状态无法驳回或审批通过
        if (branchHandler.getInitialStatus().equalsIgnoreCase(branch.getApprovalStatus())) {
            throw new FlowException("Current branch is not submitted, can not be rejected.");
        }
        // 审批通过或者驳回了无法继续驳回
        if (branchHandler.getApprovedStatus().contains(branch.getApprovalStatus()) || branchHandler.getRejectedStatus().contains(branch.getApprovalStatus())) {
            throw new FlowException("Current branch is approved or rejected, can not reject it.");
        }
        branchHandler.process(branch, "Reject", comment, user, false, false);
        return branch;
    }

    /**
     * 审批通过测试
     * http://localhost:8080/branch/approve/5/user2/审批通过测试
     *
     * @param id      单据主键id
     * @param user    当前用户
     * @param comment 审批意见
     * @return 审批后的实体
     */
    @GetMapping("/approve/{id}/{user}/{comment}")
    public Branch approve(@PathVariable Integer id, @PathVariable String user, @PathVariable String comment) {
        Branch branch = branchService.getById(id);
        // 未处于待提交状态无法驳回或审批通过
        if (branchHandler.getInitialStatus().equalsIgnoreCase(branch.getApprovalStatus())) {
            throw new FlowException("Current branch is not submitted, can not be approved.");
        }
        // 审批通过或者驳回了无法继续审批通过
        if (branchHandler.getApprovedStatus().contains(branch.getApprovalStatus()) || branchHandler.getRejectedStatus().contains(branch.getApprovalStatus())) {
            throw new FlowException("Current branch is approved or rejected, can not approve it.");
        }
        branchHandler.process(branch, "Approve", comment, user, false, false);
        return branch;
    }

    /**
     * 测试驳回后再次提交
     * http://localhost:8080/branch/submitAfterRejected/8/user1/测试驳回后再次提交
     *
     * @param id      单据主键id
     * @param user    当前用户
     * @param comment 提交说明
     * @return 提交后的实体
     */
    @GetMapping("/submitAfterRejected/{id}/{user}/{comment}")
    public Branch submitAfterRejected(@PathVariable Long id, @PathVariable String user, @PathVariable String comment) {
        Branch branch = branchService.getById(id);
        // 只有处于待提交状态的才可以提交
        if (!branchHandler.getRejectedStatus().contains(branch.getApprovalStatus())) {
            throw new FlowException("Current branch not be rejected, can not submit again.");
        }
        branchHandler.process(branch, "Submit", comment, user, true, true);
        return branch;
    }

    /**
     * 测试提交
     * http://localhost:8080/branch/submit/5/user1/测试提交
     *
     * @param id      单据主键id
     * @param user    当前用户
     * @param comment 提交说明
     * @return 提交后的实体
     */
    @GetMapping("/submit/{id}/{user}/{comment}")
    public Branch submit(@PathVariable Integer id, @PathVariable String user, @PathVariable String comment) {
        Branch branch = branchService.getById(id);
        // 只有处于待提交状态的才可以提交
        if (!branchHandler.getInitialStatus().equalsIgnoreCase(branch.getApprovalStatus())) {
            throw new FlowException("Current branch is not in initial status,  can not be submit.");
        }
        branchHandler.process(branch, "Submit", comment, user, true, true);
        return branch;
    }

    /**
     * 测试保存
     * http://localhost:8080/branch/save/lisi/A
     *
     * @param name 名称
     * @param type 类型：A/B/C
     * @return 新建的实体
     */
    @GetMapping("/save/{name}/{type}")
    public Branch save(@PathVariable String name, @PathVariable String type) {
        Branch branch = new Branch().setName(name).setType(type);
        branchHandler.initApprovalStatus(branch);
        branchService.save(branch);
        return branch;
    }

    /**
     * 列表查询
     * http://localhost:8080/branch/list
     *
     * @return 实体列表
     */
    @GetMapping("/list")
    public List<Branch> list() {
        return branchService.list();
    }
}
