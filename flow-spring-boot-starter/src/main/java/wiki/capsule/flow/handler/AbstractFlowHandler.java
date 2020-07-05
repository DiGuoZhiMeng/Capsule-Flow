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

package wiki.capsule.flow.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import wiki.capsule.flow.entity.FlowBasic;
import wiki.capsule.flow.entity.FlowLog;
import wiki.capsule.flow.entity.FlowRound;
import wiki.capsule.flow.enums.TodoEnum;
import wiki.capsule.flow.exception.FlowException;
import wiki.capsule.flow.service.FlowBasicService;
import wiki.capsule.flow.service.FlowLogService;
import wiki.capsule.flow.service.FlowRoundService;
import wiki.capsule.flow.utils.ClassUtils;
import wiki.capsule.flow.utils.RuleUtils;
import wiki.capsule.flow.vo.FlowMetaVo;
import wiki.capsule.flow.vo.FlowRootVo;
import wiki.capsule.flow.vo.FlowTraceVo;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <pre>
 * 流程处理抽象父类
 * </pre>
 *
 * @author DiGuoZhiMeng
 * @since 2020-06-14
 */
@Slf4j
public abstract class AbstractFlowHandler<T> {

    @Resource
    private FlowBasicService flowBasicService;
    @Resource
    private FlowLogService flowLogService;
    @Resource
    private FlowRoundService flowRoundService;

    private static final String EX_AND = "&&";
    private static final String EX_OR = "||";
    private static final String EX_OR_ESCAPE = "\\|\\|";

    /**
     * 流程入口处理方法
     *
     * @param bizEntity 实体对象
     * @param comment   提交或者审批意见
     * @param action    审批动作，如提交、驳回、通过，根据实际流程配置传入
     * @param user      处理用户
     * @param isSubmit  是否是提交动作，用于判断是否重新开启一个新的审批回合
     * @return List<FlowBasic> 返回本次用户处理的所有环节
     */
    public List<FlowBasic> process(T bizEntity, String action, String comment, String user, boolean isSubmit, boolean isRestart) {
        // 校验参数
        verifyParam(bizEntity, action, comment, user);

        // 判断是否需要新开启一轮新的审批
        Long flowRoundId = setFlowRoundId(bizEntity, user, isRestart);

        // 获取当前的审批状态
        String approvalStatus = ClassUtils.getStringValue(bizEntity, getApprovalStatusField());
        if (StringUtils.isBlank(approvalStatus)) {
            throw new FlowException(10016, "You have not initial approvalStatus field.");
        }

        // 获取当前状态下的，当前审批动作时的所有审批环节，单签时只有一个，会签或者并签时会有多个，并签时不包括已经审批完毕的审批环节
        List<FlowBasic> currentFlows = findCurrentFlows(approvalStatus, action, flowRoundId);
        if (CollectionUtils.isEmpty(currentFlows)) {
            throw new FlowException(10017, "Next flow not found before " + approvalStatus + " with action " + action + ".");
        }

        // 校验权限
        List<FlowBasic> canHandleFlows = authPermission(currentFlows, user);
        if (CollectionUtils.isEmpty(canHandleFlows)) {
            throw new FlowException(10009, "Current user does not have permission to handle this flow.");
        }

        // 开始流程处理并返回下一个节点
        String nextStatus = startProcess(currentFlows, canHandleFlows, bizEntity);

        // 保存审批流字段
        if (isSubmit) {
            setSubmitField(bizEntity, comment, user);
        } else {
            setAuditField(bizEntity, comment, user);
        }

        // 保存审批日志
        saveFlowLogs(canHandleFlows, nextStatus, comment, user, ClassUtils.getLongValue(bizEntity, getIdFiled()), flowRoundId);

        afterProcess(bizEntity);
        return canHandleFlows;
    }

    /**
     * 审批流处理后的回调方法，比如需要持久化到数据库的操作，可以放到此方法中进行
     *
     * @param bizEntity 业务实体
     */
    protected void afterProcess(T bizEntity) {

    }

    protected void saveFlowLogs(List<FlowBasic> canHandleFlows, String nextStatus, String comment, String user, Long orderId, Long flowRoundId) {
        List<FlowLog> logs = new ArrayList<>();
        for (FlowBasic flowBasic : canHandleFlows) {
            logs.add(new FlowLog().setEntityName(getEntityName()).setFlowName(getFlowName())
                    .setNextStatus(nextStatus).setComments(comment).setRoundId(flowRoundId)
                    .setPrevStatus(flowBasic.getPrevStatus()).setCreatedBy(user).setOrderId(orderId)
                    .setAction(flowBasic.getAction()));
        }
        flowLogService.saveBatch(logs);
    }

