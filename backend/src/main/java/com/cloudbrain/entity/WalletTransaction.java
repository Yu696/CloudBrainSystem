package com.cloudbrain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包交易记录实体
 */
@Data
@TableName("wallet_transaction")
public class WalletTransaction {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("transaction_id")
    private String transactionId;

    @TableField("patient_id")
    private String patientId;

    /** 0=充值 1=挂花费 2=药费 3=检查费 */
    private Integer type;

    private BigDecimal amount;

    @TableField("balance_after")
    private BigDecimal balanceAfter;

    @TableField("ref_id")
    private String refId;

    private String remark;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
