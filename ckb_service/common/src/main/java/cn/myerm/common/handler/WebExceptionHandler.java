package cn.myerm.common.handler;


import cn.myerm.common.dto.MessageDTO;
import cn.myerm.common.exception.NotFoundException;
import cn.myerm.common.exception.SystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class WebExceptionHandler {

    static Logger LOG = LoggerFactory.getLogger(WebExceptionHandler.class);

    @Autowired
    private MessageSource messageSource;

    /**
     * 未找到数据
     *
     * @param e
     * @return
     */
    @ExceptionHandler(NotFoundException.class)//指定异常类型
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public MessageDTO handleNotFoundException(NotFoundException e) {
        MessageDTO vo = new MessageDTO();
        fillExceptionVO(e, vo);
        return vo;
    }


    @ExceptionHandler(SystemException.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public MessageDTO handleSystemException(SystemException e) {
        MessageDTO vo = new MessageDTO();
        fillExceptionVO(e, vo);
        return vo;
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public MessageDTO globalError(Exception e) {
        LOG.error(e.getMessage(), e);
        MessageDTO vo = new MessageDTO();
        vo.setCode(99999);
        vo.setMessage(e.getMessage());
        vo.setData(e.getStackTrace());
        return vo;
    }

    /**
     * 填充异常响应消息
     *
     * @param e
     * @param vo
     */
    private void fillExceptionVO(SystemException e, MessageDTO vo) {
        if (e.getMessage() != null) {
            String message = e.getMessage();
            try {
                message = messageSource.getMessage(e.getMessage(), e.getArgs(), LocaleContextHolder.getLocale());
            } catch (NoSuchMessageException ex) {
            }
            vo.setMessage(message);
        }

        if (e.getData() != null) {
            vo.setData(e.getData());
        }

        vo.setCode(e.getErrorCode());
    }
}