    protected void setAuditField(T bizEntity, String comment, String user) {
        ClassUtils.setFieldValue(bizEntity, getLastAuditByField(), user);
        ClassUtils.setFieldValue(bizEntity, getLastAuditDateFiled(), LocalDateTime.now());
        ClassUtils.setFieldValue(bizEntity, getLastAuditMessageFiled(), comment);
    }

    protected void setSubmitField(T bizEntity, String comment, String user) {
        ClassUtils.setFieldValue(bizEntity, getLastSubmitByField(), user);
        ClassUtils.setFieldValue(bizEntity, getLastSubmitDateFiled(), LocalDateTime.now());
        ClassUtils.setFieldValue(bizEntity, getLastSubmitMessageFiled(), comment);
    }

    /**
     * 开始处理流程
     *
     * @param currentFlows   当前所有可处理环节
     * @param canHandleFlows 用户有权限处理的环节
     * @param bizEntity      业务实体
     * @return 下一个审批状态
     */
    protected String startProcess(List<FlowBasic> currentFlows, List<FlowBasic> canHandleFlows, T bizEntity) {
        String approvalStatus = ClassUtils.getStringValue(bizEntity, getApprovalStatusField());
        String approvalStatusJson = ClassUtils.getStringValue(bizEntity, getApprovalStatusJsonField());
        if (StringUtils.isBlank(approvalStatus) || StringUtils.isBlank(approvalStatusJson)) {
            throw new FlowException(10016, "You have not initial approvalStatus or approvalStatusJson field.");
        }
        JSONObject json = JSONObject.parseObject(approvalStatusJson);
        String nextStatus;
        // 并签场景，且没有审批完成，且是正向操作，需要等待所有节点都处理完成
        if (approvalStatus.contains(EX_AND) && currentFlows.size() > canHandleFlows.size() && canHandleFlows.get(0).getTaskOrder() > 0) {
            for (FlowBasic flowBasic : canHandleFlows) {
                json.put(flowBasic.getPrevStatus(), 1);
            }
            nextStatus = canHandleFlows.get(0).getNextStatus();
            ClassUtils.setFieldValue(bizEntity, getApprovalStatusJsonField(), JSON.toJSONString(json));
            return nextStatus;
        }
        nextStatus = findNextStatus(bizEntity, canHandleFlows.get(0).getNextStatus());
        ClassUtils.setFieldValue(bizEntity, getApprovalStatusField(), nextStatus);
        json = new JSONObject();
        if (nextStatus.contains(EX_AND)) {
            String[] steps = nextStatus.split(EX_AND);
            for (String step : steps) {
                json.put(step, 0);
            }
        } else if (nextStatus.contains(EX_OR)) {
            String[] steps = nextStatus.split(EX_OR_ESCAPE);
            for (String step : steps) {
                json.put(step, 0);
            }
        } else {
            json.put(nextStatus, 0);
        }
        ClassUtils.setFieldValue(bizEntity, getApprovalStatusJsonField(), json.toJSONString());
        return nextStatus;
    }

    /**
     * 如果NEXT STATUS是表达式，需要使用beetl规则引擎工具类解析一下
     *
     * @param bizEntity  业务实体
     * @param nextStatus 下一个状态
     * @return 解析后的下一个状态
     */
    protected String findNextStatus(T bizEntity, String nextStatus) {
        if (nextStatus.startsWith("<%") && nextStatus.endsWith("%>")) {
            return RuleUtils.render(nextStatus, "vo", bizEntity);
        } else {
            return nextStatus;
        }
    }

    /**
     * 根据当前用户，一级FlowBasic的handleRoles信息校验权限，返回有权限处理的Flow列表，无权限返回空即可，交给用户在子类中实现
     *
     * @param currentFlows 当前状态下的所有待处理环节
     * @param user         当前操作用户
     * @return
     */
    protected abstract List<FlowBasic> authPermission(List<FlowBasic> currentFlows, String user);

