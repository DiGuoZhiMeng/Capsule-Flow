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
import com.capsule.flow.sample.entity.Foo;
import com.capsule.flow.sample.handlers.FooSingleHandler;
import com.capsule.flow.sample.rbac.EnforcerFactory;
import com.capsule.flow.sample.service.FooService;
import com.capsule.flow.vo.FlowRootVo;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

@RestController
@Slf4j
@RequestMapping("/foo")
public class FooController {

    @Resource
    private FooService fooService;

    @Resource
    private FooSingleHandler fooSingleHandler;

    /**
     * 获取当前单据实体的流程可视化信息
     * http://localhost:8080/foo/getFlowMetaInfoV2/20
     *
     * @return FlowRootVo
     */
    @GetMapping("/getFlowMetaInfoV2/{id}")
    public FlowRootVo getFlowMetaInfoV2(@PathVariable Integer id) {
        Foo foo = fooService.getById(id);
        return fooSingleHandler.getFlowMetaInfoV2(foo);
    }

    /**
     * 获取当前单据实体的流程可视化信息
     * http://localhost:8080/foo/getFlowMetaInfoV1/20
     *
     * @return FlowRootVo
     */
    @GetMapping("/getFlowMetaInfoV1/{id}")
    public FlowRootVo getFlowMetaInfoV1(@PathVariable Integer id) {
        Foo foo = fooService.getById(id);
        return fooSingleHandler.getFlowMetaInfoV1(foo);
    }

    /**
     * 获取所有待我处理的数据：包括待提交的、待处理的
     * http://localhost:8080/foo/myTodo/user1
     *
     * @return List<Foo>
     */
    @GetMapping("/myTodo/{user}")
    public List<Foo> myTodo(@PathVariable String user) {
        List<String> userRoles = EnforcerFactory.getEnforcer().getRolesForUser(user);
        Set<String> status = fooSingleHandler.getTodoStatus(TodoEnum.ALL, Sets.newHashSet(userRoles));
        if (CollectionUtils.isEmpty(status)) {
            return null;
        }
        return fooService.list(new LambdaQueryWrapper<Foo>().in(Foo::getApprovalStatus, status));
    }

    /**
     * 获取所有待我处理的数据：不包括待提交的、只包括待处理的
     * http://localhost:8080/foo/myApproval/user1
     *
     * @return List<Parallel>
     */
    @GetMapping("/myApproval/{user}")
    public List<Foo> myApproval(@PathVariable String user) {
        List<String> userRoles = EnforcerFactory.getEnforcer().getRolesForUser(user);
        Set<String> status = fooSingleHandler.getTodoStatus(TodoEnum.ONLY_PENDING_APPROVAL, Sets.newHashSet(userRoles));
        if (CollectionUtils.isEmpty(status)) {
            return null;
        }
        return fooService.list(new LambdaQueryWrapper<Foo>().in(Foo::getApprovalStatus, status));
    }

    /**
     * 获取所有审批中的数据
     * http://localhost:8080/foo/pendingHandle
     *
     * @return List<Foo>
     */
    @GetMapping("/pendingHandle")
    public List<Foo> pendingHandle() {
        Set<String> status = fooSingleHandler.getTodoStatus(TodoEnum.ONLY_PENDING_APPROVAL, null);
        if (CollectionUtils.isEmpty(status)) {
            return null;
        }
        return fooService.list(new LambdaQueryWrapper<Foo>().in(Foo::getApprovalStatus, status));
    }

    /**
     * 获取所有待提交的数据
     * http://localhost:8080/foo/pendingSubmit
     *
     * @return List<Foo>
     */
    @GetMapping("/pendingSubmit")
    public List<Foo> pendingSubmit() {
        Set<String> status = fooSingleHandler.getTodoStatus(TodoEnum.ONLY_PENDING_SUBMIT, null);
        if (CollectionUtils.isEmpty(status)) {
            return null;
        }
        return fooService.list(new LambdaQueryWrapper<Foo>().in(Foo::getApprovalStatus, status));
    }

    /**
     * 判断是否有提交、审批等处理权限
     * http://localhost:8080/foo/verifyHandleAccess/8/user1
     *
     * @param id   单据主键id
     * @param user 当前用户
     * @return true有权限，false无权限
     */
    @GetMapping("/verifyHandleAccess/{id}/{user}")
    public boolean verifyHandleAccess(@PathVariable Integer id, @PathVariable String user) {
        Foo foo = fooService.getById(id);
        return fooSingleHandler.verifyHandleAccess(foo, user);
    }

    /**
     * 根据单据id获取当前单据所有历史审批日志
     * http://localhost:8080/foo/historyLogAll/8
     *
     * @param id 单据主键id
     * @return 日志列表
     */
    @GetMapping("/historyLogAll/{id}")
    public List<FlowLog> historyLogAll(@PathVariable Long id) {
        Foo foo = fooService.getById(id);
        return fooSingleHandler.getHistoryFlowLog(foo, false);
    }

