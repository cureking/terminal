package com.renewable.terminal.pojo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author jobob
 * @since 2019-07-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class AudioAmnout {

	private static final long serialVersionUID = 1L;

	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;

	private Integer sensorRegisterId;

	private Integer terminalId;

	private Double originTime;

	private Double amnout;

	private Integer status;

	private String mark;

	/**
	 * 创建时间
	 */
//	@JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
//	@TableField(value = "create_time", fill = FieldFill.INSERT)
	private Date createTime;

	/**
	 * 更新时间
	 */
//	@JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
//	@TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
	private Date updateTime;


}