    /**
     * 获取当前状态下的，当前审批动作时的所有审批环节，单签时只有一个，会签或者并签时会有多个，并签时不包括已经审批完毕的审批环节
     *
     * @param approvalStatus 当前状态
     * @param action         提交、审批或驳回等
     * @param roundId        回合id，用于并签时，通过查询审批日志判断哪些已经审批完成了，过滤掉
     * @return 当前审批状态下所有需要处理的审批环节
     */
    protected List<FlowBasic> findCurrentFlows(String approvalStatus, String action, Long roundId) {
        List<FlowBasic> flowBasicList = new ArrayList<>();
        List<String> waitingForHandledSteps = new ArrayList<>();
        if (approvalStatus.contains(EX_AND)) {
            String[] steps = approvalStatus.split(EX_AND);
            for (String step : steps) {
                FlowLog flowLog = flowLogService.getOne(new LambdaQueryWrapper<FlowLog>()
                        .eq(FlowLog::getRoundId, roundId)
                        .eq(StringUtils.isNotBlank(action), FlowLog::getAction, action)
                        .eq(FlowLog::getPrevStatus, step));
                if (flowLog == null) {
                    waitingForHandledSteps.add(step);
                }
            }

        } else if (approvalStatus.contains(EX_OR)) {
            String[] steps = approvalStatus.split(EX_OR_ESCAPE);
            waitingForHandledSteps.addAll(Arrays.asList(steps));
        } else {
            waitingForHandledSteps.add(approvalStatus);
        }
        if (CollectionUtils.isEmpty(waitingForHandledSteps)) {
            throw new FlowException(10008, "No waiting for handle steps.");
        }
        flowBasicList.addAll(flowBasicService.list(new LambdaQueryWrapper<FlowBasic>()
                .in(StringUtils.isNotBlank(action), FlowBasic::getAction, action)
                .in(FlowBasic::getPrevStatus, waitingForHandledSteps)
                .eq(FlowBasic::getEntityName, getEntityName())
                .eq(FlowBasic::getFlowName, getFlowName())));
        return flowBasicList;
    }

    /**
     * 判断是否需要新开启一轮新的审批，如果是则新生成一个Round id
     * 当需要记录审批前后单据对象字段值变化情况时，需要子类复写，主要将变化前后的json字符串保存至ChangeRecord字段中
     *
     * @param bizEntity 业务实体
     * @param user      操作用户
     * @param isSubmit  是否是提交动作，用于判断是否重新开启一个新的审批回合
     * @return flowRoundId
     */
    protected Long setFlowRoundId(T bizEntity, String user, boolean isSubmit) {
        if (isSubmit) { // 开启新的回合则创建新的FlowRound
            FlowRound flowRound = new FlowRound().setCreatedBy(user).setChangeRecord(null);
            flowRoundService.save(flowRound); // 保存自动生成ID
            ClassUtils.setFieldValue(bizEntity, getAroundIdField(), flowRound.getId());
            return flowRound.getId();
        }
        // 不需要开启新的回合则返回旧的FlowRound
        return ClassUtils.getLongValue(bizEntity, getAroundIdField());
    }

    /**
     * 默认的校验参数，实际业务如果有特殊要求，可在子类中复写
     *
     * @param bizEntity 业务实体
     * @param comment   提交备注或审批意见
     * @param action    提交、驳回、通过等
     * @param user      操作用户
     */
    protected void verifyParam(T bizEntity, String action, String comment, String user) {
        if (StringUtils.isBlank(getFlowName())) {
            throw new FlowException(10001, "Flow name can not be empty.");
        }
        if (StringUtils.isBlank(getEntityName())) {
            throw new FlowException(10002, "Flow entity name can not be empty.");
        }
        if (bizEntity == null) {
            throw new FlowException(10003, "Business entity can not be empty.");
        }
        if (StringUtils.isBlank(action)) {
            throw new FlowException(10004, "Handle action can not be empty.");
        }
        if (StringUtils.isBlank(user)) {
            throw new FlowException(10005, "Hander user can not be empty.");
        }
    }

    /**
     * 初始化approvalStatus和approvalStatusJson字段为初始状态值
     *
     * @param bizEntity 业务实体
     */
    public void initApprovalStatus(T bizEntity) {
        FlowBasic flowBasic = flowBasicService.getOne(new LambdaQueryWrapper<FlowBasic>()
                .eq(FlowBasic::getFlowName, getFlowName()).eq(FlowBasic::getTaskOrder, 1)
                .eq(FlowBasic::getEntityName, getEntityName()));
        if (flowBasic == null) {
            throw new FlowException(10017, "You have not config the first task for current flow:" + getFlowName());
        }
        ClassUtils.setFieldValue(bizEntity, getApprovalStatusField(), flowBasic.getPrevStatus());
        JSONObject json = new JSONObject();
        json.put(flowBasic.getPrevStatus(), 0);
        ClassUtils.setFieldValue(bizEntity, getApprovalStatusJsonField(), json.toJSONString());
    }

