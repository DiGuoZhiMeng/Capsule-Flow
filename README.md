### Capsule-Flow是一套集成Spring boot + Mybatis plus + Beetl的审批流（电子流）快速开发框架
> spring-boot.version 2.2.0.RELEASE

> mybatis-plus-boot-starter.version 3.2.0

## 目标
> 轻量化流程引擎，一个抽象基类，三张表构建起引擎核心，并可以在此基类上进行灵活定制和扩展。

#### [GITHUB](https://github.com/DiGuoZhiMeng/Capsule-Flow) | [GITEE](https://gitee.com/DiGuoZhiMeng/Capsule-Flow)

#### 

### 主要特性
- 可对任意单据实体开启审批流，同一单据实体也可以同时开启多条审批流
- 基于[Beetl模板引擎](http://ibeetl.com/)实现审批分支的动态控制
- 支持单签、并签（一个节点多个角色审批，但需要所有角色都审批通过才审批通过）及会签（一个节点多个角色审批，但有一个角色审批后即审批通过）复杂审批场景
- 自动记录详细的审批流历史日志，提供便捷的查询接口
- 自定义审批权限判断
- 可获取审批流元数据信息及审批过程数据，可用于前端可视化展示
- 支持已审批通过的节点自动跳过

## 项目结构
```text
    ├── Capsule-Flow
    ├── flow-spring-boot-starter                   核心模块
    └── flow-spring-boot-starter-sample            示例模块，以Foo、Parallel、Branch这3个业务实体，展示单签、会签及并签、多分支场景的使用方法
```

### 项目环境 
中间件 | 版本 |  备注
-|-|-
JDK | 1.8+ | JDK1.8及以上 |
MySQL | 5.7+ | 5.7.7及以上，需要支持json格式 |

### 技术选型 
技术 | 版本 |  备注
-|-|-
Spring Boot | 2.2.0.RELEASE | 最新发布稳定版 |
Mybatis | 3.5.3 | 持久层框架 |
Mybatis Plus | 3.3.1 | mybatis增强框架 |
Beetl | 3.1.7.RELEASE | 模板引擎，用于处理分支规则 |
Fastjson | 1.2.70 | JSON处理工具集 |
commons-lang3 | 3.9 | 常用工具包 |
commons-collections4 | 4.4 | 集合工具包 |
lombok | 1.18.12 | 注解生成Java Bean等工具 |

## 快速开始
### 克隆 Capsule-Flow
```bash
git clone https://gitee.com/DiGuoZhiMeng/Capsule-Flow
cd Capsule-Flow
```

## 5分钟完成审批流创建

### 1. 创建审批流3张基础数据库表
```sql
-- ----------------------------
-- Table structure for flow_basic
-- ----------------------------
DROP TABLE IF EXISTS `flow_basic`;
CREATE TABLE `flow_basic` (
  `ID` int(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `FLOW_NAME` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '流程名称',
  `ENTITY_NAME` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 's实体名称',
  `PREV_STATUS` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '上一个状态',
  `PREV_STATUS_ALIAS` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '上一个状态别名',
  `ACTION` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '审批动作：提交、通过、驳回等',
  `NEXT_STATUS` varchar(10000) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '下一个状态',
  `NEXT_STATUS_ALIAS` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '下一个状态别名',
  `HANDLE_ROLES` varchar(300) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审批角色列表，json数组',
  `TASK_ORDER` int(11) NOT NULL DEFAULT '0' COMMENT '是否是流程起点：1-是起始节点；大于1表示正向审批通过操作；0表示驳回或通过后重新提交操作；小于0表示逆向驳回操作',
  `LAST_TASK` int(11) NOT NULL DEFAULT '0' COMMENT '是否是流程终点：0-表示非流程最终节点；1表示最终审批通过节点；-1表示最终驳回的节点',
  `DELETED` int(11) NOT NULL DEFAULT '0' COMMENT '逻辑删除，0：未删除，1：已删除',
  `CREATED_BY` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人',
  `CREATE_TIME` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `UPDATED_BY` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新人',
  `UPDATE_TIME` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程基础信息表';

-- ----------------------------
-- Table structure for flow_log
-- ----------------------------
DROP TABLE IF EXISTS `flow_log`;
CREATE TABLE `flow_log` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `FLOW_NAME` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '流程名称',
  `ENTITY_NAME` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '实体名称',
  `PREV_STATUS` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '上一个状态',
  `COMMENTS` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审批意见',
  `ACTION` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '审批动作：提交、通过、驳回等',
  `NEXT_STATUS` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '下一个状态',
  `ORDER_ID` bigint(20) NOT NULL COMMENT '单据主键id',
  `ROUND_ID` bigint(20) NOT NULL COMMENT '回合ID',
  `DELETED` int(11) NOT NULL DEFAULT '0' COMMENT '逻辑删除，0：未删除，1：已删除',
  `CREATED_BY` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人',
  `CREATE_TIME` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `UPDATED_BY` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新人',
  `UPDATE_TIME` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程审批历史日志表';

-- ----------------------------
-- Table structure for flow_round
-- ----------------------------
DROP TABLE IF EXISTS `flow_round`;
CREATE TABLE `flow_round` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `CHANGE_RECORD` json DEFAULT NULL COMMENT '单据字段值审批前后变动记录',
  `DELETED` int(11) NOT NULL DEFAULT '0' COMMENT '逻辑删除，0：未删除，1：已删除',
  `CREATED_BY` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人',
  `CREATE_TIME` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `UPDATED_BY` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新人',
  `UPDATE_TIME` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程审批回合及数据变动记录表';

```
### 2. 创建测试数据库表：
> Foo单签场景实体对应的数据库表，其中AROUND_ID、APPROVAL_STATUS、APPROVAL_STATUS_JSON、LAST_SUBMIT_MESSAGE、LAST_SUBMIT_BY、LAST_SUBMIT_DATE、LAST_AUDIT_MESSAGE、LAST_AUDIT_BY、LAST_AUDIT_BY是起一条审批流必需字段字段，如果同一实体需要同时起多条审批流，需要配置多套，实际场景很少。

```sql
-- ----------------------------
-- Table structure for foo
-- ----------------------------
DROP TABLE IF EXISTS `foo`;
CREATE TABLE `foo` (
  `ID` int(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `NAME` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '流程名称',
  `AROUND_ID` bigint(20) DEFAULT NULL COMMENT '回合id',
  `APPROVAL_STATUS` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '当前审批状态',
  `APPROVAL_STATUS_JSON` json DEFAULT NULL COMMENT '当前审批状态json字段',
  `LAST_SUBMIT_MESSAGE` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最后一次提交说明',
  `LAST_SUBMIT_BY` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最后一次提交人',
  `LAST_SUBMIT_DATE` timestamp(6) NULL DEFAULT NULL COMMENT '最后一次提交时间',
  `LAST_AUDIT_MESSAGE` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最后一次审批意见',
  `LAST_AUDIT_BY` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最后一次审批人',
  `LAST_AUDIT_DATE` timestamp(6) NULL DEFAULT NULL COMMENT '最后一次审批时间',
  `DELETED` int(11) NOT NULL DEFAULT '0' COMMENT '逻辑删除，0：未删除，1：已删除',
  `CREATED_BY` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人',
  `CREATE_TIME` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `UPDATED_BY` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新人',
  `UPDATE_TIME` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流程基础信息表';

```

> Parallel会签、并签场景实体对应的数据库表，其中AROUND_ID、APPROVAL_STATUS、APPROVAL_STATUS_JSON、LAST_SUBMIT_MESSAGE、LAST_SUBMIT_BY、LAST_SUBMIT_DATE、LAST_AUDIT_MESSAGE、LAST_AUDIT_BY、LAST_AUDIT_BY是起一条审批流必需字段字段，如果同一实体需要同时起多条审批流，需要配置多套，实际场景很少。

```sql
-- ----------------------------
-- Table structure for parallel
-- ----------------------------
DROP TABLE IF EXISTS `parallel`;
CREATE TABLE `parallel` (
  `ID` int(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `NAME` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '实体名称',
  `AROUND_ID` bigint(20) DEFAULT NULL COMMENT '回合id',
  `APPROVAL_STATUS` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '当前审批状态',
  `APPROVAL_STATUS_JSON` json DEFAULT NULL COMMENT '当前审批状态json字段',
  `LAST_SUBMIT_MESSAGE` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最后一次提交说明',
  `LAST_SUBMIT_BY` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最后一次提交人',
  `LAST_SUBMIT_DATE` timestamp(6) NULL DEFAULT NULL COMMENT '最后一次提交时间',
  `LAST_AUDIT_MESSAGE` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最后一次审批意见',
  `LAST_AUDIT_BY` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最后一次审批人',
  `LAST_AUDIT_DATE` timestamp(6) NULL DEFAULT NULL COMMENT '最后一次审批时间',
  `DELETED` int(11) NOT NULL DEFAULT '0' COMMENT '逻辑删除，0：未删除，1：已删除',
  `CREATED_BY` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人',
  `CREATE_TIME` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `UPDATED_BY` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新人',
  `UPDATE_TIME` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='并签、会签流程实体表';

```

> Branch多分支场景实体对应的数据库表，其中AROUND_ID、APPROVAL_STATUS、APPROVAL_STATUS_JSON、LAST_SUBMIT_MESSAGE、LAST_SUBMIT_BY、LAST_SUBMIT_DATE、LAST_AUDIT_MESSAGE、LAST_AUDIT_BY、LAST_AUDIT_BY是起一条审批流必需字段字段，如果同一实体需要同时起多条审批流，需要配置多套，实际场景很少。

```sql
-- ----------------------------
-- Table structure for branch
-- ----------------------------
DROP TABLE IF EXISTS `branch`;
CREATE TABLE `branch` (
  `ID` int(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `NAME` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '实体名称',
  `TYPE` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT 'A' COMMENT '类型：A,B,C',
  `AROUND_ID` bigint(20) DEFAULT NULL COMMENT '回合id',
  `APPROVAL_STATUS` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '当前审批状态',
  `APPROVAL_STATUS_JSON` json DEFAULT NULL COMMENT '当前审批状态json字段',
  `LAST_SUBMIT_MESSAGE` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最后一次提交说明',
  `LAST_SUBMIT_BY` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最后一次提交人',
  `LAST_SUBMIT_DATE` timestamp(6) NULL DEFAULT NULL COMMENT '最后一次提交时间',
  `LAST_AUDIT_MESSAGE` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最后一次审批意见',
  `LAST_AUDIT_BY` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最后一次审批人',
  `LAST_AUDIT_DATE` timestamp(6) NULL DEFAULT NULL COMMENT '最后一次审批时间',
  `DELETED` int(11) NOT NULL DEFAULT '0' COMMENT '逻辑删除，0：未删除，1：已删除',
  `CREATED_BY` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人',
  `CREATE_TIME` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `UPDATED_BY` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新人',
  `UPDATE_TIME` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分支流程实体表';

```

### 3. 为测试实体Foo创建一条单签审批流，流转状态为：
> 初始状态Pending Submit经Submit变为Submitted，角色为role1

> Submitted经Approve变为A，Reject变回到Pending Submit，审批角色为role2

> A经Approve变为B，Reject变回到Submitted，审批角色为role3

> B经Approve变为Approved结束，Reject变回到Rejected，审批角色为role4

> Rejected可重新Submit变为Submitted，角色为role1

```sql
INSERT INTO `flow_basic` VALUES ( 'FOO_SINGLE_FLOW', 'FOO', 'Pending Submit', null, 'Submit', 'Submitted', '', '[\"role1\"]', '1', '0', '0', 'diguozhimeng', '2020-06-14 16:57:04', null, '2020-06-16 20:11:05');
INSERT INTO `flow_basic` VALUES ( 'FOO_SINGLE_FLOW', 'FOO', 'Submitted', null, 'Approve', 'A', null, '[\"role2\"]', '2', '0', '0', 'diguozhimeng', '2020-06-16 00:21:15', null, '2020-06-16 07:46:26');
INSERT INTO `flow_basic` VALUES ( 'FOO_SINGLE_FLOW', 'FOO', 'Submitted', null, 'Reject', 'Pending Submit', null, '[\"role2\"]', '-1', '0', '0', 'diguozhimeng', '2020-06-16 07:24:52', null, '2020-06-16 07:46:30');
INSERT INTO `flow_basic` VALUES ( 'FOO_SINGLE_FLOW', 'FOO', 'A', null, 'Approve', 'B', null, '[\"role3\"]', '3', '0', '0', 'diguozhimeng', '2020-06-16 07:26:39', null, '2020-06-16 07:46:17');
INSERT INTO `flow_basic` VALUES ( 'FOO_SINGLE_FLOW', 'FOO', 'A', null, 'Reject', 'Submitted', null, '[\"role3\"]', '-2', '0', '0', 'diguozhimeng', '2020-06-16 07:28:22', null, '2020-06-16 07:46:34');
INSERT INTO `flow_basic` VALUES ( 'FOO_SINGLE_FLOW', 'FOO', 'B', null, 'Approve', 'Approved', null, '[\"role4\"]', '4', '1', '0', 'diguozhimeng', '2020-06-16 07:29:48', null, '2020-06-16 20:09:02');
INSERT INTO `flow_basic` VALUES ( 'FOO_SINGLE_FLOW', 'FOO', 'B', null, 'Reject', 'Rejected', null, '[\"role4\"]', '-3', '-1', '0', 'diguozhimeng', '2020-06-16 07:30:21', null, '2020-06-16 07:46:40');
INSERT INTO `flow_basic` VALUES ( 'FOO_SINGLE_FLOW', 'FOO', 'Rejected', null, 'Submit', 'Submitted', null, '[\"role1\"]', '0', '0', '0', null, '2020-06-16 20:34:53', null, '2020-06-16 20:35:00');
```

### 4. 为测试实体Parallel创建一条会签、并签审批流，流转状态为：
> 初始状态Pending Submit经Submit变为 A&&B，角色为role1

> A&&B状态表示必须A和B全部Approve变为C||D状态，A或B任一一个经Reject变回到Pending Submit，A审批角色为role2，B审批角色为role3

> C||D状态经C和D任一节点经过Approve变为Approved，C或D任一节点经Reject变为Rejected，C审批角色为role4，D审批角色为role5

> Rejected可重新Submit变为Submitted，角色为role1

```sql

INSERT INTO `flow_basic` (`FLOW_NAME`, `ENTITY_NAME`, `PREV_STATUS`, `PREV_STATUS_ALIAS`, `ACTION`, `NEXT_STATUS`, `NEXT_STATUS_ALIAS`, `HANDLE_ROLES`, `TASK_ORDER`, `LAST_TASK`, `DELETED`, `CREATED_BY`, `CREATE_TIME`, `UPDATED_BY`, `UPDATE_TIME`) VALUES ('PARALLEL_FLOW', 'PARALLEL', 'Pending Submit', NULL, 'Submit', 'A&&B', NULL, '[\"role1\"]', '1', '0', '0', 'diguozhimeng', '2020-06-25 08:11:40', NULL, '2020-06-25 08:42:20');
INSERT INTO `flow_basic` (`FLOW_NAME`, `ENTITY_NAME`, `PREV_STATUS`, `PREV_STATUS_ALIAS`, `ACTION`, `NEXT_STATUS`, `NEXT_STATUS_ALIAS`, `HANDLE_ROLES`, `TASK_ORDER`, `LAST_TASK`, `DELETED`, `CREATED_BY`, `CREATE_TIME`, `UPDATED_BY`, `UPDATE_TIME`) VALUES ('PARALLEL_FLOW', 'PARALLEL', 'A', NULL, 'Approve', 'C||D', NULL, '[\"role2\"]', '3', '0', '0', 'diguozhimeng', '2020-06-25 08:13:11', NULL, '2020-06-25 08:23:56');
INSERT INTO `flow_basic` (`FLOW_NAME`, `ENTITY_NAME`, `PREV_STATUS`, `PREV_STATUS_ALIAS`, `ACTION`, `NEXT_STATUS`, `NEXT_STATUS_ALIAS`, `HANDLE_ROLES`, `TASK_ORDER`, `LAST_TASK`, `DELETED`, `CREATED_BY`, `CREATE_TIME`, `UPDATED_BY`, `UPDATE_TIME`) VALUES ('PARALLEL_FLOW', 'PARALLEL', 'B', NULL, 'Approve', 'C||D', NULL, '[\"role3\"]', '4', '0', '0', 'diguozhimeng', '2020-06-25 08:13:46', NULL, '2020-06-25 08:23:56');
INSERT INTO `flow_basic` (`FLOW_NAME`, `ENTITY_NAME`, `PREV_STATUS`, `PREV_STATUS_ALIAS`, `ACTION`, `NEXT_STATUS`, `NEXT_STATUS_ALIAS`, `HANDLE_ROLES`, `TASK_ORDER`, `LAST_TASK`, `DELETED`, `CREATED_BY`, `CREATE_TIME`, `UPDATED_BY`, `UPDATE_TIME`) VALUES ('PARALLEL_FLOW', 'PARALLEL', 'C||D', NULL, 'Approve', 'Approved', NULL, NULL, '5', '0', '0', 'diguozhimeng', '2020-06-25 08:14:28', NULL, '2020-06-25 08:23:56');
INSERT INTO `flow_basic` (`FLOW_NAME`, `ENTITY_NAME`, `PREV_STATUS`, `PREV_STATUS_ALIAS`, `ACTION`, `NEXT_STATUS`, `NEXT_STATUS_ALIAS`, `HANDLE_ROLES`, `TASK_ORDER`, `LAST_TASK`, `DELETED`, `CREATED_BY`, `CREATE_TIME`, `UPDATED_BY`, `UPDATE_TIME`) VALUES ('PARALLEL_FLOW', 'PARALLEL', 'C', NULL, 'Approve', 'Approved', NULL, '[\"role4\"]', '6', '1', '0', 'diguozhimeng', '2020-06-25 08:14:57', NULL, '2020-06-25 08:23:56');
INSERT INTO `flow_basic` (`FLOW_NAME`, `ENTITY_NAME`, `PREV_STATUS`, `PREV_STATUS_ALIAS`, `ACTION`, `NEXT_STATUS`, `NEXT_STATUS_ALIAS`, `HANDLE_ROLES`, `TASK_ORDER`, `LAST_TASK`, `DELETED`, `CREATED_BY`, `CREATE_TIME`, `UPDATED_BY`, `UPDATE_TIME`) VALUES ('PARALLEL_FLOW', 'PARALLEL', 'D', NULL, 'Approve', 'Approved', NULL, '[\"role5\"]', '7', '0', '0', 'diguozhimeng', '2020-06-25 08:15:31', NULL, '2020-06-25 08:23:56');
INSERT INTO `flow_basic` (`FLOW_NAME`, `ENTITY_NAME`, `PREV_STATUS`, `PREV_STATUS_ALIAS`, `ACTION`, `NEXT_STATUS`, `NEXT_STATUS_ALIAS`, `HANDLE_ROLES`, `TASK_ORDER`, `LAST_TASK`, `DELETED`, `CREATED_BY`, `CREATE_TIME`, `UPDATED_BY`, `UPDATE_TIME`) VALUES ('PARALLEL_FLOW', 'PARALLEL', 'A', NULL, 'Reject', 'Pending Submit', NULL, '[\"role2\"]', '-1', '0', '0', 'diguozhimeng', '2020-06-25 08:15:31', NULL, '2020-06-25 08:23:56');
INSERT INTO `flow_basic` (`FLOW_NAME`, `ENTITY_NAME`, `PREV_STATUS`, `PREV_STATUS_ALIAS`, `ACTION`, `NEXT_STATUS`, `NEXT_STATUS_ALIAS`, `HANDLE_ROLES`, `TASK_ORDER`, `LAST_TASK`, `DELETED`, `CREATED_BY`, `CREATE_TIME`, `UPDATED_BY`, `UPDATE_TIME`) VALUES ('PARALLEL_FLOW', 'PARALLEL', 'B', NULL, 'Reject', 'Pending Submit', NULL, '[\"role3\"]', '-2', '0', '0', 'diguozhimeng', '2020-06-25 08:15:31', NULL, '2020-06-25 08:23:56');
INSERT INTO `flow_basic` (`FLOW_NAME`, `ENTITY_NAME`, `PREV_STATUS`, `PREV_STATUS_ALIAS`, `ACTION`, `NEXT_STATUS`, `NEXT_STATUS_ALIAS`, `HANDLE_ROLES`, `TASK_ORDER`, `LAST_TASK`, `DELETED`, `CREATED_BY`, `CREATE_TIME`, `UPDATED_BY`, `UPDATE_TIME`) VALUES ('PARALLEL_FLOW', 'PARALLEL', 'C', NULL, 'Reject', 'Rejected', NULL, '[\"role4\"]', '-3', '-1', '0', 'diguozhimeng', '2020-06-25 08:15:31', NULL, '2020-06-25 08:23:56');
INSERT INTO `flow_basic` (`FLOW_NAME`, `ENTITY_NAME`, `PREV_STATUS`, `PREV_STATUS_ALIAS`, `ACTION`, `NEXT_STATUS`, `NEXT_STATUS_ALIAS`, `HANDLE_ROLES`, `TASK_ORDER`, `LAST_TASK`, `DELETED`, `CREATED_BY`, `CREATE_TIME`, `UPDATED_BY`, `UPDATE_TIME`) VALUES ('PARALLEL_FLOW', 'PARALLEL', 'D', NULL, 'Reject', 'Rejected', NULL, '[\"role5\"]', '-4', '0', '0', 'diguozhimeng', '2020-06-25 08:15:31', NULL, '2020-06-25 08:23:56');
INSERT INTO `flow_basic` (`FLOW_NAME`, `ENTITY_NAME`, `PREV_STATUS`, `PREV_STATUS_ALIAS`, `ACTION`, `NEXT_STATUS`, `NEXT_STATUS_ALIAS`, `HANDLE_ROLES`, `TASK_ORDER`, `LAST_TASK`, `DELETED`, `CREATED_BY`, `CREATE_TIME`, `UPDATED_BY`, `UPDATE_TIME`) VALUES ('PARALLEL_FLOW', 'PARALLEL', 'Rejected', NULL, 'Submit', 'A&&B', NULL, '[\"role1\"]', '0', '0', '0', 'diguozhimeng', '2020-06-25 09:14:12', NULL, '2020-06-25 12:47:20');

```

### 5. 为测试实体Branch创建一条多分支场景审批流，流转状态为：
> 初始状态Pending Submit经Submit后，根据实体的type属性判断转向哪个状态，如为A则转向A，如为B则转向B，如为C则转向C，角色为role1

> A经Approve变为Approved结束，Reject变回到Pending Submit，审批角色为role2

> B经Approve变为Approved结束，Reject变回到Pending Submit，审批角色为role3

> C经Approve变为Approved结束，Reject变回到Pending Submit，审批角色为role4

```sql

INSERT INTO `flow_basic` (`FLOW_NAME`, `ENTITY_NAME`, `PREV_STATUS`, `PREV_STATUS_ALIAS`, `ACTION`, `NEXT_STATUS`, `NEXT_STATUS_ALIAS`, `HANDLE_ROLES`, `TASK_ORDER`, `LAST_TASK`, `DELETED`, `CREATED_BY`, `CREATE_TIME`, `UPDATED_BY`, `UPDATE_TIME`) VALUES ('22', 'BRANCH_FLOW', 'BRANCH', 'Pending Submit', NULL, 'Submit', '<% if(vo.type==\'A\'){print(\"A\");}else if(vo.type==\'B\'){print(\"B\");}else if(vo.type==\'C\'){print(\"C\");}else{print(\"A\");} %>', '[{\"next_step\":\"A\",\"label\":\"A类型\"},{\"next_step\":\"B\",\"label\":\"B类型\"},{\"next_step\":\"C\",\"label\":\"C类型\"}]', '[\"role1\"]', '1', '0', '0', 'diguozhimeng', '2020-06-25 12:14:17', NULL, '2020-06-25 12:47:21');
INSERT INTO `flow_basic` (`FLOW_NAME`, `ENTITY_NAME`, `PREV_STATUS`, `PREV_STATUS_ALIAS`, `ACTION`, `NEXT_STATUS`, `NEXT_STATUS_ALIAS`, `HANDLE_ROLES`, `TASK_ORDER`, `LAST_TASK`, `DELETED`, `CREATED_BY`, `CREATE_TIME`, `UPDATED_BY`, `UPDATE_TIME`) VALUES ('BRANCH_FLOW', 'BRANCH', 'A', NULL, 'Approve', 'Approved', NULL, '[\"role2\"]', '2', '0', '0', 'diguozhimeng', '2020-06-25 08:15:31', NULL, '2020-06-25 12:19:56');
INSERT INTO `flow_basic` (`FLOW_NAME`, `ENTITY_NAME`, `PREV_STATUS`, `PREV_STATUS_ALIAS`, `ACTION`, `NEXT_STATUS`, `NEXT_STATUS_ALIAS`, `HANDLE_ROLES`, `TASK_ORDER`, `LAST_TASK`, `DELETED`, `CREATED_BY`, `CREATE_TIME`, `UPDATED_BY`, `UPDATE_TIME`) VALUES ('BRANCH_FLOW', 'BRANCH', 'B', NULL, 'Approve', 'Approved', NULL, '[\"role3\"]', '3', '0', '0', 'diguozhimeng', '2020-06-25 08:15:31', NULL, '2020-06-25 12:19:58');
INSERT INTO `flow_basic` (`FLOW_NAME`, `ENTITY_NAME`, `PREV_STATUS`, `PREV_STATUS_ALIAS`, `ACTION`, `NEXT_STATUS`, `NEXT_STATUS_ALIAS`, `HANDLE_ROLES`, `TASK_ORDER`, `LAST_TASK`, `DELETED`, `CREATED_BY`, `CREATE_TIME`, `UPDATED_BY`, `UPDATE_TIME`) VALUES ('BRANCH_FLOW', 'BRANCH', 'C', NULL, 'Approve', 'Approved', NULL, '[\"role4\"]', '4', '1', '0', 'diguozhimeng', '2020-06-25 08:15:31', NULL, '2020-06-25 12:20:04');
INSERT INTO `flow_basic` (`FLOW_NAME`, `ENTITY_NAME`, `PREV_STATUS`, `PREV_STATUS_ALIAS`, `ACTION`, `NEXT_STATUS`, `NEXT_STATUS_ALIAS`, `HANDLE_ROLES`, `TASK_ORDER`, `LAST_TASK`, `DELETED`, `CREATED_BY`, `CREATE_TIME`, `UPDATED_BY`, `UPDATE_TIME`) VALUES ( 'BRANCH_FLOW', 'BRANCH', 'A', NULL, 'Reject', 'Rejected', NULL, '[\"role2\"]', '-1', '0', '0', 'diguozhimeng', '2020-06-25 08:15:31', NULL, '2020-06-25 12:19:47');
INSERT INTO `flow_basic` (`FLOW_NAME`, `ENTITY_NAME`, `PREV_STATUS`, `PREV_STATUS_ALIAS`, `ACTION`, `NEXT_STATUS`, `NEXT_STATUS_ALIAS`, `HANDLE_ROLES`, `TASK_ORDER`, `LAST_TASK`, `DELETED`, `CREATED_BY`, `CREATE_TIME`, `UPDATED_BY`, `UPDATE_TIME`) VALUES ('BRANCH_FLOW', 'BRANCH', 'B', NULL, 'Reject', 'Rejected', NULL, '[\"role3\"]', '-2', '0', '0', 'diguozhimeng', '2020-06-25 08:15:31', NULL, '2020-06-25 12:19:47');
INSERT INTO `flow_basic` (`FLOW_NAME`, `ENTITY_NAME`, `PREV_STATUS`, `PREV_STATUS_ALIAS`, `ACTION`, `NEXT_STATUS`, `NEXT_STATUS_ALIAS`, `HANDLE_ROLES`, `TASK_ORDER`, `LAST_TASK`, `DELETED`, `CREATED_BY`, `CREATE_TIME`, `UPDATED_BY`, `UPDATE_TIME`) VALUES ('BRANCH_FLOW', 'BRANCH', 'C', NULL, 'Reject', 'Rejected', NULL, '[\"role4\"]', '-3', '-1', '0', 'diguozhimeng', '2020-06-25 08:15:31', NULL, '2020-06-25 12:19:28');

```

### 6.启动前需要配置一下本地数据库

```text
flow-spring-boot-starter-sample/src/main/resources/application.yml
```

```xml
############################### 启用Capsule Flow start ###########################
capsule:
  flow:
    enable: true
############################### 启用Capsule Flow end ############################

############################### 数据源配置 start ################################
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/你的数据库名称?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true
    username: 你的数据库账户名
    password: 你的数据库密码
    driver-class-name: com.mysql.cj.jdbc.Driver
############################### 数据源配置 end ################################
```

### 7. 示例工程的代码结构

```text
└── src
    └── main
        ├── java
        │   └── com
        │       └── capsule
        │           └── flow
        │               ├── rbac
        │               │   └── EnforcerFactory.java
        │               ├── controller
        │               │   ├── BranchController.java
        │               │   ├── FooController.java
        │               │   └── ParallelController.java
        │               ├── entity
        │               │   ├── Branch.java
        │               │   ├── Foo.java
        │               │   └── Parallel.java
        │               ├── handler
        │               │   ├── BranchHandler.java
        │               │   ├── FooSingleHandler.java
        │               │   └── ParallelHandler.java
        │               ├── mapper
        │               │   ├── BranchMapper.java
        │               │   ├── FooMapper.java
        │               │   └── ParallelMapper.java
        │               └── service
        │                   ├── BranchService.java
        │                   ├── FooService.java
        │                   ├── ParallelService.java
        │                   └── impl
        │                       ├── BranchServiceImpl.java
        │                       ├── FooServiceImpl.java
        │                       └── ParallelServiceImpl.java
        └── resources
            └── config
                ├── rbac_model.conf
                └── rbac_policy.csv
```

### 8. 示例工程RABC权限模型框架采用[jCasbin](https://github.com/casbin/jcasbin)

> 初始化类

```text
flow-spring-boot-starter-sample/src/main/java/com/capsule/flow/sample/rabc/EnforcerFactory.java
```

> 角色权限配置

```text
flow-spring-boot-starter-sample/src/main/resources/config/rabc/rbac_policy.csv

g, user1, role1     #user1拥有role1和role6角色
g, user1, role6
g, user2, role2     #user2拥有role2和role7角色
g, user2, role7
g, user3, role3     #user3拥有role3和role8角色
g, user3, role8
g, user4, role4     #user4拥有role4和role9角色
g, user4, role9
g, user5, role5     #user5拥有role5和role10角色
g, user5, role10
```

### 9. 启动示例项目
> [http://localhost:8080](http://localhost:8080)

```text
flow-spring-boot-starter-sample/src/main/java/com/capsule/flow/sample/SpringBootFlowSampleApplication.java
```

```java
/**
 * 流程启动测试类
 *
 * @author DiGuoZhiMeng
 * @since 2020-06-23
 */
@SpringBootApplication
public class SpringBootFlowSampleApplication{

    public static void main(String[] args) {
        SpringApplication.run(SpringBootFlowSampleApplication.class, args);
    }
}
```

### 10. 单签场景测试接口
```text
flow-spring-boot-starter-sample/src/main/java/com/capsule/flow/sample/controller/FooController.java
```
#### 单签场景-列表查询
> [http://localhost:8080/foo/list](http://localhost:8080/foo/list)

#### 单签场景-新增记录
> [http://localhost:8080/foo/save/lisi](http://localhost:8080/foo/save/lisi)

#### 单签场景-测试提交
> [http://localhost:8080/foo/submit/5/user1/测试提交](http://localhost:8080/foo/submit/5/user1/测试提交)

#### 单签场景-测试审批通过
> [http://localhost:8080/foo/approve/5/user2/审批通过测试](http://localhost:8080/foo/approve/5/user2/审批通过测试)

#### 单签场景-驳回测试
> [http://localhost:8080/foo/reject/8/user2/驳回测试](http://localhost:8080/foo/reject/8/user2/驳回测试)

#### 单签场景-测试驳回后再次提交
> [http://localhost:8080/foo/submitAfterRejected/8/user1/测试驳回后再次提交](http://localhost:8080/foo/submitAfterRejected/8/user1/测试驳回后再次提交)

#### 单签场景-根据单据id获取当前单据当前审批回合内审批日志
> [http://localhost:8080/foo/historyLog/8](http://localhost:8080/foo/historyLog/8)

#### 单签场景-根据单据id获取当前单据所有历史审批日志
> [http://localhost:8080/foo/historyLogAll/8](http://localhost:8080/foo/historyLogAll/8)

#### 单签场景-判断是否有提交、审批等处理权限
> [http://localhost:8080/foo/verifyHandleAccess/8/user1](http://localhost:8080/foo/verifyHandleAccess/8/user1)

#### 单签场景-获取所有待提交的数据
> [http://localhost:8080/foo/pendingSubmit](http://localhost:8080/foo/pendingSubmit)

#### 单签场景-获取所有审批中的数据
> [http://localhost:8080/foo/pendingHandle](http://localhost:8080/foo/pendingHandle)

#### 单签场景-获取所有待我处理的数据：包括待提交的、待处理的
> [http://localhost:8080/foo/myTodo/user1](http://localhost:8080/foo/myTodo/user1)

#### 单签场景-获取所有待我处理的数据：不包括待提交的、只包括待处理的
> [http://localhost:8080/foo/myApproval/user1](http://localhost:8080/foo/myApproval/user1)

#### 单签场景-获取审批流元数据信息，用于前端流程可视化展示，查询出所有正向流程梳理，即taskOrder>0，允许非有向无环图的场景，如果是通过规则引擎判断分支的场景需要在nextStatusAlias中配置json字段
> [http://localhost:8080/foo/getFlowMetaInfoV1/20](http://localhost:8080/foo/getFlowMetaInfoV1/20)


#### 单签场景-获取审批流元数据信息，用于前端流程可视化展示，查询出所有正向流程梳理，即taskOrder>0，按先后排序后返回，，必须是有向无环图的场景，如果是通过规则引擎判断分支的场景需要通过业务实体的实际值判断出分支走向，把实际要走过的最终线路返回
> [http://localhost:8080/foo/getFlowMetaInfoV2/20](http://localhost:8080/foo/getFlowMetaInfoV2/20)

### 11. 会签、并签场景测试接口，[详见Controller类](https://gitee.com/DiGuoZhiMeng/Capsule-Flow/blob/master/flow-spring-boot-starter-sample/src/main/java/com/capsule/flow/sample/controller/ParallelController.java)
```text
flow-spring-boot-starter-sample/src/main/java/com/capsule/flow/sample/controller/ParallelController.java
```

### 12. 多分支场景测试接口，[详见Controller类](https://gitee.com/DiGuoZhiMeng/Capsule-Flow/blob/master/flow-spring-boot-starter-sample/src/main/java/com/capsule/flow/sample/controller/BranchController.java)
```text
flow-spring-boot-starter-sample/src/main/java/com/capsule/flow/sample/controller/BranchController.java
```

## 联系
QQ 1114031364| 微信号 doragonbarru| 
-|-|

## License
Capsule Flow is under the Apache 2.0 license. See the [LICENSE](https://gitee.com/DiGuoZhiMeng/Capsule-Flow/blob/master/LICENSE) file for details.

