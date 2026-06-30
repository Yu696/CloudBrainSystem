package com.cloudbrain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cloudbrain.entity.WalletTransaction;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WalletTransactionMapper extends BaseMapper<WalletTransaction> {
}