    /**
     * 获取初始提交状态，Task Order为1的记录的prev status
     *
     * @return
     */
    public String getInitialStatus() {
        List<FlowBasic> flowBasics = flowBasicService.list(new LambdaQueryWrapper<FlowBasic>().eq(FlowBasic::getFlowName, getFlowName())
                .eq(FlowBasic::getEntityName, getEntityName()).eq(FlowBasic::getTaskOrder, 1));
        if (CollectionUtils.isEmpty(flowBasics)) {
            throw new FlowException(10018, "Flow with task order 1 not found.");
        }
        if (flowBasics.size() > 1) {
            throw new FlowException(10019, "Only one flow with task order 1.");
        }
        return flowBasics.get(0).getPrevStatus();
    }

    /**
     * 获取所有最终审批通过的状态，即所有Last Task为1的next status集合
     *
     * @return
     */
    public Set<String> getApprovedStatus() {
        List<FlowBasic> flowBasics = flowBasicService.list(new LambdaQueryWrapper<FlowBasic>().eq(FlowBasic::getFlowName, getFlowName())
                .eq(FlowBasic::getEntityName, getEntityName()).eq(FlowBasic::getLastTask, 1));
        if (CollectionUtils.isEmpty(flowBasics)) {
            throw new FlowException(10020, "There are not flow with task order 100.");
        }
        Set<String> status = new HashSet<>();
        for (FlowBasic flowBasic : flowBasics) {
            if (StringUtils.isNotBlank(flowBasic.getNextStatus())) {
                status.add(flowBasic.getNextStatus());
            }
        }
        return status;
    }

    /**
     * 获取所有最终驳回的状态，即所有Last Task为-1的next status集合
     *
     * @return
     */
    public Set<String> getRejectedStatus() {
        List<FlowBasic> flowBasics = flowBasicService.list(new LambdaQueryWrapper<FlowBasic>().eq(FlowBasic::getFlowName, getFlowName())
                .eq(FlowBasic::getEntityName, getEntityName()).eq(FlowBasic::getLastTask, -1));
        if (CollectionUtils.isEmpty(flowBasics)) {
            throw new FlowException(10021, "There are not flow with task order -1.");
        }
        Set<String> status = new HashSet<>();
        for (FlowBasic flowBasic : flowBasics) {
            if (StringUtils.isNotBlank(flowBasic.getNextStatus())) {
                status.add(flowBasic.getNextStatus());
            }
        }
        return status;
    }

    /**
     * 判断当前用户是否有权限提交或者审批当前单据对象
     *
     * @param bizEntity 单据实体
     * @param user      当前用户
     * @return true表示有权限
     */
    public boolean verifyHandleAccess(T bizEntity, String user) {
        if (bizEntity == null || StringUtils.isBlank(user)) {
            throw new FlowException(10022, "Business entity or user can not be empty when verify whether have handle access.");
        }
        String approvalStatus = ClassUtils.getStringValue(bizEntity, getApprovalStatusField());
        Long roundId = ClassUtils.getLongValue(bizEntity, getAroundIdField());
        List<FlowBasic> currentSteps = findCurrentFlows(approvalStatus, null, roundId);
        if (CollectionUtils.isEmpty(currentSteps)) {
            return false;
        }
        List<FlowBasic> canHandleFlow = authPermission(currentSteps, user);
        return CollectionUtils.isNotEmpty(canHandleFlow);
    }

    /**
     * 获取待办状态
     *
     * @param todoEnum todoEnum
     * @param roles    是否限制按当前用户角色进行限制
     * @return 所有待办状态集合
     */
    public Set<String> getTodoStatus(TodoEnum todoEnum, Set<String> roles) {
        Set<String> status = new HashSet<>();
        LambdaUpdateWrapper<FlowBasic> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(FlowBasic::getFlowName, getFlowName());

        if (todoEnum == TodoEnum.ONLY_PENDING_SUBMIT) {
            wrapper.in(FlowBasic::getTaskOrder, 0, 1);
        } else if (todoEnum == TodoEnum.ONLY_PENDING_APPROVAL) {
            wrapper.ne(FlowBasic::getTaskOrder, 0).ne(FlowBasic::getTaskOrder, 1);
        }
        List<FlowBasic> flowBasics = flowBasicService.list(wrapper);
        for (FlowBasic flowBasic : flowBasics) {
            if (StringUtils.isBlank(flowBasic.getHandleRoles())) {
                continue;
            }
            if (CollectionUtils.isEmpty(roles)) {
                status.add(flowBasic.getPrevStatus());
            } else {
                JSONArray array = JSON.parseArray(flowBasic.getHandleRoles());
                for (int i = 0; i < array.size(); i++) {
                    if (roles.contains(array.getString(i))) {
                        status.add(flowBasic.getPrevStatus());
                        break;
                    }
                }
            }
        }
        return status;
    }

