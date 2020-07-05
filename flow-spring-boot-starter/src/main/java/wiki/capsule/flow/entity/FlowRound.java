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

package wiki.capsule.flow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <pre>
 * 流程审批回合及数据变动记录表
 * </pre>
 *
 * @author DiGuoZhiMeng
 * @since 2020-06-14
 */
@Data
@Accessors(chain = true)
public class FlowRound implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.AUTO)
    private Long id;

    @TableField("CHANGE_RECORD")
    private String changeRecord;

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
