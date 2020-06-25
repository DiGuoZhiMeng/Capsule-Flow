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

package com.capsule.flow.sample.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * <pre>
 * 并签、会签流程测试实体
 * </pre>
 *
 * @author DiGuoZhiMeng
 * @since 2020-06-15
 */
@Data
@Accessors(chain = true)
public class Parallel {

    @TableId(value = "ID", type = IdType.AUTO)
    private Integer id;

    @TableField("NAME")
    private String name;

    @TableField("AROUND_ID")
    private Long aroundId;

    @TableField("APPROVAL_STATUS")
    private String approvalStatus;

    @TableField("APPROVAL_STATUS_JSON")
    private String approvalStatusJson;

    @TableField("LAST_SUBMIT_MESSAGE")
    private String lastSubmitMessage;

    @TableField("LAST_SUBMIT_BY")
    private String lastSubmitBy;

    @TableField("LAST_SUBMIT_DATE")
    private LocalDateTime lastSubmitDate;

    @TableField("LAST_AUDIT_MESSAGE")
    private String lastAuditMessage;

    @TableField("LAST_AUDIT_BY")
    private String lastAuditBy;

    @TableField("LAST_AUDIT_DATE")
    private LocalDateTime lastAuditDate;

    @TableField("DELETED")
    private Integer deleted;

    @TableField("CREATED_BY")
    private String createdBy;

    @TableField("CREATE_TIME")
    private LocalDateTime createTime;

    @TableField("UPDATED_BY")
    private String updatedBy;

    @TableField("UPDATE_TIME")
    private LocalDateTime updateTime;

}