    /**
     * 根据状态集合和APPROVAL_STATUS_JSON字段拼装查询sql语句，如果状态集合为空则直接返回空字符串
     *
     * @param statusSet 状态集合
     * @param prefix    是否限制表前缀，“表名.”
     * @return Sql，例如：(APPROVAL_STATUS_JSON ->'$."Pending Submit"' = 0 or APPROVAL_STATUS_JSON ->'$."C"' = 0 or APPROVAL_STATUS_JSON ->'$."A"' = 0)
     */
    public String todoSql(Set<String> statusSet, String prefix) {
        if (CollectionUtils.isEmpty(statusSet)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        int i = 0;
        for (String status : statusSet) {
            if (StringUtils.isNotBlank(prefix)) {
                sb.append(prefix);
            }
            sb.append(camelCase2LineSeparation(getApprovalStatusJsonField())).append(" -> '$.\"").append(status).append("\"' = 0");
            if (i != statusSet.size() - 1) {
                sb.append(" or ");
            }
            i++;
        }
        sb.append(")");
        return sb.toString();
    }

    private static String camelCase2LineSeparation(String camelWord) {
        Matcher matcher = Pattern.compile("([A-Z][a-z])|([a-z][A-Z])").matcher(camelWord);

        StringBuffer word = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(word, matcher.group(0).replaceAll("(.$)", "_$1"));
        }
        matcher.appendTail(word);

        return word.toString().toUpperCase();
    }

    /**
     * 获取审批流元数据信息，用于前端流程可视化展示
     * 查询出所有正向流程梳理，即taskOrder>0
     * 允许非有向无环图的场景
     * 如果是通过规则引擎判断分支的场景需要在nextStatusAlias中配置json字段
     *
     * @param bizEntity 业务实体
     * @return FlowRootVo
     */
    public FlowRootVo getFlowMetaInfoV1(T bizEntity) {
        if (bizEntity == null) {
            throw new FlowException(10023, "Business entity can not be empty when get flow meta info.");
        }
        FlowRootVo rootVo = new FlowRootVo();
        String approvalStatus = ClassUtils.getStringValue(bizEntity, getApprovalStatusField());
        JSONObject approvalStatusJson = JSONObject.parseObject(ClassUtils.getStringValue(bizEntity, getApprovalStatusJsonField()));
        approvalStatusJson.put("current_status", approvalStatus);
        rootVo.setApprovalStatus(approvalStatusJson);
        List<FlowMetaVo> flowMetaVoList = flowBasicService.getFlowMetaInfoV1(getFlowName());
        rootVo.setFlowMetaVoList(flowMetaVoList);
        List<FlowLog> historyLog = flowLogService.list(new LambdaQueryWrapper<FlowLog>().eq(FlowLog::getFlowName, getFlowName())
                .eq(FlowLog::getEntityName, getEntityName())
                .eq(FlowLog::getRoundId, ClassUtils.getLongValue(bizEntity, getAroundIdField()))
                .eq(FlowLog::getOrderId, ClassUtils.getLongValue(bizEntity, getIdFiled()))
                .orderByAsc(FlowLog::getCreateTime));
        if (CollectionUtils.isNotEmpty(historyLog)) {
            List<FlowTraceVo> flowTraceVos = new ArrayList<>();
            for (FlowLog log : historyLog) {
                flowTraceVos.add(new FlowTraceVo().setPrevStatus(log.getPrevStatus()).setNextStatus(log.getNextStatus()).setAction(log.getAction()));
            }
            rootVo.setFlowTraceVoList(flowTraceVos);
        }
        return rootVo;
    }

