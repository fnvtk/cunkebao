package cn.myerm.common.dto;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class MessageDTO extends BaseDTO {
    protected long code; //code=10000代表操作成功，大于10000的都代表操作不成功

    protected String message; //操作信息提示

    protected Object data; //返回的附加数据

    public MessageDTO() {
    }

    public MessageDTO(long code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public String toJSONString() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", code);
        jsonObject.put("message", message);
        jsonObject.put("data", data);
        return jsonObject.toJSONString();
    }
}
