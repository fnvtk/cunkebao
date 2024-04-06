package cn.myerm.common.controller;

import com.alibaba.fastjson.JSONObject;
import cn.myerm.common.dto.BaseDTO;
import cn.myerm.common.dto.MessageDTO;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class CommonController {

    @Autowired
    protected HttpServletRequest request;

    /**
     * 操作成功返回
     *
     * @param data 返回成功的数据体
     * @return MessageDTO
     */
    protected MessageDTO success(JSONObject data) {
        MessageDTO dto = new MessageDTO();
        dto.setCode(10000);
        dto.setMessage("操作成功");
        dto.setData(data);

        return dto;
    }

    /**
     * 操作成功返回
     *
     * @param data 返回成功的数据体
     * @return MessageDTO
     */
    protected MessageDTO success(Object data) {
        MessageDTO dto = new MessageDTO();
        dto.setCode(10000);
        dto.setMessage("操作成功");
        dto.setData(data);
        return dto;
    }

    /**
     * 操作成功返回
     *
     * @param list 返回成功的数据体
     * @return MessageDTO
     */
    protected MessageDTO success(List list) {
        MessageDTO dto = new MessageDTO();
        dto.setCode(10000);
        dto.setMessage("操作成功");
        dto.setData(list);
        return dto;
    }

    /**
     * 操作成功返回
     *
     * @param data 返回成功的数据体
     * @return MessageDTO
     */
    protected MessageDTO success(BaseDTO data) {
        MessageDTO dto = new MessageDTO();
        dto.setCode(10000);
        dto.setMessage("操作成功");
        dto.setData(data);

        return dto;
    }

    /**
     * 操作成功返回
     *
     * @param sMessage 返回成功的数据体
     * @return MessageDTO
     */
    protected MessageDTO success(String sMessage) {
        MessageDTO dto = new MessageDTO();
        dto.setCode(10000);
        dto.setMessage(sMessage);

        return dto;
    }

    protected MessageDTO success() {
        return success("操作成功");
    }

    /**
     * 操作失败返回
     *
     * @param sFailReason 操作失败的原因
     * @return MessageDTO
     */
    protected MessageDTO fail(String sFailReason) {
        MessageDTO dto = new MessageDTO();
        dto.setCode(10001);
        dto.setMessage(sFailReason);

        return dto;
    }

    /**
     * 操作失败返回
     *
     * @param sFailReason 操作失败的原因
     * @param lCode       状态码
     * @return MessageDTO
     */
    protected MessageDTO fail(String sFailReason, int lCode) {
        MessageDTO dto = new MessageDTO();
        dto.setCode(lCode);
        dto.setMessage(sFailReason);

        return dto;
    }

    /**
     * 检查必传项是否传值
     *
     * @param arrParam
     */
    protected void checkRequired(String[] arrParam, HttpServletRequest request) throws Exception {
        for (String param : arrParam) {
            if (request.getParameter(param) == null || request.getParameter(param).length() == 0) {
                throw new Exception(param + "字段必须传值");
            }
        }
    }
}