    /**
     * 获取审批流元数据信息，用于前端流程可视化展示
     * 查询出所有正向流程梳理，即taskOrder>0，按先后排序后返回
     * 必须是有向无环图的场景
     * 如果是通过规则引擎判断分支的场景需要通过业务实体的实际值判断出分支走向，把实际要走过的最终线路返回
     *
     * @param bizEntity 业务实体
     * @return FlowRootVo
     */
    public FlowRootVo getFlowMetaInfoV2(T bizEntity) {
        if (bizEntity == null) {
            throw new FlowException(10023, "Business entity can not be empty when get flow meta info.");
        }
        FlowRootVo rootVo = new FlowRootVo();
        String approvalStatus = ClassUtils.getStringValue(bizEntity, getApprovalStatusField());
        JSONObject approvalStatusJson = JSONObject.parseObject(ClassUtils.getStringValue(bizEntity, getApprovalStatusJsonField()));
        approvalStatusJson.put("current_status", approvalStatus);
        rootVo.setApprovalStatus(approvalStatusJson);
        List<FlowMetaVo> flowMetaVoList = flowBasicService.getFlowMetaInfoV2(getFlowName(), bizEntity);
        rootVo.setFlowMetaVoList(flowMetaVoList);
        List<FlowLog> historyLog = flowLogService.list(new LambdaQueryWrapper<FlowLog>().eq(FlowLog::getFlowName, getFlowName())
                .eq(FlowLog::getEntityName, getEntityName())
                .eq(FlowLog::getRoundId, ClassUtils.getLongValue(bizEntity, getAroundIdField()))
                .eq(FlowLog::getOrderId, ClassUtils.getLongValue(bizEntity, getIdFiled()))
                .orderByAsc(FlowLog::getCreateTime));
        if (CollectionUtils.isNotEmpty(historyLog)) {
            List<FlowTraceVo> flowTraceVos = new ArrayList<>();
            for (FlowLog log : historyLog) {
                flowTraceVos.add(new FlowTraceVo().setPrevStatus(log.getPrevStatus()).setNextStatus(log.getNextStatus()).setAction(log.getAction()));
            }
            rootVo.setFlowTraceVoList(flowTraceVos);
        }
        return rootVo;
    }

    /**
     * 获取当前单据的所有历史审批日志，按时间降序排列
     *
     * @param bizEntity     单据id
     * @param currentAround 是否限制为当前审批回合内的日志
     * @return
     */
    public List<FlowLog> getHistoryFlowLog(T bizEntity, boolean currentAround) {
        return flowLogService.list(new LambdaQueryWrapper<FlowLog>().eq(FlowLog::getFlowName, getFlowName())
                .eq(FlowLog::getEntityName, getEntityName())
                .eq(currentAround, FlowLog::getRoundId, ClassUtils.getLongValue(bizEntity, getAroundIdField()))
                .eq(FlowLog::getOrderId, ClassUtils.getLongValue(bizEntity, getIdFiled()))
                .orderByDesc(FlowLog::getCreateTime));
    }

    public String getIdFiled() {
        return "id";
    }

    public String getLastAuditMessageFiled() {
        return "lastAuditMessage";
    }

    public String getLastAuditByField() {
        return "lastAuditBy";
    }

    public String getLastAuditDateFiled() {
        return "lastAuditDate";
    }

    public String getLastSubmitMessageFiled() {
        return "lastSubmitMessage";
    }

    public String getLastSubmitByField() {
        return "lastSubmitBy";
    }

    public String getLastSubmitDateFiled() {
        return "lastSubmitDate";
    }

    /**
     * 获取当前审批状态字段名称，默认为approvalStatus，当一个实体需要同时开启多条审批流是需要返回不同字段作为区分
     *
     * @return 流程名称
     */
    public String getApprovalStatusField() {
        return "approvalStatus";
    }

    /**
     * 获取当前审批状态json字段名称，默认为approvalStatusJson，当一个实体需要同时开启多条审批流是需要返回不同字段作为区分
     *
     * @return 流程名称
     */
    public String getApprovalStatusJsonField() {
        return "approvalStatusJson";
    }

    /**
     * 获取当前流程处理器的流程名称，默认为flowAroundId，当一个实体需要同时开启多条审批流是需要返回不同字段作为区分
     *
     * @return 流程名称
     */
    public String getAroundIdField() {
        return "aroundId";
    }

    /**
     * 获取当前流程处理器的流程名称，不同的审批流要使用不同名称进行区分
     *
     * @return 流程名称
     */
    protected abstract String getFlowName();

    /**
     * 获取当前流程处理器的实体名称
     *
     * @return 实体名称
     */
    protected abstract String getEntityName();

}