    /**
     * 根据单据id获取当前单据当前审批回合内审批日志
     * http://localhost:8080/foo/historyLog/8
     *
     * @param id 单据主键id
     * @return 日志列表
     */
    @GetMapping("/historyLog/{id}")
    public List<FlowLog> historyLog(@PathVariable Long id) {
        Foo foo = fooService.getById(id);
        return fooSingleHandler.getHistoryFlowLog(foo, true);
    }

    /**
     * 驳回测试
     * http://localhost:8080/foo/reject/8/user2/驳回测试
     *
     * @param id      单据主键id
     * @param user    当前用户
     * @param comment 审批意见
     * @return 驳回后的实体
     */
    @GetMapping("/reject/{id}/{user}/{comment}")
    public Foo reject(@PathVariable Integer id, @PathVariable String user, @PathVariable String comment) {
        Foo foo = fooService.getById(id);
        // 未处于待提交状态无法驳回或审批通过
        if (fooSingleHandler.getInitialStatus().equalsIgnoreCase(foo.getApprovalStatus())) {
            throw new FlowException("Current foo is not submitted, can not be rejected.");
        }
        // 审批通过或者驳回了无法继续驳回
        if (fooSingleHandler.getApprovedStatus().contains(foo.getApprovalStatus()) || fooSingleHandler.getRejectedStatus().contains(foo.getApprovalStatus())) {
            throw new FlowException("Current foo is approved or rejected, can not reject it.");
        }
        fooSingleHandler.process(foo, "Reject", comment, user, false, false);
        return foo;
    }

    /**
     * 审批通过测试
     * http://localhost:8080/foo/approve/5/user2/审批通过测试
     *
     * @param id      单据主键id
     * @param user    当前用户
     * @param comment 审批意见
     * @return 审批后的实体
     */
    @GetMapping("/approve/{id}/{user}/{comment}")
    public Foo approve(@PathVariable Integer id, @PathVariable String user, @PathVariable String comment) {
        Foo foo = fooService.getById(id);
        // 未处于待提交状态无法驳回或审批通过
        if (fooSingleHandler.getInitialStatus().equalsIgnoreCase(foo.getApprovalStatus())) {
            throw new FlowException("Current foo is not submitted, can not be approved.");
        }
        // 审批通过或者驳回了无法继续审批通过
        if (fooSingleHandler.getApprovedStatus().contains(foo.getApprovalStatus()) || fooSingleHandler.getRejectedStatus().contains(foo.getApprovalStatus())) {
            throw new FlowException("Current foo is approved or rejected, can not approve it.");
        }
        fooSingleHandler.process(foo, "Approve", comment, user, false, false);
        return foo;
    }

    /**
     * 测试驳回后再次提交
     * http://localhost:8080/foo/submitAfterRejected/8/user1/测试驳回后再次提交
     *
     * @param id      单据主键id
     * @param user    当前用户
     * @param comment 提交说明
     * @return 提交后的实体
     */
    @GetMapping("/submitAfterRejected/{id}/{user}/{comment}")
    public Foo submitAfterRejected(@PathVariable Long id, @PathVariable String user, @PathVariable String comment) {
        Foo foo = fooService.getById(id);
        // 只有处于待提交状态的才可以提交
        if (!fooSingleHandler.getRejectedStatus().contains(foo.getApprovalStatus())) {
            throw new FlowException("Current foo not be rejected, can not submit again.");
        }
        fooSingleHandler.process(foo, "Submit", comment, user, true, true);
        return foo;
    }

    /**
     * 测试提交
     * http://localhost:8080/foo/submit/5/user1/测试提交
     *
     * @param id      单据主键id
     * @param user    当前用户
     * @param comment 提交说明
     * @return 提交后的实体
     */
    @GetMapping("/submit/{id}/{user}/{comment}")
    public Foo submit(@PathVariable Integer id, @PathVariable String user, @PathVariable String comment) {
        Foo foo = fooService.getById(id);
        // 只有处于待提交状态的才可以提交
        if (!fooSingleHandler.getInitialStatus().equalsIgnoreCase(foo.getApprovalStatus())) {
            throw new FlowException("Current foo is not in initial status,  can not be submit.");
        }
        fooSingleHandler.process(foo, "Submit", comment, user, true, true);
        return foo;
    }

    /**
     * 测试保存
     * http://localhost:8080/foo/save/lisi
     *
     * @param fooName 名称
     * @return 新建的实体
     */
    @GetMapping("/save/{fooName}")
    public Foo save(@PathVariable String fooName) {
        Foo foo = new Foo().setName(fooName);
        fooSingleHandler.initApprovalStatus(foo);
        fooService.save(foo);
        return foo;
    }

    /**
     * 列表查询
     * http://localhost:8080/foo/list
     *
     * @return 实体列表
     */
    @GetMapping("/list")
    public List<Foo> list() {
        return fooService.list();
    }
}
