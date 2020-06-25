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

package com.capsule.flow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <pre>
 * 流程审批历史日志表
 * </pre>
 *
 * @author DiGuoZhiMeng
 * @since 2020-06-14
 */
@Data
@Accessors(chain = true)
public class FlowLog  implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    @TableField("FLOW_NAME")
    private String flowName;

    @TableField("ENTITY_NAME")
    private String entityName;

    @TableField("PREV_STATUS")
    private String prevStatus;

    @TableField("COMMENTS")
    private String comments;

    @TableField("ACTION")
    private String action;

    @TableField("NEXT_STATUS")
    private String nextStatus;

    @TableField("ORDER_ID")
    private Long orderId;

    @TableField("ROUND_ID")
    private Long roundId;

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
