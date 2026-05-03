package com.xms.common.core.domain.entity.xms;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.xms.common.annotation.Excel;
import com.xms.common.core.domain.BaseMcgEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class XmsPLog extends BaseMcgEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @Excel(name = "用户id")
    private Long user_id;

    @Excel(name = "修改后的值")
    private String after_val;

    @Excel(name = "修改前的值")
    private String befault_val;

}
